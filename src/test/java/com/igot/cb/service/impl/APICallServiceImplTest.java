package com.igot.cb.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.igot.cb.exception.CustomException;
import com.igot.cb.model.ExternalApiIntegrationDTO;
import com.igot.cb.model.ResponseDTO;
import com.igot.cb.util.JWTTokenGeneratorUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.data.redis.core.ReactiveValueOperations;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.MultiValueMap;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("APICallServiceImpl Tests")
class APICallServiceImplTest {

    @Mock
    private ReactiveRedisOperations<String, ResponseDTO> cacheOps;

    @Mock
    private ReactiveValueOperations<String, ResponseDTO> valueOps;

    @Mock
    private JWTTokenGeneratorUtil tokenGeneratorUtil;

    private APICallServiceImpl apiCallService;

    @BeforeEach
    void setUp() {
        apiCallService = new APICallServiceImpl(cacheOps);
        ReflectionTestUtils.setField(apiCallService, "tokenGeneratorUtil", tokenGeneratorUtil);
        ReflectionTestUtils.setField(apiCallService, "objectMapper", new ObjectMapper());
        apiCallService.cacheDataTtl = 5_000L;
        apiCallService.maxResponseMemorySize = 1024 * 1024;

        when(cacheOps.opsForValue()).thenReturn(valueOps);
        when(valueOps.set(anyString(), any(ResponseDTO.class), any(Duration.class))).thenReturn(Mono.just(true));
        when(tokenGeneratorUtil.generateRedisJwtTokenKey(any(), anyString(), anyString())).thenReturn("test-token");
    }

    @Test
    void makeExternalApiCall_getRequest_returnsJsonNodeAndCachesWithDefaultTtl() throws Exception {
        HttpServer server = startServer(200, "{\"status\":\"ok\"}", null);
        try {
            ExternalApiIntegrationDTO dto = buildDto(serverUrl(server), ExternalApiIntegrationDTO.RequestMethod.GET, null, false, 0);

            StepVerifier.create(apiCallService.makeExternalApiCall(dto))
                    .assertNext(response -> {
                        assertInstanceOf(JsonNode.class, response.getResponseData());
                        assertEquals("ok", ((JsonNode) response.getResponseData()).get("status").asText());
                    })
                    .verifyComplete();

            verify(tokenGeneratorUtil, times(1))
                    .generateRedisJwtTokenKey(dto.getRequestBody(), dto.getUrl(), dto.getOperationType().name());

            ArgumentCaptor<Duration> durationCaptor = ArgumentCaptor.forClass(Duration.class);
            verify(valueOps).set(eq("test-token"), any(ResponseDTO.class), durationCaptor.capture());
            assertEquals(Duration.ofMillis(apiCallService.cacheDataTtl), durationCaptor.getValue());
        } finally {
            server.stop(0);
        }
    }

    @Test
    void makeExternalApiCall_deleteRequest_hitsGetDeleteBranch() throws Exception {
        HttpServer server = startServer(200, "{\"deleted\":true}", null);
        try {
            ExternalApiIntegrationDTO dto = buildDto(serverUrl(server), ExternalApiIntegrationDTO.RequestMethod.DELETE, null, false, 0);

            StepVerifier.create(apiCallService.makeExternalApiCall(dto))
                    .assertNext(response -> {
                        assertInstanceOf(JsonNode.class, response.getResponseData());
                        assertTrue(((JsonNode) response.getResponseData()).get("deleted").asBoolean());
                    })
                    .verifyComplete();
        } finally {
            server.stop(0);
        }
    }

    @Test
    void makeExternalApiCall_postJsonBody_parsesJsonStringResponse() throws Exception {
        HttpServer server = startServer(200, "{\"result\":\"done\"}", null);
        try {
            Map<String, Object> body = Map.of("name", "alice");
            ExternalApiIntegrationDTO dto = buildDto(serverUrl(server), ExternalApiIntegrationDTO.RequestMethod.POST, body, false, 0);

            StepVerifier.create(apiCallService.makeExternalApiCall(dto))
                    .assertNext(response -> {
                        assertInstanceOf(JsonNode.class, response.getResponseData());
                        assertEquals("done", ((JsonNode) response.getResponseData()).get("result").asText());
                    })
                    .verifyComplete();
        } finally {
            server.stop(0);
        }
    }

    @Test
    void makeExternalApiCall_postJsonBody_nonJsonResponseStoresRawString() throws Exception {
        HttpServer server = startServer(200, "plain-response", null);
        try {
            Map<String, Object> body = Map.of("name", "bob");
            ExternalApiIntegrationDTO dto = buildDto(serverUrl(server), ExternalApiIntegrationDTO.RequestMethod.POST, body, false, 0);

            StepVerifier.create(apiCallService.makeExternalApiCall(dto))
                    .assertNext(response -> assertEquals("plain-response", response.getResponseData()))
                    .verifyComplete();
        } finally {
            server.stop(0);
        }
    }

