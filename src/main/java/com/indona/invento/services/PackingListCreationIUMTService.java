package com.indona.invento.services;

import com.indona.invento.entities.PackingListCreationIUMTEntity;

import java.util.List;
import java.util.Map;

public interface PackingListCreationIUMTService {
    List<PackingListCreationIUMTEntity> createPackingListIUMT(List<PackingListCreationIUMTEntity> entities);

    List<PackingListCreationIUMTEntity> getAllPackingListsIUMT();

    List<Map<String, String>> getPackingListNo();

    List<PackingListCreationIUMTEntity> getByPackingListNo(String packingListNo);

    void deleteAllPackingLists();
}
