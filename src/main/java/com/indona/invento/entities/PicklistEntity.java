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
@Table(name = "PICKLIST")
public class PicklistEntity {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="pick_id")
    private Long id;
	
	@Column(name="stockin_id")
    private Long stockInId;
	
	@Column(name="sku_id")
    private Long skuId;
	
	@Column(name="quantity")
    private Long quantity;
	
	@Column(name="ordered_quantity")
    private Long orderedQuantity;
	
	@Column(name="bin_id")
    private Long binId;
	
	@Column(name="store_id")
    private Long storeId;
	
	@Column(name="ref_no")
    private Long refNo;
	
	@Column(name="type")
    private String type;
	
	@Column(name="picked")
    private String picked;
	
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