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
@Table(name = "ADJUSTMENTS")
public class AdjustmentEntity {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="id")
    private Long id;
	
	@Column(name="sku_name")
    private String skuName;
	
	@Column(name="sku_id")
    private Long skuId;
	
	@Column(name="quantity_diff")
    private Long quantityDiff;
	
	@Column(name="store_name")
    private String storeName;
	
	@Column(name="type")
    private String type;
	
	@Column(name="store_id")
    private Long storeId;
	
	@Column(name="ref_no")
    private String refNo;
	
	@Column(name="stock_id")
    private Long stockId;
	
	@Column(name="status")
    private Long status = -1L;

	@Column(name="created_by")
    private String createdBy;

	@Column(name="datetime")
    private Date dateTime;
    
	@PrePersist
	public void addTimestamp() {
		dateTime = new Date();
	}
}