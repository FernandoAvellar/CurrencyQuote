package com.avellar.currency_quote.services;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.avellar.currency_quote.entities.Currency;
import com.avellar.currency_quote.entities.CurrencyRate;
import com.avellar.currency_quote.exception.CurrencyNotFoundException;
import com.avellar.currency_quote.exception.ExternalApiFailureException;
import com.avellar.currency_quote.repositories.CurrencyRateRepository;
import com.avellar.currency_quote.repositories.CurrencyRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.transaction.Transactional;

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
	private final String availableCurrencyApiUrl = "https://economia.awesomeapi.com.br/json/available";
	private String VALID_COINS;

    @PostConstruct
	public void init() {
		populateValidCoins();
	}

	// (roda a cada 30 segundos das 8:30h às 18:59h somente em dias de semana)
	@Scheduled(cron = "*/30 * 8-18 * * MON-FRI")
	@Transactional
	public void fetchAndStoreCurrencyRates() {
		String apiUrl = "https://economia.awesomeapi.com.br/json/last/" + VALID_COINS;
		@SuppressWarnings("unchecked")
		Map<String, Map<String, String>> response = restTemplate.getForObject(apiUrl, Map.class);
		if (response != null) {
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
		} else {
			throw new ExternalApiFailureException("external API failure");
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
		try {
			return restTemplate.getForObject(url, Object.class);
		} catch (HttpClientErrorException e) {
			throw new CurrencyNotFoundException("Currency code [" + currency + "] not found.");
		}
	}

	private void populateValidCoins() {
		try {
			ResponseEntity<Map<String, String>> responseEntity = restTemplate.exchange(
					availableCurrencyApiUrl,
					HttpMethod.GET,
					null,
					new ParameterizedTypeReference<Map<String, String>>() {}
			);
			Map<String, String> response = responseEntity.getBody();

			if (response != null) {
				VALID_COINS = response.keySet().stream()
						.filter(s -> s.endsWith("BRL"))
						.collect(Collectors.joining(","));
			}
		} catch (Exception e) {
			throw new ExternalApiFailureException("Failed to fetch valid coins from external API");
		}
	}
}
