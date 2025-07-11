package com.igot.cb.cache;

import com.igot.cb.model.ResponseDTO;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisOperations;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

class ApiCallDataCacheTest {

    @Test
    void testConstructor() {
        ReactiveRedisConnectionFactory factory = mock(ReactiveRedisConnectionFactory.class);
        ReactiveRedisOperations<String, ResponseDTO> cacheOps = mock(ReactiveRedisOperations.class);

        ApiCallDataCache apiCallDataCache = new ApiCallDataCache(factory, cacheOps);

        assertNotNull(apiCallDataCache);
    }
}

