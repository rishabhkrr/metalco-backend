package com.indona.invento.dto;

import lombok.*;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewSalesOrderDTO {
    private String quotationNo;
    private String userId;
    private String unit;
    private String customerPoNo;
    private String customerPoFile;
    private String customerCode;
    private String customerName;
    private String customerPhone;
    private String customerEmail;
    private String billingAddress;
    private String shippingAddress;
    private Boolean sameAsBillingAddress;
    private String marketingExecutiveName;
    private String managementAuthority;
    private boolean packingRequired;
    private String creditPeriod;
    private String remark;
    private boolean acknowledgementSent;
    private boolean approvalLinkSent;
    private LocalDate targetDateOfDispatch;
    private String pdfLink;
    private PackingInstructionDTO packingInstruction;

 //   private NewPackingInstructionDTO packingInstruction;

    private List<SalesOrderItemDTO> items;

}
