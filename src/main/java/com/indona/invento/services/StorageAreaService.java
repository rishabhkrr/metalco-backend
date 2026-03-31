package com.indona.invento.services;

import com.indona.invento.entities.StorageAreaEntity;
import java.util.List;

public interface StorageAreaService {

    StorageAreaEntity addStorageArea(StorageAreaEntity storageArea);

    List<StorageAreaEntity> getAllStorageAreas();
}
