package com.indona.invento.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.indona.invento.entities.RequisitionEntity;
import com.indona.invento.services.RequisitionsService;

@RestController
@RequestMapping("/requisition")
public class RequisitionController {

	@Autowired
    private RequisitionsService requisitionsService;

    @GetMapping
    public List<RequisitionEntity> getAllRequisitions() {
        return requisitionsService.getAllRequisitions();
    }

    @GetMapping("/{id}")
    public ResponseEntity<RequisitionEntity> getRequisitionById(@PathVariable Long id) {
        RequisitionEntity requisition = requisitionsService.getRequisitionById(id);
        return requisition != null ?
                new ResponseEntity<>(requisition, HttpStatus.OK) :
                new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PostMapping
    public ResponseEntity<RequisitionEntity> createRequisition(@RequestBody RequisitionEntity requisition) {
        RequisitionEntity createdRequisition = requisitionsService.createRequisition(requisition);
        return new ResponseEntity<>(createdRequisition, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RequisitionEntity> updateRequisition(@PathVariable Long id, @RequestBody RequisitionEntity requisition) {
        RequisitionEntity updatedRequisition = requisitionsService.updateRequisition(id, requisition);
        return updatedRequisition != null ?
                new ResponseEntity<>(updatedRequisition, HttpStatus.OK) :
                new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRequisition(@PathVariable Long id) {
        requisitionsService.deleteRequisition(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}