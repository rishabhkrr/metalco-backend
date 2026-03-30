package com.indona.invento.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "salesman_incentive_rates")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalesmanIncentiveEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String materialGradeAndTemper; // From Item Master
    private Double ratePerKg;              // ₹
    private Double lapseInterestRate;      // %
    private LocalDate effectiveDate;

    @ManyToOne
    @JoinColumn(name = "salesman_id")
    @JsonIgnore
    private SalesmanMasterEntity salesman;
}

