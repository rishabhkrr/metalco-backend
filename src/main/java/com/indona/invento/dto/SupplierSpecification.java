package com.indona.invento.dto;

import com.indona.invento.entities.SupplierMasterEntity;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class SupplierSpecification {

    public static Specification<SupplierMasterEntity> buildSpecification(SupplierFilterRequest filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter == null) return cb.conjunction(); // No filter — return all

            if (filter.getGstRegistrationType() != null)
                predicates.add(cb.equal(root.get("gstRegistrationType"), filter.getGstRegistrationType()));

            if (filter.getSupplierCategory() != null)
                predicates.add(cb.equal(root.get("supplierCategory"), filter.getSupplierCategory()));

            if (filter.getSupplierType() != null)
                predicates.add(cb.equal(root.get("supplierType"), filter.getSupplierType()));

            if (filter.getSupplierCode() != null)
                predicates.add(cb.like(cb.lower(root.get("supplierCode")), "%" + filter.getSupplierCode().toLowerCase() + "%"));

            if (filter.getSupplierName() != null)
                predicates.add(cb.like(cb.lower(root.get("supplierName")), "%" + filter.getSupplierName().toLowerCase() + "%"));

            if (filter.getMailingBillingName() != null)
                predicates.add(cb.like(cb.lower(root.get("mailingBillingName")), "%" + filter.getMailingBillingName().toLowerCase() + "%"));

            if (filter.getSupplierNickname() != null)
                predicates.add(cb.like(cb.lower(root.get("supplierNickname")), "%" + filter.getSupplierNickname().toLowerCase() + "%"));

            if (filter.getMultipleAddress() != null)
                predicates.add(cb.equal(root.get("multipleAddress"), filter.getMultipleAddress()));

            if (filter.getGstOrUin() != null)
                predicates.add(cb.equal(root.get("gstOrUin"), filter.getGstOrUin()));

            if (filter.getGstStateCode() != null)
                predicates.add(cb.equal(root.get("gstStateCode"), filter.getGstStateCode()));

            if (filter.getPan() != null)
                predicates.add(cb.equal(root.get("pan"), filter.getPan()));

            if (filter.getIsTanAvailable() != null)
                predicates.add(cb.equal(root.get("isTanAvailable"), filter.getIsTanAvailable()));

            if (filter.getTanNumber() != null)
                predicates.add(cb.equal(root.get("tanNumber"), filter.getTanNumber()));

            if (filter.getIsUdyamAvailable() != null)
                predicates.add(cb.equal(root.get("isUdyamAvailable"), filter.getIsUdyamAvailable()));

            if (filter.getUdyamNumber() != null)
                predicates.add(cb.equal(root.get("udyamNumber"), filter.getUdyamNumber()));

            if (filter.getIsIecAvailable() != null)
                predicates.add(cb.equal(root.get("isIecAvailable"), filter.getIsIecAvailable()));

            if (filter.getIecCode() != null)
                predicates.add(cb.equal(root.get("iecCode"), filter.getIecCode()));

            if (filter.getInterestCalculation() != null)
                predicates.add(cb.equal(root.get("interestCalculation"), filter.getInterestCalculation()));

            if (filter.getRateOfInterest() != null)
                predicates.add(cb.equal(root.get("rateOfInterest"), filter.getRateOfInterest()));

            if (filter.getBrand() != null)
                predicates.add(cb.equal(root.get("brand"), filter.getBrand()));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
