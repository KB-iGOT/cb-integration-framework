package com.igot.cb.service.impl;

import com.igot.cb.model.ExternalApiIntegrationDTO;
import com.igot.cb.model.ResponseDTO;
import com.igot.cb.producer.Producer;
import com.igot.cb.service.APICallService;
import com.igot.cb.service.EnrichmentService;
import com.igot.cb.util.JWTTokenGeneratorUtil;
import com.igot.cb.validator.IntegrationValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.data.redis.core.ReactiveValueOperations;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Map;
import static org.mockito.Mockito.*;

class IntegrationServiceImplTest {

    private IntegrationServiceImpl service;

    @Mock private IntegrationValidator integrationValidator;
    @Mock private EnrichmentService enrichmentService;
    @Mock private Producer producer;
    @Mock private APICallService apiCallService;
    @Mock private JWTTokenGeneratorUtil tokenGeneratorUtil;
    @Mock private ReactiveRedisOperations<String, ResponseDTO> cacheOps;
    @Mock private ReactiveValueOperations<String, ResponseDTO> valueOps;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
        service = new IntegrationServiceImpl(cacheOps);

        ReflectionTestUtils.setField(service, "integrationValidator", integrationValidator);
        ReflectionTestUtils.setField(service, "enrichmentService", enrichmentService);
        ReflectionTestUtils.setField(service, "producer", producer);
        ReflectionTestUtils.setField(service, "apiCallService", apiCallService);
        ReflectionTestUtils.setField(service, "tokenGeneratorUtil", tokenGeneratorUtil);
        ReflectionTestUtils.setField(service, "callExternalServiceTopic", "topic");
    }

    private ExternalApiIntegrationDTO dto() {
        return ExternalApiIntegrationDTO.builder()
                .serviceName("svc")
                .serviceCode("code")
                .url("http://test")
                .requestHeader(Map.of("H", "V"))
                .requestMethod(ExternalApiIntegrationDTO.RequestMethod.POST)
                .requestBody(Map.of("k", "v"))
                .operationType(ExternalApiIntegrationDTO.OperationType.PEER_TO_PEER)
                .strictCache(false)
                .build();
    }

    @Test
    void fireAndForgetSuccess() {
        ExternalApiIntegrationDTO dto = dto();
        dto.setOperationType(ExternalApiIntegrationDTO.OperationType.FIRE_AND_FORGET);

        doNothing().when(integrationValidator).validate(dto);
        doNothing().when(enrichmentService).enrich(dto);
        when(tokenGeneratorUtil.generateRedisJwtTokenKey(any(), any(), any())).thenReturn("token");
        doNothing().when(producer).send(any(), any());

        StepVerifier.create(service.createExternalAPICall(dto))
                .expectNextMatches(resp -> resp.getId() != null)
                .verifyComplete();

        verify(producer).send(any(), any());
    }

    @Test
    void strictCacheFalse() {
        ExternalApiIntegrationDTO dto = dto();
        dto.setStrictCache(false);

        doNothing().when(integrationValidator).validate(dto);
        doNothing().when(enrichmentService).enrich(dto);
        when(tokenGeneratorUtil.generateRedisJwtTokenKey(any(), any(), any())).thenReturn("token");
        when(apiCallService.makeExternalApiCall(dto)).thenReturn(Mono.just(new ResponseDTO()));

        StepVerifier.create(service.createExternalAPICall(dto))
                .expectNextCount(1)
                .verifyComplete();

        verify(apiCallService).makeExternalApiCall(any());
    }

    @Test
    void strictCacheTrueAlwaysReadFromCacheHit() {
        ExternalApiIntegrationDTO dto = ExternalApiIntegrationDTO.builder()
                .serviceName("svc")
                .serviceCode("code")
                .url("http://test")
                .requestHeader(Map.of("H", "V"))
                .requestMethod(ExternalApiIntegrationDTO.RequestMethod.GET)
                .requestBody(Map.of("k", "v"))
                .operationType(ExternalApiIntegrationDTO.OperationType.PEER_TO_PEER)
                .strictCache(true)
                .alwaysDataReadFromCache(true)
                .build();

        doNothing().when(integrationValidator).validate(dto);
        doNothing().when(enrichmentService).enrich(dto);

        when(tokenGeneratorUtil.generateRedisJwtTokenKey(any(), any(), any()))
                .thenReturn("token");

        when(cacheOps.opsForValue()).thenReturn(valueOps);
        when(valueOps.get(anyString())).thenReturn(Mono.just(new ResponseDTO()));

        StepVerifier.create(service.createExternalAPICall(dto))
                .expectNextMatches(r -> r != null)
                .verifyComplete();
    }

    @Test
    void strictCacheTrueAlwaysReadFromCacheMiss() {
        ExternalApiIntegrationDTO dto = dto();
        dto.setStrictCache(true);
        dto.setAlwaysDataReadFromCache(true);

        doNothing().when(integrationValidator).validate(dto);
        doNothing().when(enrichmentService).enrich(dto);

        when(tokenGeneratorUtil.generateRedisJwtTokenKey(any(), any(), any()))
                .thenReturn("token");

        when(cacheOps.opsForValue()).thenReturn(valueOps);
        when(valueOps.get(anyString())).thenReturn(Mono.empty());

        StepVerifier.create(service.createExternalAPICall(dto))
                .expectNextCount(1)
                .verifyComplete();
    }
}
