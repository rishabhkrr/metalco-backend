package com.indona.invento.services;
import com.indona.invento.dto.SalesOrderAddressCreditDTO;
import com.indona.invento.dto.SalesOrderDTO;
import com.indona.invento.dto.SalesOrderLineItemDetailsDto;
import com.indona.invento.entities.SalesAuthority;
import com.indona.invento.entities.SalesOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface SalesOrderService {

    SalesOrder createSalesOrder(SalesOrderDTO dto);

    SalesOrder updateSalesOrder(Long id, SalesOrderDTO dto);

    SalesOrder getSalesOrderById(Long id);

    SalesOrder deleteSalesOrder(Long id);

   // SalesOrder cancelAndDeleteSalesOrder(Long id);
   Page<SalesOrder> getAllSalesOrders(Pageable pageable);

    List<SalesOrder> getAllSalesOrdersWithoutPagination();

    Page<SalesOrder> getAllSalesOrder(Pageable pageable);

    List<SalesAuthority> getAllSalesAuthorities();

    SalesAuthority addSalesAuthority(String name);

    SalesOrder getSalesOrderBySoNumber(String soNumber);

    SalesOrder cancelSalesOrder(String soNumber);

    SalesOrder updateStatus(String soNumber, String status);

    Map<String, Object> updateMultipleLineItemStatuses(List<String> lineNumbers, String status, String soNumber);

    Page<SalesOrder> getSalesOrdersBetweenDates(LocalDateTime fromDate, LocalDateTime toDate, Pageable pageable);



    void storePurchaseFollowUps(SalesOrder salesOrder);

    void storePurchaseFollowUpsV2(SalesOrder salesOrder);

    SalesOrder viewSalesOrderBySoNumber(String soNumber);

    void deleteAllSalesOrders();

    // SO Approval for Overdue Customers
    List<SalesOrder> getPendingOverdueSalesOrders();

    // SO Approval for Pending Approval SOs
    List<SalesOrder> getPendingApprovalSalesOrders();

    SalesOrder approveSalesOrder(Long id, String approvalRemarks);

    SalesOrder rejectSalesOrder(Long id, String approvalRemarks);

    SalesOrderAddressCreditDTO getAddressAndCreditDetails(String soNumber, String lineNumber);

    SalesOrder approveSalesOrderAfterCustomerClick(String soNumber);

    SalesOrderLineItemDetailsDto getLineItemDetailsBySoAndLineNumber(String soNumber, String lineNumber);
}


