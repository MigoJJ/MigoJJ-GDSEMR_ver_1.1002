package com.emr.gds.input;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * A compact JavaFX utility window for frequent EMR data entries, including BMI, HbA1c, and vital signs.
 * This window is designed as an undecorated stage that positions itself on the top-right of the screen.
 */
public class IAIFreqFrame extends Stage {

    // BMI components
    private final TextField[] bmiInputs = new TextField[3];
    private final ToggleButton heightUnitToggle = new ToggleButton("cm");
    private final ToggleButton weightUnitToggle = new ToggleButton("kg");

    // Unit conversion constants: only the metric->imperial direction is a
    // magic number; the reverse is always derived by division so the two
    // never drift out of round-trip sync with each other.
    private static final double CM_PER_INCH = 2.54;        // exact, international yard/pound agreement (1959)
    private static final double KG_PER_LB = 0.45359237;    // exact, same agreement

    // HbA1c components
    private final TextArea hba1cOutputArea = new TextArea();
    private final TextField[] hba1cInputs = new TextField[3];

    // Vital signs components
    private TextField vsInputField;
    private TextArea vsOutputArea;
    private TextArea vsDescriptionArea;
    private Set<String> vsValidInputs;
    private Integer sbp, dbp, pulseRate, respirationRate;
    private Double bodyTemperature;

    public IAIFreqFrame() {
        initStyle(StageStyle.UNDECORATED);
        setTitle("Frequent Data Input");

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(8));

        VBox content = new VBox(8);
        content.getChildren().addAll(
                createBmiPane(),
                createHba1cPane(),
                createVitalsPane(),
                createBottomButtons()
        );
        root.setCenter(content);

        setScene(new Scene(root, 360, 810));
        setOnShown(e -> positionInTopRight());

