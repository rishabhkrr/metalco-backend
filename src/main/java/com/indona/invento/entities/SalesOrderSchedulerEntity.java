package com.indona.invento.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "sales_order_scheduler")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SalesOrderSchedulerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer slNo;
    private String nextProcess;

    @Column(name = "plan_date")
    private LocalDate planDate;

    private String soNumber;
    private String lineNumber;
    private String unit;
    private String primeCustomer;
    private String customerCode;
    private String customerName;
    private Boolean packing;
    private String orderType;
    private String productCategory;
    private String itemDescription;
    private String brand;
    private String grade;
    private String temper;
    private String dimension;
    private String uomKg;
    private String uomNo;
    private String productionStrategy;

    @Column(name = "customer_category")
    private String customerCategory;

    private BigDecimal requiredQuantityKg;
    private Integer requiredQuantityNo;
    private LocalDate targetDateOfDispatch;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "pick_list_id")
    private PickListEntityScheduler pickList;

    private String retrievalStatus;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "stock_transfer_id")
    private WarehouseStockTransferEntityScheduler stockTransfer;

    @OneToOne(mappedBy = "scheduler", cascade = CascadeType.ALL)
    private SchedulerPackingInstruction schedulerPackingInstruction;

    @Column(name = "completed_time")
    private LocalDateTime completedTime;
}
