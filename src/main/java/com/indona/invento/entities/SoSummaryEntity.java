package com.indona.invento.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "so_summary")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SoSummaryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userId;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Kolkata")
    private LocalDateTime timestamp;
    private String quotationNo;
    private String soNumber;
    private String unit;
    private String customerPoNo;
    private String customerCode;
    private String customerName;
    private String customerPhoneNo;
    private String customerEmail;
    private String marketingExecutiveName;
    private String managementAuthority;
    private Boolean packingStatus;


    @OneToMany(mappedBy = "summary", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<SoSummaryItemEntity> items;
}
