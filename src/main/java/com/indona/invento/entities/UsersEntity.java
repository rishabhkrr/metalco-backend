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
@Table(name = "USERS")
public class UsersEntity {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="user_id")
    private Long id;
	
	@Column(name="user_name")
    private String userName;
	
	@Column(name="user_email")
    private String userEmail;
	
	@Column(name="password")
    private String password;
	
	@Column(name="role_id")
    private Long roleId;
	
	@Column(name="store_id")
    private Long storeId; 
	
	@Column(name="warehouse_id")
    private Long warehouseId;
	
	@Column(name="datetime")
    private Date dateTime;
	
	@PrePersist
	public void addTimestamp() {
		dateTime = new Date();
	}
}
