package com.indona.invento.dao;

import com.indona.invento.entities.GRNJobWorkEntity;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface GRNJobWorkRepository extends JpaRepository<GRNJobWorkEntity, Long> {

	@Query("SELECT g.invoiceNumber FROM GRNJobWorkEntity g " + "WHERE LOWER(g.materialUnloadingStatus) = 'pending' "
			+ "AND g.invoiceNumber IS NOT NULL")
	List<String> findPendingInvoiceNumbersIgnoreCase();

	List<GRNJobWorkEntity> findByInvoiceNumber(String invoiceNumber);
	
	 GRNJobWorkEntity findByMedcNumber(String medcNumber);

    List<GRNJobWorkEntity> findAllByMedcNumber(String medcNumber);
}