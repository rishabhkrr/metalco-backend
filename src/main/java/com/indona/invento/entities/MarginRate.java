package com.indona.invento.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "margin_rate")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MarginRate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String days;     // Credit Period Duration (in days)

    @Column(nullable = false)
    private Double marginPercent;   // Margin Rate %

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_margin_id")
    @JsonBackReference
    private ProductMargin productMargin;
}
