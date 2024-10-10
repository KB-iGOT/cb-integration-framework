package com.igot.cb.service;

import com.igot.cb.model.ExternalApiIntegrationDTO;
import reactor.core.publisher.Mono;

public interface APICallService {

    Mono makeExternalApiCall(ExternalApiIntegrationDTO externalApiIntegrationDTO);
}
