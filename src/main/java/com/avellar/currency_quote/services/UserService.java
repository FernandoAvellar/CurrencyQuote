package com.avellar.currency_quote.services;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

import com.avellar.currency_quote.entities.Currency;
import com.avellar.currency_quote.repositories.CurrencyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.avellar.currency_quote.dto.RegisterUserDto;
import com.avellar.currency_quote.entities.User;
import com.avellar.currency_quote.exception.UserAlreadyExistsException;
import com.avellar.currency_quote.repositories.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class UserService {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private CurrencyRepository currencyRepository;

	@Autowired
	private JwtDecoder jwtDecoder;

	@Autowired
	private BCryptPasswordEncoder passwordEncoder;

	@Transactional
	public ResponseEntity<Void> newUser(@RequestBody RegisterUserDto dto) {

		var userFromDb = userRepository.findByUsername(dto.username());
		if (userFromDb.isPresent()) {
			throw new UserAlreadyExistsException("user already exists");
		}

		var user = new User();
		user.setUsername(dto.username());
		user.setPassword(passwordEncoder.encode(dto.password()));

		userRepository.save(user);

		URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
				.path("/{id}")
				.buildAndExpand(user.getId())
				.toUri();

		return ResponseEntity.created(uri).build();
	}

	public ResponseEntity<List<User>> listUsers() {
		var users = userRepository.findAll();
		return ResponseEntity.ok(users);
	}

	public List<Currency> getFavoriteCurrencies(String token) {
		User user = (User) getUserFromToken(token);
		return user.getFavoriteCurrencies();
	}

	@Transactional
	public void updateFavoriteCurrencies(String token, List<String> favoriteCurrencyCodes) {
		User user = (User) getUserFromToken(token);
		List<Currency> favoriteCurrencies = favoriteCurrencyCodes.stream()
				.map(code -> {
					Currency currency = currencyRepository.findByCode(code);
					if (currency == null) {
						throw new RuntimeException("Currency not found: " + code);
					}
					return currency;
				})
				.collect(Collectors.toList());
		user.setFavoriteCurrencies(favoriteCurrencies);
		userRepository.save(user);
	}

	private Object getUserFromToken(String token) {
		var jwt = jwtDecoder.decode(token.replace("Bearer ", ""));
		String userId = jwt.getSubject();
		return userRepository.findById(Long.valueOf(userId))
				.orElseThrow(() -> new RuntimeException("User not found"));
	}
}
