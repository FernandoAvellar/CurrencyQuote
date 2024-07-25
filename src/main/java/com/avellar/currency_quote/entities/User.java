package com.avellar.currency_quote.entities;

import jakarta.persistence.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.avellar.currency_quote.dto.LoginRequestDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "tb_users")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "user_id")
	private Long id;

	@Column(unique = true)
	private String username;
	private String password;

	@ManyToMany
	@JoinTable(
			name = "user_favorite_currencies",
			joinColumns = @JoinColumn(name = "user_id"),
			inverseJoinColumns = @JoinColumn(name = "currency_id")
	)
	private List<Currency> favoriteCurrencies;

	public boolean isLoginCorrect(LoginRequestDto loginRequest, PasswordEncoder passwordEncoder) {
		return passwordEncoder.matches(loginRequest.password(), this.password);
	}
}