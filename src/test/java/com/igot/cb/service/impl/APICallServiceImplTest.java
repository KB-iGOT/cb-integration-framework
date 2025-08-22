package com.igot.cb.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.util.MultiValueMap;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.lang.reflect.Field;
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

        setField(service, "tokenGeneratorUtil", tokenUtil);
        setField(service, "objectMapper", new ObjectMapper());

        when(tokenUtil.generateRedisJwtTokenKey(any(), any(), any())).thenReturn("token");
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    @AfterEach
    void tearDown() throws Exception {
        mockWebServer.shutdown();
    }

    ExternalApiIntegrationDTO dto(HttpMethod method) {
        return dto(method, false, 5);
    }

    ExternalApiIntegrationDTO dto(HttpMethod method, boolean isFormData, long cacheTime) {
        return ExternalApiIntegrationDTO.builder()
                .url(mockWebServer.url("/").toString())
                .requestMethod(ExternalApiIntegrationDTO.RequestMethod.valueOf(method.name()))
                .requestHeader(Map.of("H", "V"))
                .requestBody(Map.of("k", "v"))
                .strictCacheTimeInMinutes(cacheTime)
                .operationType(ExternalApiIntegrationDTO.OperationType.PEER_TO_PEER)
                .isFormData(isFormData)
                .build();
    }

//    @Test
//    void testMakeExternalApiCall_GetSuccess() {
//        mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody("{\"result\":\"success\"}"));
//
//        StepVerifier.create(service.makeExternalApiCall(dto(HttpMethod.GET)))
//                .assertNext(response -> assertNotNull(response.getResponseData()))
//                .verifyComplete();
//    }

//    @Test
//    void testMakeExternalApiCall_DeleteSuccess() {
//        mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody("{\"deleted\":true}"));
//
//        StepVerifier.create(service.makeExternalApiCall(dto(HttpMethod.DELETE)))
//                .assertNext(response -> assertNotNull(response.getResponseData()))
//                .verifyComplete();
//    }

    @Test
    void testMakeExternalApiCall_PostWithJson() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody("{\"created\":true}"));

        StepVerifier.create(service.makeExternalApiCall(dto(HttpMethod.POST, false, 5)))
                .assertNext(response -> assertNotNull(response.getResponseData()))
                .verifyComplete();
    }

    @Test
    void testMakeExternalApiCall_PostWithFormData() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody("{\"form\":true}"));

        StepVerifier.create(service.makeExternalApiCall(dto(HttpMethod.POST, true, 5)))
                .assertNext(response -> assertNotNull(response.getResponseData()))
                .verifyComplete();
    }

    @Test
    void testMakeExternalApiCall_PostWithInvalidJson() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody("invalid json"));

        StepVerifier.create(service.makeExternalApiCall(dto(HttpMethod.POST, false, 5)))
                .assertNext(response -> assertEquals("invalid json", response.getResponseData()))
                .verifyComplete();
    }

    @Test
    void testMakeExternalApiCall_FormDataWithInvalidJson() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody("plain text"));

        StepVerifier.create(service.makeExternalApiCall(dto(HttpMethod.POST, true, 5)))
                .assertNext(response -> assertEquals("plain text", response.getResponseData()))
                .verifyComplete();
    }

    @Test
    void testMakeExternalApiCall_PutMethod() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody("{\"updated\":true}"));

        StepVerifier.create(service.makeExternalApiCall(dto(HttpMethod.PUT, false, 5)))
                .assertNext(response -> assertNotNull(response.getResponseData()))
                .verifyComplete();
    }

    @Test
    void testMakeExternalApiCall_PatchMethod() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody("{\"patched\":true}"));

        StepVerifier.create(service.makeExternalApiCall(dto(HttpMethod.PATCH, false, 5)))
                .assertNext(response -> assertNotNull(response.getResponseData()))
                .verifyComplete();
    }

    @Test
    void testMakeExternalApiCall_Error() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(500).setBody("Error!"));

        StepVerifier.create(service.makeExternalApiCall(dto(HttpMethod.GET)))
                .expectError(CustomException.class)
                .verify();
    }

    @Test
    void testSaveToRedis_WithUserCacheTime() {
        ResponseDTO resp = new ResponseDTO();
        ExternalApiIntegrationDTO dto = dto(HttpMethod.GET, false, 10);

        StepVerifier.create(service.saveToRedis(dto, "token", resp))
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    void testSaveToRedis_WithDefaultCacheTime() {
        ResponseDTO resp = new ResponseDTO();
        ExternalApiIntegrationDTO dto = dto(HttpMethod.GET, false, 0);

        StepVerifier.create(service.saveToRedis(dto, "token", resp))
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    void testSaveToRedis_WithNegativeCacheTime() {
        ResponseDTO resp = new ResponseDTO();
        ExternalApiIntegrationDTO dto = dto(HttpMethod.GET, false, -1);

        StepVerifier.create(service.saveToRedis(dto, "token", resp))
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    void testSaveToRedis_Error() {
        when(valueOps.set(anyString(), any(), any())).thenReturn(Mono.error(new RuntimeException("Redis error")));
        
        ResponseDTO resp = new ResponseDTO();
        ExternalApiIntegrationDTO dto = dto(HttpMethod.GET);

        StepVerifier.create(service.saveToRedis(dto, "token", resp))
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    void testConvertToStringMap() {
        Map<String, Object> input = Map.of("k", 123, "str", "value");
        MultiValueMap<String, String> result = service.convertToStringMap(input);
        
        assertNotNull(result);
        assertEquals("123", result.getFirst("k"));
        assertEquals("value", result.getFirst("str"));
    }

    @Test
    void testConvertToMultiValueMap() throws Exception {
        Method method = APICallServiceImpl.class
                .getDeclaredMethod("convertToMultiValueMap", Map.class);
        method.setAccessible(true);

        Map<String, String> input = Map.of("header1", "value1", "header2", "value2");
        MultiValueMap<String, String> result = (MultiValueMap<String, String>) method.invoke(service, input);
        
        assertNotNull(result);
        assertEquals("value1", result.getFirst("header1"));
        assertEquals("value2", result.getFirst("header2"));
    }

}