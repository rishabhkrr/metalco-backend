package com.indona.invento.util;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.indona.invento.dto.ResponseDto;

public class ResponseUtil<T> {

	public static <T> ResponseEntity<ResponseDto<T>> success(T data) {
		ResponseDto<T> response = new ResponseDto<>();
        response.setStatus(HttpStatus.OK.value());
        response.setMessage("Success");
        response.setData(data);
        return ResponseEntity.ok(response);
    }
	
	public static <T> ResponseEntity<ResponseDto<T>> success(String message) {
		ResponseDto<T> response = new ResponseDto<>();
        response.setStatus(HttpStatus.OK.value());
        response.setMessage(message);
        return ResponseEntity.ok(response);
    }

    public static <T> ResponseEntity<ResponseDto<T>> error(HttpStatus status, String message) {
    	ResponseDto<T> response = new ResponseDto<>();
        response.setStatus(status.value());
        response.setMessage(message);
        response.setData(null);
        return ResponseEntity.status(status).body(response);
    }
    
    public static <T> ResponseEntity<ResponseDto<T>> error(HttpStatus status, String message, T data) {
    	ResponseDto<T> response = new ResponseDto<>();
        response.setStatus(status.value());
        response.setMessage(message);
        response.setData(data);
        return ResponseEntity.status(status).body(response);
    }
}
