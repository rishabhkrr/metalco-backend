package com.indona.invento.dto;

import java.util.Date;
import java.util.List;

import lombok.AllArgsConstructor; 
import lombok.Data; 
import lombok.NoArgsConstructor; 
  
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChartsPayloadDto {
	private List<String> calendarOptions;
    private String line;
    private String section;
    private String area;
    private String department;
}