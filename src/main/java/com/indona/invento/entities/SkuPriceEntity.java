package com.indona.invento.entities;


import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity; 
import jakarta.persistence.GeneratedValue; 
import jakarta.persistence.GenerationType; 
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor; 
import lombok.Data;
import lombok.NoArgsConstructor; 
 
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "PRICE_MASTER")
public class SkuPriceEntity {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="id")
    private Long id;
	
	@Column(name="sku_id")
    private Long skuId;
	
	@Column(name="service")
    private String servicePrice;
	
	@Column(name="retail")
    private String retailPrice;
	
	@Column(name="pgm")
    private String pgmPrice;
	
	@Column(name="dealer")
    private String dealerPrice;
}