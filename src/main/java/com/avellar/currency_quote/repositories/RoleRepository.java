package com.avellar.currency_quote.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.avellar.currency_quote.entities.Role;

public interface RoleRepository extends JpaRepository<Role, Long> {
	<Optional>Role findByName(String name);
}
