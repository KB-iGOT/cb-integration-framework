package com.igot.cb.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

class RestExceptionHandlerTest {

    private RestExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new RestExceptionHandler();
    }

    @Test
    void testHandleCustomException_withHttpStatusCodeAndMessage() {
        CustomException ex = new CustomException("ERR_CODE", "Error occurred", "500");
        ResponseEntity<?> response = handler.handleException(ex);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody() instanceof ErrorResponse);

        ErrorResponse error = (ErrorResponse) response.getBody();
        assertEquals("ERR_CODE", error.getCode());
        assertEquals("Error occurred", error.getMessage());
        assertEquals("500", error.getHttpStatusCode());
    }

    @Test
    void testHandleCustomException_withoutHttpStatusCode() {
        CustomException ex = new CustomException("ERR_CODE", "Error occurred");
        ResponseEntity<?> response = handler.handleException(ex);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody() instanceof ErrorResponse);

        ErrorResponse error = (ErrorResponse) response.getBody();
        assertEquals("ERR_CODE", error.getCode());
        assertEquals("Error occurred", error.getMessage());
        assertEquals(String.valueOf(HttpStatus.BAD_REQUEST.value()), error.getHttpStatusCode());
    }

    @Test
    void testHandleCustomException_withBlankMessage() {
        CustomException ex = new CustomException("ERR_CODE", "");
        ResponseEntity<?> response = handler.handleException(ex);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody() instanceof ErrorResponse);

        ErrorResponse error = (ErrorResponse) response.getBody();
        assertEquals("ERR_CODE", error.getCode());
        assertEquals("", error.getMessage());
    }

    @Test
    void testHandleGenericException() {
        Exception ex = new Exception("Some error");
        ResponseEntity<?> response = handler.handleException(ex);

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody() instanceof ErrorResponse);

        ErrorResponse error = (ErrorResponse) response.getBody();
        assertEquals("Some error", error.getCode());
        assertNull(error.getMessage());
        assertNull(error.getHttpStatusCode());
    }
}

