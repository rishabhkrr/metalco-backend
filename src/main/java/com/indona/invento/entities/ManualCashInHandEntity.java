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
@Table(name = "manual_cash_in_hand")
public class ManualCashInHandEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Long id;

    @Column(name="creation_date")
    private Date creationDate;
    
    @Column(name="day_date")
    private String dayDate;

    @Column(name="cash_in_hand")
    private Long cashInHand;

    @Column(name="store_id")  
    private Long storeId;

    @PrePersist
    public void addTimestamp() {
        creationDate = new Date();
    }
}
