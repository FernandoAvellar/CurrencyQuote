package com.avellar.currency_quote.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.avellar.currency_quote.entities.Currency;

public interface CurrencyRepository extends JpaRepository<Currency, Long> {
	Currency findByCode(String code);
}
