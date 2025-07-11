package com.igot.cb.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ResponseDTOTest {

    @Test
    void testNoArgsConstructorAndSettersGetters() {
        ResponseDTO dto = new ResponseDTO();

        dto.setId("123");
        dto.setResponseData("data");

        assertEquals("123", dto.getId());
        assertEquals("data", dto.getResponseData());
    }

    @Test
    void testAllArgsConstructor() {
        ResponseDTO dto = new ResponseDTO("data", "123");

        assertEquals("123", dto.getId());
        assertEquals("data", dto.getResponseData());
    }

    @Test
    void testBuilder() {
        ResponseDTO dto = ResponseDTO.builder()
                .id("123")
                .responseData("data")
                .build();

        assertEquals("123", dto.getId());
        assertEquals("data", dto.getResponseData());
    }

    @Test
    void testJsonSerialization() throws JsonProcessingException {
        ResponseDTO dto = ResponseDTO.builder()
                .id("123")
                .responseData("data")
                .build();

        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(dto);

        assertTrue(json.contains("\"id\":\"123\""));
        assertTrue(json.contains("\"responseData\":\"data\""));
    }
}

