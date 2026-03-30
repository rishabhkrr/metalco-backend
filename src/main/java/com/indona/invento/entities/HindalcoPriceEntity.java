package com.indona.invento.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity
@Table(name = "hindalco_price")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HindalcoPriceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Kolkata")
    private Date priceDate;

    private Double price;

    private String uom;

    private String pricePdfPath;
}
