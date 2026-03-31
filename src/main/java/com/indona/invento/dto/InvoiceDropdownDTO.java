package com.indona.invento.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class InvoiceDropdownDTO {
    private String invoiceNumber;
    private String mode;
}
