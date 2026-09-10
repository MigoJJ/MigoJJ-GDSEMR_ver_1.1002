package com.emr.gds.features.medication.adapter.in.ui;

import com.emr.gds.util.StageSizing;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MedicationCategory extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/emr/gds/features/medication/launcher.fxml"));
        Parent root = loader.load();
        primaryStage.setTitle("EMR Medication Helper");
        primaryStage.setScene(new Scene(root));
        StageSizing.fitToScreen(primaryStage, 0.15, 0.7, 300, 600);
        StageSizing.moveToTopLeft(primaryStage);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
