package com.indona.invento.controllers;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.indona.invento.dao.TransferSkuDetailsRepository;
import com.indona.invento.dto.PicklistDto;
import com.indona.invento.dto.PicklistRequestDto;
import com.indona.invento.entities.StockTransferSkuEntity;
import com.indona.invento.services.PicklistService;

@RestController
@RequestMapping("/picklist")
public class PicklistController {

    @Autowired
    private PicklistService picklistService;
    
    @Autowired
    private TransferSkuDetailsRepository skuDetailsrepo;

    @PostMapping("/stock-out")
    public List<PicklistDto> generateSOPicklist(@RequestBody PicklistRequestDto req) {
    	List<StockTransferSkuEntity> entities = skuDetailsrepo.findAllByTransferNumber(req.getRefNo());
    	
        return picklistService.generateSOPicklist(req, entities);
    }
    
    @PostMapping("/stock-in")
    public List<PicklistDto> generateSILocation(@RequestBody PicklistRequestDto req) {
        return picklistService.generateSILocation(req);
    }

}