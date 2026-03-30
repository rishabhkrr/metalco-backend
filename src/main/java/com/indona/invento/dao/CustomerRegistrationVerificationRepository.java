package com.indona.invento.dao;

import com.indona.invento.entities.CustomerRegistrationVerificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRegistrationVerificationRepository extends JpaRepository<CustomerRegistrationVerificationEntity, Long> {
    boolean existsByCustomerEmail(String customerEmail);
}
