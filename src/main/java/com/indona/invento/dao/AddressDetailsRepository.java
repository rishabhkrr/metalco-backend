package com.indona.invento.dao;

import com.indona.invento.entities.AddressDetailsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface AddressDetailsRepository extends JpaRepository<AddressDetailsEntity, Long> {
}
