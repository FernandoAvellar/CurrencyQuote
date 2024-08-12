package com.avellar.currency_quote.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.avellar.currency_quote.dto.ChangePasswordDto;
import com.avellar.currency_quote.dto.CreateUserDto;
import com.avellar.currency_quote.dto.UpdateRolesDto;
import com.avellar.currency_quote.dto.UpdatePasswordDto;
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

	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@GetMapping()
	@Operation(summary = "List all created users.", description = "Endpoint to get a list of all registered users.", security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Users list retrieved with success", content = @Content(schema = @Schema(implementation = User.class))),
			@ApiResponse(responseCode = "401", description = "Invalid token") })
	public ResponseEntity<List<User>> listUsers() {
		return userService.listUsers();
	}
	
	@DeleteMapping("/{username}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Delete any user by Admin.", description = "Endpoint to delete a user by username.  (Only Admin user)", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "User deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Invalid token"),
            @ApiResponse(responseCode = "404", description = "User not found")})
    public ResponseEntity<Void> deleteUser(@PathVariable String username) {
        userService.deleteUser(username);
        return ResponseEntity.noContent().build();
    }
	
	@PutMapping("/updateuserroles")
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@Operation(summary = "Manage user's roles", description = "Endpoint to allow an Admin user to manage the roles of a user.", security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponses(value = {
	    @ApiResponse(responseCode = "200", description = "Roles updated successfully"),
	    @ApiResponse(responseCode = "400", description = "Invalid request"),
	    @ApiResponse(responseCode = "403", description = "Operation not authorized, only allowed to Admin User"),
	    @ApiResponse(responseCode = "404", description = "User not found")})
	public ResponseEntity<Void> updateUserRoles(@RequestBody UpdateRolesDto updateRolesDto) {
	    userService.updateUserRoles(updateRolesDto);
	    return ResponseEntity.ok().build();
	}

	@PutMapping("/{username}/password/change")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Update user's password by Admin.", description = "Endpoint to allow Admin user to update any user's password.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password updated successfully"),
            @ApiResponse(responseCode = "403", description = "Operation not authorized, only allowed to Admin User"),
            @ApiResponse(responseCode = "404", description = "User not found")})
    public ResponseEntity<Void> changePasswordByAdminUser(@PathVariable String username, @RequestBody UpdatePasswordDto passwordDto) {
        userService.changePasswordByAdminUser(username, passwordDto);
        return ResponseEntity.ok().build();
    }
	
	@PutMapping("/changepassword")
	@Operation(summary = "Change user's password.", description = "Endpoint to allow users to change their password if they know the current one.", security = {})
	@ApiResponses(value = {
	        @ApiResponse(responseCode = "200", description = "Password changed successfully"),
	        @ApiResponse(responseCode = "401", description = "Invalid current password"),
	        @ApiResponse(responseCode = "404", description = "User not found")
	})
	public ResponseEntity<Void> changePassword(@RequestBody ChangePasswordDto passwordDto) {
	    userService.changePassword(passwordDto);
	    return ResponseEntity.ok().build();
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
