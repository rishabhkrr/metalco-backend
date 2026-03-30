package com.indona.invento.dao;

import com.indona.invento.entities.ContactDetailsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContactDetailsRepository extends JpaRepository<ContactDetailsEntity, Long> {
}
