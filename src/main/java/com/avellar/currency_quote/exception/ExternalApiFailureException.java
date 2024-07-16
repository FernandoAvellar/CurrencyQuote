package com.avellar.currency_quote.exception;

public class ExternalApiFailureException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public ExternalApiFailureException(String message) {
		super(message);
	}
}
