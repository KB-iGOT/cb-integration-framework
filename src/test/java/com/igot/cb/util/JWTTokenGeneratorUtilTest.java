package com.igot.cb.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JWTTokenGeneratorUtil Tests")
class JWTTokenGeneratorUtilTest {

    @InjectMocks
    private JWTTokenGeneratorUtil jwtTokenGeneratorUtil;

    @Mock
    private ObjectMapper objectMapper;

    private static final String TEST_SECRET = "test-secret-key-for-unit-tests";
    private static final String TEST_URL = "https://api.example.com/test";
    private static final String TEST_OPERATION = "PEER_TO_PEER";
    private static final String SERIALIZED_BODY = "{\"key\":\"value\"}";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtTokenGeneratorUtil, "jwtSecretKey", TEST_SECRET);
    }

    @Test
    @DisplayName("generateRedisJwtTokenKey - all valid params - returns signed JWT with correct claims")
    void generateRedisJwtTokenKey_allValidParams_returnsJwtWithCorrectClaims() throws JsonProcessingException {
        Map<String, String> requestBody = Map.of("key", "value");
        when(objectMapper.writeValueAsString(requestBody)).thenReturn(SERIALIZED_BODY);

        String token = jwtTokenGeneratorUtil.generateRedisJwtTokenKey(requestBody, TEST_URL, TEST_OPERATION);

        assertNotNull(token);
        assertFalse(token.isEmpty());

        DecodedJWT decoded = JWT.require(Algorithm.HMAC256(TEST_SECRET)).build().verify(token);
        assertEquals(SERIALIZED_BODY, decoded.getClaim("requestBody").asString());
        assertEquals(TEST_URL, decoded.getClaim("url").asString());
        assertEquals(TEST_OPERATION, decoded.getClaim("operationType").asString());
    }

    @Test
    @DisplayName("generateRedisJwtTokenKey - null requestBody - returns JWT with empty requestBody claim")
    void generateRedisJwtTokenKey_nullRequestBody_returnsJwtWithEmptyRequestBodyClaim() {
        // objectMapper.writeValueAsString must NOT be called when requestBody is null
        String token = jwtTokenGeneratorUtil.generateRedisJwtTokenKey(null, TEST_URL, TEST_OPERATION);

        assertNotNull(token);
        assertFalse(token.isEmpty());

        DecodedJWT decoded = JWT.require(Algorithm.HMAC256(TEST_SECRET)).build().verify(token);
        assertEquals("", decoded.getClaim("requestBody").asString());
        assertEquals(TEST_URL, decoded.getClaim("url").asString());
        assertEquals(TEST_OPERATION, decoded.getClaim("operationType").asString());

        verifyNoInteractions(objectMapper);
    }

    @Test
    @DisplayName("generateRedisJwtTokenKey - blank url - returns empty string")
    void generateRedisJwtTokenKey_blankUrl_returnsEmptyString() {
        String token = jwtTokenGeneratorUtil.generateRedisJwtTokenKey(null, "", TEST_OPERATION);

        assertNotNull(token);
        assertEquals("", token);
    }

    @Test
    @DisplayName("generateRedisJwtTokenKey - null url - returns empty string")
    void generateRedisJwtTokenKey_nullUrl_returnsEmptyString() {
        String token = jwtTokenGeneratorUtil.generateRedisJwtTokenKey(null, null, TEST_OPERATION);

        assertNotNull(token);
        assertEquals("", token);
    }

    @Test
    @DisplayName("generateRedisJwtTokenKey - blank operationType - returns empty string")
    void generateRedisJwtTokenKey_blankOperationType_returnsEmptyString() {
        String token = jwtTokenGeneratorUtil.generateRedisJwtTokenKey(null, TEST_URL, "");

        assertNotNull(token);
        assertEquals("", token);
    }

    @Test
    @DisplayName("generateRedisJwtTokenKey - null operationType - returns empty string")
    void generateRedisJwtTokenKey_nullOperationType_returnsEmptyString() {
        String token = jwtTokenGeneratorUtil.generateRedisJwtTokenKey(null, TEST_URL, null);

        assertNotNull(token);
        assertEquals("", token);
    }

    @Test
    @DisplayName("generateRedisJwtTokenKey - ObjectMapper throws JsonProcessingException - returns JWT with empty requestBody claim")
    void generateRedisJwtTokenKey_objectMapperThrows_returnsJwtWithEmptyRequestBodyClaim() throws JsonProcessingException {
        Map<String, String> requestBody = new HashMap<>();
        when(objectMapper.writeValueAsString(any()))
                .thenThrow(new JsonProcessingException("serialization error") {
                });

        String token = jwtTokenGeneratorUtil.generateRedisJwtTokenKey(requestBody, TEST_URL, TEST_OPERATION);

        assertNotNull(token);
        assertFalse(token.isEmpty());

        DecodedJWT decoded = JWT.require(Algorithm.HMAC256(TEST_SECRET)).build().verify(token);
        assertEquals("", decoded.getClaim("requestBody").asString());
        assertEquals(TEST_URL, decoded.getClaim("url").asString());
        assertEquals(TEST_OPERATION, decoded.getClaim("operationType").asString());
    }

    @Test
    @DisplayName("generateRedisJwtTokenKeyForFile - all valid params - returns signed JWT with correct claims")
    void generateRedisJwtTokenKeyForFile_allValidParams_returnsJwtWithCorrectClaims() throws JsonProcessingException {
        Flux<FilePart> files = mock(Flux.class);
        Map<String, String> requestBody = Map.of("key", "value");
        when(objectMapper.writeValueAsString(requestBody)).thenReturn(SERIALIZED_BODY);
        String expectedFilesString = String.valueOf(files);

        String token = jwtTokenGeneratorUtil.generateRedisJwtTokenKeyForFile(files, requestBody, TEST_URL, TEST_OPERATION);

        assertNotNull(token);
        assertFalse(token.isEmpty());

        DecodedJWT decoded = JWT.require(Algorithm.HMAC256(TEST_SECRET)).build().verify(token);
        assertEquals(expectedFilesString, decoded.getClaim("files").asString());
        assertEquals(SERIALIZED_BODY, decoded.getClaim("requestBody").asString());
        assertEquals(TEST_URL, decoded.getClaim("url").asString());
        assertEquals(TEST_OPERATION, decoded.getClaim("operationType").asString());
    }

    @Test
    @DisplayName("generateRedisJwtTokenKeyForFile - null files - returns JWT with empty files claim")
    void generateRedisJwtTokenKeyForFile_nullFiles_returnsJwtWithEmptyFilesClaim() {
        String token = jwtTokenGeneratorUtil.generateRedisJwtTokenKeyForFile(null, null, TEST_URL, TEST_OPERATION);

        assertNotNull(token);
        assertFalse(token.isEmpty());

        DecodedJWT decoded = JWT.require(Algorithm.HMAC256(TEST_SECRET)).build().verify(token);
        assertEquals("", decoded.getClaim("files").asString());
        assertEquals("", decoded.getClaim("requestBody").asString());
        assertEquals(TEST_URL, decoded.getClaim("url").asString());
        assertEquals(TEST_OPERATION, decoded.getClaim("operationType").asString());

        verifyNoInteractions(objectMapper);
    }

    @Test
    @DisplayName("generateRedisJwtTokenKeyForFile - null requestBody - returns JWT with empty requestBody claim")
    void generateRedisJwtTokenKeyForFile_nullRequestBody_returnsJwtWithEmptyRequestBodyClaim() {
        Flux<FilePart> files = mock(Flux.class);
        String expectedFilesString = String.valueOf(files);

        String token = jwtTokenGeneratorUtil.generateRedisJwtTokenKeyForFile(files, null, TEST_URL, TEST_OPERATION);

        assertNotNull(token);
        assertFalse(token.isEmpty());

        DecodedJWT decoded = JWT.require(Algorithm.HMAC256(TEST_SECRET)).build().verify(token);
        assertEquals(expectedFilesString, decoded.getClaim("files").asString());
        assertEquals("", decoded.getClaim("requestBody").asString());

        verifyNoInteractions(objectMapper);
    }

    @Test
    @DisplayName("generateRedisJwtTokenKeyForFile - blank url - returns empty string")
    void generateRedisJwtTokenKeyForFile_blankUrl_returnsEmptyString() {
        String token = jwtTokenGeneratorUtil.generateRedisJwtTokenKeyForFile(null, null, "", TEST_OPERATION);

        assertNotNull(token);
        assertEquals("", token);
    }

    @Test
    @DisplayName("generateRedisJwtTokenKeyForFile - null url - returns empty string")
    void generateRedisJwtTokenKeyForFile_nullUrl_returnsEmptyString() {
        String token = jwtTokenGeneratorUtil.generateRedisJwtTokenKeyForFile(null, null, null, TEST_OPERATION);

        assertNotNull(token);
        assertEquals("", token);
    }

    @Test
    @DisplayName("generateRedisJwtTokenKeyForFile - blank operationType - returns empty string")
    void generateRedisJwtTokenKeyForFile_blankOperationType_returnsEmptyString() {
        String token = jwtTokenGeneratorUtil.generateRedisJwtTokenKeyForFile(null, null, TEST_URL, "");

        assertNotNull(token);
        assertEquals("", token);
    }

    @Test
    @DisplayName("generateRedisJwtTokenKeyForFile - null operationType - returns empty string")
    void generateRedisJwtTokenKeyForFile_nullOperationType_returnsEmptyString() {
        String token = jwtTokenGeneratorUtil.generateRedisJwtTokenKeyForFile(null, null, TEST_URL, null);

        assertNotNull(token);
        assertEquals("", token);
    }

    @Test
    @DisplayName("generateRedisJwtTokenKeyForFile - ObjectMapper throws JsonProcessingException - returns JWT with empty requestBody claim")
    void generateRedisJwtTokenKeyForFile_objectMapperThrows_returnsJwtWithEmptyRequestBodyClaim() throws JsonProcessingException {
        Map<String, String> requestBody = new HashMap<>();
        when(objectMapper.writeValueAsString(any()))
                .thenThrow(new JsonProcessingException("serialization error") {
                });

        String token = jwtTokenGeneratorUtil.generateRedisJwtTokenKeyForFile(null, requestBody, TEST_URL, TEST_OPERATION);

        assertNotNull(token);
        assertFalse(token.isEmpty());

        DecodedJWT decoded = JWT.require(Algorithm.HMAC256(TEST_SECRET)).build().verify(token);
        assertEquals("", decoded.getClaim("requestBody").asString());
        assertEquals(TEST_URL, decoded.getClaim("url").asString());
        assertEquals(TEST_OPERATION, decoded.getClaim("operationType").asString());
    }
}

