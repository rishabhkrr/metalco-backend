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
@Table(name = "STOCK_TRN_SKU")
public class StockTransferSkuEntity {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="sku_trn_id")
    private Long id;
	
	@Column(name="transfer_number")
    private String transferNumber;
	
	@Column(name="transfer_sku_code")
    private String transferSkuCode;
	
	@Column(name="transfer_quantity")
    private String transferQuantity;
	
	@Column(name="hold_quantity")
    private String holdQuantity;
	
	@Column(name="recieved_quantity")
    private String recievedQuantity;
	
	@Column(name="dispatch_quantity")
    private String dispatchQuantity;
	
	@Column(name="returned_quantity")
    private String returnedQuantity;
	
	@Column(name="sku_price")
    private String skuPrice;
	
	@Column(name="sku_total")
    private String skuTotal;
	
	@Column(name="picked")
	private String picked;
	
	@Column(name="transfer_sku_name")
    private String transferSkuName;
	
	@Column(name="datetime")
    private Date dateTime;
    
	@PrePersist
	public void addTimestamp() {
		dateTime = new Date();
	}
}
