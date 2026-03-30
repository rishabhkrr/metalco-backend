package com.indona.invento.controllers;

import com.indona.invento.dto.BlockedQuantityRequest;
import com.indona.invento.entities.BlockedQuantityEntity;
import com.indona.invento.services.BlockedQuantityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/blocked-quantity")
@RequiredArgsConstructor
public class BlockedQuantityController {

    private final BlockedQuantityService blockedQuantityService;

    @PostMapping
    public ResponseEntity<?> createBlockedQuantity(@RequestBody BlockedQuantityRequest request) {
        BlockedQuantityEntity saved = blockedQuantityService.createBlockedQuantity(request);
        return ResponseEntity.ok(saved);
    }
}
