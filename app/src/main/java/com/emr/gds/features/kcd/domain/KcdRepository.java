package com.emr.gds.features.kcd.domain;

import java.sql.SQLException;
import java.util.List;

public interface KcdRepository {
    List<KCDRecord> getAllRecords() throws SQLException;
    void addRecord(KCDRecord record) throws SQLException;
    void updateRecord(String originalDiseaseCode, KCDRecord record) throws SQLException;
    void deleteRecord(String diseaseCode) throws SQLException;
}
