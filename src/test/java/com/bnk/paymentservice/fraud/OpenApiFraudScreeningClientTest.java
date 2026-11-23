package com.bnk.paymentservice.fraud;

import com.bnk.paymentservice.config.FraudClientConfig;
import com.bnk.paymentservice.config.FraudClientProperties;
import com.bnk.paymentservice.exception.FraudVendorException;
import com.bnk.paymentservice.factory.FraudScreeningFactory;
import com.bnk.paymentservice.fraud.api.ApiClient;
import com.bnk.paymentservice.fraud.api.FraudScreeningApi;
import com.bnk.paymentservice.models.FraudDecisionType;
import com.bnk.paymentservice.models.FraudScreeningResult;
import com.bnk.paymentservice.models.PaymentSubmittedEvent;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class OpenApiFraudScreeningClientTest {

    private static final String BASE = "http://vendor.test";

    private MockRestServiceServer server;
    private OpenApiFraudScreeningClient client;

    @BeforeEach
    void setUp() {
        FraudClientProperties properties = new FraudClientProperties();
        properties.setBaseUrl(BASE);
        properties.setConnectTimeoutMs(2000);
        properties.setReadTimeoutMs(5000);

        FraudClientConfig config = new FraudClientConfig();
        XmlMapper xmlMapper = config.fraudXmlMapper();
        RestTemplate restTemplate = config.fraudRestTemplate(new RestTemplateBuilder(), properties, xmlMapper);
        server = MockRestServiceServer.createServer(restTemplate);
        ApiClient apiClient = config.fraudApiClient(restTemplate, properties);
        FraudScreeningApi api = config.fraudScreeningApi(apiClient);

        client = new OpenApiFraudScreeningClient();
        ReflectionTestUtils.setField(client, "fraudScreeningApi", api);
        ReflectionTestUtils.setField(client, "fraudXmlMapper", xmlMapper);
        ReflectionTestUtils.setField(client, "fraudScreeningFactory", new FraudScreeningFactory());
    }

    @Test
    void parsesAcceptResponse() {
        server.expect(requestTo(BASE + "/screening/v2/check"))
                .andRespond(withSuccess(
                        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                                + "<fs:ScreeningResponse xmlns:fs=\"urn:vendor:fraudscreen:v2\">"
                                + "<fs:Header>"
                                + "<fs:RequestRef>PAY-accept</fs:RequestRef>"
                                + "<fs:Timestamp>2026-11-12T09:15:22+13:00</fs:Timestamp>"
                                + "</fs:Header>"
                                + "<fs:Result>"
                                + "<fs:Decision>ACCEPT</fs:Decision>"
                                + "<fs:RiskScore>12</fs:RiskScore>"
                                + "<fs:Reasons>"
                                + "<fs:Reason code=\"R001\">Within normal risk band</fs:Reason>"
                                + "</fs:Reasons>"
                                + "</fs:Result>"
                                + "</fs:ScreeningResponse>",
                        MediaType.APPLICATION_XML));

        FraudScreeningResult result = client.screen(sampleEvent());

        assertEquals(FraudDecisionType.ACCEPT, result.decision());
        assertEquals(12, result.riskScore());
        assertEquals(1, result.reasons().size());
        assertEquals("Within normal risk band", result.reasons().get(0));
        server.verify();
    }

    @Test
    void mapsHttp500NestedErrorXml() {
        server.expect(requestTo(BASE + "/screening/v2/check"))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                        .contentType(MediaType.APPLICATION_XML)
                        .body("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                                + "<fs:ScreeningResponse xmlns:fs=\"urn:vendor:fraudscreen:v2\">"
                                + "<fs:Error>"
                                + "<fs:Code>SVC-503</fs:Code>"
                                + "<fs:Message>Scoring engine temporarily unavailable</fs:Message>"
                                + "<fs:Retryable>true</fs:Retryable>"
                                + "</fs:Error>"
                                + "</fs:ScreeningResponse>"));

        FraudVendorException ex = assertThrows(FraudVendorException.class, () -> client.screen(sampleEvent()));
        assertEquals("SVC-503", ex.getCode());
        assertTrue(ex.isRetryable());
        assertTrue(ex.getMessage().contains("unavailable"));
    }

    private PaymentSubmittedEvent sampleEvent() {
        return new PaymentSubmittedEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Alice",
                "GB29NWBK60161331926819",
                "Bob",
                "DE89370400440532013000",
                new BigDecimal("25.00"),
                "EUR",
                "REF",
                LocalDate.of(2026, 11, 20),
                Instant.parse("2026-11-19T12:00:00Z")
        );
    }
}
