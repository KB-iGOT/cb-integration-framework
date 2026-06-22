package com.igot.cb.service.impl;

import com.igot.cb.model.ExternalApiIntegrationDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("EnrichmentServiceImpl Tests")
class EnrichmentServiceImplTest {

    private EnrichmentServiceImpl enrichmentService;

    @BeforeEach
    void setUp() {
        enrichmentService = new EnrichmentServiceImpl();
    }

    @Test
    @DisplayName("enrich should not throw for populated dto")
    void enrich_withPopulatedDto_shouldNotThrowAndKeepValues() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer token");

        ExternalApiIntegrationDTO dto = ExternalApiIntegrationDTO.builder()
                .url("https://example.org")
                .requestMethod(ExternalApiIntegrationDTO.RequestMethod.POST)
                .requestHeader(headers)
                .serviceCode("svc-1")
                .operationType(ExternalApiIntegrationDTO.OperationType.PEER_TO_PEER)
                .build();

        assertDoesNotThrow(() -> enrichmentService.enrich(dto));

        // Current implementation is a no-op, so values should remain unchanged.
        assertEquals("https://example.org", dto.getUrl());
        assertEquals(ExternalApiIntegrationDTO.RequestMethod.POST, dto.getRequestMethod());
        assertEquals("Bearer token", dto.getRequestHeader().get("Authorization"));
    }

    @Test
    @DisplayName("enrich should not throw for null dto")
    void enrich_withNullDto_shouldNotThrow() {
        assertDoesNotThrow(() -> enrichmentService.enrich(null));
    }

    @Test
    @DisplayName("enrich should not throw for dto with null header")
    void enrich_withDtoHavingNullHeader_shouldNotThrowAndKeepHeaderNull() {
        ExternalApiIntegrationDTO dto = ExternalApiIntegrationDTO.builder()
                .url("https://example.org/no-header")
                .requestMethod(ExternalApiIntegrationDTO.RequestMethod.GET)
                .requestHeader(null)
                .build();

        assertDoesNotThrow(() -> enrichmentService.enrich(dto));
        assertNull(dto.getRequestHeader());
    }
}

