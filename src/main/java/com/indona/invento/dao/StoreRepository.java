package com.indona.invento.dao;


import com.indona.invento.entities.StoreEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StoreRepository extends JpaRepository<StoreEntity, Long> {
    boolean existsByStoreName(String storeName);
}
