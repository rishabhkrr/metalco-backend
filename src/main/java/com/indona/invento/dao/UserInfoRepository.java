package com.indona.invento.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.indona.invento.entities.UsersEntity; 
  
@Repository
public interface UserInfoRepository extends JpaRepository<UsersEntity, Long> { 
    UsersEntity findByUserName(String userName);
    Optional<UsersEntity> findById(Long id);
}