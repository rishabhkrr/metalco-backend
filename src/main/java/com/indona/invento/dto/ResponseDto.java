package com.indona.invento.dto;

import lombok.Data;

@Data
public class ResponseDto<T> {

	private int status;
    private String message;
    private T data;
}
