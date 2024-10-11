package com.igot.cb.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.igot.cb.exception.CustomException;
import com.igot.cb.model.ExternalApiIntegrationDTO;
import com.igot.cb.model.ResponseDTO;
import com.igot.cb.service.APICallService;
import com.igot.cb.util.JWTTokenGeneratorUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;

@Service
@Slf4j
public class APICallServiceImpl implements APICallService {
    private final ReactiveRedisOperations<String, ResponseDTO> cacheOps;

    public APICallServiceImpl(ReactiveRedisOperations<String, ResponseDTO> cacheOps) {
        this.cacheOps = cacheOps;
    }

    @Autowired
    private JWTTokenGeneratorUtil tokenGeneratorUtil;
    @Autowired
    private ObjectMapper objectMapper;
    @Value("${cache.data.ttl}")
    public Long cacheDataTtl;
    @Value("${max.response.memory.size}")
    public int maxResponseMemorySize;

    @Override
    public Mono<ResponseDTO> makeExternalApiCall(ExternalApiIntegrationDTO externalApiIntegrationDTO) {
        log.info("APICallServiceImpl::makeExternalApiCall");
        WebClient client = WebClient.builder()
                .exchangeStrategies(ExchangeStrategies.builder()
                        .codecs(configurer -> configurer.defaultCodecs()
                                .maxInMemorySize(maxResponseMemorySize))
                        .build())
                .build();

        HttpMethod httpMethod = HttpMethod.valueOf(externalApiIntegrationDTO.getRequestMethod().toString());
        String url = externalApiIntegrationDTO.getUrl();
        MultiValueMap<String, String> headers = convertToMultiValueMap(externalApiIntegrationDTO.getRequestHeader());
        Object requestBody = externalApiIntegrationDTO.getRequestBody();

        Mono<ResponseDTO> responseMono;
        if (httpMethod == HttpMethod.GET || httpMethod == HttpMethod.DELETE) {
            responseMono = client.method(httpMethod)
                    .uri(url)
                    .headers(httpHeaders -> httpHeaders.addAll(headers))
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .map(jsonNode -> {
                        ResponseDTO responseDTO = new ResponseDTO();
                        responseDTO.setResponseData(jsonNode);
                        return responseDTO;
                    });
        } else {
            if(externalApiIntegrationDTO.isFormData()){
                Map<String, Object> requestBodyMap = (Map<String, Object>) requestBody;
                MultiValueMap<String, String> formData = convertToStringMap(requestBodyMap);
                responseMono = client.method(httpMethod)
                        .uri(url)
                        .headers(httpHeaders -> httpHeaders.addAll(headers))
                        .body(BodyInserters.fromFormData(formData))
                        .retrieve()
                        .bodyToMono(String.class)
                        .map(responseBody -> {
                            ResponseDTO responseDTO = new ResponseDTO();
                            JsonNode jsonResponse;
                            try {
                                jsonResponse = objectMapper.readTree(responseBody);
                                responseDTO.setResponseData(jsonResponse);
                            } catch (JsonProcessingException e) {
                                responseDTO.setResponseData(responseBody);
                            }

                            return responseDTO;
                        });
            }else{
                responseMono = client.method(httpMethod)
                        .uri(url)
                        .headers(httpHeaders -> httpHeaders.addAll(headers))
                        .bodyValue(requestBody)
                        .retrieve()
                        .bodyToMono(String.class)
                        .map(responseBody -> {
                            ResponseDTO responseDTO = new ResponseDTO();
                            JsonNode jsonResponse;
                            try {
                                jsonResponse = objectMapper.readTree(responseBody);
                                responseDTO.setResponseData(jsonResponse);
                            } catch (JsonProcessingException e) {
                                responseDTO.setResponseData(responseBody);
                            }

                            return responseDTO;
                        });
            }

        }
        return responseMono
                .doOnSuccess(responseDTO -> {
                    // Process the response body asynchronously
                    String token = tokenGeneratorUtil.generateRedisJwtTokenKey(externalApiIntegrationDTO.getRequestBody()
                            , externalApiIntegrationDTO.getUrl()
                            , externalApiIntegrationDTO.getOperationType().name());
                    log.info("token: " + token);
                    saveToRedis(externalApiIntegrationDTO, token, responseDTO);
                    log.info("successfully got response: " + responseDTO);
                })
                .doOnError(error -> {
                    // Handle any error that occurred during the request
                    log.error("error occurred while calling external API: " + url);

                    String httpStatusCode = null;
                    String updatedError = error.toString();
                    if (error instanceof WebClientResponseException) {
                        httpStatusCode = String.valueOf(((WebClientResponseException) error).getRawStatusCode());
                        updatedError = ((WebClientResponseException) error).getResponseBodyAsString();
                    }
                    throw new CustomException("EXTERNAL_SERVICE_CALL_ERROR", updatedError, httpStatusCode);
                });
    }

    private MultiValueMap<String, String> convertToMultiValueMap(Map<String, String> requestHeader) {
        MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<>();
        multiValueMap.setAll(requestHeader);
        return multiValueMap;
    }
    public MultiValueMap<String, String> convertToStringMap(Map<String, Object> inputMap) {
        MultiValueMap<String, String> stringMap = new LinkedMultiValueMap<>();
        for (Map.Entry<String, Object> entry : inputMap.entrySet()) {
            stringMap.put(entry.getKey(), Collections.singletonList(entry.getValue().toString()));
        }
        return stringMap;
    }

    public Mono<Boolean> saveToRedis(ExternalApiIntegrationDTO externalApiIntegrationDTO, String key, ResponseDTO responseDTO) {
        log.info("IntegrationServiceImpl::saveToRedis");
        Mono<Boolean> monoCacheData;
        if (externalApiIntegrationDTO.getStrictCacheTimeInMinutes() > 0 && externalApiIntegrationDTO.getStrictCacheTimeInMinutes() != -1) {
            log.info("cacheData value from user input {}",externalApiIntegrationDTO.getStrictCacheTimeInMinutes());
            monoCacheData = cacheOps.opsForValue().set(key, responseDTO, Duration.ofMinutes(externalApiIntegrationDTO.getStrictCacheTimeInMinutes()));
        } else {
            log.info("cacheData value from properties in ms {}",cacheDataTtl);
            monoCacheData = cacheOps.opsForValue().set(key, responseDTO, Duration.ofMillis(cacheDataTtl));
        }
        monoCacheData.subscribe();
        return monoCacheData.doOnSuccess(
                ex -> {
                    responseDTO.getResponseData();
                    log.info("Data cached successfully! {}",responseDTO.getResponseData());
                }
        ).doOnError(ext -> {
            log.error("data didn't cache, Error occurred {}!", ext.toString());
        });
    }


}
