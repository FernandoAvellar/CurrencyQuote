package com.avellar.currency_quote.entities;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tb_currency")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Currency implements Serializable {
	@Serial
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true)
	private String code;
	@Column(nullable = false)
	private String name;

	@OneToMany(mappedBy = "currency", cascade = CascadeType.ALL)
	@JsonIgnore
	private List<CurrencyRate> rates;

	@ManyToMany(mappedBy = "favoriteCurrencies")
	@JsonIgnore
	private List<User> users;
}
