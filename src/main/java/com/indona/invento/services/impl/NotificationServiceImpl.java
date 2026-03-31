package com.indona.invento.services.impl;

import com.indona.invento.dao.NotificationRepository;
import com.indona.invento.entities.NotificationEntity;
import com.indona.invento.services.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Override
    @Transactional
    public NotificationEntity createNotification(String type, String title, String message,
                                                  Long recipientUserId, String moduleName,
                                                  String entityType, Long entityId, String unitCode) {
        NotificationEntity notification = NotificationEntity.builder()
                .type(type)
                .title(title)
                .message(message)
                .recipientUserId(recipientUserId)
                .moduleName(moduleName)
                .entityType(entityType)
                .entityId(entityId)
                .unitCode(unitCode)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();

        return notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public NotificationEntity createRoleNotification(String type, String title, String message,
                                                      String recipientRole, String moduleName,
                                                      String entityType, Long entityId, String unitCode) {
        NotificationEntity notification = NotificationEntity.builder()
                .type(type)
                .title(title)
                .message(message)
                .recipientUserId(0L) // 0 = broadcast to role
                .recipientRole(recipientRole)
                .moduleName(moduleName)
                .entityType(entityType)
                .entityId(entityId)
                .unitCode(unitCode)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();

        return notificationRepository.save(notification);
    }

    @Override
    public List<NotificationEntity> getUnreadNotifications(Long userId) {
        return notificationRepository.findByRecipientUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
    }

    @Override
    public Page<NotificationEntity> getAllNotifications(Long userId, Pageable pageable) {
        return notificationRepository.findByRecipientUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    @Override
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByRecipientUserIdAndIsReadFalse(userId);
    }

    @Override
    @Transactional
    public NotificationEntity markAsRead(Long notificationId) {
        NotificationEntity notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found: " + notificationId));
        notification.setIsRead(true);
        notification.setReadAt(LocalDateTime.now());
        return notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void markAllAsRead(Long userId) {
        List<NotificationEntity> unread = notificationRepository
                .findByRecipientUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
        LocalDateTime now = LocalDateTime.now();
        unread.forEach(n -> {
            n.setIsRead(true);
            n.setReadAt(now);
        });
        notificationRepository.saveAll(unread);
    }

    @Override
    public List<NotificationEntity> getRecentNotifications(Long userId) {
        return notificationRepository.findTop20ByRecipientUserIdOrderByCreatedAtDesc(userId);
    }
}
