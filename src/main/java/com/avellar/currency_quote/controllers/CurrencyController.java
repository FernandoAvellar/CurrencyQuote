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

@RestController
@RequestMapping("/currency")
public class CurrencyController {

	@Autowired
	private CurrencyService currencyService;

	@GetMapping("/rate/{code}")
	public CurrencyRate getLastCurrencyRate(@PathVariable String code) {
		return currencyService.getLastCurrencyRate(code);
	}

	@GetMapping
	public List<Currency> findAllCurrency() {
		return currencyService.findAllCurrency();
	}

	@GetMapping("/historical/{code}/{numberOfDays}")
	public Object getHistoricalRates(@PathVariable String code, @PathVariable int numberOfDays) {
		return currencyService.getHistoricalRates(code, numberOfDays);
	}
}
