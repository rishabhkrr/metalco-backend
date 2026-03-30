package com.indona.invento.dao;

import com.indona.invento.entities.UserMasterEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserMasterRepository extends JpaRepository<UserMasterEntity, Long> {
    boolean existsByUserId(String userId);

    @Transactional
    @Modifying
    @Query("DELETE FROM SubModuleAccessEntity s WHERE s.user.id = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);


    Optional<UserMasterEntity> findByUserId(String userId);
    UserMasterEntity findByUserName(String userName);  // For login authentication


    Optional<UserMasterEntity> findByUserNameIgnoreCase(String userName);


}