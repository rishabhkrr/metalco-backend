package com.indona.invento.services;

import com.indona.invento.entities.SoUpdate;
import java.util.List;

public interface SoUpdateService {
    List<SoUpdate> getAllSoUpdates();

    SoUpdate verifyStatus(String soNumber);

    SoUpdate save(SoUpdate soUpdate);

    void deleteAllSoUpdates();
}
