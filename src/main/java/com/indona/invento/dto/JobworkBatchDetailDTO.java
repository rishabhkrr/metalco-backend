package com.indona.invento.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobworkBatchDetailDTO {
    private String itemDescription;
    private String itemDimension;
    private String batchNumber;       // GRN Ref Number

    @JsonFormat(pattern = "dd-MMM-yyyy hh:mm a", timezone = "Asia/Kolkata")
    private LocalDateTime dateOfInward;  // GRN Timestamp

    private Double sentQuantityKg;
    private Integer sentQuantityNo;
    private BigDecimal itemPriceMedc;  // Item Price from Delivery Challan (MEDC)
    private Double receivedQuantityKg;
    private Integer receivedQuantityNo;
    private Double scrapQuantityKg;
    private Integer scrapQuantityNo;
}
