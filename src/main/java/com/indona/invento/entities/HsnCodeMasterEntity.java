package com.indona.invento.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "hsn_code_master")
public class HsnCodeMasterEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String materialType;       // Dropdown (Alu, SS, CU, BR)
    private String productCategory;    // Dropdown (FLAT BAR, COIL, etc.)
    private String hsnCode;            // Manual entry
    private String description;        // Description of goods/services category
    private LocalDate effectiveDate;   // Manual entry
    private String previousHsnCode;    // Auto when updated
    private String gstRate;        // GST Rate (e.g., 5.0, 12.0, 18.0)
    private LocalDate gstEffectiveDate; // GST Effective Date
    private String status;             // Pending / APPROVED / REJECTED (default: Pending)
    private String lastgstRate;      // Auto when updated
}
