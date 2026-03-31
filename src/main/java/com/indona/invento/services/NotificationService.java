package com.indona.invento.services;

import com.indona.invento.entities.NotificationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface NotificationService {

    NotificationEntity createNotification(String type, String title, String message,
                                          Long recipientUserId, String moduleName,
                                          String entityType, Long entityId, String unitCode);

    NotificationEntity createRoleNotification(String type, String title, String message,
                                              String recipientRole, String moduleName,
                                              String entityType, Long entityId, String unitCode);

    List<NotificationEntity> getUnreadNotifications(Long userId);

    Page<NotificationEntity> getAllNotifications(Long userId, Pageable pageable);

    long getUnreadCount(Long userId);

    NotificationEntity markAsRead(Long notificationId);

    void markAllAsRead(Long userId);

    List<NotificationEntity> getRecentNotifications(Long userId);

    /**
     * Convenience method for creating role-based notifications from workflows.
     * Creates a notification directed at a role (not a specific user).
     */
    default NotificationEntity createNotification(String title, String recipientRole,
                                                   String message, String moduleName, String unitCode) {
        return createRoleNotification("INFO", title, message, recipientRole,
                moduleName, moduleName, null, unitCode);
    }
}
