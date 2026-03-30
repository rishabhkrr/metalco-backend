package com.indona.invento.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobworkMergedDTO {
	@JsonFormat(pattern = "dd-MMM-yyyy hh:mm a", timezone = "Asia/Kolkata")
	private LocalDateTime timestamp;
	private String soNumber;
	private String lineNumber;
	private String unit;
	private String customerCode;
	private String customerName;

	private String orderType;
	private String productCategory;
	private String itemDescription;
	private String brand;
	private String grade;
	private String temper;
	private String dimension;
	private String productionStrategy;

	private BigDecimal quantityKg;
	private String uomKg;
	private Integer quantityNo;
	private String uomNo;

	private String medcNumber;
	private String packingListNumber;

	private String subContractorCode;
	private String subContractorName;
	private String subContractorBillingAddress;
	private String subContractorShippingAddress;
	
	private String grnDimension;
	private Double receivedQuantityKg;
	private String grnUomKg;
	private Integer receivedQuantityNos;
	private String grnUomNo;

	private Double scrapQuantityKg;
	private Integer scrapQuantityNo;

	private Double jobworkRate;
	private Double jobworkValue;

	private String packingStatus;
	private String soStatus;
	private String jobWorkStatus;

	// Batch Details for drill-down
	private List<JobworkBatchDetailDTO> batchDetails;
}

