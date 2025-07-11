package com.igot.cb.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class ExternalApiIntegrationDTOTest {

    @Test
    void testNoArgsAndSettersGetters() {
        ExternalApiIntegrationDTO<String> dto = new ExternalApiIntegrationDTO<>();

        dto.setUrl("url");
        dto.setRequestMethod(ExternalApiIntegrationDTO.RequestMethod.GET);
        dto.setRequestHeader(Collections.singletonMap("key", "value"));
        dto.setRequestBody("body");
        dto.setResponseClassType("String.class");
        dto.setServiceCode("svcCode");
        dto.setServiceName("svcName");
        dto.setServiceDescription("desc");
        dto.setResponseData("resp");
        dto.setOperationType(ExternalApiIntegrationDTO.OperationType.FIRE_AND_FORGET);
        dto.setId("id123");
        dto.setStrictCache(true);
        dto.setStrictCacheTimeInMinutes(10L);
        dto.setAlwaysDataReadFromCache(true);
        dto.setFormData(true);

        assertEquals("url", dto.getUrl());
        assertEquals(ExternalApiIntegrationDTO.RequestMethod.GET, dto.getRequestMethod());
        assertEquals("body", dto.getRequestBody());
        assertEquals("svcCode", dto.getServiceCode());
        assertEquals("svcName", dto.getServiceName());
        assertEquals("desc", dto.getServiceDescription());
        assertEquals("resp", dto.getResponseData());
        assertEquals(ExternalApiIntegrationDTO.OperationType.FIRE_AND_FORGET, dto.getOperationType());
        assertEquals("id123", dto.getId());
        assertTrue(dto.isStrictCache());
        assertEquals(10L, dto.getStrictCacheTimeInMinutes());
        assertTrue(dto.isAlwaysDataReadFromCache());
        assertTrue(dto.isFormData());
    }

    @Test
    void testAllArgsConstructor() {
        ExternalApiIntegrationDTO<String> dto = new ExternalApiIntegrationDTO<>(
                "url",
                ExternalApiIntegrationDTO.RequestMethod.POST,
                Collections.singletonMap("k", "v"),
                "body",
                "respClass",
                "svcCode",
                "svcName",
                "desc",
                "resp",
                ExternalApiIntegrationDTO.OperationType.PEER_TO_PEER,
                "id",
                false,
                5L,
                false,
                false
        );

        assertEquals("url", dto.getUrl());
        assertEquals(ExternalApiIntegrationDTO.RequestMethod.POST, dto.getRequestMethod());
        assertEquals("body", dto.getRequestBody());
        assertEquals("svcCode", dto.getServiceCode());
        assertEquals("svcName", dto.getServiceName());
        assertEquals("desc", dto.getServiceDescription());
        assertEquals("resp", dto.getResponseData());
        assertEquals(ExternalApiIntegrationDTO.OperationType.PEER_TO_PEER, dto.getOperationType());
        assertEquals("id", dto.getId());
        assertFalse(dto.isStrictCache());
        assertEquals(5L, dto.getStrictCacheTimeInMinutes());
        assertFalse(dto.isAlwaysDataReadFromCache());
        assertFalse(dto.isFormData());
    }

    @Test
    void testBuilder() {
        ExternalApiIntegrationDTO<String> dto = ExternalApiIntegrationDTO.<String>builder()
                .url("url")
                .requestMethod(ExternalApiIntegrationDTO.RequestMethod.DELETE)
                .requestHeader(Collections.singletonMap("a", "b"))
                .requestBody("body")
                .responseClassType("respClass")
                .serviceCode("svcCode")
                .serviceName("svcName")
                .serviceDescription("desc")
                .responseData("resp")
                .operationType(ExternalApiIntegrationDTO.OperationType.PEER_TO_PEER)
                .id("id")
                .strictCache(true)
                .strictCacheTimeInMinutes(15L)
                .alwaysDataReadFromCache(true)
                .isFormData(true)
                .build();

        assertEquals("url", dto.getUrl());
        assertEquals(ExternalApiIntegrationDTO.RequestMethod.DELETE, dto.getRequestMethod());
        assertEquals("body", dto.getRequestBody());
        assertEquals("svcCode", dto.getServiceCode());
        assertEquals("svcName", dto.getServiceName());
        assertEquals("desc", dto.getServiceDescription());
        assertEquals("resp", dto.getResponseData());
        assertEquals(ExternalApiIntegrationDTO.OperationType.PEER_TO_PEER, dto.getOperationType());
        assertEquals("id", dto.getId());
        assertTrue(dto.isStrictCache());
        assertEquals(15L, dto.getStrictCacheTimeInMinutes());
        assertTrue(dto.isAlwaysDataReadFromCache());
        assertTrue(dto.isFormData());
    }

    @Test
    void testOperationTypeFromValueAndToString() {
        assertEquals(ExternalApiIntegrationDTO.OperationType.FIRE_AND_FORGET,
                ExternalApiIntegrationDTO.OperationType.fromValue("FIRE_AND_FORGET"));
        assertEquals(ExternalApiIntegrationDTO.OperationType.PEER_TO_PEER,
                ExternalApiIntegrationDTO.OperationType.fromValue("PEER_TO_PEER"));
        assertNull(ExternalApiIntegrationDTO.OperationType.fromValue("INVALID"));

        assertEquals("FIRE_AND_FORGET", ExternalApiIntegrationDTO.OperationType.FIRE_AND_FORGET.toString());
    }

    @Test
    void testRequestMethodFromValueAndToString() {
        assertEquals(ExternalApiIntegrationDTO.RequestMethod.GET,
                ExternalApiIntegrationDTO.RequestMethod.fromValue("GET"));
        assertEquals(ExternalApiIntegrationDTO.RequestMethod.POST,
                ExternalApiIntegrationDTO.RequestMethod.fromValue("POST"));
        assertNull(ExternalApiIntegrationDTO.RequestMethod.fromValue("INVALID"));

        assertEquals("GET", ExternalApiIntegrationDTO.RequestMethod.GET.toString());
    }

    @Test
    void testEnumJsonSerialization() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();

        String json = mapper.writeValueAsString(ExternalApiIntegrationDTO.RequestMethod.PUT);
        assertEquals("\"PUT\"", json);

        ExternalApiIntegrationDTO.RequestMethod method =
                mapper.readValue("\"PATCH\"", ExternalApiIntegrationDTO.RequestMethod.class);
        assertEquals(ExternalApiIntegrationDTO.RequestMethod.PATCH, method);

        String jsonOp = mapper.writeValueAsString(ExternalApiIntegrationDTO.OperationType.PEER_TO_PEER);
        assertEquals("\"PEER_TO_PEER\"", jsonOp);

        ExternalApiIntegrationDTO.OperationType op =
                mapper.readValue("\"FIRE_AND_FORGET\"", ExternalApiIntegrationDTO.OperationType.class);
        assertEquals(ExternalApiIntegrationDTO.OperationType.FIRE_AND_FORGET, op);
    }
}
