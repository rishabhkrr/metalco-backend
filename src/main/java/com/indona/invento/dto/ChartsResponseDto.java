package com.indona.invento.dto;

import java.util.List;

import lombok.AllArgsConstructor; 
import lombok.Data; 
import lombok.NoArgsConstructor; 
  
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChartsResponseDto {
	private List<?> labels;
    private List<?> data;
}