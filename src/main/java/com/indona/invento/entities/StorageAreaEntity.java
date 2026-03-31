package com.indona.invento.entities;



import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "storage_area_master")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StorageAreaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String storageAreaName;   // e.g. Common, Rejection, Cantilever Rack, etc.

    private Integer storageAreaOrder; // Order/sequence of storage area
}
