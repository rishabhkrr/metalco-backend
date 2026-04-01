package com.indona.invento.services;

import com.indona.invento.dto.GateEntryEditDTO;
import com.indona.invento.dto.GateEntryPackingAndDispatchRequestDTO;
import com.indona.invento.dto.GateEntryUpdateDTO;
import com.indona.invento.entities.GateEntryPackingAndDispatch;

import java.util.List;

public interface GateEntryPackingAndDispatchService {

	GateEntryPackingAndDispatch saveGateEntry(GateEntryPackingAndDispatchRequestDTO dto);

	List<GateEntryPackingAndDispatch> getAllGateEntries();

	List<String> getVehicleNumbersWithInStatus();

	List<String> getVehicleNumbersFiltered(String purpose, String mode);

	GateEntryPackingAndDispatch updateGateEntryByRefNo(String refNo, GateEntryUpdateDTO dto);

	GateEntryPackingAndDispatch markVehicleOut(String refNo);

	GateEntryPackingAndDispatch getGateEntryByRefNo(String refNo);

	GateEntryPackingAndDispatch editGateEntryByRefNo(String refNo, GateEntryEditDTO dto);

	void deleteAllGateEntries();

}
