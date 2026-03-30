package com.indona.invento.services;

import java.util.Map;

public interface CleanupService {
    /**
     * Clear all data from all modules in the system
     * This is a destructive operation - deletes all records
     */
    Map<String, Object> clearAllData();
}

