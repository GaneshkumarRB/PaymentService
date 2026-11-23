package com.bnk.paymentservice.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.FileSystemResource;

import java.io.IOException;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DatasourceConfigurationTest {

    @Test
    void infersH2DriverForDefaultUrl() throws IOException {
        assertEquals("org.h2.Driver", properties(null).determineDriverClassName());
    }

    @Test
    void infersPostgresDriverWhenUrlIsOverridden() throws IOException {
        assertEquals("org.postgresql.Driver",
                properties("jdbc:postgresql://localhost:5432/payments").determineDriverClassName());
    }

    private DataSourceProperties properties(String urlOverride) throws IOException {
        StandardEnvironment environment = new StandardEnvironment();
        new YamlPropertySourceLoader()
                .load("application", new FileSystemResource("src/main/resources/application.yml"))
                .forEach(environment.getPropertySources()::addLast);
        if (urlOverride != null) {
            environment.getPropertySources().addFirst(new MapPropertySource("testOverride",
                    Collections.singletonMap("spring.datasource.url", urlOverride)));
        }
        return Binder.get(environment)
                .bind("spring.datasource", Bindable.of(DataSourceProperties.class))
                .get();
    }
}