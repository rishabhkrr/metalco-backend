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
@Table(name = "STOCKIN")
public class StockinEntity {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="stock_id")
    private Long id;
	
	@Column(name="sku_name")
    private String skuName;
	
	@Column(name="sku_id")
    private Long skuId;
	
	@Column(name="sku_quantity")
    private Long skuQuantity;
	
	@Column(name="sku_quantity_hold")
    private Long skuHold = 0L;
	
	@Column(name="store_name")
    private String storeName;
	
	@Column(name="store_id")
    private Long storeId;
	
	@Column(name="prev_store_id")
    private String prevStoreId;
	
	@Column(name="sto_id")
    private String stoId;
	
	@Column(name="type")
    private String type;
	
	@Column(name="transfer_number")
    private String transferNumber;
	
	@Column(name="bin_id")
    private Long binId;
	
	@Column(name="prev_bin_id")
    private Long prevBinId;
	
	@Column(name="status")
    private Integer status = 1;
	
	@Column(name="created_by")
    private String createdBy;
	
	@Column(name="soft_delete")
    private Integer deleteFlag = 0;
	
	@Column(name="delete_remark")
    private String deleteRemark;

	@Column(name="datetime")
    private Date dateTime;
	
	@Column(name="recieving_date")
    private Date recievingDate;
    
	@PrePersist
	public void addTimestamp() {
		dateTime = new Date();
	}
}