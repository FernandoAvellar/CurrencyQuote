package com.avellar.currency_quote.controllers;

import com.avellar.currency_quote.config.SecurityConfig;
import com.avellar.currency_quote.entities.Currency;
import com.avellar.currency_quote.entities.CurrencyRate;
import com.avellar.currency_quote.exception.CurrencyNotFoundException;
import com.avellar.currency_quote.services.CurrencyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CurrencyController.class)
@Import(SecurityConfig.class)
public class CurrencyControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private CurrencyService currencyService;

	@Autowired
	private JwtEncoder jwtEncoder;

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

		mockMvc.perform(MockMvcRequestBuilders.get("/currency/rate/USD-BRL")
				.header("Authorization", "Bearer " + getToken()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.bid").value("5.12345"))
				.andExpect(jsonPath("$.ask").value("5.54321"));
	}

	@Test
	public void testGetLastCurrencyRate_NotFound() throws Exception {
		Mockito.when(currencyService.getLastCurrencyRate(anyString()))
				.thenThrow(new CurrencyNotFoundException("Currency code not found"));

		mockMvc.perform(MockMvcRequestBuilders.get("/currency/rate/UNKNOWN")
				.header("Authorization", "Bearer " + getToken()))
				.andExpect(status().isNotFound())
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

		mockMvc.perform(MockMvcRequestBuilders.get("/currency")
				.header("Authorization", "Bearer " + getToken()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].code").value("USD-BRL"))
				.andExpect(jsonPath("$[0].name").value("Dólar Americano/Real Brasileiro"))
				.andExpect(jsonPath("$[1].code").value("EUR-BRL"))
				.andExpect(jsonPath("$[1].name").value("Euro/Real Brasileiro"));
	}

	@Test
	public void testGetHistoricalRates() throws Exception {
		Object historicalRates = Arrays.asList(new CurrencyRate(), new CurrencyRate());

		Mockito.when(currencyService.getHistoricalRates(anyString(), Mockito.anyInt())).thenReturn(historicalRates);

		mockMvc.perform(MockMvcRequestBuilders.get("/currency/historical/USD-BRL/10")
				.header("Authorization", "Bearer " + getToken()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(2));
	}

	private String getToken() {
		Instant now = Instant.now();
		JwtClaimsSet claims = JwtClaimsSet.builder()
				.issuer("currency_quote_backend")
				.issuedAt(now)
				.expiresAt(now.plusSeconds(1200L))
				.subject("user")
				.build();
		return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
	}
}
