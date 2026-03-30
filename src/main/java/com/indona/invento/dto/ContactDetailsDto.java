package com.indona.invento.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ContactDetailsDto {
    private String name;
    private String designation;
    private String phoneNumber;
    private String emailId;
    private boolean Primary;
}
