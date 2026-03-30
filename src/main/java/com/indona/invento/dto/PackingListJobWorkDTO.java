package com.indona.invento.dto;

import lombok.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PackingListJobWorkDTO {
	private String soNumber;
	private String lineNumber;
	private String unit;
	private String customerCode;
	private String customerName;
	private String packingStatus;
	private String orderType;
	private String productCategory;
	private String itemDescription;
	private String brand;
	private String grade;
	private String temper;
	private String dimension;

	private BigDecimal quantityKg;
	private String uomKg;

	private Integer quantityNo;
	private String uomNo;
}
