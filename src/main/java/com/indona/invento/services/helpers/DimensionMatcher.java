package com.indona.invento.services.helpers;

import java.math.BigDecimal;
import java.util.*;

/**
 * Dimension Matching Logic for CUT Order Type — FRD 4A.2A.
 *
 * For CUT orders, dimension matching depends on the Product Category.
 * Each category has one "strict" parameter that must match EXACTLY,
 * and flexible parameters that can be >= required.
 *
 * Supported Product Categories:
 *   Flat Bar, Sheet, Plate, Coil      → Strict: Thickness (dim1)
 *   Round Bar, Round Rod, Round Tube  → Strict: Diameter (dim1)
 *   Square Bar, Square Tube           → Strict: Square Side (dim1)
 *   Hexagonal Bar, Hexagonal Tube     → Strict: Hex Side (dim1)
 *   Pentagon Bar, Pentagon Tube       → Strict: Pent Side (dim1)
 *   Extrusion                         → Strict: Kg/Mtr (dim1)
 */
public class DimensionMatcher {

    /**
     * Parse a dimension string like "101 X 150 X 200" or "101 x 1500 x 3000" into [dim1, dim2, dim3].
     * Returns BigDecimal array: index 0 = first value, 1 = second, 2 = third (or ZERO if absent).
     */
    public static BigDecimal[] parseDimension(String dimensionStr) {
        BigDecimal[] result = new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO};
        if (dimensionStr == null || dimensionStr.trim().isEmpty()) return result;

