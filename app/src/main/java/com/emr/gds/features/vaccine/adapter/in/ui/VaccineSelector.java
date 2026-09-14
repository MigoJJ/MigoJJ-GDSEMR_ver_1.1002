package com.emr.gds.features.vaccine.adapter.in.ui;

import com.emr.gds.features.vaccine.application.VaccineConstants;
import javafx.collections.FXCollections;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.util.function.Consumer;

public class VaccineSelector extends VBox {

    private final ComboBox<String> comboBox;
    private Consumer<String> onSelectedCallback;

    public VaccineSelector() {
        comboBox = new ComboBox<>(FXCollections.observableArrayList(VaccineConstants.UI_ELEMENTS));
        comboBox.setPromptText("Select a vaccine...");

        configureCellFactory();
        configureButtonCell();

        comboBox.setOnAction(e -> {
            String selectedItem = comboBox.getValue();
            if (selectedItem != null && !isHeaderOrAction(selectedItem) && onSelectedCallback != null) {
                onSelectedCallback.accept(selectedItem);
            }
        });

        getChildren().addAll(new Label("Vaccine Selection:"), comboBox);
        setSpacing(10);
    }

    private void configureCellFactory() {
        comboBox.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setDisable(false);
                    setStyle("");
                } else if (isHeaderOrAction(item)) {
                    setText(stripHeader(item));
                    setStyle("-fx-font-weight: bold; -fx-background-color: #f0f0f0;");
                    setDisable(true);
                } else {
                    setText(item);
                    setDisable(false);
                    setStyle("");
                }
            }
        });
    }

    private void configureButtonCell() {
        comboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || isHeaderOrAction(item)) {
                    setText(null);
                } else {
                    setText(item);
                }
            }
        });
    }

    public VaccineSelector onSelected(Consumer<String> handler) {
        this.onSelectedCallback = handler;
        return this;
    }

    public VaccineSelector bindTo(TextField field) {
        return onSelected(field::setText);
    }

    public VaccineSelector bindAppend(TextField field) {
        return onSelected(selection -> {
            String previousText = field.getText();
            if (previousText == null || previousText.isBlank()) {
                field.setText(selection);
            } else {
                field.setText(previousText + ", " + selection);
            }
        });
    }

    public String getSelectedVaccine() {
        return comboBox.getValue();
    }

    private static boolean isHeaderOrAction(String s) {
        return s != null && (s.startsWith("###") || "Side Effect".equals(s) || "Quit".equals(s));
    }

    private static String stripHeader(String s) {
        return s.startsWith("###") ? s.substring(3).trim() : s;
    }
}
