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

import com.avellar.currency_quote.dto.AuthMeResponseDto;
import com.avellar.currency_quote.dto.LoginRequestDto;
import com.avellar.currency_quote.dto.LoginResponseDto;
import com.avellar.currency_quote.services.TokenService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/auth")
@Tag(name = "Token API")
public class TokenController {

	@Autowired
	private TokenService tokenService;

	@PostMapping("/login")
	@Operation(summary = "User login.", description = "Endpoint used to authenticate an user and returns a JWT token.", security = {})
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successful login", content = @Content(schema = @Schema(implementation = LoginResponseDto.class))),
			@ApiResponse(responseCode = "401", description = "Invalid user or password") })
	public ResponseEntity<LoginResponseDto> login(@RequestBody LoginRequestDto loginRequestDto) {
		return tokenService.login(loginRequestDto);
	}

	@GetMapping("/me")
	@Operation(summary = "User info.", description = "Endpoint used to get user info based on login token.", security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Valid credentials", content = @Content(schema = @Schema(implementation = AuthMeResponseDto.class))),
			@ApiResponse(responseCode = "401", description = "User not Found || Invalid token") })
	public ResponseEntity<?> getMe(@RequestHeader("Authorization") String authHeader) {
		if (authHeader != null && authHeader.startsWith("Bearer ")) {
			String token = authHeader.substring(7);
			return tokenService.getMe(token);
		} else {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Authorization header");
		}
	}
}
