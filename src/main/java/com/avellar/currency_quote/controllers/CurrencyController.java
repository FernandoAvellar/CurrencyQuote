package com.avellar.currency_quote.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

}
