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
@Table(name = "EMPLOYEES")
public class EmployeeEntity {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="employee_id")
    private Long id;
	
	@Column(name="employee_name")
    private String employeeName;
	
	@Column(name="employee_phone")
    private String phone;
	
	@Column(name="status")
    private Integer status = 1;
	
	@Column(name="datetime")
    private Date dateTime;
	
	@PrePersist
	public void addTimestamp() {
		dateTime = new Date();
	}
	
}