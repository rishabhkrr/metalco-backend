package com.indona.invento.controllers;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
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

import com.indona.invento.dto.FilterDto;
import com.indona.invento.dto.InvoiceSummaryDto;
import com.indona.invento.dto.TransferSkuDto;
import com.indona.invento.entities.ManualCashInHandEntity;
import com.indona.invento.entities.StockinEntity;
import com.indona.invento.entities.StockoutEntity;
import com.indona.invento.services.ManualCashInHandService;
import com.indona.invento.services.StockoutService;
import com.indona.invento.services.impl.ManualCashInHandServiceImpl;

@RestController
@RequestMapping("/stock-out")
public class StockOutController {

	@Autowired
	private StockoutService stockoutService;

	@Autowired
	private ManualCashInHandService manualCashInHandService;

	@PostMapping("/manual-cash-in-hand")
	public ResponseEntity<ManualCashInHandEntity> saveManualCashInHand(
			@RequestBody ManualCashInHandEntity manualCashInHand) {
		// Convert the creationDate to Europe/London timezone
//		ZonedDateTime londonTime = manualCashInHand.getCreationDate().toInstant().atZone(ZoneId.of("Europe/London"));
//		manualCashInHand.setCreationDate(Date.from(londonTime.toInstant()));
		ManualCashInHandEntity savedEntry = manualCashInHandService.saveManualCashInHand(manualCashInHand);
		return new ResponseEntity<>(savedEntry, HttpStatus.CREATED);
	}

	@GetMapping("/manual-cash-in-hand")
	public ResponseEntity<List<ManualCashInHandEntity>> getManualCashInHand(
			@RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date startDate,
			@RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date endDate,
			@RequestParam("storeId") Long storeId) {
		// Convert startDate and endDate to Europe/London timezone
		ZonedDateTime startLondonTime = startDate.toInstant().atZone(ZoneId.of("Europe/London"));
		ZonedDateTime endLondonTime = endDate.toInstant().atZone(ZoneId.of("Europe/London"));
		List<ManualCashInHandEntity> manualCashInHandList = manualCashInHandService.getManualCashInHandByDateRange(
				Date.from(startLondonTime.toInstant()), Date.from(endLondonTime.toInstant()), storeId);
		return manualCashInHandList != null && !manualCashInHandList.isEmpty()
				? new ResponseEntity<>(manualCashInHandList, HttpStatus.OK)
				: new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}

	@GetMapping("/returns")
	public List<StockoutEntity> getAllStockoutsReturns(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "50") int size, @RequestParam(defaultValue = "id,desc") String[] sort,
			@RequestParam(required = false) String search) {
		Sort.Direction direction = Sort.Direction.fromString(sort[1]);
		Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sort[0]));
		return stockoutService.getAllStockoutReturns(search, pageable);
	}

	@GetMapping("/invoices/{id}")
	public List<StockoutEntity> getAllStockoutsInvoices(@PathVariable Long id) {
		return stockoutService.getAllStockoutInvoices(id);
	}

	@GetMapping("/returns/store/{id}")
	public List<StockoutEntity> getAllStockoutsReturnsStore(@PathVariable Long id,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "50") int size,
			@RequestParam(defaultValue = "id,desc") String[] sort, @RequestParam(required = false) String search) {
		Sort.Direction direction = Sort.Direction.fromString(sort[1]);
		Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sort[0]));
		return stockoutService.getAllStockoutReturnsStore(id, search, pageable);
	}

	@GetMapping
	public List<StockoutEntity> getAllStockouts(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "50") int size, @RequestParam(defaultValue = "id,desc") String[] sort,
			@RequestParam(required = false) String search, @RequestParam(required = false) String type) {
		Sort.Direction direction = Sort.Direction.fromString(sort[1]);
		Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sort[0]));
		return stockoutService.getAllStockout(search, type, pageable);
	}

	@GetMapping("/sales/report")
	public List<StockoutEntity> getAllStockoutsReport(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "50") int size, @RequestParam(defaultValue = "id,desc") String[] sort,
			@RequestParam(required = false) String search, @RequestParam(required = false) String type) {
		Sort.Direction direction = Sort.Direction.fromString(sort[1]);
		Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sort[0]));
		return stockoutService.getAllStockout(search, type, pageable);
	}

	@GetMapping("/store/{id}")
	public List<StockoutEntity> getStockoutByStore(@PathVariable Long id, @RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "50") int size, @RequestParam(defaultValue = "id,desc") String[] sort,
			@RequestParam(required = false) String search, @RequestParam(required = false) String type) {
		Sort.Direction direction = Sort.Direction.fromString(sort[1]);
		Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sort[0]));
		return stockoutService.getAllStockinByStore(id, search, type, pageable);
	}

	@GetMapping("warehouse/{warehouseId}/store/{id}")
	public List<StockoutEntity> getStockoutByWarehouseStore(@PathVariable Long id, @PathVariable Long warehouseId,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "50") int size,
			@RequestParam(defaultValue = "id,desc") String[] sort, @RequestParam(required = false) String search,
			@RequestParam(required = false) String type) {
		Sort.Direction direction = Sort.Direction.fromString(sort[1]);
		Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sort[0]));
		return stockoutService.getAllStockinByStoreAndWarehouse(warehouseId, id, search, type, pageable);
	}

	@GetMapping("/{id}")
	public ResponseEntity<TransferSkuDto> getStockoutById(@PathVariable Long id) {
		TransferSkuDto line = stockoutService.getStockoutById(id);
		return line != null ? new ResponseEntity<>(line, HttpStatus.OK) : new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}

	@PostMapping
	public ResponseEntity<TransferSkuDto> createStockout(@RequestBody TransferSkuDto line) {
		TransferSkuDto createdStockout = stockoutService.createStockout(line);
		return new ResponseEntity<>(createdStockout, HttpStatus.CREATED);
	}

	@PostMapping("/summary")
	public ResponseEntity<InvoiceSummaryDto> getInvoiceSummary(@RequestBody FilterDto req) {
		InvoiceSummaryDto res = stockoutService.getInvoiceSummary(req);
		return new ResponseEntity<>(res, HttpStatus.CREATED);
	}

	@PostMapping("/summary/day")
	public ResponseEntity<List<InvoiceSummaryDto>> getInvoiceSummaryDay(@RequestBody FilterDto req) {
		List<InvoiceSummaryDto> res = stockoutService.getInvoiceSummaryDayWise(req);
		return new ResponseEntity<>(res, HttpStatus.CREATED);
	}

	@PostMapping("/summary/all")
	public ResponseEntity<InvoiceSummaryDto> getInvoiceSummaryAll(@RequestBody FilterDto req) {
		InvoiceSummaryDto res = stockoutService.getOverallInvoiceSummary(req);
		return new ResponseEntity<>(res, HttpStatus.CREATED);
	}

	@PutMapping("/{id}")
	public ResponseEntity<TransferSkuDto> updateStockout(@PathVariable Long id, @RequestBody TransferSkuDto line) {
		TransferSkuDto updatedStockout = stockoutService.updateStockout(id, line);
		return updatedStockout != null ? new ResponseEntity<>(updatedStockout, HttpStatus.OK)
				: new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}

	@DeleteMapping("/{id}/{remark}")
	public ResponseEntity<Void> deleteStockout(@PathVariable Long id, @PathVariable String remark) {
		stockoutService.deleteStockout(id, remark);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}
}