        initializeVitalsValidInputs();
        checkBridgeReady();
        show();
    }

    private TitledPane createBmiPane() {
        GridPane grid = createFormGrid();

        bmiInputs[0] = new TextField();
        bmiInputs[1] = new TextField();
        bmiInputs[2] = new TextField();
        bmiInputs[2].setPromptText("Waist (cm or inch)");

        configureUnitToggle(heightUnitToggle, bmiInputs[0], "Height", "cm", "in");
        configureUnitToggle(weightUnitToggle, bmiInputs[1], "Weight", "kg", "lb");

        for (int i = 0; i < bmiInputs.length; i++) {
            final int nextIndex = i + 1;
            bmiInputs[i].setOnAction(e -> {
                if (nextIndex < bmiInputs.length) bmiInputs[nextIndex].requestFocus();
                else onSaveBMI();
            });
        }

        grid.add(new Label("Height:"), 0, 0);
        grid.add(bmiInputs[0], 1, 0);
        grid.add(heightUnitToggle, 2, 0);

        grid.add(new Label("Weight:"), 0, 1);
        grid.add(bmiInputs[1], 1, 1);
        grid.add(weightUnitToggle, 2, 1);

        grid.add(new Label("Waist:"), 0, 2);
        grid.add(bmiInputs[2], 1, 2);

        Button saveButton = new Button("Save BMI");
        saveButton.setOnAction(e -> onSaveBMI());
        grid.add(saveButton, 0, 3, 3, 1);
        return new TitledPane("BMI Calculator", grid);
    }

    /** Wires a toggle so it flips between {@code metricUnit}/{@code imperialUnit} and updates the field's prompt text to match. */
    private void configureUnitToggle(ToggleButton toggle, TextField field, String label, String metricUnit, String imperialUnit) {
        toggle.setSelected(false);
        toggle.setText(metricUnit);
        field.setPromptText(label + " (" + metricUnit + ")");
        toggle.setOnAction(e -> {
            String unit = toggle.isSelected() ? imperialUnit : metricUnit;
            toggle.setText(unit);
            field.setPromptText(label + " (" + unit + ")");
        });
    }

    private static double inchToCm(double inch) { return inch * CM_PER_INCH; }

    private static double lbToKg(double lb) { return lb * KG_PER_LB; }

    private void onSaveBMI() {
        try {
            double heightRaw = Double.parseDouble(bmiInputs[0].getText());
            double weightRaw = Double.parseDouble(bmiInputs[1].getText());

            boolean heightIsInch = heightUnitToggle.isSelected();
            boolean weightIsLb = weightUnitToggle.isSelected();

            double heightCm = heightIsInch ? inchToCm(heightRaw) : heightRaw;
            double weightKg = weightIsLb ? lbToKg(weightRaw) : weightRaw;

            double bmi = weightKg / Math.pow(heightCm / 100.0, 2.0);
            String category = (bmi < 18.5) ? "Underweight" : (bmi < 25.0) ? "Healthy" : (bmi < 30.0) ? "Overweight" : "Obesity";
            String waist = processWaist(bmiInputs[2].getText());

            String heightDisplay = heightIsInch
                    ? String.format("%.1f in (%.1f cm)", heightRaw, heightCm)
                    : String.format("%.1f cm", heightCm);
            String weightDisplay = weightIsLb
                    ? String.format("%.1f lb (%.1f kg)", weightRaw, weightKg)
                    : String.format("%.1f kg", weightKg);

            String report = String.format("\n< BMI >\n%s : BMI: [ %.2f ] kg/m^2\nHeight : %s   Weight : %s%s",
                    category, bmi, heightDisplay, weightDisplay, waist.isEmpty() ? "" : "   Waist: " + waist + " cm");

            IAIMain.getTextAreaManager().appendTextToSection(IAITextAreaManager.AREA_O, report + "\n");
            for (TextField field : bmiInputs) field.clear();
            bmiInputs[0].requestFocus();
        } catch (NumberFormatException ex) {
            showError("Please enter valid numbers for Height and Weight.");
        }
    }

    private String processWaist(String waistRaw) {
        if (waistRaw == null || waistRaw.isBlank()) return "";
        String w = waistRaw.trim().toLowerCase();
        if (w.contains("i")) {
            double inches = Double.parseDouble(w.replaceAll("[^\\d.]", ""));
            return String.format("%.1f", inchToCm(inches));
        }
        return w.replaceAll("[^\\d.]", "");
    }

    private TitledPane createHba1cPane() {
        VBox box = new VBox(6, new Label("Output:"), hba1cOutputArea);
        hba1cOutputArea.setEditable(false);
        hba1cOutputArea.setPrefRowCount(4);

        GridPane inputs = createFormGrid();
        String[] hba1cLabels = {"FBS / PP2 time", "Glucose mg/dL", "HbA1c %"};
        for (int i = 0; i < hba1cLabels.length; i++) {
            hba1cInputs[i] = new TextField();
            hba1cInputs[i].setPromptText(hba1cLabels[i]);
            final int index = i;
            hba1cInputs[i].setOnAction(e -> onHba1cInput(index));
            inputs.add(new Label(hba1cLabels[i]), 0, i);
            inputs.add(hba1cInputs[i], 1, i);
        }

        HBox buttons = new HBox(8, createButton("Clear", e -> clearHba1c()), createButton("Save", e -> saveHba1cToEMR()));
        buttons.setAlignment(Pos.CENTER_RIGHT);
        box.getChildren().addAll(new Separator(), inputs, buttons);
        return new TitledPane("HbA1c EMR", box);
    }

    private void onHba1cInput(int index) {
        String value = hba1cInputs[index].getText().trim();
        if (value.isEmpty()) return;

        if (index == 0) {
            hba1cOutputArea.appendText("\n   " + (value.equals("0") ? "FBS" : "PP" + value));
        } else if (index == 1) {
            hba1cOutputArea.appendText("   [ " + value + " ] mg/dL");
        } else if (index == 2) {
            try {
                double hba1c = Double.parseDouble(value);
                hba1cOutputArea.appendText("   HbA1c [ " + value + " ] %\n");
                appendHba1cCalculations(hba1c);
                saveHba1cToEMR();
                clearHba1c();
            } catch (NumberFormatException ex) {
                showError("Invalid HbA1c value.");
            }
        }

        hba1cInputs[index].clear();
        int nextIndex = (index + 1) % hba1cInputs.length;
        hba1cInputs[nextIndex].requestFocus();
    }

    private void appendHba1cCalculations(double hba1c) {
        double ifcc = (hba1c - 2.15) * 10.929;
        double eagMgDl = (28.7 * hba1c) - 46.7;
        hba1cOutputArea.appendText(String.format("\n\tIFCC HbA1c: [ %.0f ] mmol/mol\n\teAG: [ %.0f ] mg/dL\n", ifcc, eagMgDl));

        String status = (hba1c > 9.0) ? "Very poor" : (hba1c > 8.5) ? "Poor" : (hba1c > 7.5) ? "Fair" : (hba1c > 6.5) ? "Good" : "Excellent";
        IAIMain.getTextAreaManager().insertLineIntoArea(IAITextAreaManager.AREA_A, "\n...now [ " + status + " ] controlled glucose status", true);
    }

    private void clearHba1c() {
        hba1cOutputArea.clear();
        for (TextField field : hba1cInputs) field.clear();
        hba1cInputs[0].requestFocus();
    }

    private void saveHba1cToEMR() {
        String text = hba1cOutputArea.getText();
        if (text != null && !text.isBlank()) {
            IAIMain.getTextAreaManager().insertBlockIntoArea(IAITextAreaManager.AREA_O, text, true);
        }
    }

    private TitledPane createVitalsPane() {
        vsInputField = new TextField("Enter code (h/o/g/l/r/i/t36.5) or numbers (SBP→DBP→PR→BT→RR)");
        vsInputField.setOnAction(e -> {
            handleVitalsInput(vsInputField.getText().trim().toLowerCase());
            vsInputField.clear();
        });

        vsDescriptionArea = new TextArea(" at GDS : Regular pulse, Right Seated Position");
        vsDescriptionArea.setPrefRowCount(2);
        vsOutputArea = new TextArea();
        vsOutputArea.setPrefRowCount(5);

        HBox buttons = new HBox(8, createButton("Clear", e -> resetVitalsFields()), createButton("Save", e -> saveVitalsToEMR()));
        buttons.setAlignment(Pos.CENTER_RIGHT);

        VBox box = new VBox(6, new Label("Input:"), vsInputField, new Label("Description:"), vsDescriptionArea, new Label("Output:"), vsOutputArea, buttons);
        return new TitledPane("Vital Sign Tracker", box);
    }

    private void initializeVitalsValidInputs() {
        vsValidInputs = new HashSet<>(List.of("h", "o", "g", "l", "r", "i"));
    }

    private void handleVitalsInput(String input) {
        if (input.isEmpty()) return;

        if (vsValidInputs.contains(input)) {
            updateVitalsDescription(input);
        } else if (input.startsWith("t")) {
            handleVitalsTemperatureInput(input);
        } else {
            try {
                processVitalsNumeric(Double.parseDouble(input));
            } catch (NumberFormatException ex) {
                vsOutputArea.setText("Invalid input. Enter a code or a number.");
            }
        }
    }

    private void updateVitalsDescription(String code) {
        String current = vsDescriptionArea.getText();
        switch (code) {
            case "h" -> vsDescriptionArea.setText("   at home by self");
            case "o" -> vsDescriptionArea.setText("   at Other clinic");
            case "g" -> vsDescriptionArea.setText(" at GDS : Regular pulse, Right Seated Position");
            case "l" -> vsDescriptionArea.setText(current.replace("Right", "Left"));
            case "r" -> vsDescriptionArea.setText(current.replace("Left", "Right"));
            case "i" -> vsDescriptionArea.setText(current.replace("Regular", "Irregular"));
        }
    }

    private void handleVitalsTemperatureInput(String input) {
        try {
            double temp = Double.parseDouble(input.substring(1));
            vsDescriptionArea.setText(" at GDS : Forehead (Temporal Artery) Thermometer:");
            vsOutputArea.setText("Body Temperature [ " + temp + " ] ℃");
        } catch (RuntimeException ex) {
            vsOutputArea.setText("Invalid temperature format. Use 't' followed by a number (e.g., t36.5).");
        }
    }

    private void processVitalsNumeric(double value) {
        if (sbp == null) {
            sbp = (int) value;
            vsOutputArea.setText("\tSBP [" + sbp + "] mmHg");
        } else if (dbp == null) {
            dbp = (int) value;
            vsOutputArea.setText("BP [" + sbp + " / " + dbp + "] mmHg");
        } else if (pulseRate == null) {
            pulseRate = (int) value;
            vsOutputArea.appendText("   PR [" + pulseRate + "]/minute");
        } else if (bodyTemperature == null) {
            bodyTemperature = value;
            vsOutputArea.appendText("\n\tBody Temperature [" + bodyTemperature + "]℃");
        } else if (respirationRate == null) {
            respirationRate = (int) value;
            vsOutputArea.appendText("\n\tRespiration Rate [" + respirationRate + "]/minute");
            resetVitalsStagedValues();
        }
    }

    private void saveVitalsToEMR() {
        String description = vsDescriptionArea.getText();
        String output = vsOutputArea.getText();
        if (description.isBlank() && output.isBlank()) return;

        IAITextAreaManager manager = IAIMain.getTextAreaManager();
        manager.focusArea(IAITextAreaManager.AREA_O);
        if (!description.isBlank()) manager.insertLineIntoFocusedArea(description);
        if (!output.isBlank()) manager.insertLineIntoFocusedArea("\t" + output);
        resetVitalsFields();
    }

    private void resetVitalsFields() {
        vsInputField.clear();
        vsOutputArea.clear();
        vsDescriptionArea.setText(" at GDS : Regular pulse, Right Seated Position");
        resetVitalsStagedValues();
    }

    private void resetVitalsStagedValues() {
        sbp = null;
        dbp = null;
        pulseRate = null;
        bodyTemperature = null;
        respirationRate = null;
    }

    private HBox createBottomButtons() {
        Button quitButton = createButton("Quit All", e -> close());
        HBox box = new HBox(8, quitButton);
        box.setAlignment(Pos.CENTER);
        return box;
    }

    private void positionInTopRight() {
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        setX(screenBounds.getMaxX() - getWidth() - 20);
        setY(screenBounds.getMinY() + 10);
    }

    private void checkBridgeReady() {
        IAIMain.getManagerSafely().ifPresentOrElse(
                manager -> { /* Ready */ },
                () -> showError("EMR connection not established. Please ensure the main application has initialized the IAIMain bridge.")
        );
    }

    private GridPane createFormGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(6);
        grid.setPadding(new Insets(6));
        return grid;
    }

    private Button createButton(String text, javafx.event.EventHandler<javafx.event.ActionEvent> handler) {
        Button button = new Button(text);
        button.setOnAction(handler);
        return button;
    }

    private void showError(String message) {
        Platform.runLater(() -> new Alert(Alert.AlertType.ERROR, message).showAndWait());
    }
}
