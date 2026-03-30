package com.indona.invento.dto;

import lombok.Data;

import java.util.Date;

@Data
public class GateInwardPartialUpdateDTO {
    private String unitCode;
    private String medcNumber;
    private String purpose;
    private String medciNumber;
    private String mode;
    private String invoiceNumber;
    private String vehicleNumber;
    private String driverName;
    private String status;
    private Date gateOutTime;
    private String dcNumber;
    private String eWayBillNumber;
    private String vehicleWeighmentStatus;
    private String materialUnloadingStatus;
    private String vehicleOutStatus;
}

