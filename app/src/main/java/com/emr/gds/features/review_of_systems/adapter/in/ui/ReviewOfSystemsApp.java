package com.emr.gds.features.review_of_systems.adapter.in.ui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class ReviewOfSystemsApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Review of Systems Editor");
        primaryStage.setWidth(1000);
        primaryStage.setHeight(600);

        BorderPane root = new BorderPane();
        TextArea chartArea = new TextArea();
        chartArea.setWrapText(true);
        chartArea.setPromptText("Reports added here will appear in this area...");

        root.setCenter(chartArea);

        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();

        ReviewOfSystemsEditor editor = new ReviewOfSystemsEditor(chartArea);
        editor.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
