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
@Table(name = "PGM")
public class PgmEntity {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="pgm_id")
    private Long id;
	
	@Column(name="pgm_name")
    private String pgmName;
	
	@Column(name="pgm_phone")
    private String phone;
	
	@Column(name="appointed_date")
    private Date appointmentDate;
	
	@Column(name="appointed_by")
    private String appointedBy;
	
	@Column(name="status")
    private Integer status = 1;
	
	@Column(name="datetime")
    private Date dateTime;
	
	@PrePersist
	public void addTimestamp() {
		dateTime = new Date();
		appointmentDate = new Date();
	}
	
}