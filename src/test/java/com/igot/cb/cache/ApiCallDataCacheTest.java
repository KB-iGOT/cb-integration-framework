package com.igot.cb.cache;

import com.igot.cb.model.ResponseDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ApiCallDataCache Tests")
class ApiCallDataCacheTest {

    @Mock
    private ReactiveRedisConnectionFactory factory;

    @Mock
    private ReactiveRedisOperations<String, ResponseDTO> cacheOps;

    @Test
    @DisplayName("constructor - with valid factory and cacheOps - creates instance and assigns both fields")
    void constructor_withValidDependencies_createsInstanceAndAssignsFields() {
        ApiCallDataCache cache = new ApiCallDataCache(factory, cacheOps);

        assertNotNull(cache);
        assertSame(factory, ReflectionTestUtils.getField(cache, "factory"));
        assertSame(cacheOps, ReflectionTestUtils.getField(cache, "cacheOps"));
    }

    @Test
    @DisplayName("constructor - with null factory - creates instance with null factory field")
    void constructor_withNullFactory_createsInstanceWithNullFactory() {
        ApiCallDataCache cache = new ApiCallDataCache(null, cacheOps);

        assertNotNull(cache);
        assertNull(ReflectionTestUtils.getField(cache, "factory"));
        assertSame(cacheOps, ReflectionTestUtils.getField(cache, "cacheOps"));
    }

    @Test
    @DisplayName("constructor - with null cacheOps - creates instance with null cacheOps field")
    void constructor_withNullCacheOps_createsInstanceWithNullCacheOps() {
        ApiCallDataCache cache = new ApiCallDataCache(factory, null);

        assertNotNull(cache);
        assertSame(factory, ReflectionTestUtils.getField(cache, "factory"));
        assertNull(ReflectionTestUtils.getField(cache, "cacheOps"));
    }

    @Test
    @DisplayName("constructor - with both null dependencies - creates instance with both fields null")
    void constructor_withBothNullDependencies_createsInstanceWithBothFieldsNull() {
        ApiCallDataCache cache = new ApiCallDataCache(null, null);

        assertNotNull(cache);
        assertNull(ReflectionTestUtils.getField(cache, "factory"));
        assertNull(ReflectionTestUtils.getField(cache, "cacheOps"));
    }
}

