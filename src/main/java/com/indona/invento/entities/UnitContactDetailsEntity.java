package com.indona.invento.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "unit_contact_details")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UnitContactDetailsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "is_primary")
    private Boolean Primary;     // TRUE/FALSE
    private String name;
    private String designation;
    private String phoneNumber;
    private String emailId;

    @ManyToOne
    @JoinColumn(name = "unit_id")
    @JsonIgnore
    private UnitMasterEntity unit;
}
