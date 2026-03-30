package com.indona.invento.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StockTransferWHReturnDto {
    private String mrNumber;
    private String lineNumber;

    private List<ReturnEntryDto> returnEntries;
}
