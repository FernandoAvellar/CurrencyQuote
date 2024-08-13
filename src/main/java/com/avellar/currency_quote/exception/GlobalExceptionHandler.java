package com.avellar.currency_quote.exception;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(CurrencyNotFoundException.class)
	public ResponseEntity<String> handleCurrencyNotFoundException(CurrencyNotFoundException ex) {
		return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
	}

	@ExceptionHandler(ExternalApiFailureException.class)
	public ResponseEntity<String> handleExternalApiFailureException(ExternalApiFailureException ex) {
		return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_GATEWAY);
	}

	@ExceptionHandler(UserAlreadyExistsException.class)
	public ResponseEntity<Object> handleUserAlreadyExistsException(UserAlreadyExistsException ex) {
		return new ResponseEntity<>(ex.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY);
	}

	@ExceptionHandler(IncorrectPasswordException.class)
	public ResponseEntity<String> handleIncorrectPasswordException(IncorrectPasswordException ex) {
		return new ResponseEntity<>(ex.getMessage(), HttpStatus.UNAUTHORIZED);
	}

	@ExceptionHandler(ResponseStatusException.class)
	public ResponseEntity<Object> handleResponseStatusException(ResponseStatusException ex, WebRequest request) {
		Map<String, Object> body = new HashMap<>();
		body.put("timestamp", LocalDateTime.now());
		body.put("status", ex.getStatusCode().value());
		body.put("error", ex.getReason());
		body.put("message", ex.getMessage());
		body.put("path", request.getDescription(false).substring(4));

		return new ResponseEntity<>(body, ex.getStatusCode());
	}
}
