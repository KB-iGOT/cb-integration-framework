package com.igot.cb.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("RestExceptionHandler Tests")
class RestExceptionHandlerTest {

    private RestExceptionHandler restExceptionHandler;

    @BeforeEach
    void setUp() {
        restExceptionHandler = new RestExceptionHandler();
    }

    @Test
    @DisplayName("handleException - CustomException with non-null httpStatusCode and non-blank message "
            + "- returns BAD_REQUEST with custom httpStatusCode")
    void handleException_customException_nonNullHttpStatus_nonBlankMessage_returnsBadRequestWithCustomStatus() {
        CustomException ex = new CustomException("ERR_CODE", "Something went wrong", "403");

        ResponseEntity response = restExceptionHandler.handleException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        ErrorResponse body = (ErrorResponse) response.getBody();
        assertNotNull(body);
        assertEquals("ERR_CODE", body.getCode());
        assertEquals("Something went wrong", body.getMessage());
        assertEquals("403", body.getHttpStatusCode()); // uses custom value
    }

    @Test
    @DisplayName("handleException - CustomException with null httpStatusCode and non-blank message "
            + "- returns BAD_REQUEST and falls back to status code '400'")
    void handleException_customException_nullHttpStatus_nonBlankMessage_returnsBadRequestWithFallbackStatus() {
        CustomException ex = new CustomException("ERR_CODE", "Something went wrong"); // httpStatusCode = null

        ResponseEntity response = restExceptionHandler.handleException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        ErrorResponse body = (ErrorResponse) response.getBody();
        assertNotNull(body);
        assertEquals("ERR_CODE", body.getCode());
        assertEquals("Something went wrong", body.getMessage());
        assertEquals("400", body.getHttpStatusCode()); // falls back to status.value()
    }

    @Test
    @DisplayName("handleException - CustomException with null httpStatusCode and null message "
            + "- skips log.error and falls back to status code '400'")
    void handleException_customException_nullHttpStatus_nullMessage_skipsLogAndReturnsBadRequest() {
        CustomException ex = new CustomException();
        ex.setCode("ERR_NULL_MSG");
        // message = null → StringUtils.isNotBlank(null) = false → log.error NOT called

        ResponseEntity response = restExceptionHandler.handleException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        ErrorResponse body = (ErrorResponse) response.getBody();
        assertNotNull(body);
        assertEquals("ERR_NULL_MSG", body.getCode());
        assertNull(body.getMessage());
        assertEquals("400", body.getHttpStatusCode());
    }

    @Test
    @DisplayName("handleException - CustomException with null httpStatusCode and blank message "
            + "- skips log.error and falls back to status code '400'")
    void handleException_customException_nullHttpStatus_blankMessage_skipsLogAndReturnsBadRequest() {
        CustomException ex = new CustomException();
        ex.setCode("ERR_BLANK_MSG");
        ex.setMessage("   "); // blank → StringUtils.isNotBlank("   ") = false → log.error NOT called

        ResponseEntity response = restExceptionHandler.handleException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        ErrorResponse body = (ErrorResponse) response.getBody();
        assertNotNull(body);
        assertEquals("ERR_BLANK_MSG", body.getCode());
        assertEquals("   ", body.getMessage());
        assertEquals("400", body.getHttpStatusCode());
    }

    @Test
    @DisplayName("handleException - CustomException with non-null httpStatusCode and null message "
            + "- returns BAD_REQUEST with custom httpStatusCode, skips log.error")
    void handleException_customException_nonNullHttpStatus_nullMessage_returnsBadRequestWithCustomStatus() {
        CustomException ex = new CustomException("ERR_CODE", null, "422");

        ResponseEntity response = restExceptionHandler.handleException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        ErrorResponse body = (ErrorResponse) response.getBody();
        assertNotNull(body);
        assertEquals("ERR_CODE", body.getCode());
        assertNull(body.getMessage());
        assertEquals("422", body.getHttpStatusCode());
    }

    @Test
    @DisplayName("handleException - generic RuntimeException - returns INTERNAL_SERVER_ERROR with exception message as code")
    void handleException_genericRuntimeException_returnsInternalServerError() {
        RuntimeException ex = new RuntimeException("Unexpected system failure");

        ResponseEntity response = restExceptionHandler.handleException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());

        ErrorResponse body = (ErrorResponse) response.getBody();
        assertNotNull(body);
        assertEquals("Unexpected system failure", body.getCode());
        assertNull(body.getMessage());
        assertNull(body.getHttpStatusCode());
    }

    @Test
    @DisplayName("handleException - generic Exception with null message - returns INTERNAL_SERVER_ERROR with null code")
    void handleException_genericException_nullMessage_returnsInternalServerError() {
        Exception ex = new Exception(); // getMessage() = null

        ResponseEntity response = restExceptionHandler.handleException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());

        ErrorResponse body = (ErrorResponse) response.getBody();
        assertNotNull(body);
        assertNull(body.getCode()); // ex.getMessage() is null
        assertNull(body.getMessage());
    }

    @Test
    @DisplayName("handleException - checked Exception (non-Custom) - returns INTERNAL_SERVER_ERROR")
    void handleException_checkedNonCustomException_returnsInternalServerError() {
        Exception ex = new IllegalArgumentException("bad argument");

        ResponseEntity response = restExceptionHandler.handleException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());

        ErrorResponse body = (ErrorResponse) response.getBody();
        assertNotNull(body);
        assertEquals("bad argument", body.getCode());
    }
}

