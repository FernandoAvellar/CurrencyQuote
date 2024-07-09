package com.avellar.currency_quote;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CurrencyQuoteApplication {

	public static void main(String[] args) {
		SpringApplication.run(CurrencyQuoteApplication.class, args);
	}

}
