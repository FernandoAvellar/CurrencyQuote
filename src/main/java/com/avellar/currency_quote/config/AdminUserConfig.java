package com.avellar.currency_quote.config;

import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.avellar.currency_quote.entities.Role;
import com.avellar.currency_quote.entities.User;
import com.avellar.currency_quote.repositories.RoleRepository;
import com.avellar.currency_quote.repositories.UserRepository;

import jakarta.transaction.Transactional;

@Configuration
public class AdminUserConfig implements CommandLineRunner {

	@Autowired
	private RoleRepository roleRepository;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;

	@Override
	@Transactional
	public void run(String... args) throws Exception {

		Role roleAdmin = roleRepository.findByName(Role.Values.ROLE_ADMIN.name());
		Optional<User> userAdmin = userRepository.findByUsername("admin");

		userAdmin.ifPresentOrElse(user -> {
			System.out.println("#### User admin já existe ####");
		}, () -> {
			User user = new User();
			user.setUsername("admin");
			user.setPassword(passwordEncoder.encode("123"));
			user.setRoles(Set.of(roleAdmin));
			userRepository.save(user);
		});
	}
}