package com.indona.invento.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "sub_module_access")
public class SubModuleAccessEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String subModuleName;
    
    @Column(name = "view_access")
    private Boolean readAccess;

    private Boolean createAccess;
    private Boolean editAccess;
    private Boolean deleteAccess;
    private Boolean approveAccess;


    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private UserMasterEntity user;
}
