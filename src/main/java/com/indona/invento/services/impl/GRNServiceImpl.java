package com.indona.invento.services.impl;

import com.indona.invento.dao.*;
import com.indona.invento.dto.*;
import com.indona.invento.entities.*;
import com.indona.invento.services.AuditLogService;
import com.indona.invento.services.GRNService;
import com.indona.invento.services.StockSummaryService;
import com.indona.invento.services.SalesOrderSchedulerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static io.netty.handler.codec.DateFormatter.format;
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class GRNServiceImpl implements GRNService {

    private final GRNRepository grnRepository;
    private final GRNItemRepository grnItemRepository;
    private final GrnLineItemRepository grnLineItemRepository;
    private final GateInwardRepository gateInwardRepository;
    private final POGenerationRepository poGenerationRepository;
    private final VehicleWeighmentRepository vehicleWeighmentRepository;
    private final StockTransferRepository stockTransferRepository;
    private final GRNInterUnitRepository grnInterUnitRepository;
    private final RackBinMasterRepository rackBinMasterRepository;
    private final ItemMasterRepository itemMasterRepository;

    @Autowired
    private StockSummaryService stockSummaryService;

    @Autowired
    private UserMasterRepository userMasterRepository;

    @Autowired
    private MaterialRequestSummaryHeaderRepository materialRequestSummaryHeaderRepository;

    @Autowired
    private DeliveryChallanCreationIUMTRepository deliveryChallanCreationIUMTRepository;

    @Autowired
    @Lazy
    private SalesOrderSchedulerService salesOrderSchedulerService;

    @Autowired
    private StockSummaryRepository stockSummaryRepository;

    @Autowired
    private SoSummaryRepository soSummaryRepository;

    @Autowired
    private SalesOrderRepository salesOrderRepository;

    @Autowired
    private SalesOrderSchedulerRepository salesOrderSchedulerRepository;

    @Autowired
    private CustomerMasterRepository customerMasterRepository;

    @Autowired
    private StockSummaryBundleRepository stockSummaryBundleRepository;

    @Autowired
    private AuditLogService auditLogService;


    // ---------- Create GRN ----------
    @Override
    @Transactional
    public GRNResponseDTO createGRN(GRNRequestDTO request) {

        String invoice = request.getInvoiceNumber();
        GateInwardEntity inward =
                gateInwardRepository.findTopByInvoiceNumberOrderByTimeStampDesc(invoice)
                        .orElse(null);

        // ---------- 🔥 INTER UNIT TRANSFER (SAVE INTO NEW MODULE) ----------
        if (inward != null &&
                "inter unit transfer".equalsIgnoreCase(inward.getMode()) &&
                inward.getMedcNumber() != null &&
                !inward.getMedcNumber().isBlank()) {

            String medc = inward.getMedcNumber();

            List<DeliveryChallanCreationIUMTEntity> dcLines =
                    deliveryChallanCreationIUMTRepository.findByDCNumber(medc);

            if (dcLines.isEmpty()) {
                throw new RuntimeException("Inter Unit DC not found: " + medc);
            }

            DeliveryChallanCreationIUMTEntity dc = dcLines.get(0); // header row


            List<MaterialRequestSummaryHeader> mrHeaders =
                    materialRequestSummaryHeaderRepository.findByMrNumberWithItems(dc.getMrNumber());

            if (mrHeaders.isEmpty())
                throw new RuntimeException("MR Number not found: " + dc.getMrNumber());

            List<MaterialRequestSummaryItem> mrLines = mrHeaders.get(0).getItems();

            // 🔥 Merge: If payload contains items → override MR items
            List<GRNInterUnitItemEntity> finalItems = new ArrayList<>();

            if (request.getItems() != null && !request.getItems().isEmpty()) {

                // Use payload items
                for (GRNItemRequestDTO dto : request.getItems()) {
                    finalItems.add(
                            GRNInterUnitItemEntity.builder()
                                    .itemDescription(dto.getItemDescription())
                                    .productCategory(dto.getProductCategory())
                                    .lineNumber(dto.getSectionNumber())
                                    .brand(dto.getBrand())
                                    .grade(dto.getGrade())
                                    .temper(dto.getTemper())
                                    .uom(dto.getUom())
                                    .quantityKg(dto.getRequestedQty() != null ? dto.getRequestedQty().doubleValue() : 0)
                                    .testCertificateNumber(dto.getTestCertificateNumber())
                                    .receivedNetWeight(dto.getReceivedNetWeight())
                                    .receivedNo(dto.getReceivedNo())
                                    .materialType(dto.getMaterialType())
                                    .build()
                    );
                }

            } else {
                finalItems = mrLines.stream().map(li ->
                        GRNInterUnitItemEntity.builder()
                                .itemDescription(li.getItemDescription())
                                .productCategory(li.getProductCategory())
                                .lineNumber(li.getLineNumber())
                                .brand(li.getBrand())
                                .grade(li.getGrade())
                                .temper(li.getTemper())
                                .uom(li.getUom())
                                .quantityKg(li.getRequiredQuantity() != null ? li.getRequiredQuantity().doubleValue() : 0)
                                .build()
                ).collect(Collectors.toList());
            }

            // ---------- Load existing IU GRN OR create new ----------
            GRNInterUnitEntity existingIU = grnInterUnitRepository.findByInvoiceNumber(invoice).orElse(null);

            boolean isNew = false;
            GRNInterUnitEntity iu;

            if (existingIU == null) {
                iu = new GRNInterUnitEntity();
                iu.setCreatedAt(new Date());
                iu.setGrnInterUnitRefNumber("MEGIU" + System.currentTimeMillis());
                isNew = true;
            } else {
                iu = existingIU;
            }

            // ---------- Update IU GRN fields ----------
            iu.setInvoiceNumber(request.getInvoiceNumber());
            iu.setEwayBillNumber(request.getEwayBillNumber());
            iu.setSupplierCode(request.getSupplierCode());
            iu.setSupplierName(request.getSupplierName());
            iu.setMedcNumber(inward.getMedcNumber());
            iu.setMrNumber(dc.getMrNumber());
            iu.setGateEntryRefNo(inward.getGatePassRefNumber());
//            iu.setVehicleNumber(dc.getVehicleNumberPackingAndDispatch());
            iu.setVehicleNumber(inward.getVehicleNumber());
            iu.setUnit(request.getUnit() != null ? request.getUnit() : dc.getUnit());
            iu.setSupplierUnit(request.getSupplierCode());
            iu.setMode("inter unit transfer");
            iu.setVehicleLoadWeightKg(request.getVehicleLoadWeightKg());
            iu.setVehicleEmptyWeightKg(request.getVehicleEmptyWeightKg());
            iu.setWeighmentRefNumber(request.getWeighmentRefNumber());
            iu.setInvoiceDocument(request.getInvoiceDocument());
            iu.setTestCertificateDocument(request.getTestCertificateDocument());
            iu.setEwayBillDocument(request.getEwayBillDocument());
            iu.setVehicleDocuments(request.getVehicleDocuments());

            if (request.getVehicleLoadWeightKg() != null && request.getVehicleEmptyWeightKg() != null) {
                iu.setWeighmentQuantity(
                        request.getVehicleLoadWeightKg() - request.getVehicleEmptyWeightKg()
                );
            }

            // ---------- Incremental IU Item Update (NO overwrite, NO clear, NO orphan-removal issues) ----------
            List<GRNInterUnitItemEntity> existingItems = iu.getItems();

            if (existingItems == null) {
                existingItems = new ArrayList<>();
                iu.setItems(existingItems);
            }

            for (GRNInterUnitItemEntity newItem : finalItems) {

                // Find matching existing item
                GRNInterUnitItemEntity existing = existingItems.stream()
                        .filter(it ->
                                Objects.equals(it.getLineNumber(), newItem.getLineNumber()) &&
                                        Objects.equals(it.getItemDescription(), newItem.getItemDescription())
                        )
                        .findFirst()
                        .orElse(null);

                if (existing != null) {
                    // ---------- Update existing item ----------
                    existing.setQuantityKg(newItem.getQuantityKg());
                    existing.setReceivedNetWeight(newItem.getReceivedNetWeight());
                    existing.setReceivedNo(newItem.getReceivedNo());
                    existing.setMaterialType(newItem.getMaterialType());
                    existing.setTestCertificateNumber(newItem.getTestCertificateNumber());
                } else {
                    // ---------- Add new item ----------
                    newItem.setGrnInterUnit(iu);
                    existingItems.add(newItem);
                }
            }


            // ---------- Save IU GRN ----------
            GRNInterUnitEntity savedIU = grnInterUnitRepository.save(iu);

            GRNResponseDTO resp = new GRNResponseDTO();
            resp.setMode("inter unit transfer");
            resp.setMedcNumber(medc);
            resp.setMrNumber(dc.getMrNumber());
            resp.setGateEntryRefNo(inward.getGatePassRefNumber());
            resp.setVehicleNumber(savedIU.getVehicleNumber());
            resp.setUnit(savedIU.getUnit());
            resp.setInvoiceNumber(savedIU.getInvoiceNumber());
            resp.setEwayBillNumber(savedIU.getEwayBillNumber());
            resp.setSupplierCode(savedIU.getSupplierCode());
            resp.setSupplierName(savedIU.getSupplierName());
            resp.setVehicleLoadWeightKg(savedIU.getVehicleLoadWeightKg());
            resp.setVehicleEmptyWeightKg(savedIU.getVehicleEmptyWeightKg());
            resp.setWeighmentQuantity(savedIU.getWeighmentQuantity());
            resp.setWeighmentRefNumber(savedIU.getWeighmentRefNumber());
            resp.setInvoiceDocument(savedIU.getInvoiceDocument());
            resp.setTestCertificateDocument(savedIU.getTestCertificateDocument());
            resp.setEwayBillDocument(savedIU.getEwayBillDocument());
            resp.setVehicleDocuments(savedIU.getVehicleDocuments());

            // Convert saved items to response DTOs
            resp.setItems(
                    savedIU.getItems().stream().map(it -> {
                        GRNItemResponseDTO r = new GRNItemResponseDTO();
                        r.setItemDescription(it.getItemDescription());
                        r.setProductCategory(it.getProductCategory());
                        r.setSectionNumber(it.getLineNumber());
                        r.setBrand(it.getBrand());
                        r.setGrade(it.getGrade());
                        r.setTemper(it.getTemper());
                        r.setUom(it.getUom());
                        r.setReceivedNetWeight(it.getReceivedNetWeight());
                        r.setReceivedNo(it.getReceivedNo());
                        r.setMaterialType(it.getMaterialType());
                        r.setPoQuantityKg(it.getQuantityKg());
                        r.setRequestedQty(String.valueOf(it.getQuantityKg()));
                        r.setTestCertificateNumber(it.getTestCertificateNumber());
                        return r;
                    }).collect(Collectors.toList())
            );

            return resp;
        }

        // ---------- NORMAL GRN LOGIC (UNCHANGED) ----------
        final GRNEntity[] grnHolder = new GRNEntity[1];

        if (invoice != null && !invoice.isBlank()) {
            grnHolder[0] = grnRepository.findByInvoiceNumber(invoice).orElse(null);
        }

        if (grnHolder[0] == null) {
            grnHolder[0] = new GRNEntity();
            grnHolder[0].setGrnRefNumber(generateUniqueGrnRef());
            grnHolder[0].setTimeStamp(new Date());
        }

        GRNEntity grn = grnHolder[0];

        // Normal GRN population (same as your existing)
        // ---------------------------------------------
        grn.setInvoiceNumber(invoice);
        grn.setWeighmentRefNumber(request.getWeighmentRefNumber());

        if (inward != null) {
            grn.setUnit(inward.getUnitCode());
            grn.setVehicleNumber(inward.getVehicleNumber());
            grn.setGateEntryRefNo(inward.getGatePassRefNumber());
            // Get first TC number from testCertificates list
            if (inward.getTestCertificates() != null && !inward.getTestCertificates().isEmpty()) {
                List<String> tcNumbers = inward.getTestCertificates().stream()
                        .map(tc -> tc.getTcNumber())
                        .filter(Objects::nonNull)
                        .toList();

                grn.setTestCertificateNumbers(tcNumbers);
            }
            grn.setEwayBillNumber(inward.getEWayBillNumber());
            grn.setInvoiceDocument(first(inward.getInvoiceScanUrls()));
            grn.setTestCertificateDocument(first(inward.getTestCertificateScanUrls()));
            grn.setEwayBillDocument(first(inward.getEWayBillScanUrls()));
            grn.setVehicleDocuments(first(inward.getVehicleDocumentsScanUrls()));
            grn.setMode(inward.getMode());

            if (inward.getPoNumbers() != null && !inward.getPoNumbers().isEmpty()) {
                grn.setPoNumber(inward.getPoNumbers().get(0));
            }
        }

        if (inward == null) {

            grn.setUnit(request.getUnit());
            grn.setGateEntryRefNo(request.getGateEntryRefNo());
            grn.setMedcNumber(request.getMedcNumber());
            grn.setSupplierCode(request.getSupplierCode());
            grn.setSupplierName(request.getSupplierName());
            grn.setVehicleNumber(request.getVehicleNumber());
            grn.setEwayBillNumber(request.getEwayBillNumber());
            grn.setEwayBillDocument(request.getEwayBillDocument());
            grn.setInvoiceDocument(request.getInvoiceDocument());
            grn.setTestCertificateDocument(request.getTestCertificateDocument());
            grn.setMode(request.getMode());
        }

        if (request.getPoNumber() != null && !request.getPoNumber().isEmpty()) {
            // Convert List<String> to comma-separated String
            String poNumberStr = String.join(",", request.getPoNumber());
            grn.setPoNumber(poNumberStr);
        }

        if (grn.getPoNumber() != null) {
            // Get first PO number from comma-separated string
            String firstPoNumber = grn.getPoNumber().split(",")[0].trim();
            poGenerationRepository.findByPoNumber(firstPoNumber).ifPresent(po -> {
                if (grn.getSupplierName() == null)
                    grn.setSupplierName(po.getSupplierName());
                if (grn.getSupplierCode() == null)
                    grn.setSupplierCode(po.getSupplierCode());
            });
        }

        // vehicle weighment (by vehicle number)
        if (grn.getVehicleNumber() != null) {
            vehicleWeighmentRepository.findTopByVehicleNumberOrderByTimeStampDesc(grn.getVehicleNumber()).ifPresent(vw -> {
                grn.setVehicleLoadWeightKg(vw.getLoadWeight());
                grn.setVehicleEmptyWeightKg(vw.getEmptyWeight());
                if (vw.getLoadWeight() != null && vw.getEmptyWeight() != null)
                    grn.setWeighmentQuantity(vw.getLoadWeight() - vw.getEmptyWeight());
            });
        }

        // override weights if provided in request
        if (request.getVehicleLoadWeightKg() != null) {
            grn.setVehicleLoadWeightKg(request.getVehicleLoadWeightKg());
            if (grn.getVehicleEmptyWeightKg() != null)
                grn.setWeighmentQuantity(grn.getVehicleLoadWeightKg() - grn.getVehicleEmptyWeightKg());
        }
        if (request.getVehicleEmptyWeightKg() != null) {
            grn.setVehicleEmptyWeightKg(request.getVehicleEmptyWeightKg());
            if (grn.getVehicleLoadWeightKg() != null)
                grn.setWeighmentQuantity(grn.getVehicleLoadWeightKg() - grn.getVehicleEmptyWeightKg());
        }

        // items
        List<GRNItemEntity> existingItems = grn.getGrnItems() == null ?
                new ArrayList<>() : grn.getGrnItems();

        // items from request
        if (request.getItems() != null) {
            for (GRNItemRequestDTO dto : request.getItems()) {
                GRNItemEntity item = new GRNItemEntity();
                item.setItemDescription(dto.getItemDescription());
                item.setSectionNumber(dto.getSectionNumber());
                item.setGrade(dto.getGrade());
                item.setTemper(dto.getTemper());
                item.setPoQuantityKg(dto.getPoQuantityKg());
                item.setUom(dto.getUom());
                item.setRate(dto.getRate());
                item.setValue(dto.getValue());
                item.setTestCertificateNumber(dto.getTestCertificateNumber());
                item.setReceivedGrossWeight(dto.getReceivedGrossWeight());
                item.setReceivedNetWeight(dto.getReceivedNetWeight());
                item.setReceivedNo(dto.getReceivedNo());
                item.setHeatNumber(dto.getHeatNumber());
                item.setLotNumber(dto.getLotNumber());
                item.setProductCategory(dto.getProductCategory());
                item.setBrand(dto.getBrand());
                item.setPoNumber(dto.getPoNumber());
                item.setGrn(grn);
                existingItems.add(item);
            }
        }
        // --- Auto-load PO items ---
        else if (grn.getPoNumber() != null) {
            List<GRNItemEntity> poItems = buildItemsFromPo(grn.getPoNumber(), invoice);
            poItems.forEach(i -> i.setGrn(grn));
            for (GRNItemEntity it : poItems) {
                if (!itemAlreadyExists(existingItems, it)) existingItems.add(it);
            }
        }

        // items without PO
        if (request.getItemsWithoutPO() != null) {
            for (GRNItemRequestDTO dto : request.getItemsWithoutPO()) {
                GRNItemEntity item = new GRNItemEntity();
                item.setItemDescription(dto.getItemDescription());
                item.setSectionNumber(dto.getSectionNumber());
                item.setGrade(dto.getGrade());
                item.setTemper(dto.getTemper());
                item.setPoQuantityKg(dto.getPoQuantityKg());
                item.setUom(dto.getUom());
                item.setRate(dto.getRate());
                item.setValue(dto.getValue());
                item.setTestCertificateNumber(dto.getTestCertificateNumber());
                item.setReceivedGrossWeight(dto.getReceivedGrossWeight());
                item.setReceivedNetWeight(dto.getReceivedNetWeight());
                item.setReceivedNo(dto.getReceivedNo());
                item.setHeatNumber(dto.getHeatNumber());
                item.setLotNumber(dto.getLotNumber());
                item.setProductCategory(dto.getProductCategory());
                item.setBrand(dto.getBrand());
                item.setPoNumber(dto.getPoNumber());
                item.setGrn(grn);

                if (!itemAlreadyExists(existingItems, item)) {
                    existingItems.add(item);
                }
            }
        }

        grn.setGrnItems(existingItems);

        // Set status from request, default to PENDING
        if (request.getStatus() != null) {
            grn.setStatus(request.getStatus());
        } else {
            grn.setStatus("PENDING");
        }

        GRNEntity saved = grnRepository.save(grn);

        // Audit log for GRN creation
        auditLogService.logAction("CREATE", "GRN", "GRN",
                saved.getId(), saved.getGrnRefNumber(), null, saved.getStatus(),
                "GRN " + saved.getGrnRefNumber() + " created for invoice " + saved.getInvoiceNumber() +
                        (saved.getSupplierName() != null ? " from supplier " + saved.getSupplierName() : ""),
                "SYSTEM", saved.getUnit());

        // 🎯 Trigger Sales Order Scheduler entry creation with updated logic
        try {
            createSalesOrderSchedulerFromGRN_Updated(saved);
        } catch (Exception e) {
            System.err.println("⚠️ Warning: Could not create Sales Order Scheduler entry: " + e.getMessage());
            e.printStackTrace();
        }

        return mapToResponse(saved);
    }

    @Override
    public GRNResponseDTO fetchDetailsByInvoice(String invoiceNumber) {

        if (invoiceNumber == null || invoiceNumber.isBlank()) {
            throw new RuntimeException("invoiceNumber required");
        }

        GateInwardEntity inward = gateInwardRepository
                .findTopByInvoiceNumberOrderByTimeStampDesc(invoiceNumber)
                .orElseThrow(() ->
                        new RuntimeException("Gate Inward not found for invoice: " + invoiceNumber)
                );

        boolean isInterUnit =
                "inter unit transfer".equalsIgnoreCase(inward.getMode())
                        && inward.getMedcNumber() != null
                        && !inward.getMedcNumber().isBlank();

        // ================= INTER UNIT TRANSFER =================
        if (isInterUnit) {

            // ---------- HEADER (UNCHANGED BEHAVIOR) ----------
            String medcRaw = inward.getMedcNumber();
            String medcNumber = medcRaw.contains(",")
                    ? medcRaw.split(",")[0].trim()
                    : medcRaw;

            List<DeliveryChallanCreationIUMTEntity> dcLines =
                    deliveryChallanCreationIUMTRepository.findByDCNumber(medcNumber);

            if (dcLines.isEmpty()) {
                throw new RuntimeException("Inter Unit DC not found for MEDC: " + medcNumber);
            }

            DeliveryChallanCreationIUMTEntity dc = dcLines.get(0);


            GRNResponseDTO dto = new GRNResponseDTO();

            dto.setInvoiceNumber(invoiceNumber);
            dto.setGateEntryRefNo(inward.getGatePassRefNumber());
            dto.setVehicleNumber(inward.getVehicleNumber());
            dto.setEwayBillNumber(inward.getEWayBillNumber());
            dto.setMode(inward.getMode());

            dto.setMedcNumber(medcNumber);
            dto.setUnit(dc.getUnit());
            dto.setSupplierName(dc.getUnit());
            dto.setSupplierCode(dc.getUnit());
            dto.setRequestingUnit(dc.getRequestingName());
            dto.setRequestingUnitCode(dc.getRequestingCode());
            dto.setMaterialType(dc.getMaterialType());

            dto.setInvoiceDocument(first(inward.getInvoiceScanUrls()));
            dto.setTestCertificateDocument(first(inward.getTestCertificateScanUrls()));
            dto.setEwayBillDocument(first(inward.getEWayBillScanUrls()));
            dto.setVehicleDocuments(first(inward.getVehicleDocumentsScanUrls()));

            // ================= MULTI MR → MR DETAILS =================

            // Split MEDCs
            List<String> medcNumbers = Arrays.stream(inward.getMedcNumber().split(","))
                    .map(String::trim)
                    .filter(s -> !s.isBlank())
                    .toList();

            // Fetch all DC lines
            List<DeliveryChallanCreationIUMTEntity> allDcLines =
                    deliveryChallanCreationIUMTRepository.findByDCNumberIn(medcNumbers);

            if (allDcLines.isEmpty()) {
                throw new RuntimeException("No DC lines found for MEDCs: " + medcNumbers);
            }

            // Group DC lines by MR number
            Map<String, List<DeliveryChallanCreationIUMTEntity>> dcByMr =
                    allDcLines.stream()
                            .collect(Collectors.groupingBy(
                                    DeliveryChallanCreationIUMTEntity::getMrNumber
                            ));

            List<MRWiseGRNItemsDTO> mrDetails = new ArrayList<>();

            for (Map.Entry<String, List<DeliveryChallanCreationIUMTEntity>> entry : dcByMr.entrySet()) {

                String mrNumber = entry.getKey();
                List<DeliveryChallanCreationIUMTEntity> mrDcLines = entry.getValue();

                MaterialRequestSummaryHeader mrHeader =
                        materialRequestSummaryHeaderRepository
                                .findByMrNumberWithItems(mrNumber)
                                .stream()
                                .findFirst()
                                .orElseThrow(() ->
                                        new RuntimeException("MR Number not found: " + mrNumber));

                Set<String> validLineNumbers = mrDcLines.stream()
                        .map(DeliveryChallanCreationIUMTEntity::getLineNumber)
                        .collect(Collectors.toSet());

                List<GRNItemResponseDTO> mrItems = new ArrayList<>();

                for (MaterialRequestSummaryItem li : mrHeader.getItems()) {

                    if (!validLineNumbers.contains(li.getLineNumber())) continue;

                    DeliveryChallanCreationIUMTEntity dcLine =
                            mrDcLines.stream()
                                    .filter(d -> d.getLineNumber().equals(li.getLineNumber()))
                                    .findFirst()
                                    .orElse(null);

                    if (dcLine == null) continue;

                    GRNItemResponseDTO item = new GRNItemResponseDTO();
                    item.setItemDescription(li.getItemDescription());
                    item.setMaterialType(li.getMaterialType());
                    item.setProductCategory(li.getProductCategory());
                    item.setSectionNumber(li.getLineNumber());
                    item.setBrand(li.getBrand());
                    item.setGrade(li.getGrade());
                    item.setTemper(li.getTemper());
                    item.setUom(li.getUom());
                    item.setRequestedQty(String.valueOf(dcLine.getQuantityKg()));
                    item.setPoQuantityKg(
                            dcLine.getQuantityKg() != null
                                    ? dcLine.getQuantityKg().doubleValue()
                                    : 0
                    );

                    mrItems.add(item);
                }

                if (!mrItems.isEmpty()) {
                    MRWiseGRNItemsDTO mrDto = new MRWiseGRNItemsDTO();
                    mrDto.setMrNumber(mrNumber);
                    mrDto.setItems(mrItems);
                    mrDetails.add(mrDto);
                }
            }

            dto.setMrDetails(mrDetails);

            // ---------- WEIGHMENT ----------
            if (inward.getVehicleNumber() != null) {
                vehicleWeighmentRepository
                        .findTopByVehicleNumberOrderByTimeStampDesc(inward.getVehicleNumber())
                        .ifPresent(vw -> {
                            dto.setVehicleLoadWeightKg(vw.getLoadWeight());
                            dto.setVehicleEmptyWeightKg(vw.getEmptyWeight());
                            dto.setWeighmentRefNumber(vw.getWeightmentRefNumber());
                            if (vw.getLoadWeight() != null && vw.getEmptyWeight() != null) {
                                dto.setWeighmentQuantity(vw.getLoadWeight() - vw.getEmptyWeight());
                            }
                        });
            }

            return dto;
        }

        // ================= NORMAL GRN (UNCHANGED) =================
        GRNResponseDTO dto = new GRNResponseDTO();
        dto.setInvoiceNumber(inward.getInvoiceNumber());
        dto.setGateEntryRefNo(inward.getGatePassRefNumber());
        // Get first TC number from testCertificates list
        if (inward.getTestCertificates() != null && !inward.getTestCertificates().isEmpty()) {
            dto.setTestCertificates(
                    inward.getTestCertificates().stream()
                            .map(tc -> TCDetailsDTO.builder()
                                    .tcNumber(tc.getTcNumber())
                                    .pdfLink(tc.getPdfLink())
                                    .build()
                            )
                            .toList()
            );
        }
        dto.setEwayBillNumber(inward.getEWayBillNumber());
        dto.setVehicleNumber(inward.getVehicleNumber());

        dto.setInvoiceDocument(first(inward.getInvoiceScanUrls()));
        dto.setTestCertificateDocument(first(inward.getTestCertificateScanUrls()));
        dto.setEwayBillDocument(first(inward.getEWayBillScanUrls()));
        dto.setVehicleDocuments(first(inward.getVehicleDocumentsScanUrls()));

        String poNumber = (inward.getPoNumbers() != null && !inward.getPoNumbers().isEmpty())
                ? inward.getPoNumbers().get(0)
                : null;
        dto.setPoNumber(inward.getPoNumbers() != null ? inward.getPoNumbers() : new ArrayList<>());

        if (poNumber != null) {
            poGenerationRepository.findByPoNumber(poNumber).ifPresent(po -> {
                dto.setSupplierName(po.getSupplierName());
                dto.setSupplierCode(po.getSupplierCode());
            });
        }

        if (dto.getVehicleNumber() != null) {
            vehicleWeighmentRepository
                    .findTopByVehicleNumberOrderByTimeStampDesc(dto.getVehicleNumber())
                    .ifPresent(vw -> {
                        dto.setVehicleLoadWeightKg(vw.getLoadWeight());
                        dto.setVehicleEmptyWeightKg(vw.getEmptyWeight());
                        dto.setWeighmentRefNumber(vw.getWeightmentRefNumber());
                        if (vw.getLoadWeight() != null && vw.getEmptyWeight() != null) {
                            dto.setWeighmentQuantity(vw.getLoadWeight() - vw.getEmptyWeight());
                        }
                    });
        }

        return dto;
    }

    // small utility
    private String first(List<String> list) {
        return (list != null && !list.isEmpty()) ? list.get(0) : null;
    }


    // ---------- get items by PO number ----------
    @Override
    public List<GRNItemRequestDTO> getItemsByPoNumber(String poNumber) {
        if (poNumber == null || poNumber.isBlank()) throw new RuntimeException("poNumber required");
        POGenerationEntity po = poGenerationRepository.findByPoNumber(poNumber)
                .orElseThrow(() -> new RuntimeException("PO not found: " + poNumber));
        return po.getItems() == null ? List.of() : po.getItems().stream().map(pi -> {
            GRNItemRequestDTO dto = new GRNItemRequestDTO();
            dto.setItemDescription(pi.getItemDescription());
            dto.setSectionNumber(pi.getSectionNo());
            dto.setGrade(pi.getGrade());
            dto.setTemper(pi.getTemper());
            dto.setPoQuantityKg(pi.getRequiredQuantity());
            dto.setUom("KGS");
            dto.setProductCategory(pi.getProductCategory());
            dto.setBrand(pi.getBrand());

            // Fetch materialType from ItemMaster by itemDescription (skuDescription)
            if (pi.getItemDescription() != null && !pi.getItemDescription().isEmpty()) {
                itemMasterRepository.findBySkuDescriptionIgnoreCase(pi.getItemDescription())
                        .ifPresent(itemMaster -> dto.setMaterialType(itemMaster.getMaterialType()));
            }
            
            return dto;
        }).collect(Collectors.toList());
    }

    // ---------- get items by GRN ID ----------
    @Override
    public List<GRNItemRequestDTO> getItemsByGrnId(Long grnId) {
        if (grnId == null) throw new RuntimeException("grnId required");
        GRNEntity grn = grnRepository.findById(grnId)
                .orElseThrow(() -> new RuntimeException("GRN not found with ID: " + grnId));

        return grn.getGrnItems() == null ? List.of() : grn.getGrnItems().stream().map(item -> {
            GRNItemRequestDTO dto = new GRNItemRequestDTO();
            dto.setItemDescription(item.getItemDescription());
            dto.setSectionNumber(item.getSectionNumber());
            dto.setGrade(item.getGrade());
            dto.setTemper(item.getTemper());
            dto.setPoQuantityKg(item.getPoQuantityKg());
            dto.setUom(item.getUom());
            dto.setRate(item.getRate());
            dto.setValue(item.getValue());
            dto.setReceivedGrossWeight(item.getReceivedGrossWeight());
            dto.setReceivedNetWeight(item.getReceivedNetWeight());
            dto.setReceivedNo(item.getReceivedNo());
            dto.setHeatNumber(item.getHeatNumber());
            dto.setLotNumber(item.getLotNumber());
            dto.setTestCertificateNumber(item.getTestCertificateNumber());
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public List<InvoiceDropdownDTO> getInvoiceNumbersForDropdown() {

        // invoices from gate inward
        List<String> inwardInvoices = gateInwardRepository
                .findDistinctInvoiceNumbersByStatus("IN PROGRESS");

        // invoices that already have GRN created
        List<String> usedInvoices = grnRepository.findAllInvoiceNumbersWithGrn();

        // filter → only invoices that do NOT have GRN created
        List<String> filtered = inwardInvoices.stream()
                .filter(inv -> !usedInvoices.contains(inv))
                .toList();

        // Now fetch mode for each invoice
        List<InvoiceDropdownDTO> result = new ArrayList<>();

        for (String invoice : filtered) {
            GateInwardEntity inward =
                    gateInwardRepository.findTopByInvoiceNumberOrderByTimeStampDesc(invoice)
                            .orElse(null);

            String mode = inward != null ? inward.getMode() : null;

            result.add(new InvoiceDropdownDTO(invoice, mode));
        }

        return result;
    }

    // New method to get all invoice numbers from GRN summary
    @Override
    public List<String> getAllInvoiceNumbers() {
        return grnRepository.findDistinctInvoiceNumbers();
    }

    @Override
    public List<String> getPoNumbersByInvoice(String invoiceNumber) {
        if (invoiceNumber == null || invoiceNumber.isBlank()) {
            throw new RuntimeException("invoiceNumber required");
        }
        GateInwardEntity inward = gateInwardRepository.findTopByInvoiceNumberOrderByTimeStampDesc(invoiceNumber)
                .orElseThrow(() -> new RuntimeException("Gate Inward not found for invoice: " + invoiceNumber));
        return inward.getPoNumbers() != null ? inward.getPoNumbers() : List.of();
    }

    @Override
    public List<PurchaseLineDetailsDTO> getPurchaseLineDetails(String invoiceNumber, String poNumber) {
        if (invoiceNumber == null || invoiceNumber.isBlank()) {
            throw new RuntimeException("invoiceNumber required");
        }
        if (poNumber == null || poNumber.isBlank()) {
            throw new RuntimeException("poNumber required");
        }

        GateInwardEntity inward = gateInwardRepository.findTopByInvoiceNumberOrderByTimeStampDesc(invoiceNumber)
                .orElseThrow(() -> new RuntimeException("Gate Inward not found for invoice: " + invoiceNumber));

        POGenerationEntity po = poGenerationRepository.findByPoNumber(poNumber)
                .orElseThrow(() -> new RuntimeException("PO not found: " + poNumber));

        if (po.getItems() == null || po.getItems().isEmpty()) {
            return List.of();
        }

        return po.getItems().stream().map(item -> PurchaseLineDetailsDTO.builder()
                .unit(inward.getUnitCode())
                .supplierCode(po.getSupplierCode())
                .supplierName(po.getSupplierName())
                .supplierBillingAddress(po.getBillingAddress())
                .supplierShippingAddress(po.getShippingAddress())
                .itemDescription(item.getItemDescription())
                .productCategory(item.getProductCategory())
                .sectionNumber(item.getSectionNo())
                .brand(item.getBrand())
                .grade(item.getGrade())
                .temper(item.getTemper())
                .build()).collect(Collectors.toList());
    }

    @Override
    public GRNResponseDTO getGRNById(Long id) {
        return grnRepository.findById(id).map(this::mapToResponse).orElseThrow(() -> new RuntimeException("GRN not found"));
    }

    @Override
    public List<GRNResponseDTO> getAllGRNs() {
        return grnRepository.findAll().stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public GRNResponseDTO markMaterialUnloaded(Long id, String notes) {
        GRNEntity grn = grnRepository.findById(id).orElseThrow(() -> new RuntimeException("GRN not found"));
        grn.setMaterialUnloadingStatus("DONE");
        grn.setMaterialUnloadingNotes(notes);
        return mapToResponse(grnRepository.save(grn));
    }

    private List<GRNItemEntity> buildItemsFromPo(String poNumber, String invoiceNumber) {
        Optional<POGenerationEntity> poOpt = poGenerationRepository.findByPoNumber(poNumber);
        List<GRNItemEntity> out = new ArrayList<>();
        if (poOpt.isPresent()) {
            POGenerationEntity po = poOpt.get();
            // fetch TC from gate inward if invoiceNumber present
            String tc = null;
            if (invoiceNumber != null) {
                tc = gateInwardRepository.findTopByInvoiceNumberOrderByTimeStampDesc(invoiceNumber)
                        .flatMap(inward -> inward.getTestCertificates() != null && !inward.getTestCertificates().isEmpty()
                                ? java.util.Optional.of(inward.getTestCertificates().get(0).getTcNumber())
                                : java.util.Optional.empty())
                        .orElse(null);
            }
            if (po.getItems() != null) {
                for (var pi : po.getItems()) {
                    GRNItemEntity gi = GRNItemEntity.builder()
                            .itemDescription(pi.getItemDescription())
                            .sectionNumber(pi.getSectionNo())
                            .grade(pi.getGrade())
                            .temper(pi.getTemper())
                            .poQuantityKg(pi.getRequiredQuantity())
                            .uom("KGS")
                            .productCategory(pi.getProductCategory())
                            .brand(pi.getBrand())
                            .rate(null)
                            .value(null)
                            .testCertificateNumber(tc)
                            .build();
                    out.add(gi);
                }
            }
        }
        return out;
    }

    private GRNResponseDTO mapToResponse(GRNEntity entity) {
        if (entity == null) return null;
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");

        GRNResponseDTO dto = new GRNResponseDTO();
        dto.setId(entity.getId());
        dto.setTimeStamp(entity.getTimeStamp() != null ? sdf.format(entity.getTimeStamp()) : null);
        dto.setGrnRefNumber(entity.getGrnRefNumber());
        dto.setUnit(entity.getUnit());
        dto.setInvoiceNumber(entity.getInvoiceNumber());

        // Convert poNumber String to List
        if (entity.getPoNumber() != null && !entity.getPoNumber().isEmpty()) {
            List<String> poNumbers = Arrays.stream(entity.getPoNumber().split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();
            dto.setPoNumber(poNumbers);
        } else {
            dto.setPoNumber(new ArrayList<>());
        }

        dto.setGateEntryRefNo(entity.getGateEntryRefNo());
//        dto.setTestCertificateNumbers(entity.getTestCertificateNumbers());
        if (entity.getInvoiceNumber() != null) {
            gateInwardRepository
                    .findTopByInvoiceNumberOrderByTimeStampDesc(entity.getInvoiceNumber())
                    .ifPresent(inward -> {
                        if (inward.getTestCertificates() != null && !inward.getTestCertificates().isEmpty()) {
                            dto.setTestCertificates(
                                    inward.getTestCertificates().stream()
                                            .map(tc -> TCDetailsDTO.builder()
                                                    .tcNumber(tc.getTcNumber())
                                                    .pdfLink(tc.getPdfLink())
                                                    .build()
                                            )
                                            .toList()
                            );
                        }
                    });
        }
        dto.setEwayBillNumber(entity.getEwayBillNumber());
        dto.setVehicleNumber(entity.getVehicleNumber());
        dto.setSupplierName(entity.getSupplierName());
        dto.setSupplierCode(entity.getSupplierCode());
        dto.setVehicleLoadWeightKg(entity.getVehicleLoadWeightKg());
        dto.setVehicleEmptyWeightKg(entity.getVehicleEmptyWeightKg());
        dto.setWeighmentQuantity(entity.getWeighmentQuantity());
        dto.setMaterialUnloadingStatus(entity.getMaterialUnloadingStatus());
        dto.setBinStatus(entity.getBinStatus());
        dto.setCreatedAt(entity.getCreatedAt() != null ? sdf.format(entity.getCreatedAt()) : null);
        dto.setUpdatedAt(entity.getUpdatedAt() != null ? sdf.format(entity.getUpdatedAt()) : null);
        dto.setInvoiceDocument(entity.getInvoiceDocument());
        dto.setTestCertificateDocument(entity.getTestCertificateDocument());
        dto.setEwayBillDocument(entity.getEwayBillDocument());
        dto.setVehicleDocuments(entity.getVehicleDocuments());
        dto.setWeighmentRefNumber(entity.getWeighmentRefNumber());
        dto.setMode(entity.getMode());
        dto.setStatus(entity.getStatus());

        if (entity.getGrnItems() != null && !entity.getGrnItems().isEmpty()) {
            List<GRNItemResponseDTO> itemDTOs = entity.getGrnItems().stream().map(item -> {
                GRNItemResponseDTO i = new GRNItemResponseDTO();
                i.setItemDescription(item.getItemDescription());
                i.setSectionNumber(item.getSectionNumber());
                i.setGrade(item.getGrade());
                i.setTemper(item.getTemper());
                i.setPoQuantityKg(Double.valueOf(item.getPoQuantityKg()));
                i.setUom(item.getUom());
                i.setRate(item.getRate());
                i.setValue(item.getValue());
                i.setTestCertificateNumber(item.getTestCertificateNumber());
                i.setReceivedGrossWeight(item.getReceivedGrossWeight());
                i.setReceivedNetWeight(item.getReceivedNetWeight());
                i.setReceivedNo(item.getReceivedNo());
                i.setHeatNumber(item.getHeatNumber());
                i.setLotNumber(item.getLotNumber());
                i.setProductCategory(item.getProductCategory());
                i.setBrand(item.getBrand());
                i.setPoNumber(item.getPoNumber());
                return i;
            }).collect(Collectors.toList());
            dto.setItems(itemDTOs);
        }

        return dto;
    }


    private String generateUniqueGrnRef() {
        String prefix = "MEGRN";
        String datePart = new SimpleDateFormat("yyMM").format(new Date());
        int seq = 1;
        String candidate;
        do {
            candidate = prefix + datePart + String.format("%03d", seq++);
        } while (grnRepository.findByGrnRefNumber(candidate).isPresent());
        return candidate;
    }

    @Override
    public List<GRNResponseDTO> getAllPendingGRNs() {

        List<GRNResponseDTO> result = new ArrayList<>();

        // --------------- NORMAL GRNs ---------------
        List<GRNEntity> normal = grnRepository.findByBinStatusIn(
                Arrays.asList("PENDING", "COMPLETED")
        );

        for (GRNEntity grn : normal) {

            // Fetch mode from GateInward if missing
            if ((grn.getMode() == null || grn.getMode().isBlank()) &&
                    grn.getInvoiceNumber() != null && !grn.getInvoiceNumber().isBlank()) {

                gateInwardRepository
                        .findTopByInvoiceNumberOrderByTimeStampDesc(grn.getInvoiceNumber())
                        .ifPresent(inward -> grn.setMode(inward.getMode()));
            }

            result.add(mapToResponse(grn));
        }

        // --------------- INTER-UNIT GRNs ---------------
        List<GRNInterUnitEntity> interUnits = grnInterUnitRepository.findAll(); // or filter for pending if needed

        for (GRNInterUnitEntity iu : interUnits) {
            GRNResponseDTO dto = new GRNResponseDTO();

            dto.setId(iu.getId());
            dto.setGrnRefNumber(iu.getGrnInterUnitRefNumber());
            dto.setMode("inter unit transfer");
            dto.setMedcNumber(iu.getMedcNumber());
            dto.setMrNumber(iu.getMrNumber());
            dto.setGateEntryRefNo(iu.getGateEntryRefNo());
            dto.setVehicleNumber(iu.getVehicleNumber());
            dto.setUnit(iu.getUnit());
            dto.setSupplierName(iu.getSupplierName());
            dto.setSupplierCode(iu.getSupplierCode());
            dto.setInvoiceDocument(iu.getInvoiceDocument());
            dto.setTestCertificateDocument(iu.getTestCertificateDocument());
            dto.setEwayBillDocument(iu.getEwayBillDocument());
            dto.setVehicleDocuments(iu.getVehicleDocuments());
            dto.setEwayBillNumber(iu.getEwayBillNumber());
            dto.setInvoiceNumber(iu.getInvoiceNumber());
            dto.setVehicleLoadWeightKg(iu.getVehicleLoadWeightKg());
            dto.setVehicleEmptyWeightKg(iu.getVehicleEmptyWeightKg());
            dto.setWeighmentQuantity(iu.getWeighmentQuantity());
            dto.setWeighmentRefNumber(iu.getWeighmentRefNumber());
            dto.setMaterialType(iu.getMode());
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            dto.setCreatedAt(iu.getCreatedAt() != null ? sdf.format(iu.getCreatedAt()) : null);

            // Convert IU items
            dto.setItems(
                    iu.getItems().stream().map(it -> {
                        GRNItemResponseDTO r = new GRNItemResponseDTO();
                        r.setItemDescription(it.getItemDescription());
                        r.setProductCategory(it.getProductCategory());
                        r.setSectionNumber(it.getLineNumber());
                        r.setBrand(it.getBrand());
                        r.setGrade(it.getGrade());
                        r.setTemper(it.getTemper());
                        r.setUom(it.getUom());
                        r.setRequestedQty(String.valueOf(it.getQuantityKg()));
                        r.setPoQuantityKg(it.getQuantityKg());
                        r.setMaterialType(it.getMaterialType());
                        r.setTestCertificateNumber(it.getTestCertificateNumber());
                        r.setReceivedNetWeight(it.getReceivedNetWeight());
                        r.setReceivedNo(it.getReceivedNo());
                        r.setValue(null);
                        r.setRate(null);
                        return r;
                    }).collect(Collectors.toList())
            );

            result.add(dto);
        }

        return result;
    }



    @Override
    public GRNResponseDTO markBinCompleted(Long id) {
        GRNEntity grn = grnRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("GRN not found with ID: " + id));
        grn.setBinStatus("COMPLETED");
        GRNEntity updated = grnRepository.save(grn);
        return mapToResponse(updated);
    }

    @Override
    public InitiateStockTransferResponseDto initiateStockTransferFromGRN(Long grnId) {
        // Fetch GRN
        GRNEntity grn = grnRepository.findById(grnId)
                .orElseThrow(() -> new RuntimeException("GRN not found with ID: " + grnId));

        // Generate unique transfer number
        String transferNumber = generateUniqueTransferNumber();

        // Create Stock Transfer entity with all GRN data
        StockTransferEntity stockTransfer = new StockTransferEntity();
        stockTransfer.setTransferNumber(transferNumber);
        stockTransfer.setTransferType("RM INWARD");
        stockTransfer.setTransferStage("INITIATED");
        stockTransfer.setStatus(1);
        stockTransfer.setDeleteFlag(0);
        stockTransfer.setCreatedBy("SYSTEM");

        // Populate GRN fields
        stockTransfer.setGrnRefNumber(grn.getGrnRefNumber());
        stockTransfer.setInvoiceNumber(grn.getInvoiceNumber());
        stockTransfer.setUnit(grn.getUnit());

        // Get first GRN item for item details (if exists)
        if (grn.getGrnItems() != null && !grn.getGrnItems().isEmpty()) {
            GRNItemEntity firstItem = grn.getGrnItems().get(0);
            stockTransfer.setItemDescription(firstItem.getItemDescription());
            stockTransfer.setSectionNumber(firstItem.getSectionNumber());
            stockTransfer.setProductCategory(firstItem.getItemDescription()); // Using itemDescription as category
            stockTransfer.setBrand(""); // Default empty, can be updated later
            stockTransfer.setGrade(firstItem.getGrade());
            stockTransfer.setTemper(firstItem.getTemper());

            // GRN Quantities
            stockTransfer.setGrnQuantityNetWeight(firstItem.getReceivedNetWeight());
            stockTransfer.setGrnQuantityNetWeightUom(firstItem.getUom());
            stockTransfer.setGrnQuantityNo(firstItem.getReceivedNo());
            stockTransfer.setGrnQuantityNoUom("NOS");

            // Initialize added quantities (will be updated when bundles are added)
            stockTransfer.setAddedQuantityNetWeight(0.0);
            stockTransfer.setAddedQuantityNetWeightUom(firstItem.getUom());
            stockTransfer.setAddedQuantityNo(0);
            stockTransfer.setAddedQuantityNoUom("NOS");
            stockTransfer.setNumberOfBundles(0);
        }

        // Warehouse storage fields (defaults - to be updated later)
        stockTransfer.setCurrentStore("Warehouse");
        stockTransfer.setRecipientStore("");
        stockTransfer.setStorageArea("Common Storage");
        stockTransfer.setRackColumnBinNumber("Common Bin");

        // Save Stock Transfer
        StockTransferEntity savedTransfer = stockTransferRepository.save(stockTransfer);

        // Build and return response
        return InitiateStockTransferResponseDto.builder()
                .stockTransferId(savedTransfer.getId())
                .transferNumber(savedTransfer.getTransferNumber())
                .grnId(grn.getId())
                .grnRefNumber(grn.getGrnRefNumber())
                .invoiceNumber(grn.getInvoiceNumber())
                .poNumber(grn.getPoNumber())
                .supplierName(grn.getSupplierName())
                .supplierCode(grn.getSupplierCode())
                .unit(grn.getUnit())
                .binStatus(grn.getBinStatus())
                .createdAt(savedTransfer.getDateTime())
                .success(true)
                .message("Stock Transfer initiated successfully from GRN: " + grn.getGrnRefNumber())
                .build();
    }

    private String generateUniqueTransferNumber() {
        String prefix = "MEST";
        String datePart = new SimpleDateFormat("yyMM").format(new Date());
        int seq = 1;
        String candidate;
        do {
            candidate = prefix + datePart + String.format("%03d", seq++);
        } while (stockTransferRepository.existsByTransferNumber(candidate));
        return candidate;
    }

    @Override
    public GRNResponseDTO saveGrnWithPo(GRNRequestDTO request) {
        if (request.getPoNumber() == null || request.getPoNumber().isEmpty()) {
            throw new RuntimeException("PO Number is required for this operation.");
        }
        return createGRN(request);
    }

    @Override
    public GRNResponseDTO saveGrnWithoutPo(GRNRequestDTO request) {
        request.setPoNumber(new ArrayList<>());
        if (request.getItemsWithoutPO() == null || request.getItemsWithoutPO().isEmpty()) {
            throw new RuntimeException("Items are required for saving GRN without PO.");
        }
        return createGRN(request);
    }

    private boolean itemAlreadyExists(List<GRNItemEntity> list, GRNItemEntity newItem) {
        return list.stream().anyMatch(i ->
                Objects.equals(i.getItemDescription(), newItem.getItemDescription()) &&
                        Objects.equals(i.getSectionNumber(), newItem.getSectionNumber()) &&
                        Objects.equals(i.getGrade(), newItem.getGrade()) &&
                        Objects.equals(i.getTemper(), newItem.getTemper())
        );
    }

    private List<GRNItemEntity> buildItemsFromMR(String mrNumber) {

        List<MaterialRequestSummaryHeader> mrHeaders =
                materialRequestSummaryHeaderRepository.findByMrNumberWithItems(mrNumber);

        if (mrHeaders.isEmpty()) {
            throw new RuntimeException("MR Number not found: " + mrNumber);
        }

        MaterialRequestSummaryHeader header = mrHeaders.get(0);

        List<GRNItemEntity> items = new ArrayList<>();

        for (MaterialRequestSummaryItem li : header.getItems()) {

            GRNItemEntity item = new GRNItemEntity();
            item.setItemDescription(li.getItemDescription());
            item.setProductCategory(li.getProductCategory());
            item.setSectionNumber(li.getLineNumber()); // MR Line Number
            item.setBrand(li.getBrand());
            item.setGrade(li.getGrade());
            item.setTemper(li.getTemper());
            item.setUom(li.getUom());
            item.setPoQuantityKg(li.getRequiredQuantity() != null ? (int) li.getRequiredQuantity().doubleValue() : 0.00);
            item.setRate(null);
            item.setValue(null);
            item.setTestCertificateNumber(null); // MR never contains certificate
            items.add(item);
        }

        return items;
    }

    private GRNInterUnitEntity createInterUnitGRN(GateInwardEntity inward,
                                                  DeliveryChallanCreationIUMTEntity dc,
                                                  List<MaterialRequestSummaryItem> mrLines) {

        GRNInterUnitEntity grnIU = new GRNInterUnitEntity();
        grnIU.setCreatedAt(new Date());
        grnIU.setGrnInterUnitRefNumber("MEGIU" + System.currentTimeMillis());
        grnIU.setMedcNumber(inward.getMedcNumber());
        grnIU.setMrNumber(dc.getMrNumber());
        grnIU.setGateEntryRefNo(inward.getGatePassRefNumber());
        grnIU.setVehicleNumber(dc.getVehicleNumberPackingAndDispatch());
        grnIU.setUnit(dc.getUnit());
        grnIU.setSupplierUnit(dc.getUnit());
        grnIU.setMode("inter unit transfer");

        grnIU.setInvoiceDocument(first(inward.getInvoiceScanUrls()));
        grnIU.setTestCertificateDocument(first(inward.getTestCertificateScanUrls()));
        grnIU.setEwayBillDocument(first(inward.getEWayBillScanUrls()));
        grnIU.setVehicleDocuments(first(inward.getVehicleDocumentsScanUrls()));

        List<GRNInterUnitItemEntity> items = mrLines.stream().map(li -> {
            return GRNInterUnitItemEntity.builder()
                    .itemDescription(li.getItemDescription())
                    .productCategory(li.getProductCategory())
                    .lineNumber(li.getLineNumber())
                    .brand(li.getBrand())
                    .grade(li.getGrade())
                    .temper(li.getTemper())
                    .uom(li.getUom())
                    .quantityKg(li.getRequiredQuantity() != null ? li.getRequiredQuantity().doubleValue() : 0)
                    .grnInterUnit(grnIU)
                    .build();
        }).collect(Collectors.toList());

        grnIU.setItems(items);

        return grnInterUnitRepository.save(grnIU);
    }

    // ============ SALES ORDER SCHEDULER INTEGRATION ============
    /**
     * Create SalesOrderScheduler entries from GRN data
     * Extracts PO number and item descriptions, then fetches SO details from PO Generation
     * and SO Summary to populate all required fields
     */
    private void createSalesOrderSchedulerFromGRN(GRNEntity grn) {
        if (grn == null || grn.getPoNumber() == null || grn.getPoNumber().isBlank()) {
            System.out.println("⚠️ No PO Number in GRN, skipping Sales Order Scheduler creation");
            return;
        }

        String poNumber = grn.getPoNumber();
        System.out.println("📦 Processing GRN for PO: " + poNumber);

        // Fetch PO details
        POGenerationEntity poEntity = poGenerationRepository.findByPoNumber(poNumber)
                .orElse(null);

        if (poEntity == null) {
            System.out.println("⚠️ PO not found: " + poNumber);
            return;
        }

        List<POGenerationItemEntity> poItems = poEntity.getItems();
        if (poItems == null || poItems.isEmpty()) {
            System.out.println("⚠️ No items in PO: " + poNumber);
            return;
        }

        // Fetch SO Summary by PO Number (assuming PO Number maps to SO Number)
        SoSummaryEntity soSummary = soSummaryRepository.findBySoNumber(poNumber);
        if (soSummary == null) {
            System.out.println("⚠️ SO Summary not found for PO/SO: " + poNumber);
            return;
        }

        System.out.println("✓ Found SO Summary for: " + poNumber);

        // Process each item in GRN
        if (grn.getGrnItems() != null && !grn.getGrnItems().isEmpty()) {
            for (GRNItemEntity grnItem : grn.getGrnItems()) {
                String itemDescription = grnItem.getItemDescription();

                // Find matching PO item
                POGenerationItemEntity matchingPoItem = poItems.stream()
                        .filter(poi -> itemDescription != null &&
                                itemDescription.trim().equalsIgnoreCase(
                                        poi.getItemDescription() != null ? poi.getItemDescription().trim() : ""))
                        .findFirst()
                        .orElse(null);

                if (matchingPoItem == null) {
                    System.out.println("⚠️ No matching PO item for: " + itemDescription);
                    continue;
                }

                String soLineNumber = matchingPoItem.getSoLineNumber();
                if (soLineNumber == null || soLineNumber.isBlank()) {
                    System.out.println("⚠️ No SO Line Number in PO Item");
                    continue;
                }

                // Find matching SO Summary Item by line number
                SoSummaryItemEntity soSummaryItem = null;
                if (soSummary.getItems() != null && !soSummary.getItems().isEmpty()) {
                    soSummaryItem = soSummary.getItems().stream()
                            .filter(item -> soLineNumber.equals(item.getLineNumber()))
                            .findFirst()
                            .orElse(null);
                }

                if (soSummaryItem == null) {
                    System.out.println("⚠️ No matching SO Summary Item for line: " + soLineNumber);
                    continue;
                }

                System.out.println("✓ Found SO Summary Item for line: " + soLineNumber);

                // Create SalesOrderSchedulerDTO with data from SO Summary and GRN
                SalesOrderSchedulerDTO schedulerDto = SalesOrderSchedulerDTO.builder()
                        .soNumber(poNumber)
                        .lineNumber(soLineNumber)
                        .unit(grn.getUnit())
                        // Customer Details from SO Summary
                        .customerCode(soSummary.getCustomerCode())
                        .customerName(soSummary.getCustomerName())
                        .customerCategory(soSummary.getCustomerCode() != null ? "REGULAR" : "")
                        // Item Details from SO Summary Item
                        .itemDescription(soSummaryItem.getItemDescription())
                        .productCategory(soSummaryItem.getProductCategory())
                        .brand(soSummaryItem.getBrand())
                        .grade(soSummaryItem.getGrade())
                        .temper(soSummaryItem.getTemper())
                        .dimension(soSummaryItem.getDimension() != null ? soSummaryItem.getDimension() : "")
                        .orderType(soSummaryItem.getOrderType() != null ? soSummaryItem.getOrderType() : "FULL")
                        // UOM Details
                        .uomKg(soSummaryItem.getUomKg())
                        .uomNo(soSummaryItem.getUomNo())
                        // Quantity Details
                        .requiredQuantityKg(soSummaryItem.getOrderQuantityKg() != null ?
                                soSummaryItem.getOrderQuantityKg() : new java.math.BigDecimal(0))
                        .requiredQuantityNo(soSummaryItem.getOrderQuantityNo() != null ?
                                soSummaryItem.getOrderQuantityNo() : 0)
                        // Packing Status from SO Summary
                        .packing(soSummary.getPackingStatus())
                        // Process and Status
                        .nextProcess("MARKING & CUTTING")
                        .retrievalStatus("PENDING")
                        .targetDateOfDispatch(soSummaryItem.getTargetDispatchDate() != null ?
                                soSummaryItem.getTargetDispatchDate() : LocalDate.now())
                        .build();

                // Save to Sales Order Scheduler
                try {
                    List<SalesOrderSchedulerDTO> dtoList = new ArrayList<>();
                    dtoList.add(schedulerDto);
                    salesOrderSchedulerService.saveSchedule(dtoList);
                    System.out.println("✅ Sales Order Scheduler entry created for SO: " + poNumber +
                            ", Line: " + soLineNumber +
                            ", Customer: " + soSummary.getCustomerName());
                } catch (Exception e) {
                    System.err.println("❌ Error creating Sales Order Scheduler entry: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Updated: Extract SO Number and Line Number from PO Summary Overall (POGenerationItemEntity)
     * Then fetch SO Summary using those exact values and populate SalesOrderScheduler
     */
    private void createSalesOrderSchedulerFromGRN_Updated(GRNEntity grn) {
        if (grn == null || grn.getPoNumber() == null || grn.getPoNumber().isBlank()) {
            System.out.println("⚠️ No PO Number in GRN, skipping Sales Order Scheduler creation");
            return;
        }

        String poNumber = grn.getPoNumber();
        String invocationId = "INVOKE_" + System.nanoTime(); // Unique identifier for this method call
        System.out.println("\n🔵 [" + invocationId + "] 📦 Processing GRN for PO: " + poNumber);

        // Step 1: Fetch PO details from PO Summary Overall
        POGenerationEntity poEntity = poGenerationRepository.findByPoNumber(poNumber)
                .orElse(null);

        if (poEntity == null) {
            System.out.println("[" + invocationId + "] ⚠️ PO not found: " + poNumber);
            return;
        }

        List<POGenerationItemEntity> poItems = poEntity.getItems();
        if (poItems == null || poItems.isEmpty()) {
            System.out.println("[" + invocationId + "] ⚠️ No items in PO: " + poNumber);
            return;
        }

        System.out.println("[" + invocationId + "] ✓ Found " + poItems.size() + " items in PO");

        // Process each item in GRN
        if (grn.getGrnItems() != null && !grn.getGrnItems().isEmpty()) {
            for (GRNItemEntity grnItem : grn.getGrnItems()) {
                String itemDescription = grnItem.getItemDescription();
                System.out.println("[" + invocationId + "] 🔍 Processing GRN item: " + itemDescription);

                // Step 2: Find matching PO item by item description
                POGenerationItemEntity matchingPoItem = poItems.stream()
                        .filter(poi -> itemDescription != null &&
                                itemDescription.trim().equalsIgnoreCase(
                                        poi.getItemDescription() != null ? poi.getItemDescription().trim() : ""))
                        .findFirst()
                        .orElse(null);

                if (matchingPoItem == null) {
                    System.out.println("⚠️ No matching PO item for: " + itemDescription);
                    continue;
                }

                // Step 3: Extract SO Number and Line Number from PO Summary Overall
                // soLineNumber format: "SO1765352705905 / 17C27947"
                // where: SO1765352705905 = SO Number, 17C27947 = Line Number
                String fullSoLineNumber = matchingPoItem.getSoLineNumber();

                if (fullSoLineNumber == null || fullSoLineNumber.isBlank()) {
                    System.out.println("⚠️ No SO Line Number in PO Item");
                    continue;
                }

                // Parse SO Number and Line Number from the combined field
                String soNumber = null;
                String soLineNumber = null;

                if (fullSoLineNumber.contains("/")) {
                    // Format: "SO_NUMBER / LINE_NUMBER"
                    String[] parts = fullSoLineNumber.split("/");
                    soNumber = parts[0].trim();  // Extract SO Number (before /)
                    soLineNumber = parts[1].trim();  // Extract Line Number (after /)
                } else {
                    // If no slash, use the whole value as SO Number
                    soNumber = fullSoLineNumber.trim();
                    soLineNumber = "1";  // Default line number
                }

                if (soNumber == null || soNumber.isBlank()) {
                    System.out.println("⚠️ SO Number not found in PO item");
                    continue;
                }

                if (soLineNumber == null || soLineNumber.isBlank()) {
                    System.out.println("⚠️ Line Number not found in PO item");
                    continue;
                }

                System.out.println("✓ Extracted from PO Summary Overall: SO Number = " + soNumber + ", Line Number = " + soLineNumber);

                // Step 4: Fetch Sales Order by SO Number (not SO Summary!)
                Optional<SalesOrder> salesOrderOpt = salesOrderRepository.findBySoNumber(soNumber);
                if (!salesOrderOpt.isPresent()) {
                    System.out.println("⚠️ Sales Order not found for SO: " + soNumber);
                    continue;
                }

                SalesOrder salesOrder = salesOrderOpt.get();
                System.out.println("✓ Found Sales Order for: " + soNumber);

                // Debug: Check if line items are loaded
                if (salesOrder.getItems() == null) {
                    System.out.println("⚠️ Sales Order Line Items are NULL!");
                    continue;
                }

                if (salesOrder.getItems().isEmpty()) {
                    System.out.println("⚠️ Sales Order Line Items are EMPTY! Count: 0");
                    continue;
                }

                System.out.println("✓ Sales Order has " + salesOrder.getItems().size() + " line items");

                // Step 5: Find matching Sales Order Line Item by line number
                SalesOrderLineItem soLineItem = null;
                if (salesOrder.getItems() != null && !salesOrder.getItems().isEmpty()) {
                    String finalSoLineNumber = soLineNumber;

                    // Debug: Print all available line numbers
                    System.out.println("📋 Available line numbers in Sales Order:");
                    for (SalesOrderLineItem item : salesOrder.getItems()) {
                        System.out.println("   - LineNumber: '" + item.getLineNumber() + "' | ItemDesc: " + item.getItemDescription());
                    }

                    // Try exact match first
                    soLineItem = salesOrder.getItems().stream()
                            .filter(item -> item.getLineNumber() != null &&
                                    finalSoLineNumber.trim().equalsIgnoreCase(item.getLineNumber().trim()))
                            .findFirst()
                            .orElse(null);

                    // If exact match fails, try case-insensitive match
                    if (soLineItem == null) {
                        System.out.println("   ℹ️ Exact match not found, trying case-insensitive match...");
                        soLineItem = salesOrder.getItems().stream()
                                .filter(item -> item.getLineNumber() != null &&
                                        item.getLineNumber().trim().equalsIgnoreCase(finalSoLineNumber.trim()))
                                .findFirst()
                                .orElse(null);
                    }

                    // If still not found, try partial string match
                    if (soLineItem == null) {
                        System.out.println("   ℹ️ Case-insensitive match not found, trying partial match...");
                        soLineItem = salesOrder.getItems().stream()
                                .filter(item -> item.getLineNumber() != null &&
                                        (item.getLineNumber().trim().toUpperCase().contains(finalSoLineNumber.trim().toUpperCase()) ||
                                                finalSoLineNumber.trim().toUpperCase().contains(item.getLineNumber().trim().toUpperCase())))
                                .findFirst()
                                .orElse(null);
                    }
                }

                if (soLineItem == null) {
                    System.out.println("❌ No matching Sales Order Line Item for line: " + soLineNumber);
                    System.out.println("   Searched for: '" + soLineNumber.trim() + "'");
                    continue;
                }

                System.out.println("✓ Found Sales Order Line Item for line: " + soLineNumber);

                // Step 6a: Fetch Customer Entity to get customer category
                CustomerMasterEntity customer = null;
                String customerCategoryValue = "";
                if (salesOrder.getCustomerCode() != null) {
                    customer = customerMasterRepository.findByCustomerCode(salesOrder.getCustomerCode());
                    if (customer != null) {
                        customerCategoryValue = customer.getCustomerCategory() != null ? customer.getCustomerCategory() : "";
                        System.out.println("✓ Found Customer Category: " + customerCategoryValue);
                    }
                }

                // Step 6: Create SalesOrderSchedulerDTO with EXACT SO number and line number from PO Summary
                SalesOrderSchedulerDTO schedulerDto = SalesOrderSchedulerDTO.builder()
                        // EXACT SO Number and Line Number from PO Summary Overall
                        .soNumber(soNumber)
                        .lineNumber(soLineNumber)
                        .unit(grn.getUnit())
                        // Customer Details from Sales Order
                        .customerCode(salesOrder.getCustomerCode())
                        .customerName(salesOrder.getCustomerName())
                        .customerCategory(customerCategoryValue)  // From Customer Master Entity
                        .primeCustomer(salesOrder.getCustomerName())
                        // Item Details from Sales Order Line Item
                        .itemDescription(soLineItem.getItemDescription())
                        .productCategory(soLineItem.getProductCategory())
                        .brand(soLineItem.getBrand())
                        .grade(soLineItem.getGrade())
                        .temper(soLineItem.getTemper())
                        .dimension(soLineItem.getDimension() != null ? soLineItem.getDimension() : "")
                        .orderType(soLineItem.getOrderType() != null ? soLineItem.getOrderType() : "FULL")
                        // UOM Details from Sales Order Line Item
                        .uomKg(soLineItem.getUomKg() != null ? soLineItem.getUomKg() : "KGS")
                        .uomNo(soLineItem.getUomNos() != null ? soLineItem.getUomNos() : "PCS")
                        // Quantity Details
                        .requiredQuantityKg(new BigDecimal(soLineItem.getQuantityKg()))
                        .requiredQuantityNo((int) soLineItem.getQuantityNos())
                        // Packing Status from Sales Order
                        .packing(salesOrder.getPackingRequired())
                        // Process and Status - Determine dynamically based on productionStrategy and orderType
                        .nextProcess(determineNextProcess(
                                soLineItem.getProductionStrategy(),
                                soLineItem.getOrderType()
                        ))
                        .retrievalStatus("PENDING")
                        .targetDateOfDispatch(salesOrder.getTargetDispatchDate() != null ?
                                salesOrder.getTargetDispatchDate() : LocalDate.now())  // From Sales Order, not line item
                        .build();

                // Step 7: Save to Sales Order Scheduler (with duplicate prevention)
                try {
                    // Check if entry already exists with same SO number and line number
                    SalesOrderSchedulerEntity existingEntry = salesOrderSchedulerRepository.findBySoNumberAndLineNumber(soNumber, soLineNumber);

                    if (existingEntry != null) {
                        System.out.println("ℹ️ Sales Order Scheduler entry already exists for SO: " + soNumber + ", Line: " + soLineNumber);
                        System.out.println("   Skipping duplicate entry creation");
                        continue;
                    }

                    List<SalesOrderSchedulerDTO> dtoList = new ArrayList<>();
                    dtoList.add(schedulerDto);
                    salesOrderSchedulerService.saveSchedule(dtoList);
                    System.out.println("✅ Sales Order Scheduler entry created:");
                    System.out.println("   SO Number: " + soNumber + " (from PO Summary Overall)");
                    System.out.println("   Line Number: " + soLineNumber + " (from PO Summary Overall)");
                    System.out.println("   Item: " + soLineItem.getItemDescription());
                    System.out.println("   Customer: " + salesOrder.getCustomerName());
                } catch (Exception e) {
                    System.err.println("❌ Error creating Sales Order Scheduler entry: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Determine next process based on production strategy and order type
     * Logic:
     * - If productionStrategy = "INHOUSE" AND orderType = "FULL" → DISPATCH
     * - If productionStrategy = "INHOUSE" AND orderType = "CUT" → MARKING & CUTTING
     * - If productionStrategy = "JOBWORK" AND (orderType = "FULL" OR "CUT") → DISPATCH
     * - Packing status is ignored (not required for logic)
     */
    private String determineNextProcess(String productionStrategy, String orderType) {
        if (productionStrategy == null || productionStrategy.isBlank()) {
            return "DISPATCH"; // default
        }

        if (orderType == null || orderType.isBlank()) {
            orderType = "FULL"; // default
        }

        String strategy = productionStrategy.trim().toUpperCase();
        String type = orderType.trim().toUpperCase();

        if ("INHOUSE".equals(strategy)) {
            if ("CUT".equals(type)) {
                return "MARKING & CUTTING";
            } else {
                return "DISPATCH"; // FULL or default
            }
        } else if ("JOBWORK".equals(strategy)) {
            return "DISPATCH"; // Both FULL and CUT → DISPATCH for JOBWORK
        }

        return "DISPATCH"; // default fallback
    }

    @Override
    public void deleteAllGRNs() {
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║       🗑️  DELETE ALL GRN SUMMARIES     ║");
        System.out.println("╚════════════════════════════════════════╝\n");

        try {
            long totalCount = grnRepository.count();
            System.out.println("📊 Total GRN summaries before deletion: " + totalCount);

            grnRepository.deleteAll();

            long afterCount = grnRepository.count();
            System.out.println("✅ All GRN summaries deleted successfully!");
            System.out.println("📊 Total GRN summaries after deletion: " + afterCount);
            System.out.println("\n╔════════════════════════════════════════╗");
            System.out.println("║     ✅ DELETION COMPLETE               ║");
            System.out.println("╚════════════════════════════════════════╝\n");
        } catch (Exception e) {
            System.err.println("❌ Error deleting all GRN summaries: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to delete all GRN summaries: " + e.getMessage());
        }
    }

    @Transactional
    @Override
    public GRNResponseDTO approveGRN(GRNApprovalRequestDTO request) {
        GRNEntity grn = grnRepository.findById(request.getGrnId())
                .orElseThrow(() -> new RuntimeException("GRN not found with ID: " + request.getGrnId()));

        // ✅ Step 1: Approve the GRN
        grn.setStatus("APPROVED");
        grn.setUpdatedAt(new Date());
        GRNEntity updated = grnRepository.save(grn);
        log.info("✅ GRN ID {} approved successfully", request.getGrnId());

        // ✅ Step 2: Process items with merge logic
        log.info("📦 Processing stock summary for approved GRN: {}", request.getGrnId());

        // Default warehouse values
//        String store = "Warehouse1";
//        String storageArea = "COMMON1";
//        String binCapacity = "UNLIMITED1";
//        String binNo = "Common bin1";
//        String rack = "";

        // Use grnRefNo from request or fallback to database value
        String grnRefNoToUse = request.getGrnRefNo() != null ? request.getGrnRefNo() : updated.getGrnRefNumber();

        // Get items list from request
        java.util.List<GRNApprovalItemDTO> items = request.getItems();

        if (items == null || items.isEmpty()) {
            log.warn("⚠️ No items provided in approval request");
            throw new RuntimeException("At least one item must be provided for GRN approval");
        }

        log.info("   📊 Processing {} items for GRN {}", items.size(), request.getGrnId());

        int successCount = 0;
        int mergeCount = 0;
        int createCount = 0;
        int errorCount = 0;

        // ✅ Step 3: Loop through each item and create or merge StockSummary entries
        for (int i = 0; i < items.size(); i++) {
            GRNApprovalItemDTO item = items.get(i);

            try {
                log.info("   🔄 Processing item {} of {} - {}", (i + 1), items.size(), item.getItemDescription());

                // Note: GRN Approve adds items to Common Bin, no cleanup needed here
                // Cleanup is done in Add Bundles when items move from Common Bin to specific rack

                // 🔍 STEP 1: Check if same item description + unit + dimension + itemGroup(RAW MATERIAL) already exists in StockSummary
                // with store = "WAREHOUSE" and storageArea = "COMMON"
                log.info("      🔍 STEP 1: Checking for existing entry in Common Bin...");
                log.info("         Search Criteria:");
                log.info("            - ItemDescription: '{}'", item.getItemDescription());
                log.info("            - Unit: '{}'", request.getUnit());
                log.info("            - Dimension: '{}'", item.getDimension());
                log.info("            - ItemGroup: 'RAW MATERIAL' (must match) OR empty/null (will be updated)");
                log.info("            - Store: 'WAREHOUSE'");
                log.info("            - StorageArea: 'COMMON'");

                String itemDimension = item.getDimension() != null ? item.getDimension() : "";

                // First, find entries with RAW MATERIAL itemGroup
                java.util.List<StockSummaryEntity> existingStocks = stockSummaryRepository.findAll().stream()
                        .filter(stock ->
                            stock.getItemDescription() != null &&
                            stock.getItemDescription().equalsIgnoreCase(item.getItemDescription()) &&
                            stock.getUnit() != null &&
                            stock.getUnit().equalsIgnoreCase(request.getUnit()) &&
                            "Warehouse".equalsIgnoreCase(stock.getStore()) &&
                            "COMMON".equalsIgnoreCase(stock.getStorageArea()) &&
                            // Match dimension (both null/empty or equal)
                            ((stock.getDimension() == null || stock.getDimension().isEmpty()) && itemDimension.isEmpty() ||
                             (stock.getDimension() != null && stock.getDimension().equalsIgnoreCase(itemDimension))) &&
                            // Must be RAW MATERIAL
                            "RAW MATERIAL".equalsIgnoreCase(stock.getItemGroup())
                        )
                        .toList();

                log.info("         Found {} matching entries with RAW MATERIAL itemGroup", existingStocks.size());

                // If no RAW MATERIAL entry found, check for entries with empty/null itemGroup
                if (existingStocks.isEmpty()) {
                    log.info("         🔍 No RAW MATERIAL entry found, checking for entries with empty itemGroup...");

                    java.util.List<StockSummaryEntity> emptyItemGroupStocks = stockSummaryRepository.findAll().stream()
                            .filter(stock ->
                                stock.getItemDescription() != null &&
                                stock.getItemDescription().equalsIgnoreCase(item.getItemDescription()) &&
                                stock.getUnit() != null &&
                                stock.getUnit().equalsIgnoreCase(request.getUnit()) &&
                                "Warehouse".equalsIgnoreCase(stock.getStore()) &&
                                "COMMON".equalsIgnoreCase(stock.getStorageArea()) &&
                                // Match dimension (both null/empty or equal)
                                ((stock.getDimension() == null || stock.getDimension().isEmpty()) && itemDimension.isEmpty() ||
                                 (stock.getDimension() != null && stock.getDimension().equalsIgnoreCase(itemDimension))) &&
                                // itemGroup is empty or null
                                (stock.getItemGroup() == null || stock.getItemGroup().isEmpty())
                            )
                            .toList();

                    log.info("         Found {} entries with empty/null itemGroup", emptyItemGroupStocks.size());

                    if (!emptyItemGroupStocks.isEmpty()) {
                        // Use these entries and update itemGroup to RAW MATERIAL
                        existingStocks = emptyItemGroupStocks;
                        log.info("         ✅ Will update these entries with itemGroup = RAW MATERIAL");
                    }
                }

                if (!existingStocks.isEmpty()) {
                    // ✅ MERGE CASE: Update existing entry
                    log.info("      ♻️ MERGE: Found existing entry for {} - {}", item.getItemDescription(), request.getUnit());

                    StockSummaryEntity existing = existingStocks.get(0);
                    log.info("         Existing Entry ID: {}, Current ItemGroup: {}", existing.getId(), existing.getItemGroup());

                    // Merge quantities
                    BigDecimal currentQtyKg = existing.getQuantityKg() != null ? existing.getQuantityKg() : BigDecimal.ZERO;
                    Integer currentQtyNo = existing.getQuantityNo() != null ? existing.getQuantityNo() : 0;

                    BigDecimal newQtyKg = item.getNetQuantityKg() != null ? item.getNetQuantityKg() : BigDecimal.ZERO;
                    Integer newQtyNo = item.getNetQuantityNo() != null ? item.getNetQuantityNo() : 0;

                    BigDecimal mergedQtyKg = currentQtyKg.add(newQtyKg);
                    Integer mergedQtyNo = currentQtyNo + newQtyNo;

                    existing.setQuantityKg(mergedQtyKg);
                    existing.setQuantityNo(mergedQtyNo);

                    // Update price to latest
                    if (item.getItemPrice() != null) {
                        existing.setItemPrice(item.getItemPrice());
                    }

                    // Update dimension if provided
                    if (item.getDimension() != null && !item.getDimension().isEmpty()) {
                        existing.setDimension(item.getDimension());
                    }

                    // Always set itemGroup to RAW MATERIAL
                    existing.setItemGroup("RAW MATERIAL");
                    existing.setRackColumnShelfNumber("Common bin");

                    // Append GRN Ref No if not already present
                    String currentGrnNumbers = existing.getGrnNumbers() != null ? existing.getGrnNumbers() : "";
                    if (!currentGrnNumbers.contains(grnRefNoToUse)) {
                        if (currentGrnNumbers.isEmpty()) {
                            existing.setGrnNumbers(grnRefNoToUse);
                        } else {
                            existing.setGrnNumbers(currentGrnNumbers + ", " + grnRefNoToUse);
                        }
                    }

                    StockSummaryEntity updated_stock = stockSummaryRepository.save(existing);
                    log.info("      ✅ MERGED: Item {} - Old Qty: {} KG, New Qty: {} KG, Total: {} KG",
                            item.getItemDescription(),
                            currentQtyKg,
                            newQtyKg,
                            mergedQtyKg);
                    log.info("         Dimension: {}", updated_stock.getDimension());
                    log.info("         ItemGroup: {}", updated_stock.getItemGroup());
                    log.info("         GRN Ref Nos: {}", updated_stock.getGrnNumbers());

                    // ✅ Create StockSummaryBundleEntity for the merged item
                    StockSummaryBundleEntity bundleEntity = StockSummaryBundleEntity.builder()
                            .stockSummary(updated_stock)
                            .grnNumber(grnRefNoToUse)
                            .grnId(request.getGrnId())
                            .itemDescription(item.getItemDescription())
                            .productCategory(item.getProductCategory())
                            .sectionNumber(item.getSectionNo())
                            .brand(item.getBrand())
                            .grade(item.getGrade())
                            .temper(item.getTemper())
                            .dimension(item.getDimension())
                            .weightmentQuantityKg(item.getNetQuantityKg())
                            .weightmentQuantityNo(item.getNetQuantityNo())
                            .heatNo(item.getHeatNumber())
                            .lotNo(item.getLotNumber())
                            .testCertificate(item.getTestCertificateNumber())
                            .itemPrice(item.getItemPrice())
                            .currentStore("Warehouse")
                            .storageArea("COMMON")
                            .rackColumnBinNumber("Common bin")
                            .status("ADDED")
                            .build();
                    StockSummaryBundleEntity savedBundle = stockSummaryBundleRepository.save(bundleEntity);
                    log.info("      ✅ BUNDLE CREATED for MERGE: Bundle ID: {}, GRN: {}", savedBundle.getId(), grnRefNoToUse);

                    mergeCount++;
                    successCount++;

                } else {
                    // ✅ CREATE CASE: New entry with itemGroup = RAW MATERIAL
                    log.info("      ✨ CREATE: New entry for {} - {} with itemGroup=RAW MATERIAL", item.getItemDescription(), request.getUnit());

                    StockSummaryEntity stockSummary = StockSummaryEntity.builder()
                            .unit(request.getUnit())
                            .itemDescription(item.getItemDescription())
                            .quantityKg(item.getNetQuantityKg())
                            .quantityNo(item.getNetQuantityNo())
                            .itemPrice(item.getItemPrice())
                            .productCategory(item.getProductCategory())
                            .brand(item.getBrand())
                            .grade(item.getGrade())
                            .temper(item.getTemper())
                            .itemPrice(item.getItemPrice())
                            .dimension(item.getDimension())       // Add dimension
                            .itemGroup("RAW MATERIAL")            // Set itemGroup to RAW MATERIAL
                            .store("Warehouse")                         // Warehouse
                            .storageArea("COMMON")             // COMMON
                            .rackColumnShelfNumber("Common bin")          // ""
                            .materialType(item.getMaterialType())
                            .sectionNo(item.getSectionNo())
                            .reprintQr(false)
                            .pickListLocked(false)
                            .grnNumbers(grnRefNoToUse)           // Store GRN reference number
                            .build();

                    StockSummaryEntity savedStock = stockSummaryRepository.save(stockSummary);
                    log.info("      ✅ CREATED: Item {} saved - ID: {}, Qty KG: {}, Qty No: {}, Dimension: {}",
                            item.getItemDescription(),
                            savedStock.getId(),
                            item.getNetQuantityKg(),
                            item.getNetQuantityNo(),
                            savedStock.getDimension());
                    log.info("         ItemGroup: {}", savedStock.getItemGroup());
                    log.info("         GRN Ref No: {}", savedStock.getGrnNumbers());

                    // ✅ Create StockSummaryBundleEntity for the new stock summary
                    StockSummaryBundleEntity bundleEntity = StockSummaryBundleEntity.builder()
                            .stockSummary(savedStock)
                            .grnNumber(grnRefNoToUse)
                            .grnId(request.getGrnId())
                            .itemDescription(item.getItemDescription())
                            .productCategory(item.getProductCategory())
                            .sectionNumber(item.getSectionNo())
                            .brand(item.getBrand())
                            .grade(item.getGrade())
                            .temper(item.getTemper())
                            .dimension(item.getDimension())
                            .weightmentQuantityKg(item.getNetQuantityKg())
                            .weightmentQuantityNo(item.getNetQuantityNo())
                            .heatNo(item.getHeatNumber())
                            .lotNo(item.getLotNumber())
                            .itemPrice(item.getItemPrice())
                            .testCertificate(item.getTestCertificateNumber())
                            .currentStore("Warehouse")
                            .storageArea("COMMON")
                            .rackColumnBinNumber("Common bin")
                            .status("ADDED")
                            .build();
                    StockSummaryBundleEntity savedBundle = stockSummaryBundleRepository.save(bundleEntity);
                    log.info("      ✅ BUNDLE CREATED: Bundle ID: {}, GRN: {}", savedBundle.getId(), grnRefNoToUse);

                    createCount++;
                    successCount++;
                }

            } catch (Exception e) {
                errorCount++;
                log.error("      ❌ Error processing item {}: {}", item.getItemDescription(), e.getMessage());
            }
        }

        // Log final summary
        log.info("✅ Stock summary processing complete");
        log.info("   - Created: {}, Merged: {}, Total Processed: {}, Errors: {}",
                createCount, mergeCount, successCount, errorCount);

        if (mergeCount > 0) {
            log.info("🔄 {} entries were merged with existing inventory", mergeCount);
        }
        if (createCount > 0) {
            log.info("✨ {} new entries were created in inventory", createCount);
        }

        // ✅ Step 4: Update RackBinMaster currentStorage with sum of receivedNetWeight from GRN items
        log.info("📦 Step 4: Updating RackBinMaster currentStorage...");

        // Sum all receivedNetWeight from GRN items
        double totalReceivedNetWeight = 0.0;
        if (grn.getGrnItems() != null && !grn.getGrnItems().isEmpty()) {
            for (GRNItemEntity grnItem : grn.getGrnItems()) {
                if (grnItem.getReceivedNetWeight() != null) {
                    totalReceivedNetWeight += grnItem.getReceivedNetWeight();
                }
            }
        }
        log.info("   📊 Total receivedNetWeight from GRN items: {} KG", totalReceivedNetWeight);

        // Search criteria for RackBinMaster
        String rackUnitName = grn.getUnit();
        String rackStorageArea = "Common";
        String rackStorageType = "Warehouse";
        String rackBinNo = "Common Bin";

        log.info("   🔍 Searching RackBinMaster with criteria:");
        log.info("      - unitName: '{}'", rackUnitName);
        log.info("      - storageArea: '{}'", rackStorageArea);
        log.info("      - storageType: '{}'", rackStorageType);
        log.info("      - binNo: '{}'", rackBinNo);

        // Find the RackBinMaster entity with matching criteria
        Optional<RackBinMasterEntity> rackBinOpt = rackBinMasterRepository
                .findByStorageTypeAndBinNoAndStorageAreaAndUnitName(
                        rackStorageType,
                        rackBinNo,
                        rackStorageArea,
                        rackUnitName
                );

        RackBinMasterEntity rackBin;
        if (rackBinOpt.isPresent()) {
            rackBin = rackBinOpt.get();
        } else {
            // Auto-create default Common Bin for this unit if not found
            log.warn("   ⚠️ RackBinMaster not found for unit '{}', creating default Common Bin...", rackUnitName);
            rackBin = RackBinMasterEntity.builder()
                    .unitName(rackUnitName)
                    .storageType(rackStorageType)
                    .storageArea(rackStorageArea)
                    .binNo(rackBinNo)
                    .binCapacity(String.valueOf(Double.MAX_VALUE))
                    .currentStorage(0.0)
                    .build();
            rackBin = rackBinMasterRepository.save(rackBin);
            log.info("   ✅ Created default Common Bin with ID: {}", rackBin.getId());
        }

        // Update currentStorage by adding the total received net weight
        double currentStorageKg = rackBin.getCurrentStorage() != null ? rackBin.getCurrentStorage() : 0.0;
        double newStorageKg = currentStorageKg + totalReceivedNetWeight;
        rackBin.setCurrentStorage(newStorageKg);
        rackBinMasterRepository.save(rackBin);

        log.info("   ✅ Updated RackBinMaster (ID: {}) currentStorage: {} KG -> {} KG (added {} KG)",
                rackBin.getId(), currentStorageKg, newStorageKg, totalReceivedNetWeight);

        return mapToResponse(updated);
    }

    @Override
    public GRNResponseDTO rejectGRN(Long id) {
        GRNEntity grn = grnRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("GRN not found with ID: " + id));
        grn.setStatus("REJECTED");
        grn.setUpdatedAt(new Date());
        GRNEntity updated = grnRepository.save(grn);
        log.info("GRN ID {} rejected successfully", id);
        return mapToResponse(updated);
    }

    @Override
    public java.util.Map<String, Object> getGrnBundleDetailsByGrnNumber(String grnNumber) {
        log.info("🔍 [GRN Bundle] Fetching details for GRN Number: {}", grnNumber);

        try {
            // Find GRN by grnRefNumber
            java.util.List<GRNEntity> grnList = grnRepository.findAll().stream()
                    .filter(grn -> grnNumber.equals(grn.getGrnRefNumber()))
                    .toList();

            if (grnList.isEmpty()) {
                log.warn("   ⚠️ GRN not found with number: {}", grnNumber);
                return java.util.Map.of(
                        "success", false,
                        "message", "GRN not found with number: " + grnNumber,
                        "data", new java.util.HashMap<>()
                );
            }

            GRNEntity grn = grnList.stream().findFirst().orElse(null);
            log.info("   ✅ GRN Found - ID: {}, Status: {}", grn.getId(), grn.getStatus());

            // Fetch store, storageArea, rack from StockSummary where grnNumbers contains this GRN
            log.info("   🔍 Fetching store, storageArea, rack from StockSummary...");
            String store = "N/A";
            String storageArea = "N/A";
            String rack = "N/A";

            java.util.List<StockSummaryEntity> stockSummaries = stockSummaryRepository.findAll().stream()
                    .filter(stock -> stock.getGrnNumbers() != null && stock.getGrnNumbers().contains(grnNumber))
                    .toList();

            if (!stockSummaries.isEmpty()) {
                StockSummaryEntity stockSummary = stockSummaries.get(0);
                store = stockSummary.getStore() != null ? stockSummary.getStore() : "N/A";
                storageArea = stockSummary.getStorageArea() != null ? stockSummary.getStorageArea() : "N/A";
                rack = stockSummary.getRackColumnShelfNumber() != null ? stockSummary.getRackColumnShelfNumber() : "N/A";
                log.info("   ✅ Found in StockSummary - Store: {}, StorageArea: {}, Rack: {}", store, storageArea, rack);
            } else {
                log.warn("   ⚠️ GRN {} not found in any StockSummary", grnNumber);
            }

            // Get all GRN items (GRNItemEntity) for this GRN
            java.util.List<GRNItemEntity> grnItems = grn.getGrnItems() != null ? grn.getGrnItems() : new java.util.ArrayList<>();

            log.info("   📦 Found {} GRN items", grnItems.size());

            if (grnItems.isEmpty()) {
                log.warn("   ⚠️ No GRN items found for GRN: {}", grnNumber);
                return java.util.Map.of(
                        "success", false,
                        "message", "No GRN items found for GRN: " + grnNumber,
                        "data", new java.util.HashMap<>()
                );
            }

            // Calculate aggregates
            Double totalQuantityKg = grnItems.stream()
                    .map(item -> item.getReceivedNetWeight() != null ? item.getReceivedNetWeight() : 0.0)
                    .reduce(0.0, Double::sum);

            Integer totalQuantityNo = grnItems.stream()
                    .map(item -> item.getReceivedNo() != null ? item.getReceivedNo() : 0)
                    .reduce(0, Integer::sum);

            Double totalValue = grnItems.stream()
                    .map(item -> item.getValue() != null ? item.getValue() : 0.0)
                    .reduce(0.0, Double::sum);

            Double averagePrice = grnItems.size() > 0 ? totalValue / grnItems.size() : 0.0;

            log.info("   📊 Aggregates - Total Kg: {}, Total No: {}, Avg Price: {}",
                    totalQuantityKg, totalQuantityNo, averagePrice);

            // Get first item for common fields (heat no, lot no, test cert)
            GRNItemEntity firstItem = grnItems.stream().findFirst().orElse(null);

            // Build response
            java.util.Map<String, Object> responseData = new java.util.LinkedHashMap<>();
            responseData.put("grnRefNo", grn.getGrnRefNumber());
            responseData.put("grnId", grn.getId());
            responseData.put("status", grn.getStatus());
            responseData.put("timestamp", grn.getCreatedAt() != null ? grn.getCreatedAt().getTime() : null);

            // Add store, storageArea, rack from StockSummary
            responseData.put("store", store);
            responseData.put("storageArea", storageArea);
            responseData.put("rack", rack);

            responseData.put("bundleCount", grnItems.size());
            responseData.put("totalQuantityKg", totalQuantityKg);
            responseData.put("totalQuantityNo", totalQuantityNo);
            responseData.put("averageItemPrice", averagePrice);
            responseData.put("supplier", grn.getSupplierName());
            responseData.put("invoiceNumber", grn.getInvoiceNumber());

            // Add heat no, lot no, test certificate from first item
            if (firstItem != null) {
                responseData.put("heatNo", firstItem.getHeatNumber() != null ? firstItem.getHeatNumber() : "");
                responseData.put("lotNo", firstItem.getLotNumber() != null ? firstItem.getLotNumber() : "");
                responseData.put("testCertificate", firstItem.getTestCertificateNumber() != null ? firstItem.getTestCertificateNumber() : "");
            } else {
                responseData.put("heatNo", "");
                responseData.put("lotNo", "");
                responseData.put("testCertificate", "");
            }

            // Add bundle details with all information
            java.util.List<java.util.Map<String, Object>> bundleDetails = new java.util.ArrayList<>();
            for (GRNItemEntity item : grnItems) {
                java.util.Map<String, Object> bundleData = new java.util.LinkedHashMap<>();
                bundleData.put("bundleId", item.getId());
                bundleData.put("itemDescription", item.getItemDescription());
                bundleData.put("brand", item.getBrand());
                bundleData.put("grade", item.getGrade());
                bundleData.put("temper", item.getTemper());
                bundleData.put("quantityKg", item.getReceivedNetWeight());
                bundleData.put("quantityNo", item.getReceivedNo());
                bundleData.put("rate", item.getRate());
                bundleData.put("value", item.getValue());
                bundleData.put("heatNo", item.getHeatNumber() != null ? item.getHeatNumber() : "");
                bundleData.put("lotNo", item.getLotNumber() != null ? item.getLotNumber() : "");
                bundleData.put("testCertificate", item.getTestCertificateNumber() != null ? item.getTestCertificateNumber() : "");
                bundleDetails.add(bundleData);
            }

            responseData.put("bundleDetails", bundleDetails);

            log.info("✅ [GRN Bundle] Response prepared successfully with store, storageArea, rack");

            return java.util.Map.of(
                    "success", true,
                    "message", "GRN bundle details fetched successfully",
                    "data", responseData
            );

        } catch (Exception e) {
            log.error("❌ [GRN Bundle] Error: {}", e.getMessage(), e);
            return java.util.Map.of(
                    "success", false,
                    "message", "Error: " + e.getMessage(),
                    "data", new java.util.HashMap<>()
            );
        }
    }

    @Override
    public java.util.Map<String, Object> getGrnBundleDetailsForMultipleGrnNumbers(List<String> grnNumbers) {
        log.info("🔍 [Multi-GRN Bundle] Fetching details for {} GRN numbers", grnNumbers.size());

        try {
            if (grnNumbers == null || grnNumbers.isEmpty()) {
                log.warn("   ⚠️ Empty GRN numbers list provided");
                return java.util.Map.of(
                        "success", false,
                        "message", "GRN numbers list cannot be empty",
                        "data", new java.util.HashMap<>()
                );
            }

            java.util.List<java.util.Map<String, Object>> allBundles = new java.util.ArrayList<>();
            java.util.List<java.util.Map<String, Object>> grnSummaries = new java.util.ArrayList<>();

            // Variables for final summation
            double totalSumQuantityKg = 0.0;
            int totalSumQuantityNo = 0;
            java.util.List<Double> allPrices = new java.util.ArrayList<>();
            java.util.Map<String, String> commonDetails = new java.util.LinkedHashMap<>();
            commonDetails.put("heatNo", "");
            commonDetails.put("lotNo", "");
            commonDetails.put("testCertificate", "");

            log.info("   📋 Processing {} GRN numbers...", grnNumbers.size());

            // Iterate through each GRN number
            for (String grnNumber : grnNumbers) {
                log.info("      🔄 Processing GRN: {}", grnNumber);

                // Find GRN by grnRefNumber
                java.util.List<GRNEntity> grnList = grnRepository.findAll().stream()
                        .filter(grn -> grnNumber.equals(grn.getGrnRefNumber()))
                        .toList();

                if (grnList.isEmpty()) {
                    log.warn("      ⚠️ GRN not found: {}", grnNumber);
                    continue;
                }

                GRNEntity grn = grnList.stream().findFirst().orElse(null);
                log.info("      ✅ GRN Found - ID: {}, Status: {}", grn.getId(), grn.getStatus());

                // Fetch store, storageArea, rack from StockSummary
                String store = "N/A";
                String storageArea = "N/A";
                String rack = "N/A";

                java.util.List<StockSummaryEntity> stockSummaries = stockSummaryRepository.findAll().stream()
                        .filter(stock -> stock.getGrnNumbers() != null && stock.getGrnNumbers().contains(grnNumber))
                        .toList();

                if (!stockSummaries.isEmpty()) {
                    StockSummaryEntity stockSummary = stockSummaries.get(0);
                    store = stockSummary.getStore() != null ? stockSummary.getStore() : "N/A";
                    storageArea = stockSummary.getStorageArea() != null ? stockSummary.getStorageArea() : "N/A";
                    rack = stockSummary.getRackColumnShelfNumber() != null ? stockSummary.getRackColumnShelfNumber() : "N/A";
                }

                // Get all GRN items for this GRN
                java.util.List<GRNItemEntity> grnItems = grn.getGrnItems() != null ? grn.getGrnItems() : new java.util.ArrayList<>();

                if (grnItems.isEmpty()) {
                    log.warn("      ⚠️ No items found for GRN: {}", grnNumber);
                    continue;
                }

                // Calculate aggregates for this GRN
                Double grnTotalQuantityKg = grnItems.stream()
                        .map(item -> item.getReceivedNetWeight() != null ? item.getReceivedNetWeight() : 0.0)
                        .reduce(0.0, Double::sum);

                Integer grnTotalQuantityNo = grnItems.stream()
                        .map(item -> item.getReceivedNo() != null ? item.getReceivedNo() : 0)
                        .reduce(0, Integer::sum);

                Double grnTotalValue = grnItems.stream()
                        .map(item -> item.getValue() != null ? item.getValue() : 0.0)
                        .reduce(0.0, Double::sum);

                Double grnAveragePrice = grnItems.size() > 0 ? grnTotalValue / grnItems.size() : 0.0;

                // Update global totals
                totalSumQuantityKg += grnTotalQuantityKg;
                totalSumQuantityNo += grnTotalQuantityNo;
                allPrices.add(grnAveragePrice);

                // Store common details from first available GRN
                if (commonDetails.get("heatNo").isEmpty()) {
                    GRNItemEntity firstItem = grnItems.stream().findFirst().orElse(null);
                    if (firstItem != null) {
                        commonDetails.put("heatNo", firstItem.getHeatNumber() != null ? firstItem.getHeatNumber() : "");
                        commonDetails.put("lotNo", firstItem.getLotNumber() != null ? firstItem.getLotNumber() : "");
                        commonDetails.put("testCertificate", firstItem.getTestCertificateNumber() != null ? firstItem.getTestCertificateNumber() : "");
                    }
                }

                // Create GRN summary
                java.util.Map<String, Object> grnSummary = new java.util.LinkedHashMap<>();
                grnSummary.put("grnRefNo", grn.getGrnRefNumber());
                grnSummary.put("grnId", grn.getId());
                grnSummary.put("status", grn.getStatus());
                grnSummary.put("timestamp", grn.getCreatedAt() != null ? grn.getCreatedAt().getTime() : null);
                grnSummary.put("dateOfInward", grn.getCreatedAt() != null ? grn.getCreatedAt().toString() : "");
                grnSummary.put("store", store);
                grnSummary.put("storageArea", storageArea);
                grnSummary.put("rack", rack);
                grnSummary.put("bundleCount", grnItems.size());
                grnSummary.put("totalQuantityKg", grnTotalQuantityKg);
                grnSummary.put("totalQuantityNo", grnTotalQuantityNo);
                grnSummary.put("averageItemPrice", grnAveragePrice);
                grnSummary.put("supplier", grn.getSupplierName());
                grnSummary.put("invoiceNumber", grn.getInvoiceNumber());
                grnSummary.put("heatNo", commonDetails.get("heatNo"));
                grnSummary.put("lotNo", commonDetails.get("lotNo"));
                grnSummary.put("testCertificate", commonDetails.get("testCertificate"));

                grnSummaries.add(grnSummary);

                // Add bundle details from GrnLineItemEntity for QR codes
                log.info("      📦 Fetching bundles from GrnLineItemEntity...");
                java.util.List<GrnLineItemEntity> lineItems = grnLineItemRepository.findByGrnNumber(grnNumber);

                // Create a map of item descriptions to their GRN item details (for heat, lot, cert)
                java.util.Map<String, GRNItemEntity> itemDetailsMap = new java.util.HashMap<>();
                if (grnItems != null && !grnItems.isEmpty()) {
                    for (GRNItemEntity grnItem : grnItems) {
                        if (grnItem.getItemDescription() != null) {
                            itemDetailsMap.put(grnItem.getItemDescription(), grnItem);
                        }
                    }
                }

                for (GrnLineItemEntity lineItem : lineItems) {
                    java.util.Map<String, Object> bundleData = new java.util.LinkedHashMap<>();
                    bundleData.put("bundleId", lineItem.getId());
                    bundleData.put("grnRefNo", grnNumber);
                    bundleData.put("dateOfInward", grn.getCreatedAt() != null ? grn.getCreatedAt().toString() : "");
                    bundleData.put("itemDescription", lineItem.getItemDescription());
                    bundleData.put("quantityKg", lineItem.getWeightmentQuantityKg() != null ? lineItem.getWeightmentQuantityKg().doubleValue() : 0.0);
                    bundleData.put("quantityNo", lineItem.getWeightmentQuantityNo() != null ? lineItem.getWeightmentQuantityNo() : 0);

                    // Try to get item details from GRNItemEntity
                    GRNItemEntity grnItemDetail = itemDetailsMap.get(lineItem.getItemDescription());
                    bundleData.put("itemPrice", grnItemDetail != null && grnItemDetail.getRate() != null ? grnItemDetail.getRate() : 0.0);
                    bundleData.put("heatNo", grnItemDetail != null && grnItemDetail.getHeatNumber() != null ? grnItemDetail.getHeatNumber() : "");
                    bundleData.put("lotNo", grnItemDetail != null && grnItemDetail.getLotNumber() != null ? grnItemDetail.getLotNumber() : "");
                    bundleData.put("testCertificate", grnItemDetail != null && grnItemDetail.getTestCertificateNumber() != null ? grnItemDetail.getTestCertificateNumber() : "");

                    bundleData.put("qrCode", lineItem.getQrCode() != null ? lineItem.getQrCode() : "");
                    bundleData.put("qrCodeImageUrl", lineItem.getQrCodeImageUrl() != null ? lineItem.getQrCodeImageUrl() : "");
                    bundleData.put("store", lineItem.getCurrentStore() != null ? lineItem.getCurrentStore() : store);
                    bundleData.put("storageArea", lineItem.getStorageArea() != null ? lineItem.getStorageArea() : storageArea);
                    bundleData.put("rack", lineItem.getRackColumnBinNumber() != null ? lineItem.getRackColumnBinNumber() : rack);

                    allBundles.add(bundleData);
                }

                log.info("      ✅ Processed GRN {} with {} bundles", grnNumber, lineItems.size());
            }

            // Build response with ONLY individual GRN summaries (no combined summary)
            java.util.Map<String, Object> responseData = new java.util.LinkedHashMap<>();
            responseData.put("grnSummaries", grnSummaries);  // Each GRN has its own separate totals
            responseData.put("bundles", allBundles);          // All bundles from all GRNs

            log.info("✅ [Multi-GRN Bundle] Response prepared with {} GRNs and {} bundles", grnSummaries.size(), allBundles.size());

            return java.util.Map.of(
                    "success", true,
                    "message", "Multi-GRN bundle details fetched successfully - Each GRN has separate totals",
                    "data", responseData
            );

        } catch (Exception e) {
            log.error("❌ [Multi-GRN Bundle] Error: {}", e.getMessage(), e);
            return java.util.Map.of(
                    "success", false,
                    "message", "Error: " + e.getMessage(),
                    "data", new java.util.HashMap<>()
            );
        }
    }

    @Override
    @Transactional
    public Map<String, Object> updateMaterialUnloadingStatusCompleted(String gateEntryRefNo, String vehicleNumber) {
        log.info("\n🔍 [Material Unloading Status Update] Starting...");
        log.info("   - Gate Entry Ref No: {}", gateEntryRefNo);
        log.info("   - Vehicle Number: {}", vehicleNumber);

        int grnUpdatedCount = 0;
        int gateInwardUpdatedCount = 0;

        try {
            // ========== UPDATE GRN SUMMARY ==========
            log.info("\n📦 Updating GRN Summary materialUnloadingStatus to COMPLETED...");

            List<GRNEntity> allGrns = grnRepository.findAll();

            // First try to match by refNo + vehicleNumber
            List<GRNEntity> matchedGrns = allGrns.stream()
                    .filter(g -> gateEntryRefNo.equalsIgnoreCase(g.getGateEntryRefNo()))
                    .filter(g -> vehicleNumber.equalsIgnoreCase(g.getVehicleNumber()))
                    .collect(Collectors.toList());

            // If no match found or multiple vehicles exist for same refNo, match by refNo only
            if (matchedGrns.isEmpty()) {
                log.info("   ℹ️ No exact match found, trying to match by refNo only...");
                matchedGrns = allGrns.stream()
                        .filter(g -> gateEntryRefNo.equalsIgnoreCase(g.getGateEntryRefNo()))
                        .collect(Collectors.toList());
            }

            for (GRNEntity grn : matchedGrns) {
                log.info("   ✅ Found GRN: ID={}, RefNo={}, Vehicle={}",
                        grn.getId(), grn.getGateEntryRefNo(), grn.getVehicleNumber());
                grn.setMaterialUnloadingStatus("COMPLETED");
                grnRepository.save(grn);
                grnUpdatedCount++;
                log.info("      ✅ Updated GRN materialUnloadingStatus to COMPLETED");
            }

            // ========== UPDATE GATE INWARD ==========
            log.info("\n🚪 Updating GateInward materialUnloadingStatus to COMPLETED...");

            List<GateInwardEntity> allGateInwards = gateInwardRepository.findAll();

            // First try to match by refNo + vehicleNumber
            List<GateInwardEntity> matchedGateInwards = allGateInwards.stream()
                    .filter(g -> gateEntryRefNo.equalsIgnoreCase(g.getGatePassRefNumber()))
                    .filter(g -> vehicleNumber.equalsIgnoreCase(g.getVehicleNumber()))
                    .collect(Collectors.toList());

            // If no match found or multiple vehicles exist for same refNo, match by refNo only
            if (matchedGateInwards.isEmpty()) {
                log.info("   ℹ️ No exact match found, trying to match by refNo only...");
                matchedGateInwards = allGateInwards.stream()
                        .filter(g -> gateEntryRefNo.equalsIgnoreCase(g.getGatePassRefNumber()))
                        .collect(Collectors.toList());
            }

            for (GateInwardEntity gateInward : matchedGateInwards) {
                log.info("   ✅ Found GateInward: ID={}, RefNo={}, Vehicle={}",
                        gateInward.getId(), gateInward.getGatePassRefNumber(), gateInward.getVehicleNumber());
                gateInward.setMaterialUnloadingStatus("COMPLETED");
                gateInwardRepository.save(gateInward);
                gateInwardUpdatedCount++;
                log.info("      ✅ Updated GateInward materialUnloadingStatus to COMPLETED");
            }

            log.info("\n✅ [Material Unloading Status Update] Complete!");
            log.info("   - GRNs Updated: {}", grnUpdatedCount);
            log.info("   - GateInwards Updated: {}", gateInwardUpdatedCount);

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("success", true);
            response.put("message", "Material unloading status updated to COMPLETED");
            response.put("grnUpdatedCount", grnUpdatedCount);
            response.put("gateInwardUpdatedCount", gateInwardUpdatedCount);
            response.put("gateEntryRefNo", gateEntryRefNo);
            response.put("vehicleNumber", vehicleNumber);

            return response;

        } catch (Exception e) {
            log.error("❌ [Material Unloading Status Update] Error: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new LinkedHashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error updating material unloading status: " + e.getMessage());
            return errorResponse;
        }
    }
}
