package com.avellar.currency_quote.entities;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tb_currency_rate")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(of = "id")
public class CurrencyRate implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(precision = 12, scale = 5)
	private BigDecimal high;
	@Column(precision = 12, scale = 5)
	private BigDecimal low;
	@Column(precision = 12, scale = 5)
	private BigDecimal varBid;
	@Column(precision = 12, scale = 5)
	private BigDecimal pctChange;
	@Column(nullable = false, precision = 12, scale = 5)
	private BigDecimal bid;
	@Column(precision = 12, scale = 5)
	private BigDecimal ask;
	@Column(nullable = false)
	private Long timestamp;
	@Column(nullable = false)
	private LocalDateTime createDate;

	@ManyToOne
	@JoinColumn(name = "currency_id")
	private Currency currency;

}
