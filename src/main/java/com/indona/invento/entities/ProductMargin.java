package com.indona.invento.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "product_margin")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductMargin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String materialType;

    @Column(nullable = false)
    private String productCategory;

    private String userId;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MMM-yyyy hh:mm a")
    private LocalDateTime timestamp;

    // Status as plain String (default "PENDING")
    @Column(nullable = false)
    private String status ;

    @OneToMany(mappedBy = "productMargin", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<MarginRate> marginRates;
}

