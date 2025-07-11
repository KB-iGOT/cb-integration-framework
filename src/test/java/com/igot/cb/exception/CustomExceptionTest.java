package com.igot.cb.exception;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CustomExceptionTest {

    @Test
    void testDefaultConstructor() {
        CustomException ex = new CustomException();
        assertNull(ex.getCode());
        assertNull(ex.getMessage());
        assertNull(ex.getHttpStatusCode());
        assertNull(ex.getErrors());

        ex.setCode("CODE");
        ex.setMessage("Message");
        ex.setHttpStatusCode("500");
        ex.setErrors(Collections.singletonMap("key", "value"));

        assertEquals("CODE", ex.getCode());
        assertEquals("Message", ex.getMessage());
        assertEquals("500", ex.getHttpStatusCode());
        assertEquals("value", ex.getErrors().get("key"));
    }

    @Test
    void testConstructorWithCodeAndMessage() {
        CustomException ex = new CustomException("ERR_CODE", "Error message");
        assertEquals("ERR_CODE", ex.getCode());
        assertEquals("Error message", ex.getMessage());
        assertNull(ex.getHttpStatusCode());
        assertNull(ex.getErrors());
    }

    @Test
    void testConstructorWithCodeMessageAndHttpStatusCode() {
        CustomException ex = new CustomException("ERR_CODE", "Error message", "400");
        assertEquals("ERR_CODE", ex.getCode());
        assertEquals("Error message", ex.getMessage());
        assertEquals("400", ex.getHttpStatusCode());
        assertNull(ex.getErrors());
    }

    @Test
    void testConstructorWithErrorsMap() {
        Map<String, String> errors = Collections.singletonMap("field", "must not be null");
        CustomException ex = new CustomException(errors);

        assertEquals(errors, ex.getErrors());
        assertEquals(errors.toString(), ex.getMessage());
        assertNull(ex.getCode());
        assertNull(ex.getHttpStatusCode());
    }
}

