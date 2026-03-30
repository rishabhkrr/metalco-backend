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
@Table(name = "STOCK_RETURNS")
public class StockReturnEntity {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="return_id")
    private Long id;
	
	@Column(name="sku_quantity")
    private Long skuQuantity;
	
	@Column(name="seq_no")
    private String seqNo;
	
	@Column(name="ref_no")
    private String refNo;

	@Column(name="return_due")
    private String returnDue;
	
	@Column(name="return_type")
    private String returnType;
	
	@Column(name="store_name")
    private String storeName;
	
	@Column(name="customer_name")
    private String customerName;
	
	@Column(name="customer_phone")
    private String customerPhone;
	
	@Column(name="transfer_type")
    private String transferType;
	
	@Column(name="transaction_type")
    private String transactionType;
	
	@Column(name="job_card_no")
    private String jobCardNo;
	
	@Column(name="labour_charges")
    private String labourCharges;
	
	@Column(name="labour_charges_desp")
    private String labourChargesDescp;
	
	@Column(name="advance_payment")
    private String advancePayment;
	
	@Column(name="payment_type")
    private String paymentType;
	
	@Column(name="payment_terms")
    private String creditTerms;
	
	@Column(name="discount_amount")
    private String discountAmount;
	
	@Column(name="tax_percentage")
	private String taxPercentage;
	
	@Column(name="total_bill")
    private String totalBill;
	
	@Column(name="payment_status")
    private String paymentStatus;
	
	@Column(name="store_id")
    private Long storeId;
	
	@Column(name="transfer_stage")
    private String transferStage;
	
	@Column(name="bin_id")
    private Long binId;
	
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
    
	@PrePersist
	public void addTimestamp() {
		dateTime = new Date();
	}
}