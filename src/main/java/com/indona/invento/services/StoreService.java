package com.indona.invento.services;


import com.indona.invento.entities.StoreEntity;
import java.util.List;

public interface StoreService {

    StoreEntity addStore(StoreEntity store);

    List<StoreEntity> getAllStores();
}
