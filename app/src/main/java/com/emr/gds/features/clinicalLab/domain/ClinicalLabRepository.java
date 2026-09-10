package com.emr.gds.features.clinicalLab.domain;

import java.util.List;

public interface ClinicalLabRepository {
    List<ClinicalLabItem> getAllItems();
    List<ClinicalLabItem> searchItems(String query);
    void insertItem(ClinicalLabItem item);
    void updateItem(ClinicalLabItem item);
    void deleteItem(int id);
}
