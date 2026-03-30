package com.indona.invento.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

/**
 * DTO for Gate Entry Summary with consolidated view
 * Contains data from both gate_inward and gate_entry_packing_and_dispatch tables
 * Includes multiple MEDC Nos, MEDCI Nos, PO Numbers
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GateInwardSummaryDTO {

    private Long id;

    // Source table indicator (GATE_INWARD or GATE_ENTRY_PACKING_DISPATCH)
    private String source;

    // Vehicle & Driver Info
    private String vehicleNumber;
    private String driverName;

    // Gate Entry Info
    private String gateEntryRefNo;  // gatePassRefNumber / gateEntryRefNoPackingAndDispatch
    private String purpose;         // purpose / purposePackingAndDispatch
    private String mode;            // mode / modePackingAndDispatch
    private String status;          // status / vehicleOutStatusPackingAndDispatch

    // Timestamps
    private Date gateInTime;        // timeStamp
    private Date gateOutTime;       // gateOutTime / timeStampPackingAndDispatch

    // Invoice & DC Info
    private String invoiceNumber;   // invoiceNumber / invoiceNumberPackingAndDispatch
    private String dcNumber;        // dcNumber (only in gate_inward)
    private String eWayBillNumber;  // eWayBillNumber / ewayBillNumberPackingAndDispatch

    // Multiple PO Numbers (only in gate_inward)
    private List<String> poNumbers;

    // Multiple MEDC Numbers (can be comma-separated in entity, we'll split)
    private List<String> medcNumbers;

    // Multiple MEDCI Numbers (can be comma-separated in entity, we'll split)
    private List<String> medciNumbers;

    // MEDCP Number
    private String medcpNumber;     // medcpNumber / medcpNumberPackingAndDispatch

    // Unit Info
    private String unitCode;        // unitCode / unitPackingAndDispatch

    // Status fields
    private String vehicleWeighmentStatus;  // vehicleWeighmentStatus / vehicleWeighmentStatusPackingAndDispatch
    private String materialUnloadingStatus; // materialUnloadingStatus (only in gate_inward)
    private String vehicleOutStatus;        // vehicleOutStatus (only in gate_inward)
}


