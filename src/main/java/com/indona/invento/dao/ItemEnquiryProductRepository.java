package com.indona.invento.dao;

import com.indona.invento.entities.ItemEnquiryProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemEnquiryProductRepository extends JpaRepository<ItemEnquiryProduct, Long> {

}