package com.indona.invento.services.impl;

import com.indona.invento.dao.*;
import com.indona.invento.dto.AvailableStockDto;
import com.indona.invento.dto.ItemEnquiryDTO;
import com.indona.invento.entities.*;
import com.indona.invento.services.ItemEnquiryService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ItemEnquiryServiceImpl implements ItemEnquiryService {

    @Autowired
    private ItemEnquiryRepository enquiryRepo;

    @Autowired
    private StockSummaryRepository stockSummaryRepository;

    @Autowired
    private ItemMasterRepository itemMasterRepository;

    @Autowired
    private BillingSummaryRepository billingSummaryRepository;

    @Autowired
    private NalcoPriceRepository nalcoPriceRepository;

    @Autowired
    private HindalcoPriceRepository hindalcoPriceRepository;

    @Autowired
    private BlockedQuantityRepository blockedQuantityRepository;

    @Autowired
    private SalesOrderRepository salesOrderRepository;

    @Autowired
    private POGenerationRepository poGenerationRepository;

    @Autowired
    private BlockedQuantityRepository blockedQuantityRepo;

    @Override
    public ItemEnquiry create(ItemEnquiryDTO dto) {
        ItemEnquiry enquiry = new ItemEnquiry();

        enquiry.setQuotationNo(generateQuotationNo());
        enquiry.setUserId(dto.getUserId());
        enquiry.setUnitId(dto.getUnitId());
        enquiry.setMarketingExecutiveName(dto.getMarketingExecutiveName());
        enquiry.setCustomerName(dto.getCustomerName());
        enquiry.setCustomerCode(dto.getCustomerCode());
        enquiry.setCustomerType(dto.getCustomerType());
        enquiry.setCustomerPhone(dto.getCustomerPhone());
        enquiry.setCustomerEmail(dto.getCustomerEmail());
        enquiry.setOrderType(dto.getOrderType());
        enquiry.setUnitCode(dto.getUnitCode());
        enquiry.setUnitGstNumber(dto.getUnitGstNumber());
        enquiry.setUnitName(dto.getUnitName());
        enquiry.setUnitAddress(dto.getUnitAddress());
        enquiry.setUnitEmail(dto.getUnitEmail());
        enquiry.setAddress(dto.getAddress());
        enquiry.setPdfLink(dto.getPdfLink());
        enquiry.setCustomerAddress(dto.getCustomerAddress());
        enquiry.setDeliveryDays(dto.getDeliveryDays());
        enquiry.setBranchName(dto.getBranchName());
        enquiry.setSupplierArea(dto.getSupplierArea());
        enquiry.setState(dto.getState());
        enquiry.setCountry(dto.getCountry());
        enquiry.setPincode(dto.getPincode());

        enquiry.setAdditionalCharges(dto.getAdditionalCharges());
        enquiry.setTaxes(dto.getTaxes());
        enquiry.setPvcApplicable(dto.isPvcApplicable());
        enquiry.setPaymentTerms(dto.getPaymentTerms());
        enquiry.setValidityHours(dto.getValidityHours());
        enquiry.setNote1(dto.getNote1());
        enquiry.setNote2(dto.getNote2());

        enquiry.setProductDetailNote(dto.getProductDetailNote());
        enquiry.setCurrentSellingPrice(dto.getCurrentSellingPrice());
        enquiry.setStatus("PENDING");
        enquiry.setApprovalStatus("PENDING");
        enquiry.setRemarks(dto.getRemarks());

        // Tax and Charges - simple fields
        enquiry.setCgst(dto.getCgst());
        enquiry.setSgst(dto.getSgst());
        enquiry.setIgst(dto.getIgst());
        enquiry.setFreightCharges(dto.getFreightCharges());
        enquiry.setHamaliCharges(dto.getHamaliCharges());
        enquiry.setPackingCharges(dto.getPackingCharges());
        enquiry.setCuttingCharges(dto.getCuttingCharges());
        enquiry.setLaminationCharges(dto.getLaminationCharges());
        enquiry.setSubTotalAmount(dto.getSubTotalAmount());
        enquiry.setTotalAmount(dto.getTotalAmount());

        // ✅ Map Products (null-safe)
        List<ItemEnquiryProduct> products = Optional.ofNullable(dto.getProducts())
                .orElse(Collections.emptyList())
                .stream()
                .map(p -> {
                    ItemEnquiryProduct prod = new ItemEnquiryProduct();
                    prod.setEnquiry(enquiry);
                    prod.setCategory(p.getCategory());
                    prod.setCurrentSellingPrice(p.getCurrentSellingPrice());
                    prod.setDescription(p.getDescription());
                    prod.setThickness(p.getThickness());
                    prod.setWidth(p.getWidth());
                    prod.setLength(p.getLength());
                    prod.setBrand(p.getBrand());
                    prod.setProductSelectedId(p.getProductSelectedId());
                    prod.setGrade(p.getGrade());
                    prod.setTemper(p.getTemper());
                    prod.setMaterialType(p.getMaterialType());
                    prod.setQuantity(p.getQuantity());
                    prod.setUom(p.getUom());
                    prod.setPrice(p.getPrice());
                    prod.setQuantityInNo(p.getQuantityInNo());
                    prod.setRequiredCategory(p.getRequiredCategory());
                    prod.setRequiredThickness(p.getRequiredThickness());
                    prod.setRequiredWidth(p.getRequiredWidth());
                    prod.setRequiredLength(p.getRequiredLength());
                    prod.setRequiredBrand(p.getRequiredBrand());
                    prod.setRequiredGrade(p.getRequiredGrade());
                    prod.setOrderType(p.getOrderType());
                    prod.setRequiredTemper(p.getRequiredTemper());
                    prod.setRequiredQuantity(p.getRequiredQuantity());
                    prod.setRequiredUom(p.getRequiredUom());
                    prod.setDimension(p.getDimension());
                    return prod;
                }).collect(Collectors.toList());

        // ✅ Map MOQ (null-safe)
        List<ItemEnquiryMoq> moqList = Optional.ofNullable(dto.getMoq())
                .orElse(Collections.emptyList())
                .stream()
                .map(m -> {
                    ItemEnquiryMoq moq = new ItemEnquiryMoq();
                    moq.setEnquiry(enquiry);
                    moq.setItem(m.getItem());
                    moq.setMinQty(m.getMinQty());
                    return moq;
                }).collect(Collectors.toList());

        enquiry.setProducts(products);
        enquiry.setMoq(moqList);

        return enquiryRepo.save(enquiry);
    }

    @Override
    public Page<ItemEnquiry> getAll(Pageable pageable) {
        return enquiryRepo.findAll(pageable);
    }

    @Override
    public List<ItemEnquiry> getAllWithoutPagination() {
        return enquiryRepo.findAll();
    }

    @Override
    public Optional<ItemEnquiry> getById(Long id) {
        return enquiryRepo.findById(id);
    }

    @Override
    public void delete(Long id) {
        enquiryRepo.deleteById(id);
    }

    private String generateQuotationNo() {
        String prefix = "QTN";
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int random = new Random().nextInt(9000) + 1000;
        return prefix + "-" + datePart + "-" + random;
    }

    @Override
    public ItemEnquiry update(Long id, ItemEnquiryDTO dto) {
        ItemEnquiry enquiry = enquiryRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Enquiry not found with id: " + id));

        if (dto.getUserId() != null)
            enquiry.setUserId(dto.getUserId());
        if (dto.getUnitId() != null)
            enquiry.setUnitId(dto.getUnitId());
        if (dto.getMarketingExecutiveName() != null)
            enquiry.setMarketingExecutiveName(dto.getMarketingExecutiveName());
        if (dto.getUnitName() != null)
            enquiry.setUnitName(dto.getUnitName());
        if (dto.getUnitCode() != null)
            enquiry.setUnitCode(dto.getUnitCode());
        if (dto.getUnitGstNumber() != null)
            enquiry.setUnitGstNumber(dto.getUnitGstNumber());
        if (dto.getUnitEmail() != null)
            enquiry.setUnitEmail(dto.getUnitEmail());
        if (dto.getUnitAddress() != null)
            enquiry.setUnitAddress(dto.getUnitAddress());
        if (dto.getPdfLink() != null)
            enquiry.setPdfLink(dto.getPdfLink());
        if (dto.getBranchName() != null)
            enquiry.setBranchName(dto.getBranchName());
        if (dto.getAddress() != null)
            enquiry.setAddress(dto.getAddress());
        if (dto.getSupplierArea() != null)
            enquiry.setSupplierArea(dto.getSupplierArea());
        if (dto.getState() != null)
            enquiry.setState(dto.getState());
        if (dto.getCountry() != null)
            enquiry.setCountry(dto.getCountry());
        if (dto.getPincode() != null)
            enquiry.setPincode(dto.getPincode());
        if (dto.getCustomerName() != null)
            enquiry.setCustomerName(dto.getCustomerName());
        if (dto.getCustomerCode() != null)
            enquiry.setCustomerCode(dto.getCustomerCode());
        if (dto.getCustomerType() != null)
            enquiry.setCustomerType(dto.getCustomerType());
        if (dto.getCustomerAddress() != null)
            enquiry.setCustomerAddress(dto.getCustomerAddress());
        if (dto.getCustomerPhone() != null)
            enquiry.setCustomerPhone(dto.getCustomerPhone());
        if (dto.getCustomerEmail() != null)
            enquiry.setCustomerEmail(dto.getCustomerEmail());
        if (dto.getNote1() != null)
            enquiry.setNote1(dto.getNote1());
        if (dto.getNote2() != null)
            enquiry.setNote2(dto.getNote2());
        // if (dto.getMaterialType() != null)
        // enquiry.setMaterialType(dto.getMaterialType());
        if (dto.getProductDetailNote() != null)
            enquiry.setProductDetailNote(dto.getProductDetailNote());
        if (dto.getCurrentSellingPrice() != null)
            enquiry.setCurrentSellingPrice(dto.getCurrentSellingPrice());
        if (dto.getOrderType() != null)
            enquiry.setOrderType(dto.getOrderType());
        if (dto.getDeliveryDays() > 0)
            enquiry.setDeliveryDays(dto.getDeliveryDays());
        if (dto.getAdditionalCharges() != null)
            enquiry.setAdditionalCharges(dto.getAdditionalCharges());
        if (dto.getTaxes() != null)
            enquiry.setTaxes(dto.getTaxes());
        enquiry.setPvcApplicable(dto.isPvcApplicable()); // boolean default false
        if (dto.getPaymentTerms() != null)
            enquiry.setPaymentTerms(dto.getPaymentTerms());
        if (dto.getValidityHours() > 0)
            enquiry.setValidityHours(dto.getValidityHours());
        if (dto.getRemarks() != null)
            enquiry.setRemarks(dto.getRemarks());
        if (dto.getStatus() != null)
            enquiry.setStatus(dto.getStatus());

        // Set updatedBy from DTO
        if (dto.getUpdatedBy() != null)
            enquiry.setUpdatedBy(dto.getUpdatedBy());

        // Tax and Charges - simple fields
        if (dto.getCgst() != null)
            enquiry.setCgst(dto.getCgst());
        if (dto.getSgst() != null)
            enquiry.setSgst(dto.getSgst());
        if (dto.getIgst() != null)
            enquiry.setIgst(dto.getIgst());
        if (dto.getFreightCharges() != null)
            enquiry.setFreightCharges(dto.getFreightCharges());
        if (dto.getHamaliCharges() != null)
            enquiry.setHamaliCharges(dto.getHamaliCharges());
        if (dto.getPackingCharges() != null)
            enquiry.setPackingCharges(dto.getPackingCharges());
        if (dto.getCuttingCharges() != null)
            enquiry.setCuttingCharges(dto.getCuttingCharges());
        if (dto.getLaminationCharges() != null)
            enquiry.setLaminationCharges(dto.getLaminationCharges());
        if (dto.getSubTotalAmount() != null)
            enquiry.setSubTotalAmount(dto.getSubTotalAmount());
        if (dto.getTotalAmount() != null)
            enquiry.setTotalAmount(dto.getTotalAmount());

        enquiry.setApprovalStatus("PENDING");

        // ✅ Update products if provided
        if (dto.getProducts() != null) {
            enquiry.getProducts().clear();
            List<ItemEnquiryProduct> updatedProducts = dto.getProducts().stream().map(p -> {
                ItemEnquiryProduct prod = new ItemEnquiryProduct();
                prod.setEnquiry(enquiry);
                prod.setCategory(p.getCategory());
                prod.setDescription(p.getDescription());
                prod.setThickness(p.getThickness());
                prod.setCurrentSellingPrice(p.getCurrentSellingPrice());
                prod.setWidth(p.getWidth());
                prod.setLength(p.getLength());
                prod.setProductSelectedId(p.getProductSelectedId());
                prod.setBrand(p.getBrand());
                prod.setGrade(p.getGrade());
                prod.setTemper(p.getTemper());
                prod.setMaterialType(p.getMaterialType());
                prod.setQuantity(p.getQuantity());
                prod.setUom(p.getUom());
                prod.setOrderType(p.getOrderType());
                prod.setQuantityInNo(p.getQuantityInNo());
                prod.setPrice(p.getPrice());
                prod.setRequiredBrand(p.getRequiredBrand());
                prod.setRequiredTemper(p.getRequiredTemper());
                prod.setRequiredQuantity(p.getRequiredQuantity());
                prod.setRequiredLength(p.getRequiredLength());
                prod.setRequiredThickness(p.getRequiredThickness());
                prod.setRequiredUom(p.getRequiredUom());
                prod.setRequiredCategory(p.getRequiredCategory());
                prod.setRequiredGrade(p.getRequiredGrade());
                prod.setRequiredWidth(p.getRequiredWidth());
                return prod;
            }).toList();
            enquiry.getProducts().addAll(updatedProducts);
        }

        // ✅ Update MOQ if provided
        if (dto.getMoq() != null) {
            enquiry.getMoq().clear();
            List<ItemEnquiryMoq> updatedMoq = dto.getMoq().stream().map(m -> {
                ItemEnquiryMoq moq = new ItemEnquiryMoq();
                moq.setEnquiry(enquiry);
                moq.setItem(m.getItem());
                moq.setMinQty(m.getMinQty());
                return moq;
            }).toList();
            enquiry.getMoq().addAll(updatedMoq);
        }

        return enquiryRepo.save(enquiry);
    }

    @Override
    public AvailableStockDto getAvailableStockList(String productCategory, String itemDescription, String brand,
            String grade, String temper, String materialType,
            String dimension, BigDecimal requiredQuantity, String uom, String unit, String orderType) {

        log.info("========== SERVICE: getAvailableStockList START ==========");
        log.info("➡️ Input Parameters:");
        log.info("   productCategory: {}", productCategory);
        log.info("   itemDescription: {}", itemDescription);
        log.info("   brand: {}", brand);
        log.info("   grade: {}", grade);
        log.info("   temper: {}", temper);
        log.info("   materialType: {}", materialType);
        log.info("   dimension: {}", dimension);
        log.info("   requiredQuantity: {}", requiredQuantity);
        log.info("   uom: {}", uom);

        String normalizedDimension = dimension;
        log.info("📐 Normalized dimension: {} -> {}", dimension, normalizedDimension);

        String brandFilter = (brand == null || brand.equalsIgnoreCase("Any")) ? "Any" : brand;
        String gradeFilter = (grade == null || grade.equalsIgnoreCase("Any")) ? "Any" : grade;
        String temperFilter = (temper == null || temper.equalsIgnoreCase("Any")) ? "Any" : temper;
        String materialTypeFilter = (materialType == null || materialType.equalsIgnoreCase("Any")) ? "Any"
                : materialType;

        log.info("🔍 Filters applied:");
        log.info("   brandFilter: {}", brandFilter);
        log.info("   gradeFilter: {}", gradeFilter);
        log.info("   temperFilter: {}", temperFilter);
        log.info("   materialTypeFilter: {}", materialTypeFilter);
        log.info("   dimensionFilter (normalized): {}", normalizedDimension);
        log.info("   dimensionFilter (original): {}", dimension);

        // Use findMatchingStock with all filters including dimension (both normalized
        // and original)
        List<StockSummaryEntity> stockList = stockSummaryRepository.findMatchingStock(
                productCategory, brandFilter, gradeFilter, temperFilter, materialTypeFilter,
                normalizedDimension, dimension, unit);

        log.info("📦 Stock fetched from DB: count={}", stockList.size());

        if (stockList.isEmpty()) {
            log.warn("⚠️ No stock found in DB for given filters!");
            log.info("========== SERVICE: getAvailableStockList END (NO DATA) ==========");
            return null;
        }

        // Log first few records for debugging
        int logCount = Math.min(stockList.size(), 5);
        log.info("📦 First {} stock records:", logCount);
        for (int i = 0; i < logCount; i++) {
            StockSummaryEntity s = stockList.get(i);
            log.info("   [{}] dimension={}, quantityKg={}, quantityNo={}, brand={}, grade={}, temper={}",
                    i, s.getDimension(), s.getQuantityKg(), s.getQuantityNo(), s.getBrand(), s.getGrade(),
                    s.getTemper());
        }

        // ✅ Aggregate all matching rows
        BigDecimal totalKg = BigDecimal.ZERO;
        int totalNo = 0;
        StockSummaryEntity sample = null;
        int matchedCount = 0;
        int skippedCount = 0;

        for (StockSummaryEntity stock : stockList) {
            boolean dimMatch = isDimensionCompatible(stock.getDimension(), normalizedDimension);
            if (!dimMatch) {
                skippedCount++;
                log.debug("   ❌ Dimension mismatch: stock={} vs requested={}", stock.getDimension(),
                        normalizedDimension);
                continue;
            }

            matchedCount++;
            log.debug("   ✅ Dimension matched: {}", stock.getDimension());

            if (stock.getQuantityKg() != null) {
                totalKg = totalKg.add(stock.getQuantityKg());
            }
            if (stock.getQuantityNo() != null) {
                totalNo += stock.getQuantityNo();
            }

            if (sample == null)
                sample = stock;
        }

        log.info("📊 Dimension matching results: matched={}, skipped={}", matchedCount, skippedCount);
        log.info("📊 Total aggregated: totalKg={}, totalNo={}", totalKg, totalNo);

        if (sample == null) {
            log.warn("⚠️ No matching stock found after dimension filter!");
            log.info("========== SERVICE: getAvailableStockList END (NO MATCH) ==========");
            return null;
        }

        // ✅ Cap the available quantity to stock
        BigDecimal finalKg = totalKg;
        int finalNo = totalNo;

        if ("Kgs".equalsIgnoreCase(uom) || "Kg".equalsIgnoreCase(uom)) {
            if (requiredQuantity.compareTo(totalKg) < 0) {
                finalKg = requiredQuantity; // enough stock, show required
                log.info("📦 UOM=Kgs: Enough stock, capping to required: {}", finalKg);
            } else {
                finalKg = totalKg; // not enough, show available
                log.info("📦 UOM=Kgs: Not enough stock, showing available: {}", finalKg);
            }
        } else if ("No".equalsIgnoreCase(uom)) {
            if (requiredQuantity.compareTo(BigDecimal.valueOf(totalNo)) < 0) {
                finalNo = requiredQuantity.intValue(); // enough stock, show required
                log.info("📦 UOM=No: Enough stock, capping to required: {}", finalNo);
            } else {
                finalNo = totalNo; // not enough, show available
                log.info("📦 UOM=No: Not enough stock, showing available: {}", finalNo);
            }
        }

        // ✅ Build DTO with capped quantities
        AvailableStockDto dto = AvailableStockDto.builder()
                .id(sample.getId())
                .unit(sample.getUnit())
                .itemDescription(sample.getItemDescription())
                .brand(sample.getBrand())
                .grade(sample.getGrade())
                .temper(sample.getTemper())
                .dimension(sample.getDimension())
                .quantityKg(finalKg)
                .quantityNo(finalNo)
                .itemPrice(sample.getItemPrice())
                .inventoryType("N/A")
                .build();

        log.info("✅ Final stock DTO: {}", dto);
        log.info("========== SERVICE: getAvailableStockList END (SUCCESS) ==========");
        return dto;
    }

    private String normalizeDimension(String dim) {
        if (dim == null || dim.isBlank())
            return dim;

        String normalized = dim.trim();

        // Handle "SQ" notation: "12 SQ X 12" or "12SQ X 12" means 12x12x12 (square
        // cross-section)
        // Pattern: <size> SQ [X <length>] -> <size>x<size>[x<length>]
        java.util.regex.Pattern sqPattern = java.util.regex.Pattern.compile(
                "(\\d+(?:\\.\\d+)?)\\s*SQ\\s*(?:[xX×]\\s*(\\d+(?:\\.\\d+)?))?",
                java.util.regex.Pattern.CASE_INSENSITIVE);
        java.util.regex.Matcher sqMatcher = sqPattern.matcher(normalized);

        if (sqMatcher.find()) {
            String size = sqMatcher.group(1);
            String length = sqMatcher.group(2);
            if (length != null && !length.isBlank()) {
                normalized = size + "x" + size + "x" + length;
            } else {
                normalized = size + "x" + size;
            }
            log.debug("📐 SQ notation expanded: {} -> {}", dim, normalized);
            return normalized;
        }

        // Handle other shape notations: HEX, PEN, OCT, RD, RECT, TRI
        // These have non-square cross-sections, so keep the shape prefix as-is
        // e.g. "999 HEX X 999" stays as "999 HEX X 999"
        java.util.regex.Pattern shapePattern = java.util.regex.Pattern.compile(
                "(\\d+(?:\\.\\d+)?)\\s*(HEX|PEN|DIA|OCT|RD|RECT|TRI)\\s*(?:[xX×]\\s*(\\d+(?:\\.\\d+)?))?(?:\\s*[xX×]\\s*(\\d+(?:\\.\\d+)?))?",
                java.util.regex.Pattern.CASE_INSENSITIVE);
        java.util.regex.Matcher shapeMatcher = shapePattern.matcher(normalized);

        if (shapeMatcher.find()) {
            // Return as-is since shape notations are stored with the prefix in the DB
            log.debug("📐 Shape notation kept as-is: {}", dim);
            return normalized;
        }

        // Standard normalization: split by x/X/× and rejoin with lowercase x
        String[] parts = normalized.split("[xX×]+");
        if (parts.length >= 2) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < parts.length; i++) {
                if (i > 0)
                    sb.append("x");
                sb.append(parts[i].trim());
            }
            return sb.toString();
        }

        return normalized.replace("X", "x").replace("×", "x");
    }

    // private boolean isParsableDimension(String dim) {
    // if (dim == null || dim.isBlank()) return false;
    //
    // String[] parts = dim.split("[xX]");
    // try {
    // for (String part : parts) {
    // if (!part.isBlank()) new BigDecimal(part.trim());
    // }
    // return parts.length >= 2; // ✅ At least thickness & width
    // } catch (NumberFormatException e) {
    // System.out.println("❌ Invalid dimension: " + dim);
    // return false;
    // }
    // }

    // private boolean isDimensionCompatible(String stockDimension, String
    // requestedDimension) {
    // if (stockDimension == null || requestedDimension == null) return false;
    //
    // List<String> stockParts = Arrays.stream(stockDimension.split("[xX]"))
    // .map(String::trim)
    // .collect(Collectors.toList());
    //
    // List<String> requestParts = Arrays.stream(requestedDimension.split("[xX]"))
    // .map(String::trim)
    // .collect(Collectors.toList());
    //
    // if (stockParts.size() < 3 || requestParts.size() < 3) return false;
    //
    // try {
    // BigDecimal stockThickness = new BigDecimal(stockParts.get(0));
    // BigDecimal stockWidth = new BigDecimal(stockParts.get(1));
    // BigDecimal stockLength = new BigDecimal(stockParts.get(2));
    //
    // BigDecimal reqThickness = new BigDecimal(requestParts.get(0));
    // BigDecimal reqWidth = new BigDecimal(requestParts.get(1));
    // BigDecimal reqLength = new BigDecimal(requestParts.get(2));
    //
    // return stockThickness.compareTo(reqThickness) == 0 &&
    // stockWidth.compareTo(reqWidth) == 0 &&
    // stockLength.compareTo(reqLength) >= 0;
    //
    // } catch (NumberFormatException e) {
    // System.out.println("❌ Dimension parsing failed for: " + stockDimension + " vs
    // " + requestedDimension);
    // return false;
    // }
    // }

    private boolean isDimensionCompatible(String stockDimension, String requestedDimension) {
        List<BigDecimal> stockParts = parseDimension(stockDimension);
        List<BigDecimal> requestParts = parseDimension(requestedDimension);

        log.debug("📐 Comparing dimensions: stock={} -> {}, requested={} -> {}",
                stockDimension, stockParts, requestedDimension, requestParts);

        // If either parsing failed, return false
        if (stockParts.isEmpty() || requestParts.isEmpty()) {
            log.debug("   ❌ Dimension parsing failed - stockParts={}, requestParts={}", stockParts, requestParts);
            return false;
        }

        int stockSize = stockParts.size();
        int reqSize = requestParts.size();

        // 3-part dimension match (width x height x length)
        if (reqSize == 3 && stockSize == 3) {
            boolean match = stockParts.get(0).compareTo(requestParts.get(0)) == 0 &&
                    stockParts.get(1).compareTo(requestParts.get(1)) == 0 &&
                    stockParts.get(2).compareTo(requestParts.get(2)) >= 0;
            log.debug("   3-part match: {}", match);
            return match;
        }

        // 3-part stock vs 2-part request (match first two parts)
        if (reqSize == 2 && stockSize == 3) {
            boolean match = stockParts.get(0).compareTo(requestParts.get(0)) == 0 &&
                    stockParts.get(1).compareTo(requestParts.get(1)) == 0;
            log.debug("   3-part stock vs 2-part request match: {}", match);
            return match;
        }

        // 2-part dimension match (width x length)
        if (reqSize == 2 && stockSize >= 2) {
            boolean match = stockParts.get(0).compareTo(requestParts.get(0)) == 0 &&
                    stockParts.get(1).compareTo(requestParts.get(1)) == 0;
            log.debug("   2-part match: {}", match);
            return match;
        }

        log.debug("   ❌ Dimension size mismatch - stockParts.size={}, requestParts.size={}",
                stockSize, reqSize);
        return false;
    }

    private List<BigDecimal> parseDimension(String dim) {
        if (dim == null || dim.isBlank())
            return Collections.emptyList();

        String normalized = dim.trim();

        // Handle "SQ" notation first: "12 SQ X 12" means 12x12x12 (square cross-section)
        java.util.regex.Pattern sqPattern = java.util.regex.Pattern.compile(
                "(\\d+(?:\\.\\d+)?)\\s*SQ\\s*(?:[xX×]\\s*(\\d+(?:\\.\\d+)?))?",
                java.util.regex.Pattern.CASE_INSENSITIVE);
        java.util.regex.Matcher sqMatcher = sqPattern.matcher(normalized);

        if (sqMatcher.find()) {
            String size = sqMatcher.group(1);
            String length = sqMatcher.group(2);
            List<BigDecimal> parsed = new ArrayList<>();
            try {
                BigDecimal sizeVal = new BigDecimal(size.trim());
                parsed.add(sizeVal); // width
                parsed.add(sizeVal); // height (same as width for square)
                if (length != null && !length.isBlank()) {
                    parsed.add(new BigDecimal(length.trim())); // length
                }
                log.debug("📐 Parsed SQ dimension: {} -> {}", dim, parsed);
                return parsed;
            } catch (NumberFormatException e) {
                log.warn("Invalid SQ dimension: {}", dim);
                return Collections.emptyList();
            }
        }

        // Handle other shape notations: HEX (hexagonal), PEN (pentagon), OCT (octagonal), RD (round), etc.
        // Pattern: <size> <SHAPE> [X <dim2> [X <dim3>]]
        // e.g. "999 HEX X 999" -> [999, 999], "12 PEN X 100" -> [12, 100]
        java.util.regex.Pattern shapePattern = java.util.regex.Pattern.compile(
                "(\\d+(?:\\.\\d+)?)\\s*(?:HEX|PEN|DIA|OCT|RD|RECT|TRI)\\s*(?:[xX×]\\s*(\\d+(?:\\.\\d+)?))?(?:\\s*[xX×]\\s*(\\d+(?:\\.\\d+)?))?",
                java.util.regex.Pattern.CASE_INSENSITIVE);
        java.util.regex.Matcher shapeMatcher = shapePattern.matcher(normalized);

        if (shapeMatcher.find()) {
            String size = shapeMatcher.group(1);
            String dim2 = shapeMatcher.group(2);
            String dim3 = shapeMatcher.group(3);
            List<BigDecimal> parsed = new ArrayList<>();
            try {
                parsed.add(new BigDecimal(size.trim())); // cross-section size
                if (dim2 != null && !dim2.isBlank()) {
                    parsed.add(new BigDecimal(dim2.trim())); // second dimension (typically length)
                }
                if (dim3 != null && !dim3.isBlank()) {
                    parsed.add(new BigDecimal(dim3.trim())); // third dimension if present
                }
                log.debug("📐 Parsed shape dimension: {} -> {}", dim, parsed);
                return parsed;
            } catch (NumberFormatException e) {
                log.warn("Invalid shape dimension: {}", dim);
                return Collections.emptyList();
            }
        }

        // Standard parsing: split by x, X, or ×
        String[] parts = normalized.split("[xX×]+");
        List<BigDecimal> parsed = new ArrayList<>();

        for (String part : parts) {
            try {
                String trimmed = part.trim();
                if (!trimmed.isEmpty()) {
                    parsed.add(new BigDecimal(trimmed));
                }
            } catch (NumberFormatException e) {
                log.warn("Invalid dimension part: '{}' in dimension: {}", part, dim);
                return Collections.emptyList(); // fail-safe return
            }
        }

        return parsed;
    }

    @Override
    public ItemEnquiry cancelAndDeleteEnquiry(Long id) {
        ItemEnquiry enquiry = enquiryRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Enquiry not found with id: " + id));

        enquiry.setStatus("CANCELLED");
        return enquiryRepo.save(enquiry); // ✅ Save updated entity instead of deleting
    }

    @Transactional
    @Override
    public ItemEnquiry markStatusAsReceived(String quotationNo) {
        // 1. Find enquiry
        ItemEnquiry enquiry = enquiryRepo.findByQuotationNo(quotationNo)
                .orElseThrow(() -> new RuntimeException("No enquiry found with quotation number: " + quotationNo));

        // 2. Update status
        enquiry.setStatus("RECEIVED");
        ItemEnquiry updated = enquiryRepo.save(enquiry);

        // 3. Check blocked quantity
        blockedQuantityRepo.findByQuotationNo(quotationNo).ifPresent(blocked -> {
            blockedQuantityRepo.delete(blocked);
        });

        return updated;
    }

    @Override
    public Map<String, Long> getStatusSummary() {
        long totalRecords = enquiryRepo.count();
        long totalPending = enquiryRepo.countByStatus("Pending");
        long totalReceived = enquiryRepo.countByStatus("Received");
        long totalCancelled = enquiryRepo.countByStatus("Cancelled");

        return Map.of(
                "totalRecords", totalRecords,
                "totalPending", totalPending,
                "totalReceived", totalReceived,
                "totalCancelled", totalCancelled);
    }

    @Override
    public Map<String, String> getItemInfoByDescription(String itemDescription) {
        ItemMasterEntity item = itemMasterRepository.findBySkuDescriptionIgnoreCase(itemDescription)
                .orElseThrow(() -> new RuntimeException("Item not found with description: " + itemDescription));

        return Map.of(
                "itemDescription", item.getSkuDescription() != null ? item.getSkuDescription() : itemDescription,
                "sectionNo", item.getSectionNumber() != null ? item.getSectionNumber() : "",
                "productCategory", item.getProductCategory() != null ? item.getProductCategory() : "",
                "brand", item.getBrand() != null ? item.getBrand() : "",
                "grade", item.getGrade() != null ? item.getGrade() : "",
                "temper", item.getTemper() != null ? item.getTemper() : "",
                "uom", item.getPrimaryUom() != null ? item.getPrimaryUom() : "");
    }

    @Override
    public ItemEnquiry updateRemarks(String quotationNo, String remarks) {
        ItemEnquiry enquiry = enquiryRepo.findByQuotationNo(quotationNo)
                .orElseThrow(() -> new RuntimeException("ItemEnquiry not found with quotationNo: " + quotationNo));

        enquiry.setRemarks(remarks);
        return enquiryRepo.save(enquiry);
    }

    @Override
    public Page<ItemEnquiry> getEnquiriesBetweenDates(LocalDateTime fromDate, LocalDateTime toDate, Pageable pageable) {
        return enquiryRepo.findByCreatedAtBetween(fromDate, toDate, pageable);
    }

    @Override
    public List<ItemEnquiry> getEnquiriesBetweenDatesWithoutPagination(LocalDateTime fromDate, LocalDateTime toDate) {
        return enquiryRepo.findByCreatedAtBetween(fromDate, toDate);
    }

    @Override
    public void deleteAllEnquiries() {
        enquiryRepo.deleteAll();
    }

    @Override
    public Map<String, Object> getPriceDetail(String itemDescription, String orderType) {
        LocalDate today = LocalDate.now();

        // Try to fetch billing summary
        Optional<BillingSummaryEntity> optionalSummary = billingSummaryRepository
                .findTopByItemDescriptionAndOrderTypeOrderByTimestampDesc(itemDescription, orderType);

        Map<String, Object> response = new LinkedHashMap<>();

        if (optionalSummary.isPresent()) {
            BillingSummaryEntity summary = optionalSummary.get();
            LocalDate lastDate = summary.getTimestamp().toLocalDate();

            // Nalco prices
            BigDecimal nalcoPriceOnLastDate = nalcoPriceRepository.findByDate(lastDate)
                    .map(NalcoPriceEntity::getNalcoPrice)
                    .orElse(BigDecimal.ZERO);
            BigDecimal nalcoPriceToday = nalcoPriceRepository.findByDate(today)
                    .map(NalcoPriceEntity::getNalcoPrice)
                    .orElse(BigDecimal.ZERO);

            // Hindalco prices
            Double hindalcoPriceOnLastDate = hindalcoPriceRepository.findByPriceDate(java.sql.Date.valueOf(lastDate))
                    .map(HindalcoPriceEntity::getPrice)
                    .orElse(0.0);
            Double hindalcoPriceToday = hindalcoPriceRepository.findByPriceDate(java.sql.Date.valueOf(today))
                    .map(HindalcoPriceEntity::getPrice)
                    .orElse(0.0);

            // Response with summary + prices
            response.put("invoiceNumber", summary.getInvoiceNumber());
            response.put("date", summary.getTimestamp());
            response.put("itemPrice", summary.getItemPrice());
            response.put("nalcoPriceOnLastDate", nalcoPriceOnLastDate);
            response.put("nalcoPriceToday", nalcoPriceToday);
            response.put("hindalcoPriceOnLastDate", hindalcoPriceOnLastDate);
            response.put("hindalcoPriceToday", hindalcoPriceToday);
            response.put("currentHindalcoPriceWithConversionMargin", null); // Current Hindalco Price + Conversion &
                                                                            // Margin %
            response.put("currentNalcoPriceWithConversionMargin", null); // Current NALCO Price + Conversion & Margin %

        } else {
            // Fallback: only current prices
            BigDecimal nalcoPriceToday = nalcoPriceRepository.findByDate(today)
                    .map(NalcoPriceEntity::getNalcoPrice)
                    .orElse(BigDecimal.ZERO);

            Double hindalcoPriceToday = hindalcoPriceRepository.findByPriceDate(java.sql.Date.valueOf(today))
                    .map(HindalcoPriceEntity::getPrice)
                    .orElse(0.0);

            response.put("date", today);
            response.put("nalcoPriceToday", nalcoPriceToday);
            response.put("hindalcoPriceToday", hindalcoPriceToday);
            response.put("currentHindalcoPriceWithConversionMargin", null); // Current Hindalco Price + Conversion &
                                                                            // Margin %
            response.put("currentNalcoPriceWithConversionMargin", null); // Current NALCO Price + Conversion & Margin %
            response.put("message", "No billing summary found, showing only current prices");
        }

        return response;
    }

    @Override
    public Map<String, Object> getAvailableQuantity(String itemDescription, String unit) {
        log.info("➡️ getAvailableQuantity called with itemDescription='{}', unit='{}'", itemDescription, unit);

        // Step 1: Stock summary total
        BigDecimal totalQuantityKg = stockSummaryRepository
                .findTotalQuantityKgByItemDescriptionAndUnit(itemDescription, unit);
        log.info("📦 Stock summary totalQuantityKg={}", totalQuantityKg);

        // Step 2: Blocked quantities
        List<BlockedQuantityEntity> blockedQuantities = blockedQuantityRepository
                .findByItemDescription(itemDescription);
        log.info("🔒 Blocked quantities fetched: count={}", blockedQuantities.size());

        List<Map<String, Object>> blockedEntries = new ArrayList<>();
        BigDecimal totalBlockedQuantityKg = BigDecimal.ZERO;

        for (BlockedQuantityEntity bq : blockedQuantities) {
            log.debug("➡️ BlockedQuantityEntity id={}, customerName={}", bq.getId(), bq.getCustomerName());
            for (BlockedProductEntity bp : bq.getProducts()) {
                log.debug("➡️ BlockedProductEntity id={}, itemDescription={}, availableQuantityKg={}", bp.getId(),
                        bp.getItemDescription(), bp.getAvailableQuantityKg());
                if (itemDescription.equals(bp.getItemDescription())) {
                    Map<String, Object> entry = new LinkedHashMap<>();
                    entry.put("id", bp.getId());
                    entry.put("itemDescription", bp.getItemDescription());
                    entry.put("availableQuantityKg", bp.getAvailableQuantityKg());
                    entry.put("customerName", bq.getCustomerName());
                    entry.put("marketingExecutiveName", bq.getMarketingExecutiveName());

                    blockedEntries.add(entry);

                    if (bp.getAvailableQuantityKg() != null) {
                        totalBlockedQuantityKg = totalBlockedQuantityKg.add(bp.getAvailableQuantityKg());
                    }
                }
            }
        }
        log.info("🔒 Total blockedQuantityKg={}", totalBlockedQuantityKg);

        // Step 3: Sales orders (active only)
        List<SalesOrder> activeOrders = salesOrderRepository.findActiveOrdersByUnitAndItemDescription(unit,
                itemDescription);
        log.info("🧾 Active sales orders fetched: count={}", activeOrders.size());

        List<Map<String, Object>> salesOrderLineItems = new ArrayList<>();
        BigDecimal totalSalesOrderQuantityKg = BigDecimal.ZERO;

        for (SalesOrder so : activeOrders) {
            log.debug("➡️ SalesOrder soNumber={}, itemsCount={}", so.getSoNumber(), so.getItems().size());
            for (SalesOrderLineItem li : so.getItems()) {
                log.debug("➡️ LineItem lineNumber={}, itemDescription={}, quantityKg={}", li.getLineNumber(),
                        li.getItemDescription(), li.getQuantityKg());
                if (itemDescription.equals(li.getItemDescription())) {
                    Map<String, Object> itemMap = new LinkedHashMap<>();
                    itemMap.put("soNumber", so.getSoNumber());
                    itemMap.put("lineNumber", li.getLineNumber());
                    itemMap.put("quantityKg", li.getQuantityKg());
                    itemMap.put("quantityNos", li.getQuantityNos());
                    itemMap.put("targetDispatchDate", li.getTargetDispatchDate());

                    salesOrderLineItems.add(itemMap);
                    totalSalesOrderQuantityKg = totalSalesOrderQuantityKg.add(BigDecimal.valueOf(li.getQuantityKg()));
                }
            }
        }
        log.info("🧾 Total salesOrderQuantityKg={}", totalSalesOrderQuantityKg);

        // Step 4: PO summary (pending receipt)
        List<POGenerationEntity> pendingPOs = poGenerationRepository.findPendingPOsByUnitAndItemDescription(unit,
                itemDescription);
        log.info("📑 Pending POs fetched: count={}", pendingPOs.size());

        List<Map<String, Object>> poEntries = new ArrayList<>();
        int totalPendingPOQuantity = 0;

        for (POGenerationEntity po : pendingPOs) {
            log.debug("➡️ PO poNumber={}, supplierName={}", po.getPoNumber(), po.getSupplierName());
            for (POGenerationItemEntity item : po.getItems()) {
                log.debug("➡️ POItem itemDescription={}, requiredQuantity={}, rmReceiptStatus={}",
                        item.getItemDescription(), item.getRequiredQuantity(), item.getRmReceiptStatus());
                if (itemDescription.equals(item.getItemDescription())
                        && "PENDING".equalsIgnoreCase(item.getRmReceiptStatus())) {
                    Map<String, Object> poMap = new LinkedHashMap<>();
                    poMap.put("poNumber", po.getPoNumber());
                    poMap.put("supplierName", po.getSupplierName());
                    poMap.put("requiredQuantity", item.getRequiredQuantity());
                    poEntries.add(poMap);

                    totalPendingPOQuantity += item.getRequiredQuantity();
                }
            }
        }
        log.info("📑 Total pendingPOQuantity={}", totalPendingPOQuantity);

        // Step 5: Net available calculation
        BigDecimal netAvailableQuantityKg = (totalQuantityKg != null ? totalQuantityKg : BigDecimal.ZERO)
                .subtract(totalBlockedQuantityKg)
                .subtract(totalSalesOrderQuantityKg);
        log.info("📊 Net availableQuantityKg={}", netAvailableQuantityKg);

        // Step 6: Prepare structured response
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("unit", unit);
        response.put("itemDescription", itemDescription);

        Map<String, Object> stockSummary = new HashMap<>();
        stockSummary.put("totalAvailableQuantityKg", totalQuantityKg);
        response.put("stockSummary", stockSummary);

        Map<String, Object> blockedSummary = new HashMap<>();
        blockedSummary.put("totalBlockedQuantityKg", totalBlockedQuantityKg);
        blockedSummary.put("entries", blockedEntries);
        response.put("blockedQuantity", blockedSummary);

        Map<String, Object> salesSummary = new HashMap<>();
        salesSummary.put("totalSalesOrderQuantityKg", totalSalesOrderQuantityKg);
        salesSummary.put("lineItems", salesOrderLineItems);
        response.put("salesOrders", salesSummary);

        response.put("netAvailableQuantityKg", netAvailableQuantityKg);

        Map<String, Object> poSummary = new HashMap<>();
        poSummary.put("totalPendingPOQuantity", totalPendingPOQuantity);
        poSummary.put("entries", poEntries);
        response.put("poSummary", poSummary);

        return response;
    }

    @Override
    @Transactional
    public ItemEnquiry approveEnquiry(Long id) {
        log.info("\n══════════════════════════════════════════════════════════════════");
        log.info("✅ [ItemEnquiry] APPROVE ENQUIRY - ID: {}", id);
        log.info("══════════════════════════════════════════════════════════════════");

        ItemEnquiry enquiry = enquiryRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Item Enquiry not found with ID: " + id));

        log.info("   📋 Quotation No: {}", enquiry.getQuotationNo());
        log.info("   📋 Current Approval Status: {}", enquiry.getApprovalStatus());
        log.info("   📋 Customer Type: {}", enquiry.getCustomerType());

        enquiry.setApprovalStatus("APPROVED");

        // FRD APR-008: Differentiate by customer type
        // Regular customer → "SO PENDING"
        // New customer → "PENDING_CUSTOMER_VERIFICATION" (until verified in New Customer Verification module)
        String customerType = enquiry.getCustomerType();
        if ("new".equalsIgnoreCase(customerType)) {
            enquiry.setStatus("PENDING_CUSTOMER_VERIFICATION");
            log.info("   🔵 New customer detected — Status changed to: PENDING_CUSTOMER_VERIFICATION");
        } else {
            enquiry.setStatus("SO PENDING");
            log.info("   🟠 Regular customer — Status changed to: SO PENDING");
        }

        ItemEnquiry saved = enquiryRepo.save(enquiry);

        log.info("   ✅ Approval Status changed to: APPROVED");
        log.info("══════════════════════════════════════════════════════════════════\n");

        return saved;
    }

    @Override
    @Transactional
    public ItemEnquiry rejectEnquiry(Long id) {
        log.info("\n══════════════════════════════════════════════════════════════════");
        log.info("❌ [ItemEnquiry] REJECT ENQUIRY - ID: {}", id);
        log.info("══════════════════════════════════════════════════════════════════");

        ItemEnquiry enquiry = enquiryRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Item Enquiry not found with ID: " + id));

        log.info("   📋 Quotation No: {}", enquiry.getQuotationNo());
        log.info("   📋 Current Approval Status: {}", enquiry.getApprovalStatus());

        enquiry.setApprovalStatus("REJECTED");
        enquiry.setStatus("REJECTED");
        ItemEnquiry saved = enquiryRepo.save(enquiry);

        log.info("   ❌ Approval Status changed to: REJECTED");
        log.info("   ❌ Status changed to: REJECTED");
        log.info("══════════════════════════════════════════════════════════════════\n");

        return saved;
    }

}
