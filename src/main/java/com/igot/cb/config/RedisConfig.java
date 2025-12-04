package com.igot.cb.config;

import com.igot.cb.model.ResponseDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Value("${spring.redis.database}")
    private int redisDatabase;

    @Bean
    @Primary
    public ReactiveRedisConnectionFactory reactiveRedisConnectionFactory(
            @Value("${spring.redis.host}") String host,
            @Value("${spring.redis.port}") int port) {

        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(host);
        config.setPort(port);
        config.setDatabase(redisDatabase);
        return new LettuceConnectionFactory(config);
    }

    @Bean
    public ReactiveRedisTemplate<String, ResponseDTO> redisOperations(ReactiveRedisConnectionFactory factory) {
        Jackson2JsonRedisSerializer<ResponseDTO> serializer = new Jackson2JsonRedisSerializer<>(ResponseDTO.class);

        RedisSerializationContext.RedisSerializationContextBuilder<String, ResponseDTO> builder =
                RedisSerializationContext.newSerializationContext(new StringRedisSerializer());

        RedisSerializationContext<String, ResponseDTO> context = builder.value(serializer).build();

        return new ReactiveRedisTemplate<>(factory, context);
    }
}
