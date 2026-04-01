package com.indona.invento.services.helpers;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * CUT Order Picklist Allocation Engine — FRD 4A.2A (4-Step Algorithm)
 *
 * Implements the 4-step cascading algorithm for Order Type = CUT:
 *   Step 1: End Pieces with EXACT dimension match (FIFO)
 *   Step 2: End Pieces with NEAREST HIGHER dimensions (ascending W×L, then FIFO)
 *   Step 3: Loose Pieces with matching/higher dimensions (ascending W×L, then FIFO)
 *   Step 4: Warehouse Bundles with matching/higher dimensions (FIFO)
 *
 * Used by: Sales Order Scheduling (CUT order type ONLY)
 * NOT used by: IU Material Transfer
 *
 * Key Rules:
 * - Strict dimension (e.g. Thickness for Flat Bar) must ALWAYS match EXACTLY
 * - Flexible dimensions (Width, Length) must be >= required (DO NOT RECOMMEND if less)
 * - Sorted by Storage Area Order, then Distance (from Rack & Bin Master)
 * - Within same sort group, FIFO by Date of Inward
 */
public class CutOrderPicklistEngine {

    /** Helper to extract quantity in Kg from a bundle map */
    private static final Function<Map<String, Object>, BigDecimal> GET_QTY_KG = bundle -> {
        Object qty = bundle.get("quantityKg");
        if (qty instanceof BigDecimal) return (BigDecimal) qty;
        if (qty instanceof Number) return BigDecimal.valueOf(((Number) qty).doubleValue());
        return BigDecimal.ZERO;
    };

    /** Helper to extract quantity in No from a bundle map */
    private static final Function<Map<String, Object>, Integer> GET_QTY_NO = bundle -> {
        Object qty = bundle.get("quantityNo");
        if (qty instanceof Number) return ((Number) qty).intValue();
        return 0;
    };

