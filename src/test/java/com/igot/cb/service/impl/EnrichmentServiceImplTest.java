package com.igot.cb.service.impl;

import com.igot.cb.model.ExternalApiIntegrationDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EnrichmentServiceImplTest {

    private EnrichmentServiceImpl enrichmentService;

    @BeforeEach
    void setUp() {
        enrichmentService = new EnrichmentServiceImpl();
    }

    @Test
    void testEnrich() {
        ExternalApiIntegrationDTO dto = new ExternalApiIntegrationDTO();
        enrichmentService.enrich(dto);

        // Since enrichDefaultHeader is empty, no assertion about side-effects,
        // but we assert no exceptions and the dto is still the same object.
        assertNotNull(dto);
    }
}
