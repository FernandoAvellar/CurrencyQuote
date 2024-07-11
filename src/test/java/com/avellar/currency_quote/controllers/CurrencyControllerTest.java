package com.avellar.currency_quote.controllers;

import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import com.avellar.currency_quote.entities.Currency;
import com.avellar.currency_quote.entities.CurrencyRate;
import com.avellar.currency_quote.exception.CurrencyNotFoundException;
import com.avellar.currency_quote.services.CurrencyService;

@WebMvcTest(CurrencyController.class)
public class CurrencyControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private CurrencyService currencyService;

	private CurrencyRate currencyRate;

	@BeforeEach
	public void setup() {
		currencyRate = new CurrencyRate();
		currencyRate.setBid(new BigDecimal("5.12345"));
		currencyRate.setAsk(new BigDecimal("5.54321"));
		currencyRate.setHigh(new BigDecimal("5.67890"));
		currencyRate.setLow(new BigDecimal("4.98765"));
		currencyRate.setVarBid(new BigDecimal("0.00123"));
		currencyRate.setPctChange(new BigDecimal("0.12"));
		currencyRate.setTimestamp(System.currentTimeMillis());
		currencyRate.setCreateDate(LocalDateTime.now());
	}

	@Test
	public void testGetLastCurrencyRate_Success() throws Exception {
		Mockito.when(currencyService.getLastCurrencyRate(anyString())).thenReturn(currencyRate);

		mockMvc.perform(get("/currency/rate/USD-BRL")).andExpect(status().isOk())
				.andExpect(jsonPath("$.bid").value("5.12345")).andExpect(jsonPath("$.ask").value("5.54321"));
	}

	@Test
	public void testGetLastCurrencyRate_NotFound() throws Exception {
		Mockito.when(currencyService.getLastCurrencyRate(anyString()))
				.thenThrow(new CurrencyNotFoundException("Currency code not found"));

		mockMvc.perform(get("/currency/rate/UNKNOWN")).andExpect(status().isNotFound())
				.andExpect(content().string("Currency code not found"));
	}
	
	@Test
    public void testFindAllCurrency() throws Exception {
        Currency currency1 = new Currency();
        currency1.setCode("USD-BRL");
        currency1.setName("Dólar Americano/Real Brasileiro");

        Currency currency2 = new Currency();
        currency2.setCode("EUR-BRL");
        currency2.setName("Euro/Real Brasileiro");

        List<Currency> currencies = Arrays.asList(currency1, currency2);

        Mockito.when(currencyService.findAllCurrency()).thenReturn(currencies);

        mockMvc.perform(get("/currency"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("USD-BRL"))
                .andExpect(jsonPath("$[0].name").value("Dólar Americano/Real Brasileiro"))
                .andExpect(jsonPath("$[1].code").value("EUR-BRL"))
                .andExpect(jsonPath("$[1].name").value("Euro/Real Brasileiro"));
    }
}
