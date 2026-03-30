package com.indona.invento.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UnitContactDetailsDto {

    private Boolean Primary;
    private String name;
    private String designation;
    private String phoneNumber;
    private String emailId;
}
