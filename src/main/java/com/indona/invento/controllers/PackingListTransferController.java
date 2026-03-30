package com.indona.invento.controllers;

import com.indona.invento.dto.PackingListTransferDTO;
import com.indona.invento.entities.PackingListTransferEntity;
import com.indona.invento.services.PackingListTransferService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/packing-list")
public class PackingListTransferController {

    @Autowired
    private PackingListTransferService service;

    @PostMapping("/save")
    public ResponseEntity<List<PackingListTransferEntity>> save(@RequestBody List<PackingListTransferDTO> dtos) {
        System.out.println("\n✅ ========== /api/packing-list/save ENDPOINT CALLED ==========");
        System.out.println("   Received " + dtos.size() + " packing list DTOs");
        dtos.forEach(dto -> {
            System.out.println("   📦 SO: " + dto.getSoNumber() + " | Line: " + dto.getLineNumber() +
                    " | Unit: " + dto.getUnit() + " | Customer: " + dto.getCustomerName());
        });
        System.out.println("==============================================================\n");

        List<PackingListTransferEntity> saved = service.savePackingList(dtos);

        System.out.println("\n✅ ========== SAVE OPERATION COMPLETE ==========");
        System.out.println("   Total saved: " + saved.size());
        System.out.println("==============================================\n");

        return ResponseEntity.ok(saved);
    }

    @GetMapping("/all")
    public ResponseEntity<List<PackingListTransferEntity>> getAll() {
        return ResponseEntity.ok(service.getAllPackingLists());
    }
}
