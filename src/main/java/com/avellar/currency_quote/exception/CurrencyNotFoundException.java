package com.avellar.currency_quote.exception;

public class CurrencyNotFoundException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public CurrencyNotFoundException(String message) {
		super(message);
	}
}
