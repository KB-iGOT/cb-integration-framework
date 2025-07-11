package com.igot.cb.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import static org.junit.jupiter.api.Assertions.*;

class MainConfigTest {

    private MainConfig config;

    @BeforeEach
    void setUp() {
        config = new MainConfig();
    }

    @Test
    void testObjectMapper() {
        ObjectMapper mapper = config.objectMapper();

        assertNotNull(mapper);
        assertFalse(mapper.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES),
                "FAIL_ON_UNKNOWN_PROPERTIES should be disabled");
    }

    @Test
    void testJacksonConverter() {
        ObjectMapper mapper = config.objectMapper();
        MappingJackson2HttpMessageConverter converter = config.jacksonConverter(mapper);

        assertNotNull(converter);
        assertSame(mapper, converter.getObjectMapper(), "Converter should use the provided ObjectMapper");
    }
}

