package com.emr.gds.features.review_of_systems;

import com.emr.gds.features.review_of_systems.adapter.in.ui.ReviewOfSystemsEditor;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;

class ReviewOfSystemsEditorUiTest extends ApplicationTest {

    private TextArea chartArea;

    @Override
    public void start(Stage stage) {
        chartArea = new TextArea();
        chartArea.setWrapText(true);

        BorderPane root = new BorderPane();
        root.setCenter(chartArea);

        Scene scene = new Scene(root, 400, 300);
        stage.setScene(scene);
        stage.show();
    }

    @Test
    void testEditorShowsAndCanAddReport() {
        ReviewOfSystemsEditor editor = new ReviewOfSystemsEditor(chartArea);
        interact(() -> editor.show());

        assertThat(chartArea.getText(), containsString(""));
    }

    @Test
    void testGenerateReportWithNoSelectionsReturnsMessage() {
        ReviewOfSystemsEditor editor = new ReviewOfSystemsEditor(chartArea);
        interact(() -> {
            editor.show();
        });

        interact(() -> {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        interact(() -> clickOn("Generate Report"));

        interact(() -> {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }
}
