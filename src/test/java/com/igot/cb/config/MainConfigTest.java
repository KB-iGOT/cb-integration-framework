package com.igot.cb.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MainConfig Tests")
class MainConfigTest {

    private MainConfig mainConfig;

    @BeforeEach
    void setUp() {
        mainConfig = new MainConfig();
    }

    @Test
    @DisplayName("objectMapper - returns a non-null ObjectMapper instance")
    void objectMapper_returnsNonNullObjectMapper() {
        ObjectMapper objectMapper = mainConfig.objectMapper();

        assertNotNull(objectMapper);
    }

    @Test
    @DisplayName("objectMapper - FAIL_ON_UNKNOWN_PROPERTIES deserialization feature is disabled")
    void objectMapper_failOnUnknownPropertiesIsDisabled() {
        ObjectMapper objectMapper = mainConfig.objectMapper();

        assertFalse(
                objectMapper.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES),
                "FAIL_ON_UNKNOWN_PROPERTIES should be disabled"
        );
    }

    @Test
    @DisplayName("objectMapper - each call returns a new independent ObjectMapper instance")
    void objectMapper_eachCallReturnsNewInstance() {
        ObjectMapper first = mainConfig.objectMapper();
        ObjectMapper second = mainConfig.objectMapper();

        assertNotSame(first, second);
    }

    @Test
    @DisplayName("jacksonConverter - returns a non-null MappingJackson2HttpMessageConverter instance")
    void jacksonConverter_returnsNonNullConverter() {
        ObjectMapper objectMapper = mainConfig.objectMapper();

        MappingJackson2HttpMessageConverter converter = mainConfig.jacksonConverter(objectMapper);

        assertNotNull(converter);
    }

    @Test
    @DisplayName("jacksonConverter - sets the provided ObjectMapper on the converter")
    void jacksonConverter_setsProvidedObjectMapperOnConverter() {
        ObjectMapper objectMapper = mainConfig.objectMapper();

        MappingJackson2HttpMessageConverter converter = mainConfig.jacksonConverter(objectMapper);

        assertSame(objectMapper, converter.getObjectMapper());
    }

    @Test
    @DisplayName("jacksonConverter - converter ObjectMapper has FAIL_ON_UNKNOWN_PROPERTIES disabled")
    void jacksonConverter_converterObjectMapperHasFailOnUnknownPropertiesDisabled() {
        ObjectMapper objectMapper = mainConfig.objectMapper();

        MappingJackson2HttpMessageConverter converter = mainConfig.jacksonConverter(objectMapper);

        assertFalse(
                converter.getObjectMapper().isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES),
                "Converter's ObjectMapper should also have FAIL_ON_UNKNOWN_PROPERTIES disabled"
        );
    }
}

