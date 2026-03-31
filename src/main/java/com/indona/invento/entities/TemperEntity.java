package com.indona.invento.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tempers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TemperEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "temper_value", unique = true, nullable = false)
    private String temperValue;
}
