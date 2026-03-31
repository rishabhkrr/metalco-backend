package com.indona.invento.entities;


import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity; 
import jakarta.persistence.GeneratedValue; 
import jakarta.persistence.GenerationType; 
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor; 
import lombok.Data;
import lombok.NoArgsConstructor; 
 
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "USER_ROLES")
public class UserRolesEntity {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="role_id")
    private Long id;
	
	@Column(name="role_name")
    private String roleName;
	
	@Column(name="role_key")
    private String roleKey;

	@Column(name="datetime")
    private Date dateTime;
	
	@PrePersist
	public void addTimestamp() {
		dateTime = new Date();
	}
}
