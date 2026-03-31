package com.indona.invento.dao;

import com.indona.invento.entities.PackingListJobWorkEntity;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PackingListJobWorkRepository extends JpaRepository<PackingListJobWorkEntity, Long> {
	  List<PackingListJobWorkEntity> findByPackingListNumber(String packingListNumber);
}
