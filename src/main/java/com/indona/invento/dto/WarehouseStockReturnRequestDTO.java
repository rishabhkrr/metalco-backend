package com.indona.invento.dto;

import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WarehouseStockReturnRequestDTO {
    private String soNumber;
    private String lineNumber;
    private List<WarehouseStockReturnEntryDTO> returnEntries;
}
