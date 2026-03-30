package com.indona.invento.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "material_request_summary_header")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaterialRequestSummaryHeader {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime timestamp;
    private String mrNumber;
    private String unitCode;
    private String unitName;
    private String requestingUnit;
    private String deliveryAddress;
    private String Status;
    private String requestingUnitUnitCode;

    @OneToMany(mappedBy = "summaryHeader", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MaterialRequestSummaryItem> items;
}
