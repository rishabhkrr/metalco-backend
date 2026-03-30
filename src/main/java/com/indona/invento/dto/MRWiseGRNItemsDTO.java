package com.indona.invento.dto;

import lombok.Data;

import java.util.List;

@Data
public class MRWiseGRNItemsDTO {
    private String mrNumber;
    private List<GRNItemResponseDTO> items;
}