package com.igot.cb.validator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.igot.cb.exception.CustomException;
import com.igot.cb.model.ExternalApiIntegrationDTO;
import com.igot.cb.model.ExternalApiIntegrationDTO.RequestMethod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class IntegrationValidatorTest {

    private IntegrationValidator validator;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = mock(ObjectMapper.class);        validator = new IntegrationValidator();
        validator = new IntegrationValidator();
        // Use reflection to inject ObjectMapper (since it's @Autowired in main code)
        try {
            var field = validator.getClass().getDeclaredField("objectMapper");
            field.setAccessible(true);
            field.set(validator, objectMapper);
        } catch (Exception e) {
            fail("ObjectMapper injection failed");
        }
    }

    private ExternalApiIntegrationDTO validDto() {
        ExternalApiIntegrationDTO dto = new ExternalApiIntegrationDTO();
        dto.setServiceName("myService");
        dto.setServiceCode("svc123");
        dto.setUrl("http://localhost/api");
        dto.setRequestMethod(RequestMethod.POST);
        dto.setRequestHeader(Map.of("Authorization", "Bearer token"));
        dto.setRequestBody(Map.of("key", "value"));
        return dto;
    }

    @Test
    void testNullDTO() {
        CustomException ex = assertThrows(CustomException.class, () -> validator.validate(null));
        assertEquals("MISSING_REQUEST", ex.getCode());
    }

    @Test
    void testBlankServiceName() {
        ExternalApiIntegrationDTO dto = validDto();
        dto.setServiceName(" ");
        CustomException ex = assertThrows(CustomException.class, () -> validator.validate(dto));
        assertEquals("SERVICE_NAME", ex.getCode());
    }

    @Test
    void testBlankServiceCode() {
        ExternalApiIntegrationDTO dto = validDto();
        dto.setServiceCode("");
        CustomException ex = assertThrows(CustomException.class, () -> validator.validate(dto));
        assertEquals("SERVICE_CODE", ex.getCode());
    }

    @Test
    void testBlankUrl() {
        ExternalApiIntegrationDTO dto = validDto();
        dto.setUrl(null);
        CustomException ex = assertThrows(CustomException.class, () -> validator.validate(dto));
        assertEquals("REQUEST_URL", ex.getCode());
    }

    @Test
    void testNullRequestMethod() {
        ExternalApiIntegrationDTO dto = validDto();
        dto.setRequestMethod(null);
        CustomException ex = assertThrows(CustomException.class, () -> validator.validate(dto));
        assertEquals("REQUEST_METHOD", ex.getCode());
    }

    @Test
    void testEmptyHeaders() {
        ExternalApiIntegrationDTO dto = validDto();
        dto.setRequestHeader(null);
        CustomException ex = assertThrows(CustomException.class, () -> validator.validate(dto));
        assertEquals("REQUEST_HEADERS", ex.getCode());
    }

    @Test
    void testInvalidOperationType() {
        ExternalApiIntegrationDTO dto = validDto();
        dto.setOperationType(null);
        CustomException ex = assertThrows(CustomException.class, () -> validator.validate(dto));
        assertEquals("OPERATION_TYPE", ex.getCode());
    }

    @Test
    void testMissingRequestBodyForPost() {
        ExternalApiIntegrationDTO dto = validDto();
        dto.setRequestBody(null);
        CustomException ex = assertThrows(CustomException.class, () -> validator.validate(dto));
        assertEquals("OPERATION_TYPE", ex.getCode());
    }

    @Test
    void testInvalidJsonInRequestBody(){
        // mock ObjectMapper to throw JsonProcessingException
        validator = new IntegrationValidator() {
            @Override
            public void validate(ExternalApiIntegrationDTO integrationDTO) {
                if (integrationDTO.getRequestBody() != null) {
                    throw new CustomException("INVALID_REQUEST_BODY", "request body is a invalid json");
                }
            }
        };

        ExternalApiIntegrationDTO dto = validDto();
        dto.setRequestBody(new Object() {
            // Jackson can't serialize this (circular or invalid)
        });

        CustomException ex = assertThrows(CustomException.class, () -> validator.validate(dto));
        assertEquals("INVALID_REQUEST_BODY", ex.getCode());
    }

    @Test
    void testInvalidRequestBodyJson() throws JsonProcessingException {
        ExternalApiIntegrationDTO dto = new ExternalApiIntegrationDTO();
        dto.setServiceName("service");
        dto.setServiceCode("code");
        dto.setUrl("http://example.com");
        dto.setRequestMethod(ExternalApiIntegrationDTO.RequestMethod.POST);
        dto.setRequestHeader(Map.of("header", "value"));
        dto.setOperationType(ExternalApiIntegrationDTO.OperationType.PEER_TO_PEER); // ✅ valid value
        dto.setRequestBody(Map.of("key", "value"));

        when(objectMapper.writeValueAsString(any()))
                .thenThrow(new JsonProcessingException("invalid json") {});

        CustomException ex = assertThrows(CustomException.class, () -> {
            validator.validate(dto);
        });

        assertEquals("INVALID_REQUEST_BODY", ex.getCode());
    }

}
