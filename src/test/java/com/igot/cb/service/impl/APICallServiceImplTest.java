package com.igot.cb.service.impl;

import com.igot.cb.exception.CustomException;
import com.igot.cb.model.ExternalApiIntegrationDTO;
import com.igot.cb.model.ResponseDTO;
import com.igot.cb.util.JWTTokenGeneratorUtil;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.*;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.data.redis.core.ReactiveValueOperations;
import org.springframework.http.HttpMethod;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.lang.reflect.Method;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class APICallServiceImplTest {

    private APICallServiceImpl service;
    private ReactiveRedisOperations<String, ResponseDTO> cacheOps;
    private ReactiveValueOperations<String, ResponseDTO> valueOps;
    private JWTTokenGeneratorUtil tokenUtil;
    private MockWebServer mockWebServer;

    @BeforeEach
    void setUp() throws Exception {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        cacheOps = mock(ReactiveRedisOperations.class);
        valueOps = mock(ReactiveValueOperations.class);
        tokenUtil = mock(JWTTokenGeneratorUtil.class);

        when(cacheOps.opsForValue()).thenReturn(valueOps);
        when(valueOps.set(anyString(), any(), any())).thenReturn(Mono.just(true));

        service = new APICallServiceImpl(cacheOps);
        service.cacheDataTtl = 1000L;
        service.maxResponseMemorySize = 1024 * 1024;

        when(tokenUtil.generateRedisJwtTokenKey(any(), any(), any())).thenReturn("token");
    }

    @AfterEach
    void tearDown() throws Exception {
        mockWebServer.shutdown();
    }

    ExternalApiIntegrationDTO dto(HttpMethod method) {
        return ExternalApiIntegrationDTO.builder()
                .url(mockWebServer.url("/").toString())
                .requestMethod(ExternalApiIntegrationDTO.RequestMethod.valueOf(method.name()))
                .requestHeader(Map.of("H", "V"))
                .requestBody(Map.of("k", "v"))
                .strictCacheTimeInMinutes(5)
                .operationType(ExternalApiIntegrationDTO.OperationType.PEER_TO_PEER)
                .build();
    }

    @Test
    void testMakeExternalApiCall_Error() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(500).setBody("Error!"));

        StepVerifier.create(service.makeExternalApiCall(dto(HttpMethod.GET)))
                .expectError(CustomException.class)
                .verify();
    }

    @Test
    void testSaveToRedis() {
        ResponseDTO resp = new ResponseDTO();
        ExternalApiIntegrationDTO dto = dto(HttpMethod.GET);

        StepVerifier.create(service.saveToRedis(dto, "token", resp))
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    void testConvertToStringMap() throws Exception {
        Method method = APICallServiceImpl.class
                .getDeclaredMethod("convertToStringMap", Map.class);
        method.setAccessible(true);

        Map<String, Object> input = Map.of("k", 123);
        Object result = method.invoke(service, input);
        assertNotNull(result);
        assertTrue(result.toString().contains("123"));
    }
}
