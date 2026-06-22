package com.igot.cb.validator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.igot.cb.exception.CustomException;
import com.igot.cb.model.ExternalApiIntegrationDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("IntegrationValidator Tests")
class IntegrationValidatorTest {

    @InjectMocks
    private IntegrationValidator integrationValidator;

    @Mock
    private ObjectMapper objectMapper;

    /**
     * Returns a fully valid POST DTO that passes all validations.
     */
    private ExternalApiIntegrationDTO buildValidPostDto() {
        return ExternalApiIntegrationDTO.builder()
                .serviceName("Test Service")
                .serviceCode("TEST_001")
                .url("https://api.example.com/test")
                .requestMethod(ExternalApiIntegrationDTO.RequestMethod.POST)
                .requestHeader(Map.of("Content-Type", "application/json"))
                .operationType(ExternalApiIntegrationDTO.OperationType.PEER_TO_PEER)
                .requestBody(Map.of("key", "value"))
                .build();
    }

    /**
     * Returns a fully valid GET DTO that passes all validations (no request body).
     */
    private ExternalApiIntegrationDTO buildValidGetDto() {
        return ExternalApiIntegrationDTO.builder()
                .serviceName("Test Service")
                .serviceCode("TEST_001")
                .url("https://api.example.com/test")
                .requestMethod(ExternalApiIntegrationDTO.RequestMethod.GET)
                .requestHeader(Map.of("Content-Type", "application/json"))
                .operationType(ExternalApiIntegrationDTO.OperationType.PEER_TO_PEER)
                .build();
    }

    @Test
    @DisplayName("validate - null DTO - throws MISSING_REQUEST exception")
    void validate_nullDto_throwsMissingRequest() {
        CustomException ex = assertThrows(CustomException.class,
                () -> integrationValidator.validate(null));

        assertEquals("MISSING_REQUEST", ex.getCode());
        assertEquals("request is missing!", ex.getMessage());
        verifyNoInteractions(objectMapper);
    }

    @Test
    @DisplayName("validate - blank serviceName - throws SERVICE_NAME exception")
    void validate_blankServiceName_throwsServiceName() {
        ExternalApiIntegrationDTO dto = buildValidPostDto();
        dto.setServiceName("");

        CustomException ex = assertThrows(CustomException.class,
                () -> integrationValidator.validate(dto));

        assertEquals("SERVICE_NAME", ex.getCode());
        assertEquals("service name is missing in request!", ex.getMessage());
    }

    @Test
    @DisplayName("validate - null serviceName - throws SERVICE_NAME exception")
    void validate_nullServiceName_throwsServiceName() {
        ExternalApiIntegrationDTO dto = buildValidPostDto();
        dto.setServiceName(null);

        CustomException ex = assertThrows(CustomException.class,
                () -> integrationValidator.validate(dto));

        assertEquals("SERVICE_NAME", ex.getCode());
    }

    @Test
    @DisplayName("validate - blank serviceCode - throws SERVICE_CODE exception")
    void validate_blankServiceCode_throwsServiceCode() {
        ExternalApiIntegrationDTO dto = buildValidPostDto();
        dto.setServiceCode("");

        CustomException ex = assertThrows(CustomException.class,
                () -> integrationValidator.validate(dto));

        assertEquals("SERVICE_CODE", ex.getCode());
        assertEquals("service code is missing in request!", ex.getMessage());
    }

    @Test
    @DisplayName("validate - null serviceCode - throws SERVICE_CODE exception")
    void validate_nullServiceCode_throwsServiceCode() {
        ExternalApiIntegrationDTO dto = buildValidPostDto();
        dto.setServiceCode(null);

        CustomException ex = assertThrows(CustomException.class,
                () -> integrationValidator.validate(dto));

        assertEquals("SERVICE_CODE", ex.getCode());
    }

    @Test
    @DisplayName("validate - blank url - throws REQUEST_URL exception")
    void validate_blankUrl_throwsRequestUrl() {
        ExternalApiIntegrationDTO dto = buildValidPostDto();
        dto.setUrl("");

        CustomException ex = assertThrows(CustomException.class,
                () -> integrationValidator.validate(dto));

        assertEquals("REQUEST_URL", ex.getCode());
        assertEquals("url is missing in request", ex.getMessage());
    }

    @Test
    @DisplayName("validate - null url - throws REQUEST_URL exception")
    void validate_nullUrl_throwsRequestUrl() {
        ExternalApiIntegrationDTO dto = buildValidPostDto();
        dto.setUrl(null);

        CustomException ex = assertThrows(CustomException.class,
                () -> integrationValidator.validate(dto));

        assertEquals("REQUEST_URL", ex.getCode());
    }

    @Test
    @DisplayName("validate - null requestMethod - throws REQUEST_METHOD exception")
    void validate_nullRequestMethod_throwsRequestMethod() {
        ExternalApiIntegrationDTO dto = buildValidPostDto();
        dto.setRequestMethod(null);

        CustomException ex = assertThrows(CustomException.class,
                () -> integrationValidator.validate(dto));

        assertEquals("REQUEST_METHOD", ex.getCode());
        assertEquals("request method is missing in request!", ex.getMessage());
    }

    @Test
    @DisplayName("validate - null requestHeaders - throws REQUEST_HEADERS exception")
    void validate_nullRequestHeaders_throwsRequestHeaders() {
        ExternalApiIntegrationDTO dto = buildValidPostDto();
        dto.setRequestHeader(null);

        CustomException ex = assertThrows(CustomException.class,
                () -> integrationValidator.validate(dto));

        assertEquals("REQUEST_HEADERS", ex.getCode());
        assertEquals("request headers are missing in request", ex.getMessage());
    }

