package com.indona.invento.services.helpers;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;


/**
 * FIFO Picklist Allocation Engine — FRD 4A.2A (9-Step FULL Algorithm)
 *
 * Implements the complete 9-step cascading algorithm for Order Type = FULL:
 *   Step 1: Exact Match (Loose + Warehouse)
 *   Step 2: Loose Pieces ±5% (3 cases)
 *   Step 3: Single Warehouse Bundle ±5%
 *   Step 4: Multiple Warehouse Bundles ±5%
 *   Step 5: Bundle + Loose Combination
 *   Step 6: Only Bundles Available
 *   Step 7: Total Check
 *   Step 8: Common + Bundles + Loose
 *   Step 9: Common + Bundles only
 *
 * Used by: Sales Order Scheduling (FULL), IU Material Transfer (FULL only)
 * All bundles must be pre-sorted by inward time (FIFO — oldest first).
 */
public class FifoPicklistEngine {

    private static final BigDecimal TOLERANCE_PERCENT = new BigDecimal("0.05");

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
     * Run the 9-step FIFO allocation algorithm for FULL order type.
     *
     * @param requiredQtyKg      Required quantity in Kg
     * @param loosePieceBundles   Loose piece bundles sorted FIFO (oldest first)
     * @param warehouseBundles    Warehouse rack bundles sorted FIFO
     * @param commonBundles       Common store bundles sorted FIFO (no QR)
     * @return FifoResult with selected bundles and metadata
     */
    public static FifoResult allocate(
            BigDecimal requiredQtyKg,
            List<Map<String, Object>> loosePieceBundles,
            List<Map<String, Object>> warehouseBundles,
            List<Map<String, Object>> commonBundles) {

        // Defensive copies
        List<Map<String, Object>> loose = loosePieceBundles != null ? new ArrayList<>(loosePieceBundles) : new ArrayList<>();
        List<Map<String, Object>> warehouse = warehouseBundles != null ? new ArrayList<>(warehouseBundles) : new ArrayList<>();
        List<Map<String, Object>> common = commonBundles != null ? new ArrayList<>(commonBundles) : new ArrayList<>();

        // ±5% tolerance
        BigDecimal toleranceAmt = requiredQtyKg.multiply(TOLERANCE_PERCENT);
        BigDecimal lowerBound = requiredQtyKg.subtract(toleranceAmt);
        BigDecimal upperBound = requiredQtyKg.add(toleranceAmt);

        log("╔══════════════════════════════════════════════════════════════════════════════╗");
        log("║        📋 FIFO ENGINE — 9-STEP FULL ORDER ALGORITHM (FRD 4A.2A)            ║");
        log("╠══════════════════════════════════════════════════════════════════════════════╣");
        log("║ Required Qty  : " + requiredQtyKg + " KG");
        log("║ Tolerance     : ±5% [" + lowerBound + " — " + upperBound + " KG]");
        log("║ Loose Pieces  : " + loose.size() + " bundles | " + sumQty(loose) + " KG");
        log("║ Warehouse     : " + warehouse.size() + " bundles | " + sumQty(warehouse) + " KG");
        log("║ Common        : " + common.size() + " bundles | " + sumQty(common) + " KG");
        log("╚══════════════════════════════════════════════════════════════════════════════╝");

        // ═══════════════════════════════════════════════════════════════════
        // STEP 1: Exact Match (Loose + Warehouse)
        // ═══════════════════════════════════════════════════════════════════
        log("\n┌─ STEP 1: EXACT MATCH (Loose + Warehouse) ─────────────────────────────────┐");

        // Search loose first (higher priority), then warehouse
        List<Map<String, Object>> step1Pool = new ArrayList<>();
        step1Pool.addAll(loose);
        step1Pool.addAll(warehouse);

        for (Map<String, Object> bundle : step1Pool) {
            BigDecimal qty = GET_QTY_KG.apply(bundle);
            if (qty.compareTo(requiredQtyKg) == 0) {
                log("│ ✅ EXACT MATCH: Bundle " + bundle.get("bundleId") + " = " + qty + " KG");
                return buildResult(List.of(bundle), requiredQtyKg, getStoreLabel(bundle, loose, warehouse, common),
                        "STEP 1: Exact match — Bundle " + bundle.get("bundleId") + " = " + qty + " KG", 1);
            }
        }
        log("│ ❌ No exact match found");
        log("└─ → Continue to Step 2 ─────────────────────────────────────────────────────┘");

        // ═══════════════════════════════════════════════════════════════════
        // STEP 2: Loose Pieces ±5% Tolerance (3 Cases)
        // ═══════════════════════════════════════════════════════════════════
        if (!loose.isEmpty()) {
            log("\n┌─ STEP 2: LOOSE PIECES (±5% Tolerance) ─────────────────────────────────────┐");

            // Case 1: Within tolerance [RQ-5%, RQ+5%] — pick closest to RQ
            List<Map<String, Object>> inTolerance = new ArrayList<>();
            for (Map<String, Object> b : loose) {
                BigDecimal qty = GET_QTY_KG.apply(b);
                if (qty.compareTo(lowerBound) >= 0 && qty.compareTo(upperBound) <= 0) {
                    inTolerance.add(b);
                    log("│   ✅ Case 1 candidate: Bundle " + b.get("bundleId") + " = " + qty + " KG (within tolerance)");
                }
            }

            if (!inTolerance.isEmpty()) {
                // Pick closest to RQ, then FIFO
                Map<String, Object> best = inTolerance.stream()
                        .min(Comparator.comparing(b -> GET_QTY_KG.apply(b).subtract(requiredQtyKg).abs()))
                        .orElse(inTolerance.get(0));
                log("│ ✅ Case 1: Selected closest to RQ — Bundle " + best.get("bundleId") + " = " + GET_QTY_KG.apply(best) + " KG");
                return buildResult(List.of(best), requiredQtyKg, "LOOSE_PIECE",
                        "STEP 2 Case 1: Loose piece within ±5% — " + best.get("bundleId") + " = " + GET_QTY_KG.apply(best) + " KG", 2);
            }

            // Case 2: All loose pieces < RQ-5% — pick smallest available (closest to RQ)
            List<Map<String, Object>> belowTolerance = new ArrayList<>();
            for (Map<String, Object> b : loose) {
                BigDecimal qty = GET_QTY_KG.apply(b);
                if (qty.compareTo(lowerBound) < 0 && qty.compareTo(BigDecimal.ZERO) > 0) {
                    belowTolerance.add(b);
                }
            }

            boolean allBelow = !loose.isEmpty() && inTolerance.isEmpty()
                    && loose.stream().allMatch(b -> GET_QTY_KG.apply(b).compareTo(lowerBound) < 0);

            if (allBelow && !belowTolerance.isEmpty()) {
                // Select smallest available loose piece
                Map<String, Object> smallest = belowTolerance.stream()
                        .min(Comparator.comparing(b -> GET_QTY_KG.apply(b)))
                        .orElse(belowTolerance.get(0));
                log("│ ✅ Case 2: All loose < -5%. Selected smallest: Bundle " + smallest.get("bundleId") + " = " + GET_QTY_KG.apply(smallest) + " KG");
                return buildResult(List.of(smallest), requiredQtyKg, "LOOSE_PIECE",
                        "STEP 2 Case 2: All loose below tolerance — smallest = " + GET_QTY_KG.apply(smallest) + " KG", 2);
            }

            // Case 3: No suitable loose piece → proceed
            log("│ ❌ Case 3: No suitable loose piece → proceed to Step 3");
            log("└─ → Continue to Step 3 ─────────────────────────────────────────────────────┘");
        }

        // ═══════════════════════════════════════════════════════════════════
        // STEP 3: Single Warehouse Bundle ±5%
        // ═══════════════════════════════════════════════════════════════════
        if (!warehouse.isEmpty()) {
            log("\n┌─ STEP 3: SINGLE WAREHOUSE BUNDLE (±5%) ───────────────────────────────────┐");

            Map<String, Object> bestMatch = null;
            BigDecimal bestDiff = null;

            for (Map<String, Object> b : warehouse) {
                BigDecimal qty = GET_QTY_KG.apply(b);
                if (qty.compareTo(lowerBound) >= 0 && qty.compareTo(upperBound) <= 0) {
                    BigDecimal diff = qty.subtract(requiredQtyKg).abs();
                    log("│   ✅ Candidate: Bundle " + b.get("bundleId") + " = " + qty + " KG");
                    if (bestDiff == null || diff.compareTo(bestDiff) < 0) {
                        bestMatch = b;
                        bestDiff = diff;
                    }
                }
            }

            if (bestMatch != null) {
                log("│ ✅ Selected: Bundle " + bestMatch.get("bundleId") + " = " + GET_QTY_KG.apply(bestMatch) + " KG");
                return buildResult(List.of(bestMatch), requiredQtyKg, "WAREHOUSE",
                        "STEP 3: Single warehouse bundle within ±5% — " + bestMatch.get("bundleId") + " = " + GET_QTY_KG.apply(bestMatch) + " KG", 3);
            }
            log("│ ❌ No single warehouse bundle within ±5%");
            log("└─ → Continue to Step 4 ─────────────────────────────────────────────────────┘");
        }

        // ═══════════════════════════════════════════════════════════════════
        // STEP 4: Multiple Warehouse Bundles (sum within ±5%)
        // ═══════════════════════════════════════════════════════════════════
        if (!warehouse.isEmpty()) {
            log("\n┌─ STEP 4: MULTIPLE WAREHOUSE BUNDLES (sum ±5%) ────────────────────────────┐");

            List<Map<String, Object>> tempSelected = new ArrayList<>();
            BigDecimal runningTotal = BigDecimal.ZERO;

            for (Map<String, Object> b : warehouse) {
                BigDecimal qty = GET_QTY_KG.apply(b);
                tempSelected.add(b);
                runningTotal = runningTotal.add(qty);
                log("│   + Bundle " + b.get("bundleId") + " | " + qty + " KG | Running: " + runningTotal + " KG");

                // Check if within tolerance
                if (runningTotal.compareTo(lowerBound) >= 0 && runningTotal.compareTo(upperBound) <= 0) {
                    log("│ ✅ Sum within ±5% tolerance!");
                    return buildResult(tempSelected, requiredQtyKg, "WAREHOUSE",
                            "STEP 4: " + tempSelected.size() + " warehouse bundles = " + runningTotal + " KG (within ±5%)", 4);
                }
                if (runningTotal.compareTo(upperBound) > 0) {
                    log("│ ⚠️ Sum exceeded upper bound (" + upperBound + " KG)");
                    break;
                }
            }
            log("│ ❌ Cannot achieve ±5% with warehouse bundles alone (total: " + runningTotal + " KG)");
            log("└─ → Continue to Step 5 ─────────────────────────────────────────────────────┘");
        }

        // ═══════════════════════════════════════════════════════════════════
        // STEP 5: Bundle + Loose Combination
        // ═══════════════════════════════════════════════════════════════════
        if (!warehouse.isEmpty() && !loose.isEmpty()) {
            log("\n┌─ STEP 5: BUNDLE + LOOSE COMBINATION ──────────────────────────────────────┐");

            List<Map<String, Object>> tempSelected = new ArrayList<>();
            BigDecimal runningTotal = BigDecimal.ZERO;

            // Add warehouse bundles first (FIFO)
            for (Map<String, Object> b : warehouse) {
                BigDecimal qty = GET_QTY_KG.apply(b);
                tempSelected.add(b);
                runningTotal = runningTotal.add(qty);
                log("│   + WH " + b.get("bundleId") + " | " + qty + " KG | Running: " + runningTotal + " KG");
                if (runningTotal.compareTo(requiredQtyKg) >= 0) break;
            }

            // Add loose pieces if still short (FIFO)
            if (runningTotal.compareTo(requiredQtyKg) < 0) {
                for (Map<String, Object> b : loose) {
                    BigDecimal qty = GET_QTY_KG.apply(b);
                    tempSelected.add(b);
                    runningTotal = runningTotal.add(qty);
                    log("│   + LP " + b.get("bundleId") + " | " + qty + " KG | Running: " + runningTotal + " KG");
                    if (runningTotal.compareTo(requiredQtyKg) >= 0) break;
                }
            }

            if (runningTotal.compareTo(lowerBound) >= 0) {
                log("│ ✅ Combined: " + runningTotal + " KG");
                return buildResult(tempSelected, requiredQtyKg, "WAREHOUSE_PLUS_LOOSE_PIECE",
                        "STEP 5: Warehouse + Loose Piece = " + runningTotal + " KG", 5);
            }
            log("│ ❌ Combined still insufficient: " + runningTotal + " KG");
            log("└─ → Continue to Step 6 ─────────────────────────────────────────────────────┘");
        }

        // ═══════════════════════════════════════════════════════════════════
        // STEP 6: Only Bundles Available (no loose)
        // ═══════════════════════════════════════════════════════════════════
        List<Map<String, Object>> step6Selected = new ArrayList<>();
        BigDecimal step6Total = BigDecimal.ZERO;

        if (!warehouse.isEmpty()) {
            log("\n┌─ STEP 6: ONLY BUNDLES AVAILABLE ──────────────────────────────────────────┐");

            for (Map<String, Object> b : warehouse) {
                BigDecimal qty = GET_QTY_KG.apply(b);
                step6Selected.add(b);
                step6Total = step6Total.add(qty);
                log("│   + Bundle " + b.get("bundleId") + " | " + qty + " KG | Running: " + step6Total + " KG");
                if (step6Total.compareTo(requiredQtyKg) >= 0) break;
            }
            log("│ Closest bundles total: " + step6Total + " KG");
            log("└─ → Continue to Step 7 ─────────────────────────────────────────────────────┘");
        }

        // ═══════════════════════════════════════════════════════════════════
        // STEP 7: Total Selected Quantity Check
        // ═══════════════════════════════════════════════════════════════════
        log("\n┌─ STEP 7: TOTAL SELECTED QUANTITY CHECK ───────────────────────────────────┐");

        // Combine step 6 + any remaining loose
        List<Map<String, Object>> step7Selected = new ArrayList<>(step6Selected);
        BigDecimal step7Total = step6Total;

        // Add any loose pieces not already in step 6
        if (!loose.isEmpty() && step7Total.compareTo(requiredQtyKg) < 0) {
            for (Map<String, Object> b : loose) {
                if (!step7Selected.contains(b)) {
                    BigDecimal qty = GET_QTY_KG.apply(b);
                    step7Selected.add(b);
                    step7Total = step7Total.add(qty);
                    if (step7Total.compareTo(requiredQtyKg) >= 0) break;
                }
            }
        }

        log("│ Total (Warehouse + Loose): " + step7Total + " KG | Required: " + requiredQtyKg + " KG");

        if (step7Total.compareTo(requiredQtyKg) >= 0) {
            log("│ ✅ Total >= Required → allocation complete");
            String sType = step6Selected.size() > 0 && loose.stream().anyMatch(step7Selected::contains)
                    ? "WAREHOUSE_PLUS_LOOSE_PIECE" : "WAREHOUSE";
            return buildResult(step7Selected, requiredQtyKg, sType,
                    "STEP 7: Total = " + step7Total + " KG (Warehouse + Loose)", 7);
        }
        log("│ ❌ Total < Required → proceed to Step 8");
        log("└─ → Continue to Step 8 ─────────────────────────────────────────────────────┘");

        // ═══════════════════════════════════════════════════════════════════
        // STEP 8: Common + Bundles + Loose
        // ═══════════════════════════════════════════════════════════════════
        if (!common.isEmpty() && !loose.isEmpty()) {
            log("\n┌─ STEP 8: COMMON + BUNDLES + LOOSE ────────────────────────────────────────┐");

            List<Map<String, Object>> step8Selected = new ArrayList<>(step7Selected);
            BigDecimal step8Total = step7Total;

            for (Map<String, Object> b : common) {
                BigDecimal qty = GET_QTY_KG.apply(b);
                step8Selected.add(b);
                step8Total = step8Total.add(qty);
                log("│   + Common " + b.get("bundleId") + " | " + qty + " KG | Running: " + step8Total + " KG");
                if (step8Total.compareTo(requiredQtyKg) >= 0) break;
            }

            if (step8Total.compareTo(requiredQtyKg) >= 0) {
                log("│ ✅ All three stores combined: " + step8Total + " KG");
                return buildResult(step8Selected, requiredQtyKg, "WAREHOUSE_PLUS_LOOSE_PIECE_PLUS_COMMON",
                        "STEP 8: Common + Bundles + Loose = " + step8Total + " KG", 8);
            }

            // Even with all three, still short — will fall into shortfall
            log("│ ❌ All three stores still insufficient: " + step8Total + " KG");
            log("└─ → Continue to Step 9 ─────────────────────────────────────────────────────┘");

            // Use step8 selection for final shortfall
            step7Selected = step8Selected;
            step7Total = step8Total;
        }

        // ═══════════════════════════════════════════════════════════════════
        // STEP 9: Common + Bundles (no loose)
        // ═══════════════════════════════════════════════════════════════════
        if (!common.isEmpty()) {
            log("\n┌─ STEP 9: COMMON + BUNDLES (no loose available or insufficient) ────────────┐");

            List<Map<String, Object>> step9Selected = new ArrayList<>(step7Selected);
            BigDecimal step9Total = step7Total;

            // Add common bundles if not already added in step 8
            for (Map<String, Object> b : common) {
                if (!step9Selected.contains(b)) {
                    BigDecimal qty = GET_QTY_KG.apply(b);
                    step9Selected.add(b);
                    step9Total = step9Total.add(qty);
                    log("│   + Common " + b.get("bundleId") + " | " + qty + " KG | Running: " + step9Total + " KG");
                    if (step9Total.compareTo(requiredQtyKg) >= 0) break;
                }
            }

            if (step9Total.compareTo(requiredQtyKg) >= 0) {
                log("│ ✅ Common + Bundles: " + step9Total + " KG");
                String sType = step9Selected.stream().anyMatch(loose::contains)
                        ? "WAREHOUSE_PLUS_LOOSE_PIECE_PLUS_COMMON" : "WAREHOUSE_PLUS_COMMON";
                return buildResult(step9Selected, requiredQtyKg, sType,
                        "STEP 9: Common + Bundles = " + step9Total + " KG", 9);
            }

            // Use step9 for final shortfall
            step7Selected = step9Selected;
            step7Total = step9Total;

            log("│ ❌ Still insufficient: " + step9Total + " KG");
            log("└──────────────────────────────────────────────────────────────────────────────┘");
        }

        // ═══════════════════════════════════════════════════════════════════
        // SHORTFALL — Allocate whatever is available
        // ═══════════════════════════════════════════════════════════════════
        log("\n┌─ ⚠️ SHORTFALL — Allocating all available stock ─────────────────────────────┐");

        // If no bundles selected at all, gather everything
        if (step7Selected.isEmpty()) {
            for (Map<String, Object> b : loose) step7Selected.add(b);
            for (Map<String, Object> b : warehouse) { if (!step7Selected.contains(b)) step7Selected.add(b); }
            for (Map<String, Object> b : common) { if (!step7Selected.contains(b)) step7Selected.add(b); }
            step7Total = step7Selected.stream().map(GET_QTY_KG).reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        BigDecimal shortfall = requiredQtyKg.subtract(step7Total);
        log("│ Allocated: " + step7Total + " KG | Required: " + requiredQtyKg + " KG | Shortfall: " + shortfall + " KG");
        log("│ ⚠️ Generate Purchase Indent for shortfall quantity");
        log("└──────────────────────────────────────────────────────────────────────────────┘");

        String sType = "ALL_STORES";
        if (step7Selected.stream().allMatch(warehouse::contains)) sType = "WAREHOUSE";

        return FifoResult.builder()
                .selectedBundles(step7Selected)
                .totalSelectedQtyKg(step7Total)
                .totalSelectedQtyNo(step7Selected.stream().mapToInt(GET_QTY_NO::apply).sum())
                .storageType(sType)
                .selectionReason("SHORTFALL: Only " + step7Total + " KG available of " + requiredQtyKg + " KG required. Shortfall: " + shortfall + " KG")
                .matchedStep(0)
                .selectionDone(true) // partial allocation is still "done"
                .shortfallKg(shortfall)
                .hasShortfall(true)
                .build();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // UTILITY METHODS
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Build a successful FifoResult.
     */
    private static FifoResult buildResult(
            List<Map<String, Object>> selected,
            BigDecimal requiredQtyKg,
            String storageType,
            String reason,
            int step) {

        BigDecimal totalKg = selected.stream().map(GET_QTY_KG).reduce(BigDecimal.ZERO, BigDecimal::add);
        int totalNo = selected.stream().mapToInt(GET_QTY_NO::apply).sum();
        boolean hasShortfall = totalKg.compareTo(requiredQtyKg) < 0;
        BigDecimal shortfall = hasShortfall ? requiredQtyKg.subtract(totalKg) : BigDecimal.ZERO;

        log("\n╔══════════════════════════════════════════════════════════════════════════════╗");
        log("║        📋 FIFO ENGINE — RESULT                                              ║");
        log("╠══════════════════════════════════════════════════════════════════════════════╣");
        log("║ Step          : " + step);
        log("║ Storage Type  : " + storageType);
        log("║ Selected      : " + selected.size() + " bundles | " + totalKg + " KG | " + totalNo + " Nos");
        log("║ Required      : " + requiredQtyKg + " KG");
        log("║ Shortfall     : " + (hasShortfall ? shortfall + " KG ⚠️" : "None ✅"));
        log("╚══════════════════════════════════════════════════════════════════════════════╝");

        for (int i = 0; i < selected.size(); i++) {
            Map<String, Object> b = selected.get(i);
            log("   [" + (i + 1) + "] BundleID: " + b.get("bundleId") + " | Qty: " + GET_QTY_KG.apply(b) +
                    " KG | GRN: " + b.get("grnRefNo") + " | Store: " + b.get("store"));
        }

        return FifoResult.builder()
                .selectedBundles(new ArrayList<>(selected))
                .totalSelectedQtyKg(totalKg)
                .totalSelectedQtyNo(totalNo)
                .storageType(storageType)
                .selectionReason(reason)
                .matchedStep(step)
                .selectionDone(true)
                .shortfallKg(shortfall)
                .hasShortfall(hasShortfall)
                .build();
    }

    /**
     * Determine which store a bundle belongs to.
     */
    private static String getStoreLabel(
            Map<String, Object> bundle,
            List<Map<String, Object>> loose,
            List<Map<String, Object>> warehouse,
            List<Map<String, Object>> common) {
        if (loose.contains(bundle)) return "LOOSE_PIECE";
        if (warehouse.contains(bundle)) return "WAREHOUSE";
        if (common.contains(bundle)) return "WAREHOUSE_COMMON";
        return "UNKNOWN";
    }

    /**
     * Sum all quantities in a list of bundles.
     */
    private static BigDecimal sumQty(List<Map<String, Object>> bundles) {
        return bundles.stream().map(GET_QTY_KG).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Console logging helper.
     */
    private static void log(String msg) {
        System.out.println(msg);
    }
}
