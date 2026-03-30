package com.indona.invento.services;

import com.indona.invento.entities.ProductMargin;

import java.util.List;

public interface ProductMarginService {

    ProductMargin createMargin(ProductMargin margin);
    List<ProductMargin> getAllMargins();
    ProductMargin approveMargin(Long id);
    ProductMargin rejectMargin(Long id);
    ProductMargin editMargin(Long id, ProductMargin margin);
    ProductMargin deleteMargin(Long id);
}
