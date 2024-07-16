package com.avellar.currency_quote.services;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import com.avellar.currency_quote.dto.LoginRequestDto;
import com.avellar.currency_quote.dto.LoginResponseDto;
import com.avellar.currency_quote.repositories.UserRepository;

@Service
public class TokenService {

	@Autowired
	private JwtEncoder jwtEncoder;

	@Autowired
	private JwtDecoder jwtDecoder;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private BCryptPasswordEncoder passwordEncoder;

	public ResponseEntity<LoginResponseDto> login(@RequestBody LoginRequestDto loginRequest) {

		var user = userRepository.findByUsername(loginRequest.username());

		if (user.isEmpty() || !user.get().isLoginCorrect(loginRequest, passwordEncoder)) {
			throw new BadCredentialsException("user or password is invalid!");
		}

		var now = Instant.now();
		var expiresIn = 300L;

		var claims = JwtClaimsSet.builder().issuer("currency_quote_backend").subject(user.get().getId().toString())
				.issuedAt(now).expiresAt(now.plusSeconds(expiresIn)).build();

		var jwtValue = jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();

		return ResponseEntity.ok(new LoginResponseDto(jwtValue, expiresIn));
	}

	public ResponseEntity<?> getMe(String token) {
		try {
			Jwt decodedJwt = jwtDecoder.decode(token);
			String userId = decodedJwt.getSubject();
			var user = userRepository.findById(Long.valueOf(userId));

			if (user.isEmpty()) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
			}

			Map<String, Object> userInfo = new HashMap<>();
			userInfo.put("username", user.get().getUsername());

			return ResponseEntity.ok(userInfo);

		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
		}
	}
}
