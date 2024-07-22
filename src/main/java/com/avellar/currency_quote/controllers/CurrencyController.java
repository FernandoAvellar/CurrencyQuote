package com.avellar.currency_quote.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.avellar.currency_quote.entities.Currency;
import com.avellar.currency_quote.entities.CurrencyRate;
import com.avellar.currency_quote.services.CurrencyService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/currency")
@Tag(name = "Currency API")
public class CurrencyController {

	@Autowired
	private CurrencyService currencyService;

	@GetMapping("/rate/{code}")
	@Operation(summary = "Return last cotation from an specific code.", description = "Endpoint to get the last cotation from an specific code (Ex: USDBRL).", security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Last cotation from choosen currency code", content = @Content(schema = @Schema(implementation = CurrencyRate.class))),
			@ApiResponse(responseCode = "401", description = "Not authorized"),
			@ApiResponse(responseCode = "404", description = "Code not found")})
	public CurrencyRate getLastCurrencyRate(@PathVariable String code) {
		return currencyService.getLastCurrencyRate(code);
	}

	@GetMapping
	@Operation(summary = "Return all valid currency pairs.", description = "Endpoint to get all valid currency pairs code.", security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "List of all available currency", content = @Content(schema = @Schema(implementation = Currency.class))),
			@ApiResponse(responseCode = "401", description = "Not authorized")})
	public List<Currency> findAllCurrency() {
		return currencyService.findAllCurrency();
	}

	@GetMapping("/historical/{code}/{numberOfDays}")
	@Operation(summary = "Return an historical cotations from an specific code.", description = "Endpoint to get an historical from an specific code during indicated number of days.", security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "List with historical cotations value from a code", content = @Content(schema = @Schema(implementation = Currency.class))),
			@ApiResponse(responseCode = "401", description = "Not authorized")})
	public Object getHistoricalRates(@PathVariable String code, @PathVariable int numberOfDays) {
		return currencyService.getHistoricalRates(code, numberOfDays);
	}
}
