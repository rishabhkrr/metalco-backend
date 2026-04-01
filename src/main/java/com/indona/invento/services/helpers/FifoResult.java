package com.indona.invento.services.helpers;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Result of the FIFO Picklist Allocation algorithm.
 * Contains the selected bundles, storage type, and any shortfall information.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FifoResult {

    /** The bundles selected by the algorithm (in FIFO order) */
    private List<Map<String, Object>> selectedBundles;

    /** Total quantity in Kg of selected bundles */
    private BigDecimal totalSelectedQtyKg;

    /** Total quantity in Nos of selected bundles */
    private int totalSelectedQtyNo;

    /** Storage type describing where the stock was selected from */
    private String storageType;

    /** Human-readable reason for the selection */
    private String selectionReason;

    /** Which step in the algorithm matched */
    private int matchedStep;

    /** Whether the algorithm found sufficient stock */
    private boolean selectionDone;

    /** Shortfall information (if total < required) */
    private BigDecimal shortfallKg;

    /** Whether there is a shortfall */
    private boolean hasShortfall;
}
