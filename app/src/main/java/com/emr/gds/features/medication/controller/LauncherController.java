package com.emr.gds.features.medication.controller;

import com.emr.gds.features.medication.db.MedicationDatabaseManager;
import com.emr.gds.util.StageSizing;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

public class LauncherController {

    @FXML private VBox categoryContainer;

    private final MedicationDatabaseManager dbManager = new MedicationDatabaseManager();

    @FXML
    public void initialize() {
        dbManager.createTables();
        dbManager.ensureSeedData();

        var categories = dbManager.getOrderedCategories();
        String btnStyle = """
            -fx-background-color: linear-gradient(to bottom, #007bff, #0056b3);
            -fx-text-fill: white;
            -fx-border-color: #0056b3;
            -fx-border-width: 1;
            -fx-font-size: 14px;
            -fx-font-weight: bold;
            -fx-padding: 12 16;
            -fx-background-radius: 8;
            -fx-border-radius: 8;
            -fx-cursor: hand;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 3, 0, 0, 1);
            """;

        for (String cat : categories) {
            Button btn = new Button(cat);
            btn.setMaxWidth(Double.MAX_VALUE);
            btn.setStyle(btnStyle);
            btn.setOnAction(e -> openMainView(cat));
            categoryContainer.getChildren().add(btn);
        }
    }

    private void openMainView(String category) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/emr/gds/features/medication/main.fxml"));
            Parent root = loader.load();

            MainController controller = loader.getController();
            controller.setSelectedCategory(category);

            Stage stage = (Stage) categoryContainer.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("EMR Helper – " + category);
            StageSizing.fitToScreen(stage, 0.7, 0.8, 900, 650);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    private void onQuit() {
        Stage stage = (Stage) categoryContainer.getScene().getWindow();
        stage.close();
    }
}
