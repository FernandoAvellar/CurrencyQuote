package com.avellar.currency_quote.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.avellar.currency_quote.dto.RegisterUserDto;
import com.avellar.currency_quote.entities.User;
import com.avellar.currency_quote.services.UserService;

@RestController
@RequestMapping("/users")
public class UserController {

	@Autowired
	private UserService userService;

	@PostMapping("/register")
	public ResponseEntity<Void> newUser(@RequestBody RegisterUserDto registerUserDto) {
		return userService.newUser(registerUserDto);
	}

	@GetMapping()
	public ResponseEntity<List<User>> listUsers() {
		return userService.listUsers();
	}
}
