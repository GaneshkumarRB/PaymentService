package com.bnk.paymentservice.config;

import com.bnk.paymentservice.common.AppConstants;
import com.bnk.paymentservice.fraud.ReasonMixin;
import com.bnk.paymentservice.fraud.ScreeningResponseMixin;
import com.bnk.paymentservice.fraud.ScreeningResultMixin;
import com.bnk.paymentservice.fraud.api.ApiClient;
import com.bnk.paymentservice.fraud.api.FraudScreeningApi;
import com.bnk.paymentservice.fraud.api.model.Reason;
import com.bnk.paymentservice.fraud.api.model.ScreeningResponse;
import com.bnk.paymentservice.fraud.api.model.ScreeningResult;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.servlet.context.ServletWebServerInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.Arrays;

@Configuration
public class FraudClientConfig {

    @Bean
    public XmlMapper fraudXmlMapper() {
        XmlMapper xmlMapper = new XmlMapper();
        xmlMapper.registerModule(new JavaTimeModule());
        xmlMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        xmlMapper.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true);
        xmlMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        xmlMapper.addMixIn(ScreeningResponse.class, ScreeningResponseMixin.class);
        xmlMapper.addMixIn(ScreeningResult.class, ScreeningResultMixin.class);
        xmlMapper.addMixIn(Reason.class, ReasonMixin.class);
        return xmlMapper;
    }

    @Bean
    public RestTemplate fraudRestTemplate(RestTemplateBuilder builder,
                                          FraudClientProperties properties,
                                          XmlMapper fraudXmlMapper) {
        MappingJackson2XmlHttpMessageConverter xmlConverter =
                new MappingJackson2XmlHttpMessageConverter(fraudXmlMapper);
        return builder
                .setConnectTimeout(Duration.ofMillis(properties.getConnectTimeoutMs()))
                .setReadTimeout(Duration.ofMillis(properties.getReadTimeoutMs()))
                .messageConverters(Arrays.asList(
                        xmlConverter,
                        new MappingJackson2HttpMessageConverter()
                ))
                .build();
    }

    @Bean
    public ApiClient fraudApiClient(RestTemplate fraudRestTemplate, FraudClientProperties properties) {
        ApiClient apiClient = new ApiClient(fraudRestTemplate);
        apiClient.setBasePath(trimTrailingSlash(properties.getBaseUrl()));
        return apiClient;
    }

    @Bean
    public FraudScreeningApi fraudScreeningApi(ApiClient fraudApiClient) {
        return new FraudScreeningApi(fraudApiClient);
    }

    /**
     * When the in-process stub is enabled, always call this JVM's web server so tests and
     * local runs do not accidentally hit another process on a hard-coded port.
     */
    @Bean
    public ApplicationListener<ServletWebServerInitializedEvent> fraudApiBasePathUpdater(
            ApiClient fraudApiClient,
            FraudClientProperties properties) {
        return event -> {
            if (!properties.isEnabledStub()) {
                return;
            }
            int port = event.getWebServer().getPort();
            fraudApiClient.setBasePath("http://localhost:" + port);
        };
    }

    private String trimTrailingSlash(String baseUrl) {
        if (baseUrl == null || baseUrl.trim().isEmpty()) {
            return AppConstants.DEFAULT_FRAUD_BASE_URL;
        }
        return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }
}
