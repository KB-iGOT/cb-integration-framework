package com.igot.cb.controller;


import com.igot.cb.model.ExternalApiIntegrationDTO;
import com.igot.cb.model.ResponseDTO;
import com.igot.cb.service.IntegrationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;


@RestController
@RequestMapping("/integration")
@Slf4j
public class IntegrationController {

    @Autowired
    private IntegrationService IntegrationService;

    @PostMapping("/v1/create-external-call")
    public Mono<ResponseDTO> createExternalAPICall(@RequestBody ExternalApiIntegrationDTO integrationDTO) {
        try {
            Mono<ResponseDTO> responseDTOMono = IntegrationService.createExternalAPICall(integrationDTO);

            return responseDTOMono;
        } catch (Exception e) {
            return Mono.error(e);
        }
    }

    @GetMapping("/v1/health")
    public String healthCheck() {
        return "Success";
    }

}
