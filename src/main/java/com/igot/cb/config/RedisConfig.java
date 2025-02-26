package com.igot.cb.config;

import com.igot.cb.model.ResponseDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Value("${spring.redis.host}")
    private String redisHost;

    @Value("${spring.redis.port}")
    private int redisPort;

    @Bean
    public ReactiveRedisConnectionFactory reactiveRedisConnectionFactory() {
        return new LettuceConnectionFactory(redisHost, redisPort);
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
