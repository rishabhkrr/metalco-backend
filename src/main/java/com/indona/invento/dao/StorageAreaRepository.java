package com.indona.invento.dao;



import com.indona.invento.entities.StorageAreaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StorageAreaRepository extends JpaRepository<StorageAreaEntity, Long> {
    boolean existsByStorageAreaName(String storageAreaName);
}
