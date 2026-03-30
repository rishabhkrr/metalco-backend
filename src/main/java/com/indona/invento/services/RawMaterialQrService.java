package com.indona.invento.services;

import com.indona.invento.dto.RawMaterialQrDTO;
import com.indona.invento.entities.RawMaterialQrEntity;

public interface RawMaterialQrService {

    RawMaterialQrEntity createRawMaterialQr(RawMaterialQrDTO dto);

    RawMaterialQrEntity getByRawMaterialQrId(String rawMaterialQrId);
}
