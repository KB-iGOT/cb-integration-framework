package com.tarento.integration.integrationframework.service;

import com.tarento.integration.integrationframework.model.ExternalApiIntegrationDTO;
import reactor.core.publisher.Mono;

public interface APICallService {

    Mono makeExternalApiCall(ExternalApiIntegrationDTO externalApiIntegrationDTO);
}
