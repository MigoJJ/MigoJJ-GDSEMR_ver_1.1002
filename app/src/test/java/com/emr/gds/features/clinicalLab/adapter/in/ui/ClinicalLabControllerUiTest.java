package com.emr.gds.features.clinicalLab.adapter.in.ui;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
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
 * Drives the real clinicalLab FXML scene through TestFX rather than a
 * throwaway harness Application, replacing the manual verification step
 * described in docs/architecture.md § "Verifying UI changes" for this
 * feature. Read-only against the real seed database (app/db/ClinicalLabItemsSqlite3.db) —
 * no inserts/updates/deletes here, see JdbcClinicalLabRepositoryTest for
 * that against an isolated test database file.
 *
 * Deliberately uses {@code FxRobot.interact(Runnable)} (runs directly on the
 * FX Application Thread) rather than {@code clickOn}/{@code write}/{@code type}
 * (OS-level synthetic input via the xdg-desktop-portal RemoteDesktop
 * interface). The latter is unreliable in this dev environment — the portal
 * denies pointer/keyboard injection ("Session is not allowed to call
 * NotifyPointer methods"), which surfaced as an intermittent
 * NoSuchElementException in TestFX's WindowFinder when run alongside other
 * tests. This is the same "lack of xdotool/TestFX in this environment"
 * limitation docs/architecture.md already notes for the harness technique;
 * `interact` sidesteps it while still exercising the real FXML/controller
 * wiring and real control event handlers.
 */
@ExtendWith(ApplicationExtension.class)
class ClinicalLabControllerUiTest {

    private TableView<?> labTable;
    private TextField searchField;

    @Start
    private void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/emr/gds/features/clinicalLab/main.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();

        labTable = (TableView<?>) scene.lookup("#labTable");
        searchField = (TextField) scene.lookup("#searchField");
    }

    @Test
    void loadsRealDataOnStartup() {
        assertNotNull(labTable, "fx:id lookup should resolve, confirming the fx:controller wiring is correct");
        assertFalse(labTable.getItems().isEmpty(), "Table should load real rows from the seed database");
    }

    @Test
    void searchFiltersToMatchingRowsOnly(FxRobot robot) {
        int totalRows = labTable.getItems().size();

        robot.interact(() -> {
            searchField.setText("glucose");
            searchField.getOnAction().handle(new ActionEvent());
        });

        assertFalse(labTable.getItems().isEmpty(), "Expected at least one glucose-related test in the seed data");
        assertTrue(labTable.getItems().size() < totalRows, "Search should narrow the result set");

        robot.interact(() -> {
            searchField.setText("");
            searchField.getOnAction().handle(new ActionEvent());
        });

        assertEquals(totalRows, labTable.getItems().size(), "Clearing the search should restore the full list");
    }
}
