package com.avellar.currency_quote.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.avellar.currency_quote.entities.RefreshToken;
import com.avellar.currency_quote.entities.User;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
	Optional<RefreshToken> findByToken(String token);
	Optional<RefreshToken> findByUser(User user);
    void deleteByUser(User user);
}
