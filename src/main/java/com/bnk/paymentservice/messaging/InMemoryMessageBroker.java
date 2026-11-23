package com.bnk.paymentservice.messaging;

import com.bnk.paymentservice.common.AppConstants;
import com.bnk.paymentservice.config.PaymentMessagingProperties;
import com.bnk.paymentservice.entity.DeadLetterMessage;
import com.bnk.paymentservice.entity.ProcessedEvent;
import com.bnk.paymentservice.models.BrokerMessage;
import com.bnk.paymentservice.models.PaymentSubmittedEvent;
import com.bnk.paymentservice.repository.DeadLetterMessageRepository;
import com.bnk.paymentservice.repository.ProcessedEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * In-process broker stub with per-consumer queues, concurrent workers,
 * exponential backoff retries, and durable DLQ persistence.
 * Consumer failures are isolated: one slow/failing consumer does not block others.
 */
@Component
public class InMemoryMessageBroker {

    private static final Logger log = LoggerFactory.getLogger(InMemoryMessageBroker.class);

    private final Map<String, DelayQueue<DelayedBrokerMessage>> queues = new ConcurrentHashMap<>();
    private final Map<String, EventHandler> handlers = new ConcurrentHashMap<>();
    private final AtomicBoolean running = new AtomicBoolean(true);

    @Autowired
    private PaymentMessagingProperties properties;
    @Autowired
    private ProcessedEventRepository processedEventRepository;
    @Autowired
    private DeadLetterMessageRepository deadLetterMessageRepository;
    @Autowired
    private Clock clock;
    @Autowired
    private List<EventHandler> eventHandlers;

    private ExecutorService workers;

    @PostConstruct
    public void start() {
        int perConsumer = Math.max(1, properties.getWorkerThreads());
        int poolSize = Math.max(1, perConsumer * Math.max(1, eventHandlers.size()));
        this.workers = Executors.newFixedThreadPool(poolSize, r -> {
            Thread t = new Thread(r);
            t.setName(AppConstants.BROKER_WORKER_THREAD_PREFIX + t.getId());
            t.setDaemon(true);
            return t;
        });
        for (EventHandler handler : eventHandlers) {
            register(handler, perConsumer);
        }
    }

    public void register(EventHandler handler) {
        register(handler, Math.max(1, properties.getWorkerThreads()));
    }

    private void register(EventHandler handler, int perConsumer) {
        this.handlers.put(handler.consumerName(), handler);
        queues.computeIfAbsent(handler.consumerName(), ignored -> new DelayQueue<>());
        for (int i = 0; i < perConsumer; i++) {
            workers.submit(() -> drain(handler.consumerName()));
        }
        log.info("Registered consumer {} with {} workers", handler.consumerName(), perConsumer);
    }

    public void publish(PaymentSubmittedEvent event) {
        Instant now = clock.instant();
        for (String consumerName : handlers.keySet()) {
            BrokerMessage message = new BrokerMessage(UUID.randomUUID(), consumerName, event, 1, now);
            queues.get(consumerName).offer(new DelayedBrokerMessage(message));
        }
        log.debug("Published PaymentSubmitted eventId={} to {} consumers", event.eventId(), handlers.size());
    }

    private void drain(String consumerName) {
        DelayQueue<DelayedBrokerMessage> queue = queues.get(consumerName);
        EventHandler handler = handlers.get(consumerName);
        while (running.get()) {
            try {
                DelayedBrokerMessage delayed = queue.poll(500, TimeUnit.MILLISECONDS);
                if (delayed == null) {
                    continue;
                }
                process(handler, delayed.message());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            } catch (Exception e) {
                log.error("Unexpected broker loop error for consumer {}", consumerName, e);
            }
        }
    }

    private void process(EventHandler handler, BrokerMessage message) {
        PaymentSubmittedEvent event = message.event();
        if (processedEventRepository.existsByConsumerNameAndEventId(handler.consumerName(), event.eventId())) {
            log.debug("Skipping already processed eventId={} for {}", event.eventId(), handler.consumerName());
            return;
        }
        try {
            handler.handle(event);
            processedEventRepository.save(new ProcessedEvent(handler.consumerName(), event.eventId(), clock.instant()));
        } catch (Exception ex) {
            handleFailure(handler, message, ex);
        }
    }

    private void handleFailure(EventHandler handler, BrokerMessage message, Exception ex) {
        if (message.attempt() >= properties.getMaxAttempts()) {
            deadLetterMessageRepository.save(new DeadLetterMessage(
                    UUID.randomUUID(),
                    handler.consumerName(),
                    message.event().eventId(),
                    message.event().paymentId(),
                    message.event().toString(),
                    truncate(ex.getMessage()),
                    message.attempt(),
                    clock.instant()
            ));
            log.error("Moved eventId={} to DLQ for consumer {} after {} attempts: {}",
                    message.event().eventId(), handler.consumerName(), message.attempt(), ex.getMessage());
            return;
        }
        long delay = computeBackoffMs(message.attempt());
        Instant next = clock.instant().plusMillis(delay);
        queues.get(handler.consumerName()).offer(new DelayedBrokerMessage(message.nextAttempt(next)));
        log.warn("Retrying eventId={} for consumer {} attempt {} in {}ms: {}",
                message.event().eventId(), handler.consumerName(), message.attempt() + 1, delay, ex.getMessage());
    }

    private long computeBackoffMs(int attempt) {
        double delay = properties.getInitialBackoffMs() * Math.pow(properties.getBackoffMultiplier(), Math.max(0, attempt - 1));
        return Math.min(properties.getMaxBackoffMs(), (long) delay);
    }

    private String truncate(String message) {
        if (message == null) {
            return AppConstants.UNKNOWN_ERROR;
        }
        return message.length() <= 1000 ? message : message.substring(0, 1000);
    }

    @PreDestroy
    public void shutdown() {
        running.set(false);
        if (workers != null) {
            workers.shutdownNow();
        }
    }

    private static final class DelayedBrokerMessage implements Delayed {
        private final BrokerMessage message;

        private DelayedBrokerMessage(BrokerMessage message) {
            this.message = message;
        }

        private BrokerMessage message() {
            return message;
        }

        @Override
        public long getDelay(TimeUnit unit) {
            long delayMs = message.availableAt().toEpochMilli() - Instant.now().toEpochMilli();
            return unit.convert(delayMs, TimeUnit.MILLISECONDS);
        }

        @Override
        public int compareTo(Delayed other) {
            return Long.compare(getDelay(TimeUnit.MILLISECONDS), other.getDelay(TimeUnit.MILLISECONDS));
        }
    }
}
