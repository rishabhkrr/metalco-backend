package com.indona.invento.dao;

import com.indona.invento.entities.SalesAuthority;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SalesAuthorityRepository extends JpaRepository<SalesAuthority, Long> {
    boolean existsByName(String name);
}
