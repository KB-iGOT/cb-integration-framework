package com.igot.cb.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JWTTokenGeneratorUtilTest {

    private JWTTokenGeneratorUtil tokenUtil;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() throws Exception {
        objectMapper = mock(ObjectMapper.class);
        tokenUtil = new JWTTokenGeneratorUtil();

        // Use reflection to inject private fields
        var omField = JWTTokenGeneratorUtil.class.getDeclaredField("objectMapper");
        omField.setAccessible(true);
        omField.set(tokenUtil, objectMapper);

        var secretField = JWTTokenGeneratorUtil.class.getDeclaredField("jwtSecretKey");
        secretField.setAccessible(true);
        secretField.set(tokenUtil, "mysecretkey123");
    }

    @Test
    void testGenerateToken_success() throws JsonProcessingException {
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"key\":\"val\"}");

        String token = tokenUtil.generateRedisJwtTokenKey(Map.of("key", "val"), "http://test.com", "READ");
        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    @Test
    void testGenerateToken_nullRequestBody() {
        String token = tokenUtil.generateRedisJwtTokenKey(null, "http://test.com", "READ");
        assertNotNull(token);
    }

    @Test
    void testGenerateToken_blankUrlOrOpType() throws JsonProcessingException {
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"key\":\"val\"}");

        String token1 = tokenUtil.generateRedisJwtTokenKey(Map.of("key", "val"), "", "READ");
        String token2 = tokenUtil.generateRedisJwtTokenKey(Map.of("key", "val"), "http://test.com", "");

        assertEquals("", token1);
        assertEquals("", token2);
    }

    @Test
    void testGenerateToken_objectMapperFails() throws JsonProcessingException {
        when(objectMapper.writeValueAsString(any())).thenThrow(new JsonProcessingException("fail") {});

        String token = tokenUtil.generateRedisJwtTokenKey(Map.of("key", "val"), "http://test.com", "READ");
        assertNotNull(token); // fallback to empty body, still builds token
    }

    @Test
    void testGenerateTokenWithFile_fluxNull() throws JsonProcessingException {
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"key\":\"val\"}");

        String token = tokenUtil.generateRedisJwtTokenKeyForFile(null, Map.of("key", "val"), "http://test.com", "READ");
        assertNotNull(token);
    }

    @Test
    void testGenerateTokenWithFile_fluxNotNull() throws JsonProcessingException {
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"key\":\"val\"}");
        Flux<FilePart> files = Flux.fromIterable(List.of(mock(FilePart.class)));

        String token = tokenUtil.generateRedisJwtTokenKeyForFile(files, Map.of("key", "val"), "http://test.com", "READ");
        assertNotNull(token);
        assertTrue(token.contains("."));
    }

    @Test
    void testGenerateTokenWithFile_blankUrl() throws JsonProcessingException {
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"key\":\"val\"}");
        Flux<FilePart> files = Flux.fromIterable(List.of(mock(FilePart.class)));

        String token = tokenUtil.generateRedisJwtTokenKeyForFile(files, Map.of("key", "val"), "", "READ");
        assertEquals("", token);
    }
}
