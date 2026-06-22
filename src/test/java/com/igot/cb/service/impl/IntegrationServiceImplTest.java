package com.igot.cb.service.impl;

import com.igot.cb.exception.CustomException;
import com.igot.cb.model.ExternalApiIntegrationDTO;
import com.igot.cb.model.ResponseDTO;
import com.igot.cb.producer.Producer;
import com.igot.cb.service.APICallService;
import com.igot.cb.service.EnrichmentService;
import com.igot.cb.util.JWTTokenGeneratorUtil;
import com.igot.cb.validator.IntegrationValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.data.redis.core.ReactiveValueOperations;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("IntegrationServiceImpl Tests")
class IntegrationServiceImplTest {

    @Mock
    private IntegrationValidator integrationValidator;

    @Mock
    private APICallService apiCallService;

    @Mock
    private Producer producer;

    @Mock
    private EnrichmentService enrichmentService;

    @Mock
    private JWTTokenGeneratorUtil tokenGeneratorUtil;

    @Mock
    private ReactiveRedisOperations<String, ResponseDTO> cacheOps;

    @Mock
    private ReactiveValueOperations<String, ResponseDTO> valueOps;

    private IntegrationServiceImpl integrationService;

    @BeforeEach
    void setUp() {
        integrationService = new IntegrationServiceImpl(cacheOps);
        ReflectionTestUtils.setField(integrationService, "integrationValidator", integrationValidator);
        ReflectionTestUtils.setField(integrationService, "apiCallService", apiCallService);
        ReflectionTestUtils.setField(integrationService, "producer", producer);
        ReflectionTestUtils.setField(integrationService, "enrichmentService", enrichmentService);
        ReflectionTestUtils.setField(integrationService, "tokenGeneratorUtil", tokenGeneratorUtil);
        ReflectionTestUtils.setField(integrationService, "callExternalServiceTopic", "integration-topic");
    }

    @Test
    void createExternalAPICall_fireAndForget_success() {
        ExternalApiIntegrationDTO dto = baseDto(ExternalApiIntegrationDTO.OperationType.FIRE_AND_FORGET);
        when(tokenGeneratorUtil.generateRedisJwtTokenKey(any(), anyString(), anyString())).thenReturn("token-1");

        StepVerifier.create(integrationService.createExternalAPICall(dto))
                .assertNext(response -> {
                    assertNotNull(response.getId());
                    assertEquals(dto.getId(), response.getId());
                })
                .verifyComplete();

        verify(integrationValidator).validate(dto);
        verify(enrichmentService).enrich(dto);
        verify(tokenGeneratorUtil).generateRedisJwtTokenKey(dto.getRequestBody(), dto.getUrl(), dto.getOperationType().name());
        verify(producer).send("integration-topic", dto);
        verifyNoInteractions(apiCallService);
        verify(cacheOps, never()).opsForValue();
    }

    @Test
    void createExternalAPICall_fireAndForget_producerThrows_wrapsAsCustomException() {
        ExternalApiIntegrationDTO dto = baseDto(ExternalApiIntegrationDTO.OperationType.FIRE_AND_FORGET);
        when(tokenGeneratorUtil.generateRedisJwtTokenKey(any(), anyString(), anyString())).thenReturn("token-1");
        doThrow(new RuntimeException("kafka unavailable")).when(producer).send(anyString(), any());

        CustomException ex = assertThrows(CustomException.class,
                () -> integrationService.createExternalAPICall(dto));

        assertEquals("ERROR_IN_KAFKA_PRODUCER", ex.getCode());
        assertTrue(ex.getMessage().contains("kafka unavailable"));
        verifyNoInteractions(apiCallService);
        verify(cacheOps, never()).opsForValue();
    }

    @Test
    void createExternalAPICall_nonStrictCache_callsExternalApi() {
        ExternalApiIntegrationDTO dto = baseDto(ExternalApiIntegrationDTO.OperationType.PEER_TO_PEER);
        dto.setStrictCache(false);

        ResponseDTO apiResponse = ResponseDTO.builder().id("id-1").responseData("from-api").build();
        when(tokenGeneratorUtil.generateRedisJwtTokenKey(any(), anyString(), anyString())).thenReturn("token-2");
        when(apiCallService.makeExternalApiCall(dto)).thenReturn(Mono.just(apiResponse));

        StepVerifier.create(integrationService.createExternalAPICall(dto))
                .expectNext(apiResponse)
                .verifyComplete();

        verify(apiCallService).makeExternalApiCall(dto);
        verify(cacheOps, never()).opsForValue();
        verifyNoInteractions(producer);
    }

