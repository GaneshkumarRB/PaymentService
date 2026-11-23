package com.bnk.paymentservice.fraud;

import com.bnk.paymentservice.config.FraudClientProperties;
import com.bnk.paymentservice.config.FraudStubProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FraudScreeningStubControllerTest {

    private FraudScreeningStubController controller;

    @BeforeEach
    void setUp() {
        FraudStubProperties properties = new FraudStubProperties();
        properties.setMinLatencyMs(0);
        properties.setMaxLatencyMs(0);
        controller = new FraudScreeningStubController();
        ReflectionTestUtils.setField(controller, "properties", properties);
        ReflectionTestUtils.setField(controller, "clientProperties", new FraudClientProperties());
        controller.init();
    }

    @ParameterizedTest
    @CsvSource({"999.99, ACCEPT", "1000, DECLINE", "1200, DECLINE", "4999.99, DECLINE"})
    void appliesRiskThresholdToNamespacedAmountWithAttributes(String amount, String decision) throws Exception {
        ResponseEntity<String> response = controller.screen(
                "<fs:ScreeningRequest xmlns:fs=\"urn:vendor:fraudscreen:v2\">"
                        + "<fs:Amount currency=\"EUR\">" + amount + "</fs:Amount>"
                        + "</fs:ScreeningRequest>");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("<fs:Decision>" + decision + "</fs:Decision>"));
    }

    @Test
    void appliesFailureThresholdBeforeRiskThreshold() throws Exception {
        ResponseEntity<String> response = controller.screen(
                "<ScreeningRequest><Amount>5000</Amount></ScreeningRequest>");
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "",
            "<ScreeningRequest/>",
            "<ScreeningRequest><Amount>invalid</Amount></ScreeningRequest>",
            "<ScreeningRequest><Amount>0</Amount></ScreeningRequest>",
            "<ScreeningRequest><Amount>1000</Amount><Amount>1</Amount></ScreeningRequest>",
            "<ScreeningRequest><Amount>1200</Amount>",
            "<!DOCTYPE ScreeningRequest [<!ENTITY xxe SYSTEM 'file:///nonexistent'>]><ScreeningRequest><Amount>&xxe;</Amount></ScreeningRequest>"
    })
    void rejectsInvalidRequestsInsteadOfAcceptingThem(String xml) throws Exception {
        assertEquals(HttpStatus.BAD_REQUEST, controller.screen(xml).getStatusCode());
    }
}