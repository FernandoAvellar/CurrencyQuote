package com.avellar.currency_quote.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.avellar.currency_quote.dto.CreateUserDto;
import com.avellar.currency_quote.entities.Currency;
import com.avellar.currency_quote.entities.User;
import com.avellar.currency_quote.services.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/users")
@Tag(name = "User API")
public class UserController {

	@Autowired
	private UserService userService;

	@PostMapping("/register")
	@Operation(summary = "Create a new user.", description = "Endpoint to create a new user.", security = {})
	@ApiResponses(value = { @ApiResponse(responseCode = "201", description = "User created successfully"),
			@ApiResponse(responseCode = "422", description = "User already exists.") })
	public ResponseEntity<?> createUser(@RequestBody CreateUserDto createUserDto) {
		return userService.createUser(createUserDto);
	}

	@GetMapping()
	@Operation(summary = "List all created users.", description = "Endpoint to get a list of all registered users.", security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Users list retrieved with success", content = @Content(schema = @Schema(implementation = User.class))),
			@ApiResponse(responseCode = "401", description = "Invalid token") })
	public ResponseEntity<List<User>> listUsers() {
		return userService.listUsers();
	}

	@GetMapping("/favorites")
	@Operation(summary = "Get favorite currencies.", description = "Endpoint to get favorite currencies of the authenticated user.", security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Favorite currencies retrieved with success", content = @Content(schema = @Schema(implementation = Currency.class))),
			@ApiResponse(responseCode = "401", description = "Invalid token"),
			@ApiResponse(responseCode = "404", description = "User not found") })
	public ResponseEntity<List<Currency>> getFavoriteCurrencies(@RequestHeader("Authorization") String token) {
		List<Currency> favorites = userService.getFavoriteCurrencies(token);
		return ResponseEntity.ok(favorites);
	}

	@PostMapping("/favorites")
	@Operation(summary = "Update favorite currencies.", description = "Endpoint to update favorite currencies of the authenticated user.", security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponses(value = {
			@ApiResponse(responseCode = "204", description = "Favorite currencies updated successfully"),
			@ApiResponse(responseCode = "401", description = "Invalid token"),
			@ApiResponse(responseCode = "404", description = "User not found") })
	public ResponseEntity<Void> updateFavoriteCurrencies(@RequestHeader("Authorization") String token,
			@RequestBody List<String> favoriteCurrencyCodes) {
		userService.updateFavoriteCurrencies(token, favoriteCurrencyCodes);
		return ResponseEntity.noContent().build();
	}
}