    @Test
    @DisplayName("validate - empty requestHeaders map - throws REQUEST_HEADERS exception")
    void validate_emptyRequestHeaders_throwsRequestHeaders() {
        ExternalApiIntegrationDTO dto = buildValidPostDto();
        dto.setRequestHeader(new HashMap<>());

        CustomException ex = assertThrows(CustomException.class,
                () -> integrationValidator.validate(dto));

        assertEquals("REQUEST_HEADERS", ex.getCode());
    }

    @Test
    @DisplayName("validate - null operationType - throws OPERATION_TYPE exception")
    void validate_nullOperationType_throwsOperationType() {
        ExternalApiIntegrationDTO dto = buildValidPostDto();
        dto.setOperationType(null);

        CustomException ex = assertThrows(CustomException.class,
                () -> integrationValidator.validate(dto));

        assertEquals("OPERATION_TYPE", ex.getCode());
        assertEquals("Operation type is not valid!", ex.getMessage());
    }

    @Test
    @DisplayName("validate - null requestBody with POST method - throws MISSING_REQUEST_BODY exception")
    void validate_nullRequestBodyWithPostMethod_throwsMissingRequestBody() {
        ExternalApiIntegrationDTO dto = buildValidPostDto();
        dto.setRequestBody(null);

        CustomException ex = assertThrows(CustomException.class,
                () -> integrationValidator.validate(dto));

        assertEquals("MISSING_REQUEST_BODY", ex.getCode());
        assertEquals("request body is missing in request", ex.getMessage());
        verifyNoInteractions(objectMapper);
    }

    @Test
    @DisplayName("validate - null requestBody with PUT method - throws MISSING_REQUEST_BODY exception")
    void validate_nullRequestBodyWithPutMethod_throwsMissingRequestBody() {
        ExternalApiIntegrationDTO dto = buildValidPostDto();
        dto.setRequestMethod(ExternalApiIntegrationDTO.RequestMethod.PUT);
        dto.setRequestBody(null);

        CustomException ex = assertThrows(CustomException.class,
                () -> integrationValidator.validate(dto));

        assertEquals("MISSING_REQUEST_BODY", ex.getCode());
    }

    @Test
    @DisplayName("validate - null requestBody with DELETE method - throws MISSING_REQUEST_BODY exception")
    void validate_nullRequestBodyWithDeleteMethod_throwsMissingRequestBody() {
        ExternalApiIntegrationDTO dto = buildValidPostDto();
        dto.setRequestMethod(ExternalApiIntegrationDTO.RequestMethod.DELETE);
        dto.setRequestBody(null);

        CustomException ex = assertThrows(CustomException.class,
                () -> integrationValidator.validate(dto));

        assertEquals("MISSING_REQUEST_BODY", ex.getCode());
    }

    @Test
    @DisplayName("validate - null requestBody with GET method - does not throw (GET allows no body)")
    void validate_nullRequestBodyWithGetMethod_doesNotThrow() {
        ExternalApiIntegrationDTO dto = buildValidGetDto();
        // requestBody is null by default in buildValidGetDto

        assertDoesNotThrow(() -> integrationValidator.validate(dto));
        verifyNoInteractions(objectMapper);
    }

    @Test
    @DisplayName("validate - requestBody causes JsonProcessingException - throws INVALID_REQUEST_BODY exception")
    void validate_invalidRequestBodyJson_throwsInvalidRequestBody() throws JsonProcessingException {
        ExternalApiIntegrationDTO dto = buildValidPostDto();
        dto.setRequestBody(new Object());
        when(objectMapper.writeValueAsString(any()))
                .thenThrow(new JsonProcessingException("serialization error") {
                });

        CustomException ex = assertThrows(CustomException.class,
                () -> integrationValidator.validate(dto));

        assertEquals("INVALID_REQUEST_BODY", ex.getCode());
        assertEquals("request body is a invalid json", ex.getMessage());
    }

    @Test
    @DisplayName("validate - fully valid POST request - does not throw any exception")
    void validate_validPostRequest_doesNotThrow() throws JsonProcessingException {
        ExternalApiIntegrationDTO dto = buildValidPostDto();

        assertDoesNotThrow(() -> integrationValidator.validate(dto));
        verify(objectMapper).writeValueAsString(dto.getRequestBody());
    }

    @Test
    @DisplayName("validate - fully valid GET request with no body - does not throw any exception")
    void validate_validGetRequest_doesNotThrow() {
        ExternalApiIntegrationDTO dto = buildValidGetDto();

        assertDoesNotThrow(() -> integrationValidator.validate(dto));
        verifyNoInteractions(objectMapper);
    }

    @Test
    @DisplayName("validate - FIRE_AND_FORGET operationType - does not throw any exception")
    void validate_fireAndForgetOperationType_doesNotThrow() {
        ExternalApiIntegrationDTO dto = buildValidPostDto();
        dto.setOperationType(ExternalApiIntegrationDTO.OperationType.FIRE_AND_FORGET);

        assertDoesNotThrow(() -> integrationValidator.validate(dto));
    }

    @Test
    @DisplayName("validate - PATCH method with request body - does not throw any exception")
    void validate_patchMethodWithBody_doesNotThrow() {
        ExternalApiIntegrationDTO dto = buildValidPostDto();
        dto.setRequestMethod(ExternalApiIntegrationDTO.RequestMethod.PATCH);

        assertDoesNotThrow(() -> integrationValidator.validate(dto));
    }
}


