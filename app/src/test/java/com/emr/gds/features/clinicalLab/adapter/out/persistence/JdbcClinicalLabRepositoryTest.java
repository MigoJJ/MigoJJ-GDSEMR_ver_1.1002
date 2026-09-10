package com.emr.gds.features.clinicalLab.adapter.out.persistence;

import com.emr.gds.features.clinicalLab.domain.ClinicalLabItem;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JdbcClinicalLabRepositoryTest {

    private static final String TEST_DB_FILE = "test_clinical_lab_items.db";

    @AfterEach
    void tearDown() {
        // Best effort cleanup, mirroring MedicationDatabaseManagerTest's convention
        // of exercising real SQLite persistence against an isolated file rather
        // than the tracked production seed database.
        for (String dir : new String[] {"app/db/", "db/", ""}) {
            File file = new File(dir + TEST_DB_FILE);
            if (file.exists()) file.delete();
        }
    }

    @Test
    void insertUpdateSearchDeleteRoundTrip() {
        JdbcClinicalLabRepository repo = new JdbcClinicalLabRepository(TEST_DB_FILE);

        ClinicalLabItem item = new ClinicalLabItem(0, "Chemistry", "Test Glucose", "mg/dL",
                70.0, 100.0, 70.0, 100.0, "70-100", "70-100", "GLU", "fasting");
        repo.insertItem(item);
        assertTrue(item.getId() > 0, "Insert should assign a generated id");

        List<ClinicalLabItem> found = repo.searchItems("glucose");
        assertEquals(1, found.size(), "Search should find the inserted item by substring");
        assertEquals("Test Glucose", found.get(0).getTestName());

        item.setComments("updated");
        repo.updateItem(item);
        List<ClinicalLabItem> all = repo.getAllItems();
        assertTrue(all.stream().anyMatch(i -> i.getId() == item.getId() && "updated".equals(i.getComments())),
                "Update should be visible via getAllItems");

        repo.deleteItem(item.getId());
        assertTrue(repo.getAllItems().stream().noneMatch(i -> i.getId() == item.getId()),
                "Deleted item should no longer be present");
    }
}
