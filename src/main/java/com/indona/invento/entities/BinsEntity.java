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
@Table(name = "BINS")
public class BinsEntity {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="bin_id")
    private Long id;
	
	@Column(name="bin_name")
    private String binName;
	
	@Column(name="bin_location")
    private String binLocation;
	
	@Column(name="store_id")
    private Long storeId;
	
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