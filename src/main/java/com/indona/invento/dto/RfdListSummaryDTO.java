package com.indona.invento.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RfdListSummaryDTO {
    private String packingListNumber;  // RFD List Number
    private LocalDateTime timestamp;
    private String productionStrategy; // = transferType (Inhouse/Jobwork)
    private String unitName;           // = unit
    private String customerCode;
    private String customerName;
    private String customerBillingAddress;
    private String customerShippingAddress;
    private String customerPoNumber;
    private String customerPoDate;
    private String vehicleNumber;
    private String dispatchThrough;
    private int itemCount;             // Number of SO/Line items in this RFD List
}
