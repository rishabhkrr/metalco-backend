package com.indona.invento.dao;

import com.indona.invento.entities.ItemEnquiryMoq;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemEnquiryMoqRepository extends JpaRepository<ItemEnquiryMoq, Long> {

}