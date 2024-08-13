package com.avellar.currency_quote.entities;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.security.crypto.password.PasswordEncoder;

import com.avellar.currency_quote.dto.LoginRequestDto;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tb_users")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "user_id")
	private Long id;

	@Column(unique = true, nullable = false)
	private String username;
	@Column(nullable = false)
	private String password;

	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(name = "tb_users_roles", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
	private Set<Role> roles = new HashSet<>();

	@ManyToMany
	@JoinTable(name = "user_favorite_currencies", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "currency_id"))
	private List<Currency> favoriteCurrencies = new ArrayList<>();
	
	@OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
	@JsonIgnore
    private RefreshToken refreshToken;

	public boolean isLoginCorrect(LoginRequestDto loginRequest, PasswordEncoder passwordEncoder) {
		return passwordEncoder.matches(loginRequest.password(), this.password);
	}
	
	public void setRefreshToken(RefreshToken refreshToken) {
        if (refreshToken != null) {
            refreshToken.setUser(this);
        }
        this.refreshToken = refreshToken;
    }
}