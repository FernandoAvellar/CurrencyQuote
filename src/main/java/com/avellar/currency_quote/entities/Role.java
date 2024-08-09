package com.avellar.currency_quote.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "tb_roles")
@Getter
@Setter
public class Role {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "role_id")
	private Long roleId;
	private String name;

	public enum Values {
		ROLE_ADMIN(1L), ROLE_BASIC(2L);

		long roleId;

		Values(long roleId) {
			this.roleId = roleId;
		}

		public long getRoleId() {
			return roleId;
		}
	}
}
