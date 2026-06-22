package com.igot.cb.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CustomException Tests")
class CustomExceptionTest {

    @Test
    @DisplayName("default constructor - creates instance with all fields null")
    void defaultConstructor_createsInstanceWithAllFieldsNull() {
        CustomException ex = new CustomException();

        assertNotNull(ex);
        assertNull(ex.getCode());
        assertNull(ex.getMessage());
        assertNull(ex.getHttpStatusCode());
        assertNull(ex.getErrors());
    }

    @Test
    @DisplayName("constructor(code, message) - sets code and message correctly")
    void codeMessageConstructor_setsCodeAndMessage() {
        CustomException ex = new CustomException("ERR_001", "Something went wrong");

        assertEquals("ERR_001", ex.getCode());
        assertEquals("Something went wrong", ex.getMessage());
        assertNull(ex.getHttpStatusCode());
        assertNull(ex.getErrors());
    }

    @Test
    @DisplayName("constructor(code, message) - with null values - stores nulls")
    void codeMessageConstructor_withNullValues_storesNulls() {
        CustomException ex = new CustomException(null, null);

        assertNull(ex.getCode());
        assertNull(ex.getMessage());
    }

    @Test
    @DisplayName("constructor(code, message, httpStatusCode) - sets all three fields")
    void codeMessageHttpStatusConstructor_setsAllThreeFields() {
        CustomException ex = new CustomException("ERR_002", "Not found", "404");

        assertEquals("ERR_002", ex.getCode());
        assertEquals("Not found", ex.getMessage());
        assertEquals("404", ex.getHttpStatusCode());
        assertNull(ex.getErrors());
    }

    @Test
    @DisplayName("constructor(code, message, httpStatusCode) - with null httpStatusCode - stores null")
    void codeMessageHttpStatusConstructor_withNullHttpStatus_storesNull() {
        CustomException ex = new CustomException("ERR_003", "Error", null);

        assertEquals("ERR_003", ex.getCode());
        assertEquals("Error", ex.getMessage());
        assertNull(ex.getHttpStatusCode());
    }

    @Test
    @DisplayName("constructor(Map<errors>) - sets message from map toString and stores errors map")
    void errorsMapConstructor_setsMessageFromMapToStringAndStoresErrors() {
        Map<String, String> errors = new HashMap<>();
        errors.put("field1", "must not be blank");
        errors.put("field2", "must be positive");

        CustomException ex = new CustomException(errors);

        assertEquals(errors.toString(), ex.getMessage());
        assertEquals(errors, ex.getErrors());
        assertNull(ex.getCode());
        assertNull(ex.getHttpStatusCode());
    }

    @Test
    @DisplayName("constructor(Map<errors>) - with empty map - sets empty map string as message")
    void errorsMapConstructor_withEmptyMap_setsEmptyMapStringAsMessage() {
        Map<String, String> errors = new HashMap<>();

        CustomException ex = new CustomException(errors);

        assertEquals("{}", ex.getMessage());
        assertTrue(ex.getErrors().isEmpty());
    }

    @Test
    @DisplayName("setCode - updates the code field")
    void setCode_updatesCodeField() {
        CustomException ex = new CustomException();
        ex.setCode("NEW_CODE");

        assertEquals("NEW_CODE", ex.getCode());
    }

    @Test
    @DisplayName("setMessage - updates the message field")
    void setMessage_updatesMessageField() {
        CustomException ex = new CustomException();
        ex.setMessage("updated message");

        assertEquals("updated message", ex.getMessage());
    }

    @Test
    @DisplayName("setHttpStatusCode - updates the httpStatusCode field")
    void setHttpStatusCode_updatesHttpStatusCodeField() {
        CustomException ex = new CustomException();
        ex.setHttpStatusCode("500");

        assertEquals("500", ex.getHttpStatusCode());
    }

    @Test
    @DisplayName("setErrors - updates the errors field")
    void setErrors_updatesErrorsField() {
        CustomException ex = new CustomException();
        Map<String, String> errors = Map.of("email", "invalid format");
        ex.setErrors(errors);

        assertEquals(errors, ex.getErrors());
    }

    @Test
    @DisplayName("CustomException - is a RuntimeException")
    void customException_isRuntimeException() {
        CustomException ex = new CustomException("CODE", "message");

        assertInstanceOf(RuntimeException.class, ex);
    }

    @Test
    @DisplayName("CustomException - can be thrown and caught as RuntimeException")
    void customException_canBeThrownAndCaughtAsRuntimeException() {
        assertThrows(RuntimeException.class, () -> {
            throw new CustomException("THROWN", "thrown message");
        });
    }
}

