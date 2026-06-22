package com.igot.cb.config;

import com.igot.cb.model.ResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RedisConfig Tests")
class RedisConfigTest {

    private RedisConfig redisConfig;

    @Mock
    private ReactiveRedisConnectionFactory mockFactory;

    private static final String TEST_HOST = "localhost";
    private static final int TEST_PORT = 6379;
    private static final int TEST_DATABASE = 1;

    @BeforeEach
    void setUp() {
        redisConfig = new RedisConfig();
        ReflectionTestUtils.setField(redisConfig, "redisHost", TEST_HOST);
        ReflectionTestUtils.setField(redisConfig, "redisPort", TEST_PORT);
        ReflectionTestUtils.setField(redisConfig, "redisDatabase", TEST_DATABASE);
    }

    @Test
    @DisplayName("reactiveRedisConnectionFactory - returns a non-null LettuceConnectionFactory instance")
    void reactiveRedisConnectionFactory_returnsLettuceConnectionFactory() {
        ReactiveRedisConnectionFactory factory = redisConfig.reactiveRedisConnectionFactory();

        assertNotNull(factory);
        assertInstanceOf(LettuceConnectionFactory.class, factory);
    }

    @Test
    @DisplayName("reactiveRedisConnectionFactory - sets correct host name from @Value property")
    void reactiveRedisConnectionFactory_setsCorrectHostName() {
        LettuceConnectionFactory factory =
                (LettuceConnectionFactory) redisConfig.reactiveRedisConnectionFactory();

        assertEquals(TEST_HOST, factory.getHostName());
    }

    @Test
    @DisplayName("reactiveRedisConnectionFactory - sets correct port from @Value property")
    void reactiveRedisConnectionFactory_setsCorrectPort() {
        LettuceConnectionFactory factory =
                (LettuceConnectionFactory) redisConfig.reactiveRedisConnectionFactory();

        assertEquals(TEST_PORT, factory.getPort());
    }

    @Test
    @DisplayName("reactiveRedisConnectionFactory - sets correct database index from @Value property")
    void reactiveRedisConnectionFactory_setsCorrectDatabaseIndex() {
        LettuceConnectionFactory factory =
                (LettuceConnectionFactory) redisConfig.reactiveRedisConnectionFactory();

        assertEquals(TEST_DATABASE, factory.getDatabase());
    }

    @Test
    @DisplayName("reactiveRedisConnectionFactory - custom database index is applied correctly")
    void reactiveRedisConnectionFactory_customDatabaseIndex_isApplied() {
        ReflectionTestUtils.setField(redisConfig, "redisDatabase", 3);

        LettuceConnectionFactory factory =
                (LettuceConnectionFactory) redisConfig.reactiveRedisConnectionFactory();

        assertEquals(3, factory.getDatabase());
    }

    @Test
    @DisplayName("reactiveRedisConnectionFactory - each call returns a new independent factory instance")
    void reactiveRedisConnectionFactory_eachCallReturnsNewInstance() {
        ReactiveRedisConnectionFactory first = redisConfig.reactiveRedisConnectionFactory();
        ReactiveRedisConnectionFactory second = redisConfig.reactiveRedisConnectionFactory();

        assertNotSame(first, second);
    }

    @Test
    @DisplayName("redisOperations - returns a non-null ReactiveRedisTemplate")
    void redisOperations_returnsNonNullReactiveRedisTemplate() {
        ReactiveRedisTemplate<String, ResponseDTO> template =
                redisConfig.redisOperations(mockFactory);

        assertNotNull(template);
    }

    @Test
    @DisplayName("redisOperations - returns a ReactiveRedisTemplate instance")
    void redisOperations_returnsReactiveRedisTemplateInstance() {
        ReactiveRedisTemplate<String, ResponseDTO> template =
                redisConfig.redisOperations(mockFactory);

        assertInstanceOf(ReactiveRedisTemplate.class, template);
    }

    @Test
    @DisplayName("redisOperations - each call with same factory returns a new template instance")
    void redisOperations_eachCallReturnsNewInstance() {
        ReactiveRedisTemplate<String, ResponseDTO> first = redisConfig.redisOperations(mockFactory);
        ReactiveRedisTemplate<String, ResponseDTO> second = redisConfig.redisOperations(mockFactory);

        assertNotSame(first, second);
    }
}

