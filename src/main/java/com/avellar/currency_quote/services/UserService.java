package com.avellar.currency_quote.services;

import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.avellar.currency_quote.dto.ChangePasswordDto;
import com.avellar.currency_quote.dto.CreateUserDto;
import com.avellar.currency_quote.dto.UpdateRolesDto;
import com.avellar.currency_quote.dto.UpdatePasswordDto;
import com.avellar.currency_quote.entities.Currency;
import com.avellar.currency_quote.entities.Role;
import com.avellar.currency_quote.entities.User;
import com.avellar.currency_quote.exception.IncorrectPasswordException;
import com.avellar.currency_quote.exception.UserAlreadyExistsException;
import com.avellar.currency_quote.repositories.CurrencyRepository;
import com.avellar.currency_quote.repositories.RoleRepository;
import com.avellar.currency_quote.repositories.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class UserService {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private CurrencyRepository currencyRepository;

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private JwtDecoder jwtDecoder;

	@Autowired
	private BCryptPasswordEncoder passwordEncoder;

	@Transactional
	public ResponseEntity<Void> createUser(@RequestBody CreateUserDto dto) {

		var userFromDb = userRepository.findByUsername(dto.username());
		if (userFromDb.isPresent()) {
			throw new UserAlreadyExistsException("user already exists");
		}

		var user = new User();
		var basicRole = roleRepository.findByName(Role.Values.ROLE_BASIC.name());
		user.setUsername(dto.username());
		user.setPassword(passwordEncoder.encode(dto.password()));
		user.setRoles(Set.of(basicRole));
		userRepository.save(user);

		URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(user.getId()).toUri();
		return ResponseEntity.created(uri).build();
	}

	public ResponseEntity<List<User>> listUsers() {
		var users = userRepository.findAll();
		return ResponseEntity.ok(users);
	}

	@Transactional
	public void deleteUser(String username) {
		User user = userRepository.findByUsername(username)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
		userRepository.delete(user);
	}

	@Transactional
	public void changePasswordByAdminUser(String username, UpdatePasswordDto passwordDto) {
		User user = userRepository.findByUsername(username)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

		user.setPassword(passwordEncoder.encode(passwordDto.newPassword()));
		userRepository.save(user);
	}

	@Transactional
	public void updateUserRoles(UpdateRolesDto updateRolesDto) {
		String username = updateRolesDto.username();
		List<String> roles = updateRolesDto.roles();

		User user = userRepository.findByUsername(username)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

		if (roles.isEmpty()) {
			throw new IllegalArgumentException("Role Array must have at least one role.");
		}

		Set<Role> validRoles = new HashSet<>();
		for (String roleName : roles) {
			Role role = roleRepository.findByName(roleName);
			if (role == null) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Role not found: " + roleName);
			}
			validRoles.add(role);
		}

		user.setRoles(validRoles);
		userRepository.save(user);
	}

	@Transactional
	public void changePassword(ChangePasswordDto changePasswordDto) {
		User user = userRepository.findByUsername(changePasswordDto.username())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

		if (!passwordEncoder.matches(changePasswordDto.actualPassword(), user.getPassword())) {
			throw new IncorrectPasswordException("Incorrect actual password: " + changePasswordDto.actualPassword());
		}

		user.setPassword(passwordEncoder.encode(changePasswordDto.newPassword()));
		userRepository.save(user);
	}

	public List<Currency> getFavoriteCurrencies(String token) {
		User user = (User) getUserFromToken(token);
		return user.getFavoriteCurrencies();
	}

	@Transactional
	public void updateFavoriteCurrencies(String token, List<String> favoriteCurrencyCodes) {
		User user = (User) getUserFromToken(token);
		List<Currency> favoriteCurrencies = favoriteCurrencyCodes.stream().map(code -> {
			Currency currency = currencyRepository.findByCode(code);
			if (currency == null) {
				throw new RuntimeException("Currency not found: " + code);
			}
			return currency;
		}).collect(Collectors.toList());
		user.setFavoriteCurrencies(favoriteCurrencies);
		userRepository.save(user);
	}

	private Object getUserFromToken(String token) {
		var jwt = jwtDecoder.decode(token.replace("Bearer ", ""));
		String userId = jwt.getSubject();
		return userRepository.findById(Long.valueOf(userId)).orElseThrow(() -> new RuntimeException("User not found"));
	}
}
