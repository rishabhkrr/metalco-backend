package com.indona.invento.dao;

import com.indona.invento.entities.SubContractorAddressEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SubContractorAddressRepository extends JpaRepository<SubContractorAddressEntity, Long> {
	@Query("SELECT a FROM SubContractorAddressEntity a WHERE a.subContractor.subContractorCode = :code AND a.Primary = true")
	SubContractorAddressEntity findPrimaryAddressBySubContractorCode(@Param("code") String code);

	@Query("SELECT a FROM SubContractorAddressEntity a WHERE a.subContractor.subContractorName = :name AND a.Primary = true")
	SubContractorAddressEntity findPrimaryAddressBySubContractorName(@Param("name") String name);
}