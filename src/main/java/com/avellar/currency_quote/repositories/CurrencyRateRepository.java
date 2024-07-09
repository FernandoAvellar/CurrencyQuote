package com.avellar.currency_quote.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.avellar.currency_quote.entities.CurrencyRate;

public interface CurrencyRateRepository extends JpaRepository<CurrencyRate, Long> {
}
