package com.bnk.paymentservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Starts the payment service and enables scheduled background processing.
 */
@SpringBootApplication
@EnableScheduling
@ComponentScan(
        basePackages = "com.bnk.paymentservice",
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.REGEX,
                pattern = "com\\.bnk\\.paymentservice\\.fraud\\.api\\..*"
        )
)
public class PaymentServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaymentServiceApplication.class, args);
    }
}
