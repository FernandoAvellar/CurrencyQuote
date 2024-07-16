package com.avellar.currency_quote.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.avellar.currency_quote.dto.LoginRequestDto;
import com.avellar.currency_quote.dto.LoginResponseDto;
import com.avellar.currency_quote.services.TokenService;

@RestController
@RequestMapping("/auth")
public class TokenController {

	@Autowired
	private TokenService tokenService;

	@PostMapping("/login")
	public ResponseEntity<LoginResponseDto> login(@RequestBody LoginRequestDto loginRequestDto) {
		return tokenService.login(loginRequestDto);
	}

	@GetMapping("/me")
	public ResponseEntity<?> getMe(@RequestHeader("Authorization") String authHeader) {
		if (authHeader != null && authHeader.startsWith("Bearer ")) {
			String token = authHeader.substring(7);
			return tokenService.getMe(token);
		} else {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Authorization header");
		}
	}
}
