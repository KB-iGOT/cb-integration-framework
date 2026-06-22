package com.igot.cb.controller;

import com.igot.cb.model.ExternalApiIntegrationDTO;
import com.igot.cb.model.ResponseDTO;
import com.igot.cb.service.IntegrationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit test class for IntegrationController
 * Tests all endpoints and error handling scenarios
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("IntegrationController Tests")
public class IntegrationControllerTest {

    @Mock
    private IntegrationService integrationService;

    @InjectMocks
    private IntegrationController integrationController;

    private ExternalApiIntegrationDTO testRequestDTO;
    private ResponseDTO testResponseDTO;

    @BeforeEach
    void setUp() {
        // Initialize test data
        testRequestDTO = ExternalApiIntegrationDTO.builder()
                .url("https://api.example.com/test")
                .requestMethod(ExternalApiIntegrationDTO.RequestMethod.POST)
                .requestHeader(createTestHeaders())
                .requestBody(createTestRequestBody())
                .serviceCode("SERVICE_001")
                .serviceName("Test Service")
                .serviceDescription("Test Service Description")
                .operationType(ExternalApiIntegrationDTO.OperationType.PEER_TO_PEER)
                .id("test-id-123")
                .strictCache(false)
                .strictCacheTimeInMinutes(0)
                .alwaysDataReadFromCache(false)
                .isFormData(false)
                .build();

        testResponseDTO = ResponseDTO.builder()
                .responseData("Success Response")
                .id("test-id-123")
                .build();
    }

    @Test
    @DisplayName("Should successfully create external API call and return response")
    void testCreateExternalAPICallSuccess() {
        when(integrationService.createExternalAPICall(any(ExternalApiIntegrationDTO.class)))
                .thenReturn(Mono.just(testResponseDTO));

        // Act & Assert
        Mono<ResponseDTO> result = integrationController.createExternalAPICall(testRequestDTO);

        StepVerifier.create(result)
                .expectNext(testResponseDTO)
                .verifyComplete();

        verify(integrationService, times(1)).createExternalAPICall(any(ExternalApiIntegrationDTO.class));
    }

    @Test
    @DisplayName("Should handle exception and return error mono")
    void testCreateExternalAPICallWithException() {
        // Arrange
        RuntimeException exception = new RuntimeException("Service unavailable");
        when(integrationService.createExternalAPICall(any(ExternalApiIntegrationDTO.class)))
                .thenReturn(Mono.error(exception));

        // Act & Assert
        Mono<ResponseDTO> result = integrationController.createExternalAPICall(testRequestDTO);

        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();

        verify(integrationService, times(1)).createExternalAPICall(any(ExternalApiIntegrationDTO.class));
    }

    @Test
    @DisplayName("Should successfully handle health check endpoint")
    void testHealthCheck() {
        // Act
        String result = integrationController.healthCheck();

        // Assert
        assert result.equals("Success");
    }

    @Test
    @DisplayName("Should correctly pass POST request with headers and body")
    void testCreateExternalAPICallWithComplexPayload() {
        // Arrange
        ExternalApiIntegrationDTO complexRequest = ExternalApiIntegrationDTO.builder()
                .url("https://api.example.com/complex")
                .requestMethod(ExternalApiIntegrationDTO.RequestMethod.PUT)
                .requestHeader(createComplexHeaders())
                .requestBody(createComplexRequestBody())
                .serviceCode("COMPLEX_SERVICE")
                .serviceName("Complex Service")
                .operationType(ExternalApiIntegrationDTO.OperationType.FIRE_AND_FORGET)
                .strictCache(true)
                .strictCacheTimeInMinutes(60)
                .alwaysDataReadFromCache(true)
                .isFormData(false)
                .build();

        ResponseDTO complexResponse = ResponseDTO.builder()
                .responseData("Complex Response")
                .id("complex-id-456")
                .build();

        when(integrationService.createExternalAPICall(any(ExternalApiIntegrationDTO.class)))
                .thenReturn(Mono.just(complexResponse));

        // Act & Assert
        Mono<ResponseDTO> result = integrationController.createExternalAPICall(complexRequest);

        StepVerifier.create(result)
                .expectNext(complexResponse)
                .verifyComplete();

        verify(integrationService, times(1)).createExternalAPICall(any(ExternalApiIntegrationDTO.class));
    }

    @Test
    @DisplayName("Should handle GET request method")
    void testCreateExternalAPICallWithGetMethod() {
        // Arrange
        ExternalApiIntegrationDTO getRequest = ExternalApiIntegrationDTO.builder()
                .url("https://api.example.com/resource")
                .requestMethod(ExternalApiIntegrationDTO.RequestMethod.GET)
                .serviceCode("GET_SERVICE")
                .serviceName("Get Service")
                .operationType(ExternalApiIntegrationDTO.OperationType.PEER_TO_PEER)
                .build();

        when(integrationService.createExternalAPICall(any(ExternalApiIntegrationDTO.class)))
                .thenReturn(Mono.just(testResponseDTO));

        // Act & Assert
        Mono<ResponseDTO> result = integrationController.createExternalAPICall(getRequest);

        StepVerifier.create(result)
                .expectNext(testResponseDTO)
                .verifyComplete();

        verify(integrationService, times(1)).createExternalAPICall(any(ExternalApiIntegrationDTO.class));
    }

