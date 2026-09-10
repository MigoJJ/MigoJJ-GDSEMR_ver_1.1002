package com.emr.gds.features.clinicalLab.adapter.in.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ClinicalLabLauncher extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/emr/gds/features/clinicalLab/main.fxml"));
        Parent root = loader.load();
        primaryStage.setTitle("Clinical Lab Items");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    public static void open() {
        try {
            new ClinicalLabLauncher().start(new Stage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
