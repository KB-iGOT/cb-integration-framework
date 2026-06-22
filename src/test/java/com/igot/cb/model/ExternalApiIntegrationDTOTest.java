package com.igot.cb.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ExternalApiIntegrationDTO Tests")
class ExternalApiIntegrationDTOTest {

    // ─────────────────────────────────────────────────────────────
    // No-args constructor
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("noArgsConstructor - creates instance with default values")
    void noArgsConstructor_createsInstanceWithDefaultValues() {
        ExternalApiIntegrationDTO<String> dto = new ExternalApiIntegrationDTO<>();

        assertNotNull(dto);
        assertNull(dto.getUrl());
        assertNull(dto.getRequestMethod());
        assertNull(dto.getRequestHeader());
        assertNull(dto.getRequestBody());
        assertNull(dto.getResponseClassType());
        assertNull(dto.getServiceCode());
        assertNull(dto.getServiceName());
        assertNull(dto.getServiceDescription());
        assertNull(dto.getResponseData());
        assertNull(dto.getOperationType());
        assertNull(dto.getId());
        assertFalse(dto.isStrictCache());
        assertEquals(0L, dto.getStrictCacheTimeInMinutes());
        assertFalse(dto.isAlwaysDataReadFromCache());
        assertFalse(dto.isFormData());
    }

    // ─────────────────────────────────────────────────────────────
    // All-args constructor
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("allArgsConstructor - sets all fields correctly")
    void allArgsConstructor_setsAllFieldsCorrectly() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer token");

        ExternalApiIntegrationDTO<String> dto = new ExternalApiIntegrationDTO<>(
                "https://example.com/api",
                ExternalApiIntegrationDTO.RequestMethod.POST,
                headers,
                "{\"key\":\"value\"}",
                "String",
                "SVC001",
                "TestService",
                "A test service",
                "responseData",
                ExternalApiIntegrationDTO.OperationType.PEER_TO_PEER,
                "id-123",
                true,
                30L,
                false,
                true
        );

