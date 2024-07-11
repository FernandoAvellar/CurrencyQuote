package com.avellar.currency_quote.services;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.avellar.currency_quote.entities.Currency;
import com.avellar.currency_quote.entities.CurrencyRate;
import com.avellar.currency_quote.exception.CurrencyNotFoundException;
import com.avellar.currency_quote.repositories.CurrencyRateRepository;
import com.avellar.currency_quote.repositories.CurrencyRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class CurrencyService {

	@Autowired
	private CurrencyRepository currencyRepository;

	@Autowired
	private CurrencyRateRepository currencyRateRepository;

	@Autowired
	private RedisTemplate<String, Object> redisTemplate;

	@Autowired
	private ObjectMapper objectMapper;

	private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	private final RestTemplate restTemplate = new RestTemplate();
	private final String VALID_COINS = "USD-BRL,EUR-BRL,JPY-BRL,BTC-BRL,CAD-BRL,GBP-BRL,ARS-BRL,CHF-BRL,AUD-BRL,CNY-BRL,ETH-BRL,SGD-BRL,AED-BRL,SEK-BRL,CLP-BRL,PYG-BRL,MXN-BRL,UYU-BRL,COP-BRL,BOB-BRL";
	private final String apiUrl = "https://economia.awesomeapi.com.br/json/last/" + VALID_COINS;

	@Scheduled(cron = "*/30 * 8-18 * * MON-FRI") // (roda a cada 30 segundos das 8:30h às 18:59h somente em dias de
													// semana)
	public void fetchAndStoreCurrencyRates() {
		@SuppressWarnings("unchecked")
		Map<String, Map<String, String>> response = restTemplate.getForObject(apiUrl, Map.class);

		for (Map.Entry<String, Map<String, String>> entry : response.entrySet()) {
			String code = entry.getKey();
			Map<String, String> data = entry.getValue();

			Currency currency = currencyRepository.findByCode(code);
			if (currency == null) {
				currency = new Currency();
				currency.setCode(code);
				currency.setName(data.get("name"));
				currencyRepository.save(currency);
			}

			CurrencyRate currencyRate = new CurrencyRate();
			currencyRate.setCurrency(currency);
			currencyRate.setHigh(new BigDecimal(data.get("high")));
			currencyRate.setLow(new BigDecimal(data.get("low")));
			currencyRate.setVarBid(new BigDecimal(data.get("varBid")));
			currencyRate.setPctChange(new BigDecimal(data.get("pctChange")));
			currencyRate.setBid(new BigDecimal(data.get("bid")));
			currencyRate.setAsk(new BigDecimal(data.get("ask")));
			currencyRate.setTimestamp(Long.parseLong(data.get("timestamp")));
			currencyRate.setCreateDate(LocalDateTime.parse(data.get("create_date"), dateTimeFormatter));

			currencyRateRepository.save(currencyRate);

			// Store the last quotation in cache using Redis Server
			redisTemplate.opsForValue().set(code, currencyRate);
		}
	}

	public CurrencyRate getLastCurrencyRate(String code) {
		Object currencyRateObj = redisTemplate.opsForValue().get(code);
		if (currencyRateObj == null) {
			throw new CurrencyNotFoundException("Currency code [" + code + "] not found.");
		}
		return objectMapper.convertValue(currencyRateObj, CurrencyRate.class);
	}

	public List<Currency> findAllCurrency() {
		return currencyRepository.findAll();
	}

	public Object getHistoricalRates(String currency, int numberOfDays) {
		// Converter USDBRL para USD-BRL
		String formattedCurrency = currency.substring(0, 3) + "-" + currency.substring(3);
		String url = "https://economia.awesomeapi.com.br/json/daily/" + formattedCurrency + "/" + numberOfDays;
		return restTemplate.getForObject(url, Object.class);
	}
}
