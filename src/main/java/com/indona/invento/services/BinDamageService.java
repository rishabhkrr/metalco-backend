package com.indona.invento.services;

import java.util.List;
import com.indona.invento.entities.BinDamageEntity;

public interface BinDamageService {
    List<BinDamageEntity> getAllCombinations();
    BinDamageEntity getCombinationById(Long id);
    BinDamageEntity createCombination(BinDamageEntity combination);
    BinDamageEntity updateCombination(Long id, BinDamageEntity combination);
    void deleteCombination(Long id);
}
