package com.avellar.currency_quote.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.avellar.currency_quote.entities.User;

public interface UserRepository extends JpaRepository<User, Long> {
	Optional<User> findByUsername(String username);
}
