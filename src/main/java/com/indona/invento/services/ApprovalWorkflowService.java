package com.indona.invento.services;

/**
 * Generic approval workflow service used across all modules.
 * Handles state transitions: PENDING_APPROVAL → APPROVED / REJECTED
 * Creates audit logs and notifications for each transition.
 */
public interface ApprovalWorkflowService {

    /**
     * Submit an entity for approval.
     * @param entityType e.g., "SupplierMaster", "CustomerMaster", "SalesOrder", "GRN"
     * @param entityId The entity's primary key
     * @param entityRef Human-readable reference like "SUP-001"
     * @param moduleName The module name for routing
     * @param submittedBy Username of submitter
     * @param unitCode Unit code for scoping
     * @param approverUserId User ID of the designated approver (0 = any admin)
     */
    void submitForApproval(String entityType, Long entityId, String entityRef,
                           String moduleName, String submittedBy, String unitCode,
                           Long approverUserId);

    /**
     * Approve an entity.
     * @return true if approval was successful
     */
    boolean approve(String entityType, Long entityId, String entityRef,
                    String moduleName, String approvedBy, String comments,
                    String unitCode);

    /**
     * Reject an entity.
     * @return true if rejection was successful
     */
    boolean reject(String entityType, Long entityId, String entityRef,
                   String moduleName, String rejectedBy, String reason,
                   String unitCode);
}