        try {
            // Split by "X", "x", or "×"
            String[] parts = dimensionStr.trim().split("\\s*[Xx×]\\s*");
            for (int i = 0; i < Math.min(parts.length, 3); i++) {
                String cleaned = parts[i].trim().replaceAll("[^0-9.]", "");
                if (!cleaned.isEmpty()) {
                    result[i] = new BigDecimal(cleaned);
                }
            }
        } catch (Exception e) {
            System.out.println("⚠️ DimensionMatcher: Failed to parse dimension '" + dimensionStr + "': " + e.getMessage());
        }
        return result;
    }

    /**
     * Check if a stock dimension is eligible for a CUT order.
     *
     * Rules:
     * - Strict parameter (typically dim1 / thickness / diameter) must match EXACTLY
     * - Flexible parameters (dim2, dim3) must be >= required
     * - If ANY flexible parameter is LESS than required → DO NOT RECOMMEND
     *
     * @param stockDim     Parsed stock dimension [dim1, dim2, dim3]
     * @param requiredDim  Parsed required dimension [dim1, dim2, dim3]
     * @param productCategory  Product category string (case-insensitive)
     * @return true if eligible, false if DO NOT RECOMMEND
     */
    public static boolean isDimensionEligibleForCut(
            BigDecimal[] stockDim,
            BigDecimal[] requiredDim,
            String productCategory) {

        if (stockDim == null || requiredDim == null) return false;

        String cat = (productCategory != null) ? productCategory.trim().toUpperCase() : "";

        // Determine which index is strict (must be EXACT) vs flexible (must be >=)
        // For ALL categories, dim1 (index 0) is the strict parameter
        int strictIndex = 0;
        List<Integer> flexIndices = getFlexibleIndices(cat, stockDim, requiredDim);

        // STRICT CHECK: dim1 must match EXACTLY
        if (stockDim[strictIndex].compareTo(requiredDim[strictIndex]) != 0) {
            return false; // DO NOT RECOMMEND
        }

        // FLEXIBLE CHECK: all flex dimensions must be >= required
        for (int idx : flexIndices) {
            if (idx < stockDim.length && idx < requiredDim.length) {
                if (requiredDim[idx].compareTo(BigDecimal.ZERO) > 0) { // Only check if required dim exists
                    if (stockDim[idx].compareTo(requiredDim[idx]) < 0) {
                        return false; // DO NOT RECOMMEND
                    }
                }
            }
        }

        return true; // ELIGIBLE
    }

    /**
     * Check if ALL dimensions match exactly (for Step 1 of CUT algorithm).
     */
    public static boolean isExactDimensionMatch(BigDecimal[] stockDim, BigDecimal[] requiredDim) {
        if (stockDim == null || requiredDim == null) return false;
        for (int i = 0; i < 3; i++) {
            if (stockDim[i].compareTo(requiredDim[i]) != 0) return false;
        }
        return true;
    }

    /**
     * Calculate the "area" of flexible dimensions for sorting (ascending W×L).
     * Used in CUT Step 2 and 3 to prefer smaller usable pieces.
     */
    public static BigDecimal getFlexibleArea(BigDecimal[] stockDim, String productCategory) {
        String cat = (productCategory != null) ? productCategory.trim().toUpperCase() : "";
        List<Integer> flexIndices = getFlexibleIndices(cat, stockDim, stockDim);

        BigDecimal area = BigDecimal.ONE;
        for (int idx : flexIndices) {
            if (idx < stockDim.length && stockDim[idx].compareTo(BigDecimal.ZERO) > 0) {
                area = area.multiply(stockDim[idx]);
            }
        }
        return area;
    }

    /**
     * Get the indices of flexible parameters based on product category.
     */
    private static List<Integer> getFlexibleIndices(String cat, BigDecimal[] stockDim, BigDecimal[] requiredDim) {
        // Default: dim1 is strict, dim2 and dim3 are flexible
        List<Integer> flexIndices = new ArrayList<>();

        switch (cat) {
            // 3-dimensional (Thickness × Width × Length)
            case "FLAT BAR":
            case "SHEET":
            case "PLATE":
                flexIndices.add(1); // Width
                flexIndices.add(2); // Length
                break;

            // 2-dimensional (Diameter × Length)
            case "ROUND BAR":
            case "ROUND ROD":
                flexIndices.add(1); // Length
                break;

            // 3-dimensional (Diameter × Thickness × Length)
            case "ROUND TUBE":
                flexIndices.add(1); // Wall Thickness
                flexIndices.add(2); // Length
                break;

            // 2-dimensional (Side × Length)
            case "SQUARE BAR":
            case "HEXAGONAL BAR":
            case "PENTAGON BAR":
                flexIndices.add(1); // Length
                break;

            // 3-dimensional (Side × Thickness × Length)
            case "SQUARE TUBE":
            case "HEXAGONAL TUBE":
            case "PENTAGON TUBE":
                flexIndices.add(1); // Wall Thickness
                flexIndices.add(2); // Length
                break;

            // 2-dimensional (Thickness × Width)
            case "COIL":
                flexIndices.add(1); // Width
                break;

            // 2-dimensional (Kg/Mtr × Length)
            case "EXTRUSION":
                flexIndices.add(1); // Length
                break;

            default:
                // Fallback: dim2 and dim3 are flexible
                flexIndices.add(1);
                flexIndices.add(2);
                break;
        }

        return flexIndices;
    }

    /**
     * Get a human-readable label for the strict parameter of a product category.
     */
    public static String getStrictParamLabel(String productCategory) {
        String cat = (productCategory != null) ? productCategory.trim().toUpperCase() : "";
        switch (cat) {
            case "FLAT BAR": case "SHEET": case "PLATE": case "COIL":
                return "Thickness";
            case "ROUND BAR": case "ROUND ROD": case "ROUND TUBE":
                return "Diameter";
            case "SQUARE BAR": case "SQUARE TUBE":
                return "Square Side";
            case "HEXAGONAL BAR": case "HEXAGONAL TUBE":
                return "Hex Side";
            case "PENTAGON BAR": case "PENTAGON TUBE":
                return "Pent Side";
            case "EXTRUSION":
                return "Kg/Mtr";
            default:
                return "Dim1";
        }
    }
}