    @Test
    @DisplayName("Should handle DELETE request method")
    void testCreateExternalAPICallWithDeleteMethod() {
        // Arrange
        ExternalApiIntegrationDTO deleteRequest = ExternalApiIntegrationDTO.builder()
                .url("https://api.example.com/resource/123")
                .requestMethod(ExternalApiIntegrationDTO.RequestMethod.DELETE)
                .serviceCode("DELETE_SERVICE")
                .serviceName("Delete Service")
                .operationType(ExternalApiIntegrationDTO.OperationType.FIRE_AND_FORGET)
                .build();

        when(integrationService.createExternalAPICall(any(ExternalApiIntegrationDTO.class)))
                .thenReturn(Mono.just(testResponseDTO));

        // Act & Assert
        Mono<ResponseDTO> result = integrationController.createExternalAPICall(deleteRequest);

        StepVerifier.create(result)
                .expectNext(testResponseDTO)
                .verifyComplete();

        verify(integrationService, times(1)).createExternalAPICall(any(ExternalApiIntegrationDTO.class));
    }

    @Test
    @DisplayName("Should handle timeout exception from service")
    void testCreateExternalAPICallWithTimeoutException() {
        // Arrange
        IllegalStateException exception = new IllegalStateException("Request timeout");
        when(integrationService.createExternalAPICall(any(ExternalApiIntegrationDTO.class)))
                .thenReturn(Mono.error(exception));

        // Act & Assert
        Mono<ResponseDTO> result = integrationController.createExternalAPICall(testRequestDTO);

        StepVerifier.create(result)
                .expectError(IllegalStateException.class)
                .verify();

        verify(integrationService, times(1)).createExternalAPICall(any(ExternalApiIntegrationDTO.class));
    }

    @Test
    @DisplayName("Should handle multiple sequential API calls")
    void testMultipleSequentialCalls() {
        // Arrange
        ResponseDTO response1 = ResponseDTO.builder().responseData("Response 1").id("id-1").build();
        ResponseDTO response2 = ResponseDTO.builder().responseData("Response 2").id("id-2").build();

        when(integrationService.createExternalAPICall(any(ExternalApiIntegrationDTO.class)))
                .thenReturn(Mono.just(response1))
                .thenReturn(Mono.just(response2));

        // Act & Assert
        Mono<ResponseDTO> result1 = integrationController.createExternalAPICall(testRequestDTO);
        Mono<ResponseDTO> result2 = integrationController.createExternalAPICall(testRequestDTO);

        StepVerifier.create(result1)
                .expectNext(response1)
                .verifyComplete();

        StepVerifier.create(result2)
                .expectNext(response2)
                .verifyComplete();

        verify(integrationService, times(2)).createExternalAPICall(any(ExternalApiIntegrationDTO.class));
    }

    @Test
    @DisplayName("Should handle form data flag in request")
    void testCreateExternalAPICallWithFormData() {
        // Arrange
        ExternalApiIntegrationDTO formDataRequest = ExternalApiIntegrationDTO.builder()
                .url("https://api.example.com/upload")
                .requestMethod(ExternalApiIntegrationDTO.RequestMethod.POST)
                .serviceCode("FORM_SERVICE")
                .serviceName("Form Service")
                .isFormData(true)
                .operationType(ExternalApiIntegrationDTO.OperationType.PEER_TO_PEER)
                .build();

        when(integrationService.createExternalAPICall(any(ExternalApiIntegrationDTO.class)))
                .thenReturn(Mono.just(testResponseDTO));

        // Act & Assert
        Mono<ResponseDTO> result = integrationController.createExternalAPICall(formDataRequest);

        StepVerifier.create(result)
                .expectNext(testResponseDTO)
                .verifyComplete();

        verify(integrationService, times(1)).createExternalAPICall(any(ExternalApiIntegrationDTO.class));
    }

    // Helper methods
    private Map<String, String> createTestHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", "Bearer token123");
        return headers;
    }

    private Map<String, String> createComplexHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", "Bearer token123");
        headers.put("X-Custom-Header", "custom-value");
        headers.put("X-Request-ID", "req-123");
        return headers;
    }

    private Map<String, Object> createTestRequestBody() {
        Map<String, Object> body = new HashMap<>();
        body.put("name", "Test User");
        body.put("email", "test@example.com");
        body.put("action", "create");
        return body;
    }

    private Map<String, Object> createComplexRequestBody() {
        Map<String, Object> body = new HashMap<>();
        body.put("id", "123");
        body.put("name", "Complex Test");
        body.put("nested", createTestRequestBody());
        return body;
    }
}

