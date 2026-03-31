package com.indona.invento.dto;

import java.sql.Date;
import java.util.List;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor; 
import lombok.Data; 
import lombok.NoArgsConstructor; 
  
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransferSkuDto { 
	
	private String customerName;
	
	private String customerPhone;
  
	private String transferNumber;
	
    private String transferType;

    private String transactionType;

    private String invoiceRemark;

    private String jobCardNo;

    private String advancePayment;

    private String paymentType;

    private String creditTerms;

    private String discountAmount;

    private String taxPercentage;

    private String paymentStatus;

    private String totalBill;

    private String seqNo;

    private String returnDue;

    private String returnType;

    private String labourCharges;

    private String labourChargesDescp;

    private Date recievingDate;

    private List<TransferSkuDetailsDto> transferSkuList;

    private Long fromStore;

    private Long toStore;

    private String transferStage;

    // ========== GRN Related Fields ==========
    private String grnRefNumber;

    private String invoiceNumber;

    private String unit;

    private String itemDescription;

    private String sectionNumber;

    private String productCategory;

    private String brand;

    private String grade;

    private String temper;

    // GRN Quantities
    private Double grnQuantityNetWeight;

    private String grnQuantityNetWeightUom;

    private Integer grnQuantityNo;

    private String grnQuantityNoUom;

    // Added Quantities (from Add Bundles)
    private Double addedQuantityNetWeight;

    private String addedQuantityNetWeightUom;

    private Integer addedQuantityNo;

    private String addedQuantityNoUom;

    private Integer numberOfBundles;

    // Warehouse Storage Fields
    private String currentStore;

    private String recipientStore;

    private String storageArea;

    private String rackColumnBinNumber;

}