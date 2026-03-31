package com.indona.invento.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "salesman_master")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalesmanMasterEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String unitCode;         // Auto from User Master
    private String unitName;         // Auto from User Master
    private String userId;           // Dropdown & Select
    private String userName;         // From User Master
    private String department;       // Auto
    private String designation;      // Auto
    private LocalDate dateOfJoining;
    private LocalDate dateOfEnding; // Manual
    private String userIdStatus;     // Auto
    private String modulesWithAccess;// Auto
    private String status;           // Pending / APPROVED / REJECTED (default: Pending)

    @OneToMany(mappedBy = "salesman", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SalesmanIncentiveEntity> incentiveRates;
}
