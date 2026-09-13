package com.emr.gds.features.glp1;

import com.emr.gds.features.glp1.adapter.in.ui.Glp1SemaglutidePane;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class Glp1SemaglutidePaneUiTest extends ApplicationTest {

    private Glp1SemaglutidePane pane;

    @Override
    public void start(Stage stage) {
        pane = new Glp1SemaglutidePane();

        BorderPane root = new BorderPane();
        root.setCenter(pane);

        Scene scene = new Scene(root, 800, 900);
        stage.setScene(scene);
        stage.show();
    }

    @Test
    void testPaneInitializesSuccessfully() {
        assertNotNull(pane);
    }

    @Test
    void testBasicProblemListGeneration() {
        interact(() -> pane.clearAll());

        String result = pane.toProblemListString();

        assertThat(result, containsString("MEDICATION - GLP-1RA (SEMAGLUTIDE)"));
        assertThat(result, containsString("NOT currently on therapy"));
        assertThat(result, containsString("None documented"));
    }

    @Test
    void testClearAllResetsFields() {
        interact(() -> pane.clearAll());

        String clearedResult = pane.toProblemListString();

        assertThat(clearedResult, containsString("NOT currently on therapy"));
        assertThat(clearedResult, containsString("None documented"));
    }

    @Test
    void testAssessmentSummaryHasDateAfterClear() {
        interact(() -> pane.clearAll());

        String summary = pane.toAssessmentSummary();

        assertTrue(summary.contains("Next visit"));
    }

    @Test
    void testHasCriticalContraindicationsInitiallyFalse() {
        interact(() -> pane.clearAll());

        boolean hasCritical = pane.hasCriticalContraindications();

        assertFalse(hasCritical);
    }

    @Test
    void testProblemListFormatStructure() {
        interact(() -> pane.clearAll());

        String result = pane.toProblemListString();

        assertTrue(result.contains("Status / Indication:"));
        assertTrue(result.contains("Dose:"));
        assertTrue(result.contains("Follow-up:"));
        assertTrue(result.contains("Contraindications / Cautions:"));
    }
}
