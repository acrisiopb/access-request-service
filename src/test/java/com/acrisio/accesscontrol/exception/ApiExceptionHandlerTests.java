package com.acrisio.accesscontrol.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApiExceptionHandlerTests {

    @Mock
    private HttpServletRequest request;

    @Mock
    private BindingResult bindingResult;

    @InjectMocks
    private ApiExceptionHandler handler;

    private static final String REQUEST_URI = "/test/path";

    @BeforeEach
    void setUp() {
        when(request.getRequestURI()).thenReturn(REQUEST_URI);
        when(request.getMethod()).thenReturn("POST");
    }

    @Test
    void entityNotFoundException_Returns404NotFound() {

        String message = "User not found.";

        EntityNotFoundException exception = new EntityNotFoundException(message);

        ResponseEntity<ErrorMessage> response = handler.entityNotFoundException(exception, request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND.value(), Objects.requireNonNull(response.getBody()).getStatus());
        assertEquals(message, response.getBody().getMessage());
        assertEquals(REQUEST_URI, response.getBody().getPath());
    }

    @Test
    void nameUniqueViolationException_Returns409Conflict() {
        String message = "Email already registered.";
        NameUniqueViolationException exception = new NameUniqueViolationException(message);

        ResponseEntity<ErrorMessage> response = handler.nameUniqueViolationException(exception, request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals(HttpStatus.CONFLICT.value(), Objects.requireNonNull(response.getBody()).getStatus());
        assertEquals(message, response.getBody().getMessage());
    }

    @Test
    void unprocessableEntityException_Returns422UnprocessableEntity() {

        String message = "Invalid data format.";
        UnprocessableEntityException exception = new UnprocessableEntityException(message);

        ResponseEntity<ErrorMessage> response = handler.unprocessableEntityException(exception, request);

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.value(), Objects.requireNonNull(response.getBody()).getStatus());
        assertEquals(message, response.getBody().getMessage());
    }

    @Test
    void illegalArgumentException_Returns401Unauthorized() {
        String message = "Rule violation.";
        IllegalArgumentException exception = new IllegalArgumentException(message);

        ResponseEntity<ErrorMessage> response = handler.illegalArgumentException(exception, request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals(HttpStatus.UNAUTHORIZED.value(), Objects.requireNonNull(response.getBody()).getStatus());
        assertEquals(message, response.getBody().getMessage());
    }

    @Test
    void methodArgumentNotValidException_Returns422UnprocessableEntityWithErrors() {

        String message = "Campo(s) inválido(s)";

        // Mock MethodArgumentNotValidException which requires BindingResult
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(null, bindingResult);

        ResponseEntity<ErrorMessage> response = handler.methodArgumentNotValidException(exception, request);

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
        assertEquals(message, Objects.requireNonNull(response.getBody()).getMessage());
        assertNotNull(response.getBody().getErrors()); // Ensures the errors map is initialized
    }
}