    @Test
    void createExternalAPICall_strictCache_alwaysReadFromCache_hit() {
        ExternalApiIntegrationDTO dto = baseDto(ExternalApiIntegrationDTO.OperationType.PEER_TO_PEER);
        dto.setStrictCache(true);
        dto.setAlwaysDataReadFromCache(true);

        ResponseDTO cached = ResponseDTO.builder().id("c1").responseData("cached-data").build();
        when(tokenGeneratorUtil.generateRedisJwtTokenKey(any(), anyString(), anyString())).thenReturn("token-3");
        when(cacheOps.opsForValue()).thenReturn(valueOps);
        when(valueOps.get("token-3")).thenReturn(Mono.just(cached));

        StepVerifier.create(integrationService.createExternalAPICall(dto))
                .expectNext(cached)
                .verifyComplete();

        verify(apiCallService, never()).makeExternalApiCall(any());
        verifyNoInteractions(producer);
    }

    @Test
    void createExternalAPICall_strictCache_alwaysReadFromCache_miss_returnsEmptyResponse() {
        ExternalApiIntegrationDTO dto = baseDto(ExternalApiIntegrationDTO.OperationType.PEER_TO_PEER);
        dto.setStrictCache(true);
        dto.setAlwaysDataReadFromCache(true);

        when(tokenGeneratorUtil.generateRedisJwtTokenKey(any(), anyString(), anyString())).thenReturn("token-4");
        when(cacheOps.opsForValue()).thenReturn(valueOps);
        when(valueOps.get("token-4")).thenReturn(Mono.empty());

        StepVerifier.create(integrationService.createExternalAPICall(dto))
                .assertNext(response -> {
                    assertNull(response.getId());
                    assertNull(response.getResponseData());
                })
                .verifyComplete();

        verify(apiCallService, never()).makeExternalApiCall(any());
        verifyNoInteractions(producer);
    }

    @Test
    void createExternalAPICall_strictCache_cacheHit_returnsCachedData() {
        ExternalApiIntegrationDTO dto = baseDto(ExternalApiIntegrationDTO.OperationType.PEER_TO_PEER);
        dto.setStrictCache(true);
        dto.setAlwaysDataReadFromCache(false);

        ResponseDTO cached = ResponseDTO.builder().id("c2").responseData("from-redis").build();
        when(tokenGeneratorUtil.generateRedisJwtTokenKey(any(), anyString(), anyString())).thenReturn("token-5");
        when(cacheOps.opsForValue()).thenReturn(valueOps);
        when(valueOps.get("token-5")).thenReturn(Mono.just(cached));
        // makeExternalApiCall is evaluated eagerly at Mono assembly time inside switchIfEmpty,
        // even on a cache hit. Stub it to avoid NullPointerException; its result is never subscribed to.
        when(apiCallService.makeExternalApiCall(dto)).thenReturn(Mono.empty());

        StepVerifier.create(integrationService.createExternalAPICall(dto))
                .expectNext(cached)
                .verifyComplete();

        // The method is invoked once at assembly time (not at subscription/execution time).
        // The cached result is returned, so no actual external API call is made.
        verify(apiCallService, atMost(1)).makeExternalApiCall(any());
        verifyNoInteractions(producer);
    }

    @Test
    void createExternalAPICall_strictCache_cacheMiss_callsExternalApi() {
        ExternalApiIntegrationDTO dto = baseDto(ExternalApiIntegrationDTO.OperationType.PEER_TO_PEER);
        dto.setStrictCache(true);
        dto.setAlwaysDataReadFromCache(false);

        ResponseDTO apiResponse = ResponseDTO.builder().id("a1").responseData("api-fallback").build();
        when(tokenGeneratorUtil.generateRedisJwtTokenKey(any(), anyString(), anyString())).thenReturn("token-6");
        when(cacheOps.opsForValue()).thenReturn(valueOps);
        when(valueOps.get("token-6")).thenReturn(Mono.empty());
        when(apiCallService.makeExternalApiCall(dto)).thenReturn(Mono.just(apiResponse));

        StepVerifier.create(integrationService.createExternalAPICall(dto))
                .expectNext(apiResponse)
                .verifyComplete();

        verify(apiCallService).makeExternalApiCall(dto);
        verifyNoInteractions(producer);
    }

    @Test
    void createExternalAPICall_validatorThrows_propagatesAndSkipsDownstream() {
        ExternalApiIntegrationDTO dto = baseDto(ExternalApiIntegrationDTO.OperationType.PEER_TO_PEER);
        doThrow(new CustomException("INVALID", "invalid input")).when(integrationValidator).validate(dto);

        CustomException ex = assertThrows(CustomException.class,
                () -> integrationService.createExternalAPICall(dto));

        assertEquals("INVALID", ex.getCode());
        verify(enrichmentService, never()).enrich(any());
        verifyNoInteractions(tokenGeneratorUtil, apiCallService, producer, cacheOps);
    }

    private ExternalApiIntegrationDTO baseDto(ExternalApiIntegrationDTO.OperationType operationType) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        return ExternalApiIntegrationDTO.builder()
                .url("https://example.org/external")
                .requestMethod(ExternalApiIntegrationDTO.RequestMethod.POST)
                .requestHeader(headers)
                .requestBody(Map.of("name", "test"))
                .serviceCode("svc-code")
                .serviceName("svc-name")
                .operationType(operationType)
                .strictCache(false)
                .alwaysDataReadFromCache(false)
                .build();
    }
}

