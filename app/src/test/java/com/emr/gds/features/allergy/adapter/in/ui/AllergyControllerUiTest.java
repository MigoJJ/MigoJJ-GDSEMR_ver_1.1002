package com.emr.gds.features.allergy.adapter.in.ui;

import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Drives the real allergy scene (code-built, no FXML) through TestFX rather
 * than a throwaway harness Application, replacing the manual verification
 * step described in docs/architecture.md § "Verifying UI changes" for this
 * feature. Exercises the real AllergyController wired to a real AllergyView
 * and the real AllergyDataService's static reference data - no persistence
 * involved in this feature.
 *
 * Uses {@code FxRobot.interact(Runnable)} rather than
 * {@code clickOn}/{@code write}/{@code type} for the same reason as
 * ClinicalLabControllerUiTest: OS-level synthetic input via the
 * xdg-desktop-portal is unreliable in this dev environment.
 */
@ExtendWith(ApplicationExtension.class)
class AllergyControllerUiTest {

    private AllergyController controller;
    private TableView<?> symptomTable;
    private TableView<?> causeTable;
    private TextField searchField;
    private TextArea outputArea;

    @Start
    private void start(Stage stage) {
        controller = new AllergyController();
        Scene scene = new Scene(controller.getView());
        stage.setScene(scene);
        stage.show();

        symptomTable = controller.getView().getSymptomTable();
        causeTable = controller.getView().getCauseTable();
        searchField = controller.getView().getSearchField();
        outputArea = controller.getView().getOutputArea();
    }

    @Test
    void loadsRealReferenceDataAndDefaultsToNoKnownAllergies() {
        assertFalse(symptomTable.getItems().isEmpty(), "Symptom table should load real items from AllergyDataService");
        assertFalse(causeTable.getItems().isEmpty(), "Cause table should load real allergen items from AllergyDataService");
        assertTrue(outputArea.getText().contains("No known drug, food, or environmental allergies"),
                "Constructor calls resetToDefault(), so the note should start in the default 'none reported' state");
    }

    @Test
    void searchFiltersSymptomTableToMatchingRowsOnly(FxRobot robot) {
        int totalRows = symptomTable.getItems().size();

        robot.interact(() -> searchField.setText("rash"));

        assertFalse(symptomTable.getItems().isEmpty(), "Expected at least one 'Rash' symptom in the reference data");
        assertTrue(symptomTable.getItems().size() < totalRows, "Search should narrow the result set");

        robot.interact(() -> searchField.setText(""));

        assertEquals(totalRows, symptomTable.getItems().size(), "Clearing the search should restore the full list");
    }

    @Test
    void denyAllTemplateMenuItemRewritesTheNote(FxRobot robot) {
        robot.interact(() -> controller.getView().getAllDeniedTemplateMenuItem().getOnAction().handle(new ActionEvent()));

        assertTrue(outputArea.getText().contains("denies ALL allergic symptoms"),
                "Deny-all template should produce the explicit denial note");

        robot.interact(() -> controller.getView().getDefaultTemplateMenuItem().getOnAction().handle(new ActionEvent()));

        assertTrue(outputArea.getText().contains("No known drug, food, or environmental allergies"),
                "Switching back to the default template should restore the default note");
    }
}
