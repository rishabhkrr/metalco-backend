package com.indona.invento.entities;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "bin_damage")
public class BinDamageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "combination_id")
    private Long id;

    @Column(name = "bin_name")
    private String binName; 

    @Column(name = "part_number")
    private String partNumber;
    
    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "created_date")
    private Date createdDate;

    @PrePersist
    public void addTimestamp() {
        createdDate = new Date();
    }
}