    /**
     * Run the 4-step CUT order allocation algorithm.
     *
     * @param requiredQtyKg     Required quantity in Kg
     * @param endPieceBundles   End Piece storage bundles (pre-filtered by eligible dimensions)
     * @param loosePieceBundles  Loose Piece storage bundles (pre-filtered by eligible dimensions)
     * @param warehouseBundles   Warehouse bundles (pre-filtered by eligible dimensions)
     * @param requiredDimension  Parsed required dimension [dim1, dim2, dim3]
     * @param productCategory    Product category for dimension matching rules
     * @return FifoResult with selected bundles and metadata
     */
    public static FifoResult allocate(
            BigDecimal requiredQtyKg,
            List<Map<String, Object>> endPieceBundles,
            List<Map<String, Object>> loosePieceBundles,
            List<Map<String, Object>> warehouseBundles,
            BigDecimal[] requiredDimension,
            String productCategory) {

        // Defensive copies
        List<Map<String, Object>> endPieces = endPieceBundles != null ? new ArrayList<>(endPieceBundles) : new ArrayList<>();
        List<Map<String, Object>> loose = loosePieceBundles != null ? new ArrayList<>(loosePieceBundles) : new ArrayList<>();
        List<Map<String, Object>> warehouse = warehouseBundles != null ? new ArrayList<>(warehouseBundles) : new ArrayList<>();

        List<Map<String, Object>> selectedBundles = new ArrayList<>();
        BigDecimal remainingQty = requiredQtyKg;
        BigDecimal totalAllocated = BigDecimal.ZERO;

        String strictParamLabel = DimensionMatcher.getStrictParamLabel(productCategory);

        log("╔══════════════════════════════════════════════════════════════════════════════╗");
        log("║        ✂️ CUT ORDER ENGINE — 4-STEP ALGORITHM (FRD 4A.2A)                  ║");
        log("╠══════════════════════════════════════════════════════════════════════════════╣");
        log("║ Required Qty      : " + requiredQtyKg + " KG");
        log("║ Required Dimension: " + formatDim(requiredDimension));
        log("║ Product Category  : " + productCategory);
        log("║ Strict Parameter  : " + strictParamLabel + " = " + requiredDimension[0]);
        log("║ End Pieces        : " + endPieces.size() + " bundles | " + sumQty(endPieces) + " KG");
        log("║ Loose Pieces      : " + loose.size() + " bundles | " + sumQty(loose) + " KG");
        log("║ Warehouse         : " + warehouse.size() + " bundles | " + sumQty(warehouse) + " KG");
        log("╚══════════════════════════════════════════════════════════════════════════════╝");

        // ═══════════════════════════════════════════════════════════════════
        // STEP 1: End Pieces — EXACT Dimension Match (FIFO)
        // ═══════════════════════════════════════════════════════════════════
        log("\n┌─ STEP 1: END PIECES — EXACT DIMENSION MATCH ─────────────────────────────┐");

        List<Map<String, Object>> exactEndPieces = new ArrayList<>();
        List<Map<String, Object>> higherEndPieces = new ArrayList<>();

        for (Map<String, Object> ep : endPieces) {
            BigDecimal[] stockDim = getDimFromBundle(ep);
            if (DimensionMatcher.isExactDimensionMatch(stockDim, requiredDimension)) {
                exactEndPieces.add(ep);
            } else {
                higherEndPieces.add(ep); // For Step 2
            }
        }

        // Sort exact matches by FIFO (GRN timestamp)
        exactEndPieces.sort(Comparator.comparingLong(b -> getTimestamp(b)));

        for (Map<String, Object> ep : exactEndPieces) {
            if (remainingQty.compareTo(BigDecimal.ZERO) <= 0) break;
            BigDecimal qty = GET_QTY_KG.apply(ep);
            BigDecimal takeQty = qty.min(remainingQty);

            Map<String, Object> allocation = new LinkedHashMap<>(ep);
            allocation.put("allocatedQty", takeQty);
            allocation.put("allocationStep", 1);
            allocation.put("isPartial", takeQty.compareTo(qty) < 0);
            if (takeQty.compareTo(qty) < 0) {
                allocation.put("remainingInBundle", qty.subtract(takeQty));
            }

            selectedBundles.add(allocation);
            totalAllocated = totalAllocated.add(takeQty);
            remainingQty = remainingQty.subtract(takeQty);

            log("│ ✅ EP (Exact) " + ep.get("bundleId") + " | Dim: " + formatDim(getDimFromBundle(ep)) +
                    " | Take: " + takeQty + " KG | Remaining: " + remainingQty + " KG");
        }

        if (remainingQty.compareTo(BigDecimal.ZERO) <= 0) {
            log("│ ✅ FULFILLED from End Pieces (exact match)");
            return buildCutResult(selectedBundles, requiredQtyKg, totalAllocated, "END_PIECE",
                    "STEP 1: End Pieces exact match — " + totalAllocated + " KG", 1);
        }
        log("│ Allocated so far: " + totalAllocated + " KG | Remaining: " + remainingQty + " KG");
        log("└─ → Continue to Step 2 ─────────────────────────────────────────────────────┘");

        // ═══════════════════════════════════════════════════════════════════
        // STEP 2: End Pieces — NEAREST HIGHER Dimensions (ascending W×L, then FIFO)
        // ═══════════════════════════════════════════════════════════════════
        log("\n┌─ STEP 2: END PIECES — HIGHER DIMENSIONS ─────────────────────────────────┐");

        // Sort by ascending flexible area (W×L), then FIFO
        higherEndPieces.sort(Comparator
                .comparing((Map<String, Object> b) -> DimensionMatcher.getFlexibleArea(getDimFromBundle(b), productCategory))
                .thenComparingLong(b -> getTimestamp(b)));

        for (Map<String, Object> ep : higherEndPieces) {
            if (remainingQty.compareTo(BigDecimal.ZERO) <= 0) break;
            BigDecimal qty = GET_QTY_KG.apply(ep);
            BigDecimal takeQty = qty.min(remainingQty);

            Map<String, Object> allocation = new LinkedHashMap<>(ep);
            allocation.put("allocatedQty", takeQty);
            allocation.put("allocationStep", 2);
            allocation.put("isPartial", takeQty.compareTo(qty) < 0);
            if (takeQty.compareTo(qty) < 0) {
                allocation.put("remainingInBundle", qty.subtract(takeQty));
            }

            selectedBundles.add(allocation);
            totalAllocated = totalAllocated.add(takeQty);
            remainingQty = remainingQty.subtract(takeQty);

            log("│ ✅ EP (Higher) " + ep.get("bundleId") + " | Dim: " + formatDim(getDimFromBundle(ep)) +
                    " | Take: " + takeQty + " KG | Remaining: " + remainingQty + " KG");
        }

        if (remainingQty.compareTo(BigDecimal.ZERO) <= 0) {
            log("│ ✅ FULFILLED from End Pieces (exact + higher)");
            return buildCutResult(selectedBundles, requiredQtyKg, totalAllocated, "END_PIECE",
                    "STEP 1+2: End Pieces = " + totalAllocated + " KG", 2);
        }
        log("│ Allocated so far: " + totalAllocated + " KG | Remaining: " + remainingQty + " KG");
        log("└─ → Continue to Step 3 ─────────────────────────────────────────────────────┘");

        // ═══════════════════════════════════════════════════════════════════
        // STEP 3: Loose Pieces — Matching/Higher Dimensions (ascending W×L, then FIFO)
        // ═══════════════════════════════════════════════════════════════════
        log("\n┌─ STEP 3: LOOSE PIECES — MATCHING/HIGHER DIMENSIONS ─────────────────────┐");

        // Sort by ascending flexible area, then FIFO
        loose.sort(Comparator
                .comparing((Map<String, Object> b) -> DimensionMatcher.getFlexibleArea(getDimFromBundle(b), productCategory))
                .thenComparingLong(b -> getTimestamp(b)));

        for (Map<String, Object> lp : loose) {
            if (remainingQty.compareTo(BigDecimal.ZERO) <= 0) break;
            BigDecimal qty = GET_QTY_KG.apply(lp);
            BigDecimal takeQty = qty.min(remainingQty);

            Map<String, Object> allocation = new LinkedHashMap<>(lp);
            allocation.put("allocatedQty", takeQty);
            allocation.put("allocationStep", 3);
            allocation.put("isPartial", takeQty.compareTo(qty) < 0);
            if (takeQty.compareTo(qty) < 0) {
                allocation.put("remainingInBundle", qty.subtract(takeQty));
            }

            selectedBundles.add(allocation);
            totalAllocated = totalAllocated.add(takeQty);
            remainingQty = remainingQty.subtract(takeQty);

            log("│ ✅ LP " + lp.get("bundleId") + " | Dim: " + formatDim(getDimFromBundle(lp)) +
                    " | Take: " + takeQty + " KG | Remaining: " + remainingQty + " KG");
        }

        if (remainingQty.compareTo(BigDecimal.ZERO) <= 0) {
            log("│ ✅ FULFILLED from End Pieces + Loose Pieces");
            return buildCutResult(selectedBundles, requiredQtyKg, totalAllocated, "END_PIECE_PLUS_LOOSE_PIECE",
                    "STEP 1-3: End + Loose = " + totalAllocated + " KG", 3);
        }
        log("│ Allocated so far: " + totalAllocated + " KG | Remaining: " + remainingQty + " KG");
        log("└─ → Continue to Step 4 ─────────────────────────────────────────────────────┘");

        // ═══════════════════════════════════════════════════════════════════
        // STEP 4: Warehouse Bundles — Matching/Higher Dimensions (FIFO)
        // ═══════════════════════════════════════════════════════════════════
        log("\n┌─ STEP 4: WAREHOUSE BUNDLES — MATCHING/HIGHER DIMENSIONS ────────────────┐");

        // Sort warehouse by FIFO only
        warehouse.sort(Comparator.comparingLong(b -> getTimestamp(b)));

        for (Map<String, Object> wh : warehouse) {
            if (remainingQty.compareTo(BigDecimal.ZERO) <= 0) break;
            BigDecimal qty = GET_QTY_KG.apply(wh);
            BigDecimal takeQty = qty.min(remainingQty);

            Map<String, Object> allocation = new LinkedHashMap<>(wh);
            allocation.put("allocatedQty", takeQty);
            allocation.put("allocationStep", 4);
            allocation.put("isPartial", takeQty.compareTo(qty) < 0);
            if (takeQty.compareTo(qty) < 0) {
                allocation.put("remainingInBundle", qty.subtract(takeQty));
                log("│ ⚠️ PARTIAL: Taking " + takeQty + " KG from " + qty + " KG bundle. " +
                        (qty.subtract(takeQty)) + " KG remains at same location.");
            }

            selectedBundles.add(allocation);
            totalAllocated = totalAllocated.add(takeQty);
            remainingQty = remainingQty.subtract(takeQty);

            log("│ ✅ WH " + wh.get("bundleId") + " | Dim: " + formatDim(getDimFromBundle(wh)) +
                    " | Take: " + takeQty + " KG | Remaining: " + remainingQty + " KG");
        }

        if (remainingQty.compareTo(BigDecimal.ZERO) <= 0) {
            log("│ ✅ FULFILLED from all stores");
            return buildCutResult(selectedBundles, requiredQtyKg, totalAllocated, "ALL_STORES",
                    "STEP 1-4: End + Loose + Warehouse = " + totalAllocated + " KG", 4);
        }

        // ═══════════════════════════════════════════════════════════════════
        // SHORTFALL
        // ═══════════════════════════════════════════════════════════════════
        BigDecimal shortfall = remainingQty;
        log("│ ⚠️ SHORTFALL: " + shortfall + " KG still needed");
        log("│ → Generate Purchase Indent for shortfall");
        log("└──────────────────────────────────────────────────────────────────────────────┘");

        String storageType = determineStorageType(selectedBundles);
        return FifoResult.builder()
                .selectedBundles(selectedBundles)
                .totalSelectedQtyKg(totalAllocated)
                .totalSelectedQtyNo(selectedBundles.stream().mapToInt(GET_QTY_NO::apply).sum())
                .storageType(storageType)
                .selectionReason("SHORTFALL: Allocated " + totalAllocated + " KG of " + requiredQtyKg + " KG. Shortfall: " + shortfall + " KG")
                .matchedStep(4)
                .selectionDone(true)
                .shortfallKg(shortfall)
                .hasShortfall(true)
                .build();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // UTILITY METHODS
    // ═══════════════════════════════════════════════════════════════════════════

    private static FifoResult buildCutResult(
            List<Map<String, Object>> selected,
            BigDecimal requiredQtyKg,
            BigDecimal totalAllocated,
            String storageType,
            String reason,
            int step) {

        boolean hasShortfall = totalAllocated.compareTo(requiredQtyKg) < 0;
        BigDecimal shortfall = hasShortfall ? requiredQtyKg.subtract(totalAllocated) : BigDecimal.ZERO;
        int totalNo = selected.stream().mapToInt(GET_QTY_NO::apply).sum();

        log("\n╔══════════════════════════════════════════════════════════════════════════════╗");
        log("║        ✂️ CUT ENGINE — RESULT                                               ║");
        log("╠══════════════════════════════════════════════════════════════════════════════╣");
        log("║ Step          : " + step);
        log("║ Storage Type  : " + storageType);
        log("║ Selected      : " + selected.size() + " items | " + totalAllocated + " KG");
        log("║ Required      : " + requiredQtyKg + " KG");
        log("║ Shortfall     : " + (hasShortfall ? shortfall + " KG ⚠️" : "None ✅"));
        log("╚══════════════════════════════════════════════════════════════════════════════╝");

        return FifoResult.builder()
                .selectedBundles(selected)
                .totalSelectedQtyKg(totalAllocated)
                .totalSelectedQtyNo(totalNo)
                .storageType(storageType)
                .selectionReason(reason)
                .matchedStep(step)
                .selectionDone(true)
                .shortfallKg(shortfall)
                .hasShortfall(hasShortfall)
                .build();
    }

    private static BigDecimal[] getDimFromBundle(Map<String, Object> bundle) {
        // Try pre-parsed dims first
        Object d1 = bundle.get("dim1");
        Object d2 = bundle.get("dim2");
        Object d3 = bundle.get("dim3");

        if (d1 instanceof Number) {
            return new BigDecimal[]{
                    toBigDecimal(d1),
                    toBigDecimal(d2),
                    toBigDecimal(d3)
            };
        }

        // Fallback: parse from dimension string
        Object dimStr = bundle.get("dimension");
        if (dimStr instanceof String) {
            return DimensionMatcher.parseDimension((String) dimStr);
        }

        return new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO};
    }

