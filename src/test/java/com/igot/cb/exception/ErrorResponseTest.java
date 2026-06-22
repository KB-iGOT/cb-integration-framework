package com.igot.cb.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ErrorResponse Tests")
class ErrorResponseTest {

    @Test
    @DisplayName("builder - with all fields set - creates instance with correct values")
    void builder_withAllFields_createsInstanceWithCorrectValues() {
        Map<String, String> errors = Map.of("field", "must not be blank");

        ErrorResponse response = ErrorResponse.builder()
                .code("ERR_001")
                .message("Validation failed")
                .errors(errors)
                .httpStatusCode("400")
                .build();

        assertNotNull(response);
        assertEquals("ERR_001", response.getCode());
        assertEquals("Validation failed", response.getMessage());
        assertEquals(errors, response.getErrors());
        assertEquals("400", response.getHttpStatusCode());
    }

    @Test
    @DisplayName("builder - with no fields set - creates instance with all fields null")
    void builder_withNoFields_createsInstanceWithAllFieldsNull() {
        ErrorResponse response = ErrorResponse.builder().build();

        assertNotNull(response);
        assertNull(response.getCode());
        assertNull(response.getMessage());
        assertNull(response.getErrors());
        assertNull(response.getHttpStatusCode());
    }

    @Test
    @DisplayName("builder - with only code - sets only code, rest null")
    void builder_withOnlyCode_setsOnlyCode() {
        ErrorResponse response = ErrorResponse.builder()
                .code("ERR_CODE")
                .build();

        assertEquals("ERR_CODE", response.getCode());
        assertNull(response.getMessage());
        assertNull(response.getErrors());
        assertNull(response.getHttpStatusCode());
    }

    @Test
    @DisplayName("builder - with only message - sets only message, rest null")
    void builder_withOnlyMessage_setsOnlyMessage() {
        ErrorResponse response = ErrorResponse.builder()
                .message("Something went wrong")
                .build();

        assertNull(response.getCode());
        assertEquals("Something went wrong", response.getMessage());
        assertNull(response.getErrors());
        assertNull(response.getHttpStatusCode());
    }

    @Test
    @DisplayName("builder - with only errors map - sets only errors, rest null")
    void builder_withOnlyErrors_setsOnlyErrors() {
        Map<String, String> errors = Map.of("email", "invalid format");

        ErrorResponse response = ErrorResponse.builder()
                .errors(errors)
                .build();

        assertNull(response.getCode());
        assertNull(response.getMessage());
        assertEquals(errors, response.getErrors());
        assertNull(response.getHttpStatusCode());
    }

    @Test
    @DisplayName("builder - with only httpStatusCode - sets only httpStatusCode, rest null")
    void builder_withOnlyHttpStatusCode_setsOnlyHttpStatusCode() {
        ErrorResponse response = ErrorResponse.builder()
                .httpStatusCode("500")
                .build();

        assertNull(response.getCode());
        assertNull(response.getMessage());
        assertNull(response.getErrors());
        assertEquals("500", response.getHttpStatusCode());
    }

    @Test
    @DisplayName("equals - two instances with identical fields - returns true")
    void equals_identicalFields_returnsTrue() {
        Map<String, String> errors = Map.of("f", "v");

        ErrorResponse a = ErrorResponse.builder()
                .code("C").message("M").errors(errors).httpStatusCode("400").build();
        ErrorResponse b = ErrorResponse.builder()
                .code("C").message("M").errors(errors).httpStatusCode("400").build();

        assertEquals(a, b);
    }

    @Test
    @DisplayName("equals - same instance - returns true")
    void equals_sameInstance_returnsTrue() {
        ErrorResponse response = ErrorResponse.builder().code("C").build();

        assertEquals(response, response);
    }

    @Test
    @DisplayName("equals - different code - returns false")
    void equals_differentCode_returnsFalse() {
        ErrorResponse a = ErrorResponse.builder().code("AAA").build();
        ErrorResponse b = ErrorResponse.builder().code("BBB").build();

        assertNotEquals(a, b);
    }

    @Test
    @DisplayName("equals - different message - returns false")
    void equals_differentMessage_returnsFalse() {
        ErrorResponse a = ErrorResponse.builder().message("msg-1").build();
        ErrorResponse b = ErrorResponse.builder().message("msg-2").build();

        assertNotEquals(a, b);
    }

    @Test
    @DisplayName("equals - different httpStatusCode - returns false")
    void equals_differentHttpStatusCode_returnsFalse() {
        ErrorResponse a = ErrorResponse.builder().httpStatusCode("400").build();
        ErrorResponse b = ErrorResponse.builder().httpStatusCode("500").build();

        assertNotEquals(a, b);
    }

    @Test
    @DisplayName("equals - compared to null - returns false")
    void equals_comparedToNull_returnsFalse() {
        ErrorResponse response = ErrorResponse.builder().code("C").build();

        assertNotEquals(null, response);
    }

    @Test
    @DisplayName("hashCode - equal objects - produce same hash code")
    void hashCode_equalObjects_produceSameHashCode() {
        ErrorResponse a = ErrorResponse.builder().code("C").message("M").build();
        ErrorResponse b = ErrorResponse.builder().code("C").message("M").build();

        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    @DisplayName("hashCode - different objects - produce different hash codes")
    void hashCode_differentObjects_produceDifferentHashCodes() {
        ErrorResponse a = ErrorResponse.builder().code("AAA").build();
        ErrorResponse b = ErrorResponse.builder().code("BBB").build();

        assertNotEquals(a.hashCode(), b.hashCode());
    }

    @Test
    @DisplayName("toString - contains all field values")
    void toString_containsAllFieldValues() {
        ErrorResponse response = ErrorResponse.builder()
                .code("ERR_001")
                .message("Bad Request")
                .httpStatusCode("400")
                .build();

        String result = response.toString();

        assertNotNull(result);
        assertTrue(result.contains("ERR_001"), "toString should contain code");
        assertTrue(result.contains("Bad Request"), "toString should contain message");
        assertTrue(result.contains("400"), "toString should contain httpStatusCode");
    }

    @Test
    @DisplayName("toString - all null fields - returns non-null string")
    void toString_allNullFields_returnsNonNullString() {
        ErrorResponse response = ErrorResponse.builder().build();

        assertNotNull(response.toString());
    }
}

