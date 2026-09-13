package com.emr.gds.features.kcd.adapter.in.ui;

import com.emr.gds.features.kcd.domain.KCDRecord;
import javafx.scene.Scene;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Drives the real, code-built (no FXML) kcd scene through TestFX rather than
 * a throwaway harness Application, replacing the manual verification step
 * described in docs/architecture.md § "Verifying UI changes" for this
 * feature. Read-only against the real bundled seed database
 * (src/main/resources/database/kcd_database.db, ~19,900 real KCD codes) -
 * no inserts/updates/deletes here.
 *
 * Uses {@code FxRobot.interact(Runnable)} rather than
 * {@code clickOn}/{@code write}/{@code type} for the same reason as
 * ClinicalLabControllerUiTest / AllergyControllerUiTest: OS-level synthetic
 * input via the xdg-desktop-portal is unreliable in this dev environment.
 */
@ExtendWith(ApplicationExtension.class)
class KCDDatabaseManagerJavaFXUiTest {

    private TableView<KCDRecord> table;
    private TextField searchField;

    @Start
    private void start(Stage stage) {
        KCDDatabaseManagerJavaFX manager = new KCDDatabaseManagerJavaFX();
        manager.start(stage);

        table = manager.getTable();
        searchField = manager.getSearchField();
    }

    @Test
    void loadsRealDataOnStartup() throws InterruptedException {
        // loadInitialData() runs the DB read on a background Task; give it a moment to finish.
        waitUntil(() -> !table.getItems().isEmpty(), 3000);
        assertFalse(table.getItems().isEmpty(), "Table should load real rows from the bundled KCD database");
        assertTrue(table.getItems().size() > 1000, "Expected the full ~19,900-row KCD dataset to load, got " + table.getItems().size());
    }

    @Test
    void searchFiltersToMatchingRowsOnly(FxRobot robot) throws InterruptedException {
        waitUntil(() -> !table.getItems().isEmpty(), 3000);
        int totalRows = table.getItems().size();

        robot.interact(() -> searchField.setText("콜레라"));
        waitUntil(() -> table.getItems().size() < totalRows, 2000);

        assertFalse(table.getItems().isEmpty(), "Expected at least one '콜레라' (cholera) match in the real dataset");
        assertTrue(table.getItems().size() < totalRows, "Search should narrow the result set");

        robot.interact(() -> searchField.setText(""));
        waitUntil(() -> table.getItems().size() == totalRows, 2000);

        assertEquals(totalRows, table.getItems().size(), "Clearing the search should restore the full list");
    }

    private void waitUntil(java.util.function.BooleanSupplier condition, long timeoutMillis) throws InterruptedException {
        long deadline = System.currentTimeMillis() + timeoutMillis;
        while (System.currentTimeMillis() < deadline) {
            if (condition.getAsBoolean()) {
                return;
            }
            Thread.sleep(50);
        }
    }
}
