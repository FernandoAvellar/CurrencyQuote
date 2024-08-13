package com.avellar.currency_quote.services;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import com.avellar.currency_quote.dto.RefreshTokenRequestDto;
import com.avellar.currency_quote.entities.RefreshToken;
import com.avellar.currency_quote.entities.Role;
import com.avellar.currency_quote.entities.User;
import com.avellar.currency_quote.repositories.RefreshTokenRepository;
import com.avellar.currency_quote.repositories.UserRepository;

@Service
public class TokenService {

	@Value("${jwt.access-token.expiration}")
	private Long accessTokenExpiresIn;

	@Value("${jwt.refresh-token.expiration}")
	private Long refreshTokenExpiresIn;

	@Autowired
	private JwtEncoder jwtEncoder;

	@Autowired
	private JwtDecoder jwtDecoder;

	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private RefreshTokenRepository refreshTokenRepository;

	@Autowired
	private BCryptPasswordEncoder passwordEncoder;

	public ResponseEntity<LoginResponseDto> login(@RequestBody LoginRequestDto loginRequest) {

		var user = userRepository.findByUsername(loginRequest.username());

		if (user.isEmpty() || !user.get().isLoginCorrect(loginRequest, passwordEncoder)) {
			throw new BadCredentialsException("user or password is invalid!");
		}

		var accessToken = generateAccessToken(user.get());
		var refreshToken = generateRefreshToken(user.get());

		return ResponseEntity.ok(new LoginResponseDto(accessToken, accessTokenExpiresIn, refreshToken));
	}

	public ResponseEntity<LoginResponseDto> refresh(RefreshTokenRequestDto refreshTokenRequestDto) {
		try {
			Jwt decodedJwt = jwtDecoder.decode(refreshTokenRequestDto.refreshToken());

			if (!"refresh".equals(decodedJwt.getClaim("type"))) {
				throw new BadCredentialsException("Invalid refresh token");
			}

			String userId = decodedJwt.getSubject();
			var user = userRepository.findById(Long.valueOf(userId));

			if (user.isEmpty()) {
				throw new BadCredentialsException("User not found");
			}

			var accessToken = generateAccessToken(user.get());

			return ResponseEntity.ok(new LoginResponseDto(accessToken, accessTokenExpiresIn, refreshTokenRequestDto.refreshToken()));

		} catch (Exception e) {
			throw new BadCredentialsException("Invalid refresh token");
		}
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
			userInfo.put("userRole", user.get().getRoles());

			return ResponseEntity.ok(userInfo);

		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
		}
	}

	private String generateAccessToken(User user) {
	    return generateToken(user, accessTokenExpiresIn, "access");
	}

	private String generateRefreshToken(User user) {
	    var existingToken = refreshTokenRepository.findByUser(user);
	    if (existingToken.isPresent()) {
	        return updateExistingRefreshToken(existingToken.get());
	    } else {
	        return createNewRefreshToken(user);
	    }
	}
	
	private String createNewRefreshToken(User user) {
	    var refreshToken = new RefreshToken();
	    refreshToken.setUser(user);
	    refreshToken.setExpiryDate(Instant.now().plusSeconds(refreshTokenExpiresIn));

	    var tokenValue = generateToken(user, refreshTokenExpiresIn, "refresh");
	    refreshToken.setToken(tokenValue);

	    refreshTokenRepository.save(refreshToken);
	    return tokenValue;
	}

	private String updateExistingRefreshToken(RefreshToken refreshToken) {
	    refreshToken.setExpiryDate(Instant.now().plusSeconds(refreshTokenExpiresIn));

	    var tokenValue = generateToken(refreshToken.getUser(), refreshTokenExpiresIn, "refresh");
	    refreshToken.setToken(tokenValue);

	    refreshTokenRepository.save(refreshToken);
	    return tokenValue;
	}
	
	private String generateToken(User user, Long expiresIn, String tokenType) {
	    var now = Instant.now();

	    var roles = user.getRoles().stream()
	            .map(Role::getName)
	            .collect(Collectors.toList());

	    var claims = JwtClaimsSet.builder()
	            .issuer("currency_quote_backend")
	            .subject(user.getId().toString())
	            .issuedAt(now)
	            .expiresAt(now.plusSeconds(expiresIn))
	            .claim("roles", roles)
	            .claim("type", tokenType)
	            .build();

	    return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
	}
}
