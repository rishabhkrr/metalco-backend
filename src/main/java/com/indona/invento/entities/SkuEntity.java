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
@Table(name = "SKU")
public class SkuEntity {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="sku_id")
    private Long id;
	
	@Column(name="category_id")
    private Long categoryId;
	
	@Column(name="sub_category_id")
    private Long subCategoryId;
	
	@Column(name="sku_name")
    private String skuName;
	
	@Column(name="sku_code")
    private String skuCode;
	
	@Column(name="sku_uom")
    private String skuUom;
	
	@Column(name="sku_image")
    private String skuImage;
	
	@Column(name="sku_supplier")
    private String skuSupplier;
	
	@Column(name="sku_description")
    private String description;
	
	@Column(name="sku_quantity")
    private Long skuQuantity;
	
	@Column(name="sku_qrcode")
    private String skuQrcode;
	
	@Column(name="sku_lead_time")
    private String skuLeadTime;
	
	@Column(name="sku_min")
    private Long skuMin;
	
	@Column(name="sku_max")
    private Long skuMax;
	
	@Column(name="sku_unit_cost")
    private Long skuUnitCost;
	
	@Column(name="sku_reorder_point")
    private Long skuReorderPoint;
	
	@Column(name="sku_class")
    private String skuClass;
	
	@Column(name="sku_moq")
    private String skuMoq;
	
	@Column(name="service")
    private String servicePrice;
	
	@Column(name="retail")
    private String retailPrice;
	
	@Column(name="pgm")
    private String pgmPrice;
	
	@Column(name="dealer")
    private String dealerPrice;
	
	@Column(name="status")
    private Integer status = 1;
	
	@Column(name="created_by")
    private String createdBy;

	@Column(name="datetime")
    private Date dateTime;
    
	@PrePersist
	public void addTimestamp() {
		dateTime = new Date();
	}
}
