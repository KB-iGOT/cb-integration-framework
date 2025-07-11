package com.igot.cb.config;

import com.igot.cb.model.ResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RedisConfigTest {

    private RedisConfig redisConfig;

    @BeforeEach
    void setUp() {
        redisConfig = new RedisConfig();
        redisConfig.getClass(); // avoid warnings
        // set redisHost and redisPort manually since @Value won't populate them here
        redisConfig = new RedisConfig();
        setField(redisConfig, "redisHost", "localhost");
        setField(redisConfig, "redisPort", 6379);
    }

    @Test
    void testReactiveRedisConnectionFactory() {
        var factory = redisConfig.reactiveRedisConnectionFactory();

        assertNotNull(factory);
        assertEquals("localhost", ((org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory) factory).getHostName());
        assertEquals(6379, ((org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory) factory).getPort());
    }

    @Test
    void testRedisOperations() {
        ReactiveRedisConnectionFactory mockFactory = mock(ReactiveRedisConnectionFactory.class);

        ReactiveRedisTemplate<String, ResponseDTO> template = redisConfig.redisOperations(mockFactory);

        assertNotNull(template);
    }

    // helper method for setting private fields
    private void setField(Object target, String fieldName, Object value) {
        try {
            var field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
