package com.indona.invento.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.indona.invento.entities.UserRolesEntity;
  
@Repository
public interface UserRolesRepository extends JpaRepository<UserRolesEntity, Long> { 
    
}