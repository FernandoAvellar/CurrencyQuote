package com.avellar.currency_quote.repositories;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import com.avellar.currency_quote.entities.Currency;

@DataJpaTest
@ActiveProfiles("test")
public class CurrencyRepositoryTest {

	@Autowired
	private CurrencyRepository currencyRepository;

	@Test
	public void testFindByCode() {
		// Dado um novo Currency salvo no banco
		Currency currency = new Currency();
		currency.setCode("USD-BRL");
		currency.setName("Dólar Americano/Real Brasileiro");
		currencyRepository.save(currency);

		// Quando buscamos por código
		Currency foundCurrency = currencyRepository.findByCode("USD-BRL");

		// Então verificamos se o currency foi encontrado corretamente
		assertThat(foundCurrency).isNotNull();
		assertThat(foundCurrency.getCode()).isEqualTo("USD-BRL");
		assertThat(foundCurrency.getName()).isEqualTo("Dólar Americano/Real Brasileiro");
	}

	@Test
	public void testFindByCode_NotFound() {
		// Quando buscamos por um código inexistente
		Currency foundCurrency = currencyRepository.findByCode("EUR-BRL");

		// Então verificamos se o resultado é null
		assertThat(foundCurrency).isNull();
	}
}
