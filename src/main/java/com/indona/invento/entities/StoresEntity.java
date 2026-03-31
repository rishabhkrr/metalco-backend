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
@Table(name = "STORES")
public class StoresEntity {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="store_id")
    private Long id;
	
	@Column(name="store_name")
    private String storeName;
	
	@Column(name="store_code")
    private String storeCode;
	
	@Column(name="status")
    private Integer status = 1;
	
	@Column(name="created_by")
    private String createdBy;
	
	@Column(name="datetime")
    private Date dateTime;
    
	@PrePersist
	public void addTimestamp() {
		dateTime = new Date();
	}
	
}