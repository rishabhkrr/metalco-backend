package com.indona.invento.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "contact_details")
public class ContactDetailsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String designation;
    private String phoneNumber;
    private String emailId;
    private boolean isPrimary;

    @ManyToOne
    @JoinColumn(name = "supplier_id")
    @JsonIgnore
    private SupplierMasterEntity supplier;
}