    @Test
    void makeExternalApiCall_formDataRequest_convertsAndSendsFormData() throws Exception {
        AtomicReference<String> capturedBody = new AtomicReference<>("");
        HttpServer server = startServer(200, "{\"ok\":true}", exchange -> capturedBody.set(readRequestBody(exchange)));
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("a", 1);
            body.put("b", "two");
            ExternalApiIntegrationDTO dto = buildDto(serverUrl(server), ExternalApiIntegrationDTO.RequestMethod.POST, body, true, 0);

            StepVerifier.create(apiCallService.makeExternalApiCall(dto))
                    .assertNext(response -> assertInstanceOf(JsonNode.class, response.getResponseData()))
                    .verifyComplete();

            assertTrue(capturedBody.get().contains("a=1"));
            assertTrue(capturedBody.get().contains("b=two"));
        } finally {
            server.stop(0);
        }
    }

    @Test
    void makeExternalApiCall_whenWebClientResponseException_mapsToCustomExceptionWithStatusCode() throws Exception {
        HttpServer server = startServer(500, "upstream-failure", null);
        try {
            ExternalApiIntegrationDTO dto = buildDto(serverUrl(server), ExternalApiIntegrationDTO.RequestMethod.GET, null, false, 0);

            StepVerifier.create(apiCallService.makeExternalApiCall(dto))
                    .expectErrorSatisfies(error -> {
                        assertInstanceOf(CustomException.class, error);
                        CustomException customException = (CustomException) error;
                        assertEquals("EXTERNAL_SERVICE_CALL_ERROR", customException.getCode());
                        assertEquals("500", customException.getHttpStatusCode());
                        assertTrue(customException.getMessage().contains("upstream-failure"));
                    })
                    .verify();

            verify(tokenGeneratorUtil, never()).generateRedisJwtTokenKey(any(), anyString(), anyString());
        } finally {
            server.stop(0);
        }
    }

    @Test
    void makeExternalApiCall_whenGenericError_mapsToCustomExceptionWithoutStatusCode() {
        ExternalApiIntegrationDTO dto = buildDto("http://127.0.0.1:1/", ExternalApiIntegrationDTO.RequestMethod.GET, null, false, 0);

        StepVerifier.create(apiCallService.makeExternalApiCall(dto))
                .expectErrorSatisfies(error -> {
                    assertInstanceOf(CustomException.class, error);
                    CustomException customException = (CustomException) error;
                    assertEquals("EXTERNAL_SERVICE_CALL_ERROR", customException.getCode());
                    assertNull(customException.getHttpStatusCode());
                    assertNotNull(customException.getMessage());
                })
                .verify();
    }

    @Test
    void saveToRedis_whenStrictCacheTimeProvided_usesMinutesDuration() {
        ExternalApiIntegrationDTO dto = buildDto("http://localhost", ExternalApiIntegrationDTO.RequestMethod.GET, null, false, 10);
        ResponseDTO responseDTO = new ResponseDTO();
        responseDTO.setResponseData("cached");

        StepVerifier.create(apiCallService.saveToRedis(dto, "cache-key", responseDTO))
                .expectNext(true)
                .verifyComplete();

        verify(valueOps).set(eq("cache-key"), eq(responseDTO), eq(Duration.ofMinutes(10)));
    }

    @Test
    void saveToRedis_whenStrictCacheTimeMinusOne_usesDefaultMillisDuration() {
        ExternalApiIntegrationDTO dto = buildDto("http://localhost", ExternalApiIntegrationDTO.RequestMethod.GET, null, false, -1);
        ResponseDTO responseDTO = new ResponseDTO();
        responseDTO.setResponseData("cached-default");

        StepVerifier.create(apiCallService.saveToRedis(dto, "cache-key-2", responseDTO))
                .expectNext(true)
                .verifyComplete();

        verify(valueOps).set(eq("cache-key-2"), eq(responseDTO), eq(Duration.ofMillis(apiCallService.cacheDataTtl)));
    }

    @Test
    void saveToRedis_whenRedisFails_propagatesError() {
        when(valueOps.set(anyString(), any(ResponseDTO.class), any(Duration.class)))
                .thenReturn(Mono.error(new RuntimeException("redis down")));

        ExternalApiIntegrationDTO dto = buildDto("http://localhost", ExternalApiIntegrationDTO.RequestMethod.GET, null, false, 0);
        ResponseDTO responseDTO = new ResponseDTO();

        StepVerifier.create(apiCallService.saveToRedis(dto, "cache-key-3", responseDTO))
                .expectErrorMatches(error -> error instanceof RuntimeException
                        && error.getMessage().contains("redis down"))
                .verify();
    }

    @Test
    void convertToStringMap_convertsAllValuesToString() {
        Map<String, Object> input = new HashMap<>();
        input.put("num", 42);
        input.put("flag", true);

        MultiValueMap<String, String> result = apiCallService.convertToStringMap(input);

        assertEquals("42", result.getFirst("num"));
        assertEquals("true", result.getFirst("flag"));
    }

    private ExternalApiIntegrationDTO buildDto(String url,
                                               ExternalApiIntegrationDTO.RequestMethod method,
                                               Object requestBody,
                                               boolean isFormData,
                                               long strictCacheMinutes) {
        return ExternalApiIntegrationDTO.builder()
                .url(url)
                .requestMethod(method)
                .requestHeader(Map.of("Content-Type", "application/json"))
                .requestBody(requestBody)
                .operationType(ExternalApiIntegrationDTO.OperationType.PEER_TO_PEER)
                .strictCacheTimeInMinutes(strictCacheMinutes)
                .isFormData(isFormData)
                .build();
    }

    private HttpServer startServer(int statusCode,
                                   String responseBody,
                                   java.util.function.Consumer<HttpExchange> requestInspector) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/", exchange -> {
            if (requestInspector != null) {
                requestInspector.accept(exchange);
            }
            byte[] bytes = responseBody.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(statusCode, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        });
        server.start();
        return server;
    }

    private String serverUrl(HttpServer server) {
        return "http://localhost:" + server.getAddress().getPort() + "/";
    }

    private String readRequestBody(HttpExchange exchange) {
        try {
            return new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            return "";
        }
    }
}


