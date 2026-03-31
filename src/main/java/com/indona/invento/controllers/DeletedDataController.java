package com.indona.invento.controllers;

import java.util.List;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.indona.invento.dto.DeletedDataDto;
import com.indona.invento.entities.AdjustmentEntity;
import com.indona.invento.services.AdjustmentService;
import com.indona.invento.services.DeletedDataService;

@RestController
@RequestMapping("/deleted")
public class DeletedDataController {

	@Autowired
    private DeletedDataService deleteDataSrv;

    @GetMapping
    public List<DeletedDataDto> getAllStockins() {
        return deleteDataSrv.getAllData();
    }
    
}