    private static BigDecimal toBigDecimal(Object val) {
        if (val == null) return BigDecimal.ZERO;
        if (val instanceof BigDecimal) return (BigDecimal) val;
        if (val instanceof Number) return BigDecimal.valueOf(((Number) val).doubleValue());
        try { return new BigDecimal(val.toString()); } catch (Exception e) { return BigDecimal.ZERO; }
    }

    private static long getTimestamp(Map<String, Object> bundle) {
        Object ts = bundle.get("grnTimestamp");
        if (ts instanceof Number) return ((Number) ts).longValue();
        return Long.MAX_VALUE;
    }

    private static String formatDim(BigDecimal[] dim) {
        if (dim == null) return "N/A";
        StringBuilder sb = new StringBuilder();
        sb.append(dim[0]);
        if (dim[1].compareTo(BigDecimal.ZERO) > 0) sb.append(" × ").append(dim[1]);
        if (dim[2].compareTo(BigDecimal.ZERO) > 0) sb.append(" × ").append(dim[2]);
        return sb.toString();
    }

    private static BigDecimal sumQty(List<Map<String, Object>> bundles) {
        return bundles.stream().map(GET_QTY_KG).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static String determineStorageType(List<Map<String, Object>> selected) {
        Set<String> stores = selected.stream()
                .map(b -> b.getOrDefault("store", "").toString().toUpperCase())
                .collect(Collectors.toSet());

        if (stores.size() == 1) {
            String store = stores.iterator().next();
            if (store.contains("END")) return "END_PIECE";
            if (store.contains("LOOSE")) return "LOOSE_PIECE";
            return "WAREHOUSE";
        }
        return "MULTI_STORE";
    }

    private static void log(String msg) {
        System.out.println(msg);
    }
}
