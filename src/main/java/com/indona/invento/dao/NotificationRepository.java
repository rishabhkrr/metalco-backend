package com.indona.invento.dao;

import com.indona.invento.entities.NotificationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {

    List<NotificationEntity> findByRecipientUserIdAndIsReadFalseOrderByCreatedAtDesc(Long recipientUserId);

    Page<NotificationEntity> findByRecipientUserIdOrderByCreatedAtDesc(Long recipientUserId, Pageable pageable);

    long countByRecipientUserIdAndIsReadFalse(Long recipientUserId);

    List<NotificationEntity> findByRecipientRoleAndIsReadFalseOrderByCreatedAtDesc(String recipientRole);

    List<NotificationEntity> findByUnitCodeAndIsReadFalseOrderByCreatedAtDesc(String unitCode);

    List<NotificationEntity> findTop20ByRecipientUserIdOrderByCreatedAtDesc(Long recipientUserId);
}
