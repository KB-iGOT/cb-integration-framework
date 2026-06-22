package com.igot.cb.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ResponseDTO Tests")
class ResponseDTOTest {

    @Test
    @DisplayName("noArgsConstructor - creates instance with all fields null")
    void noArgsConstructor_createsInstanceWithAllFieldsNull() {
        ResponseDTO dto = new ResponseDTO();

        assertNotNull(dto);
        assertNull(dto.getResponseData());
        assertNull(dto.getId());
    }

    @Test
    @DisplayName("allArgsConstructor - sets all fields correctly")
    void allArgsConstructor_setsAllFieldsCorrectly() {
        Object data = "some response data";
        ResponseDTO dto = new ResponseDTO(data, "id-001");

        assertEquals(data, dto.getResponseData());
        assertEquals("id-001", dto.getId());
    }

    @Test
    @DisplayName("allArgsConstructor - with null values - stores nulls")
    void allArgsConstructor_withNullValues_storesNulls() {
        ResponseDTO dto = new ResponseDTO(null, null);

        assertNull(dto.getResponseData());
        assertNull(dto.getId());
    }

    @Test
    @DisplayName("allArgsConstructor - with complex object as responseData - stores correctly")
    void allArgsConstructor_withComplexResponseData_storesCorrectly() {
        Object complexData = new java.util.HashMap<String, Object>() {{
            put("key1", "value1");
            put("key2", 42);
        }};

        ResponseDTO dto = new ResponseDTO(complexData, "id-complex");

        assertEquals(complexData, dto.getResponseData());
        assertEquals("id-complex", dto.getId());
    }

    @Test
    @DisplayName("builder - builds dto with all fields set")
    void builder_buildsDtoWithAllFieldsSet() {
        Object data = "built response";

        ResponseDTO dto = ResponseDTO.builder()
                .responseData(data)
                .id("id-builder-001")
                .build();

        assertEquals(data, dto.getResponseData());
        assertEquals("id-builder-001", dto.getId());
    }

    @Test
    @DisplayName("builder - builds dto with no fields set - all fields null")
    void builder_buildsDtoWithNoFieldsSet_allFieldsNull() {
        ResponseDTO dto = ResponseDTO.builder().build();

        assertNotNull(dto);
        assertNull(dto.getResponseData());
        assertNull(dto.getId());
    }

    @Test
    @DisplayName("builder - builds dto with only responseData set")
    void builder_buildsDtoWithOnlyResponseData() {
        ResponseDTO dto = ResponseDTO.builder()
                .responseData("only data")
                .build();

        assertEquals("only data", dto.getResponseData());
        assertNull(dto.getId());
    }

    @Test
    @DisplayName("builder - builds dto with only id set")
    void builder_buildsDtoWithOnlyId() {
        ResponseDTO dto = ResponseDTO.builder()
                .id("only-id")
                .build();

        assertNull(dto.getResponseData());
        assertEquals("only-id", dto.getId());
    }

    @Test
    @DisplayName("setResponseData and getResponseData - round-trip string value")
    void setAndGetResponseData_roundTrip() {
        ResponseDTO dto = new ResponseDTO();
        dto.setResponseData("test data");

        assertEquals("test data", dto.getResponseData());
    }

    @Test
    @DisplayName("setId and getId - round-trip string value")
    void setAndGetId_roundTrip() {
        ResponseDTO dto = new ResponseDTO();
        dto.setId("test-id-123");

        assertEquals("test-id-123", dto.getId());
    }

    @Test
    @DisplayName("setters - overwrite initial values")
    void setters_overwriteInitialValues() {
        ResponseDTO dto = new ResponseDTO("initial data", "initial-id");

        dto.setResponseData("updated data");
        dto.setId("updated-id");

        assertEquals("updated data", dto.getResponseData());
        assertEquals("updated-id", dto.getId());
    }

    @Test
    @DisplayName("setResponseData - accepts null - clears value")
    void setResponseData_withNull_clearsValue() {
        ResponseDTO dto = new ResponseDTO("some data", "id-1");
        dto.setResponseData(null);

        assertNull(dto.getResponseData());
    }

    @Test
    @DisplayName("setId - accepts null - clears value")
    void setId_withNull_clearsValue() {
        ResponseDTO dto = new ResponseDTO("data", "some-id");
        dto.setId(null);

        assertNull(dto.getId());
    }

    @Test
    @DisplayName("setResponseData - accepts non-string object")
    void setResponseData_withNonStringObject_storesCorrectly() {
        ResponseDTO dto = new ResponseDTO();
        Integer numericData = 12345;
        dto.setResponseData(numericData);

        assertEquals(numericData, dto.getResponseData());
    }
}

