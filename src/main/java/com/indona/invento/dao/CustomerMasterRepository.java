package com.indona.invento.dao;

import com.indona.invento.entities.CustomerMasterEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CustomerMasterRepository extends JpaRepository<CustomerMasterEntity, Long> {
    boolean existsByCustomerCode(String customerCode);
    boolean existsByCustomerNameIgnoreCase(String customerName);
    CustomerMasterEntity findByCustomerCode(String customerCode);

    @Query("SELECT c FROM CustomerMasterEntity c LEFT JOIN FETCH c.contactDetails WHERE c.customerCode = :customerCode")
    CustomerMasterEntity findByCustomerCodeWithContacts(@Param("customerCode") String customerCode);
    
    @Query("SELECT MAX(c.customerCode) FROM CustomerMasterEntity c")
    String findMaxCustomerCode();

}
