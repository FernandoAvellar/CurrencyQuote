package com.avellar.currency_quote.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.client.RestTemplate;

import com.avellar.currency_quote.entities.Currency;
import com.avellar.currency_quote.entities.CurrencyRate;
import com.avellar.currency_quote.exception.CurrencyNotFoundException;
import com.avellar.currency_quote.repositories.CurrencyRateRepository;
import com.avellar.currency_quote.repositories.CurrencyRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
public class CurrencyServiceTest {

	@Mock
	private CurrencyRepository currencyRepository;

	@Mock
	private CurrencyRateRepository currencyRateRepository;

	@Mock
	private RedisTemplate<String, Object> redisTemplate;

	@Mock
	private ValueOperations<String, Object> valueOperations;

	@Mock
	private RestTemplate restTemplate;

	@Mock
	private ObjectMapper objectMapper;

	@InjectMocks
	private CurrencyService currencyService;

	private CurrencyRate currencyRate;
	private Currency currency;

	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);

		currency = new Currency();
		currency.setCode("USD-BRL");
		currency.setName("Dólar Americano/Real Brasileiro");

		currencyRate = new CurrencyRate();
		currencyRate.setCurrency(currency);
		currencyRate.setBid(new BigDecimal("5.12345"));
		currencyRate.setAsk(new BigDecimal("5.54321"));
		currencyRate.setHigh(new BigDecimal("5.67890"));
		currencyRate.setLow(new BigDecimal("4.98765"));
		currencyRate.setVarBid(new BigDecimal("0.00123"));
		currencyRate.setPctChange(new BigDecimal("0.12"));
		currencyRate.setTimestamp(System.currentTimeMillis());
		currencyRate.setCreateDate(LocalDateTime.now());

		when(redisTemplate.opsForValue()).thenReturn(valueOperations);
	}

	@Test
    public void testFetchAndStoreCurrencyRates() {
        // Simulando a resposta da API externa
        Map<String, Map<String, String>> apiResponse = new HashMap<>();
        Map<String, String> currencyData = new HashMap<>();
        currencyData.put("code", "USD");
        currencyData.put("codein", "BRL");
        currencyData.put("name", "Dólar Americano/Real Brasileiro");
        currencyData.put("high", "5.67890");
        currencyData.put("low", "4.98765");
        currencyData.put("varBid", "0.00123");
        currencyData.put("pctChange", "0.12");
        currencyData.put("bid", "5.12345");
        currencyData.put("ask", "5.54321");
        currencyData.put("timestamp", String.valueOf(System.currentTimeMillis()));
        currencyData.put("create_date", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        apiResponse.put("USDBRL", currencyData);

        when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(apiResponse);
        when(currencyRepository.findByCode("USDBRL")).thenReturn(currency);
        when(currencyRateRepository.save(any(CurrencyRate.class))).thenReturn(currencyRate);

        currencyService.fetchAndStoreCurrencyRates();

        // Verificando se os dados foram salvos corretamente no banco de dados 
        verify(currencyRepository, times(1)).findByCode("USDBRL");
        verify(currencyRateRepository, times(20)).save(any(CurrencyRate.class));
        verify(redisTemplate.opsForValue(), times(20)).set(anyString(), any(CurrencyRate.class));
    }

	@Test
	public void testGetLastCurrencyRate_Success() {
		when(valueOperations.get(anyString())).thenReturn(currencyRate);
		when(objectMapper.convertValue(any(), eq(CurrencyRate.class))).thenReturn(currencyRate);

		CurrencyRate result = currencyService.getLastCurrencyRate("USD-BRL");

		assertEquals(currencyRate, result);
	}

	@Test
	public void testGetLastCurrencyRate_NotFound() {
		when(valueOperations.get(anyString())).thenReturn(null);

		assertThrows(CurrencyNotFoundException.class, () -> {
			currencyService.getLastCurrencyRate("UNKNOWN");
		});
	}
}
