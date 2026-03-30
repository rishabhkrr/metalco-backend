package com.indona.invento.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Entity
@Table(name = "grade_density")
public class GradeDensityEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String grade; // Altitude in meters
    @Column(precision = 20, scale = 10)
    private BigDecimal density; // Density in kg/m^3

    // Getters and setters
}
