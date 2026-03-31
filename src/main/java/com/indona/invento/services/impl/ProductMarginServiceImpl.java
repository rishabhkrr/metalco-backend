package com.indona.invento.services.impl;

import com.indona.invento.dao.ProductMarginRepository;
import com.indona.invento.entities.MarginRate;
import com.indona.invento.entities.ProductMargin;
import com.indona.invento.services.ProductMarginService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProductMarginServiceImpl implements ProductMarginService {

    private final ProductMarginRepository productMarginRepository;

    public ProductMarginServiceImpl(ProductMarginRepository productMarginRepository) {
        this.productMarginRepository = productMarginRepository;
    }

    @Override
    @Transactional
    public ProductMargin createMargin(ProductMargin margin) {
        // ✅ Validation: Check if materialType + productCategory combination already exists
        if (margin.getMaterialType() != null && margin.getProductCategory() != null) {
            java.util.Optional<ProductMargin> existingMargin = productMarginRepository
                    .findByMaterialTypeAndProductCategory(margin.getMaterialType(), margin.getProductCategory());

            if (existingMargin.isPresent()) {
                throw new RuntimeException(
                    "❌ Error: A Product Margin already exists for Material Type '" + margin.getMaterialType() +
                    "' and Product Category '" + margin.getProductCategory() + "'. " +
                    "Cannot create duplicate combination."
                );
            }
        } else {
            throw new RuntimeException("❌ Error: Material Type and Product Category are required.");
        }

        // Hard-code status and timestamp
        margin.setStatus("PENDING");
        margin.setTimestamp(LocalDateTime.now());

        // Cascade ensures MarginRates are saved
        if (margin.getMarginRates() != null) {
            margin.getMarginRates().forEach(rate -> rate.setProductMargin(margin));
        }

        return productMarginRepository.save(margin);
    }

    @Override public List<ProductMargin> getAllMargins() {
        return productMarginRepository.findAll();
    }

    @Override
    @Transactional
    public ProductMargin approveMargin(Long id) {
        ProductMargin margin = productMarginRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product Margin not found with ID: " + id));
        margin.setStatus("APPROVED");
        return productMarginRepository.save(margin);
    }

    @Override
    @Transactional
    public ProductMargin rejectMargin(Long id) {
        ProductMargin margin = productMarginRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product Margin not found with ID: " + id));
        margin.setStatus("REJECTED");
        return productMarginRepository.save(margin);
    }

    @Override
    @Transactional
    public ProductMargin editMargin(Long id, ProductMargin margin) {
        ProductMargin existing = productMarginRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product Margin not found with ID: " + id));

        // ✅ Validation: Check if new materialType + productCategory combination already exists (excluding current record)
        String newMaterialType = margin.getMaterialType() != null ? margin.getMaterialType() : existing.getMaterialType();
        String newProductCategory = margin.getProductCategory() != null ? margin.getProductCategory() : existing.getProductCategory();

        if (newMaterialType != null && newProductCategory != null) {
            // Check if combination exists with a different ID
            java.util.Optional<ProductMargin> conflictingMargin = productMarginRepository
                    .findByMaterialTypeAndProductCategory(newMaterialType, newProductCategory);

            if (conflictingMargin.isPresent() && !conflictingMargin.get().getId().equals(id)) {
                throw new RuntimeException(
                    "❌ Error: A Product Margin already exists for Material Type '" + newMaterialType +
                    "' and Product Category '" + newProductCategory + "'. " +
                    "Cannot update to duplicate combination."
                );
            }
        }

        // Update only provided fields, keep others unchanged
        if (margin.getMaterialType() != null) {
            existing.setMaterialType(margin.getMaterialType());
        }
        if (margin.getProductCategory() != null) {
            existing.setProductCategory(margin.getProductCategory());
        }
        if (margin.getUserId() != null) {
            existing.setUserId(margin.getUserId());
        }
        if (margin.getStatus() != null) {
            existing.setStatus(margin.getStatus());
        }
        margin.setStatus("PENDING"); // Reset status to PENDING on edit

        // Handle marginRates carefully to avoid cascade orphan delete issues
        if (margin.getMarginRates() != null && !margin.getMarginRates().isEmpty()) {
            // Clear existing rates
            existing.getMarginRates().clear();
            // Add new rates
            for (MarginRate rate : margin.getMarginRates()) {
                rate.setProductMargin(existing);
                existing.getMarginRates().add(rate);
            }
        }

        return productMarginRepository.save(existing);
    }

    @Override
    @Transactional
    public ProductMargin deleteMargin(Long id) {
        ProductMargin margin = productMarginRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product Margin not found with ID: " + id));
        productMarginRepository.deleteById(id);
        return margin;
    }
}
