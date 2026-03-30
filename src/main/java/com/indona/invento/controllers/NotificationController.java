package com.indona.invento.controllers;

import com.indona.invento.entities.NotificationEntity;
import com.indona.invento.services.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @GetMapping("/unread/{userId}")
    public ResponseEntity<List<NotificationEntity>> getUnreadNotifications(@PathVariable Long userId) {
        return ResponseEntity.ok(notificationService.getUnreadNotifications(userId));
    }

    @GetMapping("/count/{userId}")
    public ResponseEntity<Map<String, Long>> getUnreadCount(@PathVariable Long userId) {
        long count = notificationService.getUnreadCount(userId);
        return ResponseEntity.ok(Map.of("unreadCount", count));
    }

    @GetMapping("/recent/{userId}")
    public ResponseEntity<List<NotificationEntity>> getRecentNotifications(@PathVariable Long userId) {
        return ResponseEntity.ok(notificationService.getRecentNotifications(userId));
    }

    @GetMapping("/all/{userId}")
    public ResponseEntity<Page<NotificationEntity>> getAllNotifications(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(notificationService.getAllNotifications(userId, pageable));
    }

    @PutMapping("/read/{id}")
    public ResponseEntity<NotificationEntity> markAsRead(@PathVariable Long id) {
        return ResponseEntity.ok(notificationService.markAsRead(id));
    }

    @PutMapping("/read-all/{userId}")
    public ResponseEntity<Map<String, String>> markAllAsRead(@PathVariable Long userId) {
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok(Map.of("message", "All notifications marked as read"));
    }
}
