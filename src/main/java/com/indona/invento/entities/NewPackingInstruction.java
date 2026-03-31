package com.indona.invento.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "new_packing_instruction")
public class NewPackingInstruction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String typeOfPacking;
    private String weightInstructions;
    private String additionalRemarks;

    @OneToOne
    @JoinColumn(name = "new_sales_order_id")
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private NewSalesOrder newSalesOrder;
}