        assertEquals("https://example.com/api", dto.getUrl());
        assertEquals(ExternalApiIntegrationDTO.RequestMethod.POST, dto.getRequestMethod());
        assertEquals(headers, dto.getRequestHeader());
        assertEquals("{\"key\":\"value\"}", dto.getRequestBody());
        assertEquals("String", dto.getResponseClassType());
        assertEquals("SVC001", dto.getServiceCode());
        assertEquals("TestService", dto.getServiceName());
        assertEquals("A test service", dto.getServiceDescription());
        assertEquals("responseData", dto.getResponseData());
        assertEquals(ExternalApiIntegrationDTO.OperationType.PEER_TO_PEER, dto.getOperationType());
        assertEquals("id-123", dto.getId());
        assertTrue(dto.isStrictCache());
        assertEquals(30L, dto.getStrictCacheTimeInMinutes());
        assertFalse(dto.isAlwaysDataReadFromCache());
        assertTrue(dto.isFormData());
    }

    // ─────────────────────────────────────────────────────────────
    // Builder
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("builder - builds dto with all fields set")
    void builder_buildsDtoWithAllFieldsSet() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        ExternalApiIntegrationDTO<Object> dto = ExternalApiIntegrationDTO.builder()
                .url("https://api.example.com")
                .requestMethod(ExternalApiIntegrationDTO.RequestMethod.GET)
                .requestHeader(headers)
                .requestBody(null)
                .responseClassType(null)
                .serviceCode("SVC002")
                .serviceName("MyService")
                .serviceDescription("My service description")
                .responseData("data")
                .operationType(ExternalApiIntegrationDTO.OperationType.FIRE_AND_FORGET)
                .id("abc-456")
                .strictCache(true)
                .strictCacheTimeInMinutes(60L)
                .alwaysDataReadFromCache(true)
                .isFormData(false)
                .build();

        assertEquals("https://api.example.com", dto.getUrl());
        assertEquals(ExternalApiIntegrationDTO.RequestMethod.GET, dto.getRequestMethod());
        assertEquals(headers, dto.getRequestHeader());
        assertNull(dto.getRequestBody());
        assertEquals("SVC002", dto.getServiceCode());
        assertEquals("MyService", dto.getServiceName());
        assertEquals("My service description", dto.getServiceDescription());
        assertEquals("data", dto.getResponseData());
        assertEquals(ExternalApiIntegrationDTO.OperationType.FIRE_AND_FORGET, dto.getOperationType());
        assertEquals("abc-456", dto.getId());
        assertTrue(dto.isStrictCache());
        assertEquals(60L, dto.getStrictCacheTimeInMinutes());
        assertTrue(dto.isAlwaysDataReadFromCache());
        assertFalse(dto.isFormData());
    }

    @Test
    @DisplayName("builder - builds dto with default boolean fields when not set")
    void builder_buildsDtoWithDefaultBooleanFields() {
        ExternalApiIntegrationDTO<Object> dto = ExternalApiIntegrationDTO.builder()
                .url("https://api.example.com")
                .build();

        assertFalse(dto.isStrictCache());
        assertFalse(dto.isAlwaysDataReadFromCache());
        assertFalse(dto.isFormData());
        assertEquals(0L, dto.getStrictCacheTimeInMinutes());
    }

    // ─────────────────────────────────────────────────────────────
    // Getters & Setters
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("setters and getters - round-trip all fields")
    void settersAndGetters_roundTripAllFields() {
        ExternalApiIntegrationDTO<String> dto = new ExternalApiIntegrationDTO<>();

        Map<String, String> headers = new HashMap<>();
        headers.put("X-Custom-Header", "value");

        dto.setUrl("http://localhost:8080");
        dto.setRequestMethod(ExternalApiIntegrationDTO.RequestMethod.PUT);
        dto.setRequestHeader(headers);
        dto.setRequestBody("body");
        dto.setResponseClassType("responseType");
        dto.setServiceCode("CODE1");
        dto.setServiceName("ServiceName");
        dto.setServiceDescription("Description");
        dto.setResponseData("response");
        dto.setOperationType(ExternalApiIntegrationDTO.OperationType.PEER_TO_PEER);
        dto.setId("id-789");
        dto.setStrictCache(true);
        dto.setStrictCacheTimeInMinutes(120L);
        dto.setAlwaysDataReadFromCache(true);
        dto.setFormData(true);

        assertEquals("http://localhost:8080", dto.getUrl());
        assertEquals(ExternalApiIntegrationDTO.RequestMethod.PUT, dto.getRequestMethod());
        assertEquals(headers, dto.getRequestHeader());
        assertEquals("body", dto.getRequestBody());
        assertEquals("responseType", dto.getResponseClassType());
        assertEquals("CODE1", dto.getServiceCode());
        assertEquals("ServiceName", dto.getServiceName());
        assertEquals("Description", dto.getServiceDescription());
        assertEquals("response", dto.getResponseData());
        assertEquals(ExternalApiIntegrationDTO.OperationType.PEER_TO_PEER, dto.getOperationType());
        assertEquals("id-789", dto.getId());
        assertTrue(dto.isStrictCache());
        assertEquals(120L, dto.getStrictCacheTimeInMinutes());
        assertTrue(dto.isAlwaysDataReadFromCache());
        assertTrue(dto.isFormData());
    }

    // ─────────────────────────────────────────────────────────────
    // OperationType enum
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("OperationType.fromValue - returns correct enum for valid value")
    void operationType_fromValue_returnsCorrectEnum() {
        assertEquals(ExternalApiIntegrationDTO.OperationType.PEER_TO_PEER,
                ExternalApiIntegrationDTO.OperationType.fromValue("PEER_TO_PEER"));
        assertEquals(ExternalApiIntegrationDTO.OperationType.FIRE_AND_FORGET,
                ExternalApiIntegrationDTO.OperationType.fromValue("FIRE_AND_FORGET"));
    }

    @Test
    @DisplayName("OperationType.fromValue - returns null for unknown value")
    void operationType_fromValue_returnsNullForUnknownValue() {
        assertNull(ExternalApiIntegrationDTO.OperationType.fromValue("UNKNOWN"));
        assertNull(ExternalApiIntegrationDTO.OperationType.fromValue(""));
        assertNull(ExternalApiIntegrationDTO.OperationType.fromValue("peer_to_peer"));
    }

    @Test
    @DisplayName("OperationType.toString - returns string value")
    void operationType_toString_returnsStringValue() {
        assertEquals("PEER_TO_PEER", ExternalApiIntegrationDTO.OperationType.PEER_TO_PEER.toString());
        assertEquals("FIRE_AND_FORGET", ExternalApiIntegrationDTO.OperationType.FIRE_AND_FORGET.toString());
    }

    @Test
    @DisplayName("OperationType.values - contains all expected constants")
    void operationType_values_containsAllExpectedConstants() {
        ExternalApiIntegrationDTO.OperationType[] values = ExternalApiIntegrationDTO.OperationType.values();
        assertEquals(2, values.length);
        assertEquals(ExternalApiIntegrationDTO.OperationType.PEER_TO_PEER, values[0]);
        assertEquals(ExternalApiIntegrationDTO.OperationType.FIRE_AND_FORGET, values[1]);
    }

    @Test
    @DisplayName("OperationType.valueOf - returns correct enum constant by name")
    void operationType_valueOf_returnsCorrectEnumByName() {
        assertEquals(ExternalApiIntegrationDTO.OperationType.PEER_TO_PEER,
                ExternalApiIntegrationDTO.OperationType.valueOf("PEER_TO_PEER"));
        assertEquals(ExternalApiIntegrationDTO.OperationType.FIRE_AND_FORGET,
                ExternalApiIntegrationDTO.OperationType.valueOf("FIRE_AND_FORGET"));
    }

    // ─────────────────────────────────────────────────────────────
    // RequestMethod enum
    // ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("RequestMethod.fromValue - returns correct enum for valid value")
    void requestMethod_fromValue_returnsCorrectEnum() {
        assertEquals(ExternalApiIntegrationDTO.RequestMethod.GET,
                ExternalApiIntegrationDTO.RequestMethod.fromValue("GET"));
        assertEquals(ExternalApiIntegrationDTO.RequestMethod.HEAD,
                ExternalApiIntegrationDTO.RequestMethod.fromValue("HEAD"));
        assertEquals(ExternalApiIntegrationDTO.RequestMethod.POST,
                ExternalApiIntegrationDTO.RequestMethod.fromValue("POST"));
        assertEquals(ExternalApiIntegrationDTO.RequestMethod.PUT,
                ExternalApiIntegrationDTO.RequestMethod.fromValue("PUT"));
        assertEquals(ExternalApiIntegrationDTO.RequestMethod.PATCH,
                ExternalApiIntegrationDTO.RequestMethod.fromValue("PATCH"));
        assertEquals(ExternalApiIntegrationDTO.RequestMethod.DELETE,
                ExternalApiIntegrationDTO.RequestMethod.fromValue("DELETE"));
        assertEquals(ExternalApiIntegrationDTO.RequestMethod.OPTIONS,
                ExternalApiIntegrationDTO.RequestMethod.fromValue("OPTIONS"));
        assertEquals(ExternalApiIntegrationDTO.RequestMethod.TRACE,
                ExternalApiIntegrationDTO.RequestMethod.fromValue("TRACE"));
    }

    @Test
    @DisplayName("RequestMethod.fromValue - returns null for unknown value")
    void requestMethod_fromValue_returnsNullForUnknownValue() {
        assertNull(ExternalApiIntegrationDTO.RequestMethod.fromValue("CONNECT"));
        assertNull(ExternalApiIntegrationDTO.RequestMethod.fromValue(""));
        assertNull(ExternalApiIntegrationDTO.RequestMethod.fromValue("get"));
    }

    @Test
    @DisplayName("RequestMethod.toString - returns string value for all methods")
    void requestMethod_toString_returnsStringValue() {
        assertEquals("GET", ExternalApiIntegrationDTO.RequestMethod.GET.toString());
        assertEquals("HEAD", ExternalApiIntegrationDTO.RequestMethod.HEAD.toString());
        assertEquals("POST", ExternalApiIntegrationDTO.RequestMethod.POST.toString());
        assertEquals("PUT", ExternalApiIntegrationDTO.RequestMethod.PUT.toString());
        assertEquals("PATCH", ExternalApiIntegrationDTO.RequestMethod.PATCH.toString());
        assertEquals("DELETE", ExternalApiIntegrationDTO.RequestMethod.DELETE.toString());
        assertEquals("OPTIONS", ExternalApiIntegrationDTO.RequestMethod.OPTIONS.toString());
        assertEquals("TRACE", ExternalApiIntegrationDTO.RequestMethod.TRACE.toString());
    }

    @Test
    @DisplayName("RequestMethod.values - contains all eight HTTP methods")
    void requestMethod_values_containsAllEightMethods() {
        ExternalApiIntegrationDTO.RequestMethod[] values = ExternalApiIntegrationDTO.RequestMethod.values();
        assertEquals(8, values.length);
    }

    @Test
    @DisplayName("RequestMethod.valueOf - returns correct enum constant by name")
    void requestMethod_valueOf_returnsCorrectEnumByName() {
        assertEquals(ExternalApiIntegrationDTO.RequestMethod.GET,
                ExternalApiIntegrationDTO.RequestMethod.valueOf("GET"));
        assertEquals(ExternalApiIntegrationDTO.RequestMethod.DELETE,
                ExternalApiIntegrationDTO.RequestMethod.valueOf("DELETE"));
    }
}

