package com.indona.invento.services.impl;

import com.indona.invento.services.ApprovalWorkflowService;
import com.indona.invento.services.AuditLogService;
import com.indona.invento.services.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ApprovalWorkflowServiceImpl implements ApprovalWorkflowService {

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private NotificationService notificationService;

    @Override
    @Transactional
    public void submitForApproval(String entityType, Long entityId, String entityRef,
                                   String moduleName, String submittedBy, String unitCode,
                                   Long approverUserId) {
        // Log the submission
        auditLogService.logAction(
                "SUBMIT_FOR_APPROVAL",
                moduleName,
                entityType,
                entityId,
                entityRef,
                null,
                "PENDING_APPROVAL",
                entityType + " " + entityRef + " submitted for approval by " + submittedBy,
                submittedBy,
                unitCode
        );

        // Create notification for approver
        String title = "Approval Required: " + entityType.replaceAll("([A-Z])", " $1").trim();
        String message = entityRef + " has been submitted for your approval by " + submittedBy;

        if (approverUserId != null && approverUserId > 0) {
            notificationService.createNotification(
                    "APPROVAL_REQUIRED",
                    title,
                    message,
                    approverUserId,
                    moduleName,
                    entityType,
                    entityId,
                    unitCode
            );
        } else {
            // Broadcast to admin role
            notificationService.createRoleNotification(
                    "APPROVAL_REQUIRED",
                    title,
                    message,
                    "ADMIN",
                    moduleName,
                    entityType,
                    entityId,
                    unitCode
            );
        }
    }

    @Override
    @Transactional
    public boolean approve(String entityType, Long entityId, String entityRef,
                           String moduleName, String approvedBy, String comments,
                           String unitCode) {
        // Log the approval
        auditLogService.logAction(
                "APPROVE",
                moduleName,
                entityType,
                entityId,
                entityRef,
                "PENDING_APPROVAL",
                "APPROVED",
                entityType + " " + entityRef + " approved by " + approvedBy +
                        (comments != null ? ". Comments: " + comments : ""),
                approvedBy,
                unitCode
        );

        // Notify the submitter (using role-based for now)
        notificationService.createRoleNotification(
                "STATUS_CHANGE",
                entityType.replaceAll("([A-Z])", " $1").trim() + " Approved",
                entityRef + " has been approved by " + approvedBy,
                "USER",
                moduleName,
                entityType,
                entityId,
                unitCode
        );

        return true;
    }

    @Override
    @Transactional
    public boolean reject(String entityType, Long entityId, String entityRef,
                          String moduleName, String rejectedBy, String reason,
                          String unitCode) {
        // Log the rejection
        auditLogService.logAction(
                "REJECT",
                moduleName,
                entityType,
                entityId,
                entityRef,
                "PENDING_APPROVAL",
                "REJECTED",
                entityType + " " + entityRef + " rejected by " + rejectedBy +
                        ". Reason: " + reason,
                rejectedBy,
                unitCode
        );

        // Notify the submitter
        notificationService.createRoleNotification(
                "STATUS_CHANGE",
                entityType.replaceAll("([A-Z])", " $1").trim() + " Rejected",
                entityRef + " has been rejected by " + rejectedBy + ". Reason: " + reason,
                "USER",
                moduleName,
                entityType,
                entityId,
                unitCode
        );

        return true;
    }
}
