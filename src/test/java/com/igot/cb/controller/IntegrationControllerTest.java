package com.igot.cb.controller;

import com.igot.cb.model.ExternalApiIntegrationDTO;
import com.igot.cb.model.ResponseDTO;
import com.igot.cb.service.IntegrationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

class IntegrationControllerTest {

    private IntegrationController controller;
    private IntegrationService integrationService;

    @BeforeEach
    void setUp() throws Exception {
        controller = new IntegrationController();
        integrationService = Mockito.mock(IntegrationService.class);

        // set private field via reflection
        Field field = controller.getClass().getDeclaredField("IntegrationService");
        field.setAccessible(true);
        field.set(controller, integrationService);
    }

    @Test
    void testCreateExternalAPICall_Success() {
        ExternalApiIntegrationDTO dto = new ExternalApiIntegrationDTO();
        ResponseDTO responseDTO = new ResponseDTO();
        responseDTO.setId("123");

        Mockito.when(integrationService.createExternalAPICall(any()))
                .thenReturn(Mono.just(responseDTO));

        Mono<ResponseDTO> result = controller.createExternalAPICall(dto);
        ResponseDTO actual = result.block();

        assertNotNull(actual);
        assertEquals("123", actual.getId());

        Mockito.verify(integrationService).createExternalAPICall(any());
    }

    @Test
    void testCreateExternalAPICall_Exception() {
        ExternalApiIntegrationDTO dto = new ExternalApiIntegrationDTO();

        Mockito.when(integrationService.createExternalAPICall(any()))
                .thenThrow(new RuntimeException("error"));

        Mono<ResponseDTO> result = controller.createExternalAPICall(dto);

        Exception exception = assertThrows(RuntimeException.class, result::block);
        assertEquals("error", exception.getMessage());

        Mockito.verify(integrationService).createExternalAPICall(any());
    }

    @Test
    void testHealthCheck() {
        String result = controller.healthCheck();
        assertEquals("Success", result);
    }
}
