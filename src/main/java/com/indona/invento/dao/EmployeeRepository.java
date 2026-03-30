package com.indona.invento.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.indona.invento.entities.EmployeeEntity;
@Repository
public interface EmployeeRepository extends JpaRepository<EmployeeEntity, Long> {

	EmployeeEntity findByEmployeeName(String skuCode); 

}