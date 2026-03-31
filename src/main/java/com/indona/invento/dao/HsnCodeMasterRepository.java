package com.indona.invento.dao;

import com.indona.invento.entities.HsnCodeMasterEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface HsnCodeMasterRepository extends JpaRepository<HsnCodeMasterEntity, Long> {
	Optional<HsnCodeMasterEntity> findByMaterialTypeAndProductCategory(String materialType, String productCategory);

	Optional<HsnCodeMasterEntity> findByHsnCode(String hsnCode);

	@Query("SELECT h.hsnCode FROM HsnCodeMasterEntity h WHERE h.productCategory = :productCategory")
	String findHsnCodeByProductCategory(@Param("productCategory") String productCategory);
}
