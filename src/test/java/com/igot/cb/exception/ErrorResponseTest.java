package com.igot.cb.exception;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ErrorResponseTest {

    @Test
    void testBuilderAndGetters() {
        Map<String, String> errorsMap = Collections.singletonMap("field", "must not be null");

        ErrorResponse response = ErrorResponse.builder()
                .code("ERR001")
                .message("Something went wrong")
                .errors(errorsMap)
                .httpStatusCode("400")
                .build();

        assertEquals("ERR001", response.getCode());
        assertEquals("Something went wrong", response.getMessage());
        assertEquals(errorsMap, response.getErrors());
        assertEquals("400", response.getHttpStatusCode());
    }

    @Test
    void testEqualsAndHashCode() {
        ErrorResponse r1 = ErrorResponse.builder()
                .code("ERR001")
                .message("Error")
                .build();

        ErrorResponse r2 = ErrorResponse.builder()
                .code("ERR001")
                .message("Error")
                .build();

        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());
    }

    @Test
    void testToString() {
        ErrorResponse response = ErrorResponse.builder()
                .code("ERR001")
                .message("Error")
                .build();

        String toString = response.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("ERR001"));
        assertTrue(toString.contains("Error"));
    }
}
