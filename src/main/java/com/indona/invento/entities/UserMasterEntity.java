package com.indona.invento.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Entity
@Table(name = "user_master")
public class UserMasterEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String unitCode;
    private String unitName;
    private String userId;
    private String userName;
    private String password;
    private String department;
    private String designation;
    private String modulesWithAccess;
    private String status;
    private Boolean isSalesmanCreated;
    private LocalDate dateOfJoining;
    private LocalDate dateOfEnding;
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<SubModuleAccessEntity> subModulesWithAccess;



}
