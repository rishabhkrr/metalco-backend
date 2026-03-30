package com.indona.invento.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

@Data
public class PackingListJobWorkResponseDTO {
    private Long id;
    @JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss", timezone = "Asia/Kolkata")
    private LocalDateTime timestamp;
    private String packingListNumber;
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
    private String hsnCode;


    
    private BigDecimal itemPrice;
    
    private String emailId;
    private String unitAddress;
    private String pan;
    private String gstOrUin;
    private String gstStateCode;
    private String state;
}
