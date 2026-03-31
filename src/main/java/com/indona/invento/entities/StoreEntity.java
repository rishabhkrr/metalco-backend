package com.indona.invento.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "store_master")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoreEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String storeName;   // e.g. Common, Rejection, Dispatch, etc.
}
