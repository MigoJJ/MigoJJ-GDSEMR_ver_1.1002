package com.emr.gds.features.review_of_systems.application;

import com.emr.gds.soap.ros.EMR_ROS_JtableDATA;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ReviewOfSystemsReportService {

    private ReviewOfSystemsReportService() {
    }

    public static String generateReport(Map<String, List<Boolean>> categorySelections) {
        StringBuilder report = new StringBuilder();
        report.append("REVIEW OF SYSTEMS:\n");
        report.append("=========================\n\n");

        boolean hasSelections = false;

        for (String category : EMR_ROS_JtableDATA.columnNames()) {
            List<String> selectedItems = new ArrayList<>();
            List<String> deniedItems = new ArrayList<>();

            List<Boolean> selections = categorySelections.getOrDefault(category, new ArrayList<>());
            String[] categoryItems = getItemsForCategory(category);

            for (int i = 0; i < categoryItems.length; i++) {
                boolean selected = i < selections.size() && selections.get(i);
                if (selected) {
                    selectedItems.add(categoryItems[i]);
                } else {
                    deniedItems.add(categoryItems[i]);
                }
            }

            if (!selectedItems.isEmpty()) {
                hasSelections = true;
                String categoryName = category.replace("<", "").replace(">", "").trim();
                report.append(String.format("%-45s\n", categoryName.toUpperCase() + ":"));

                int maxRows = Math.max(selectedItems.size(), deniedItems.size());
                for (int i = 0; i < maxRows; i++) {
                    String leftCol = "";
                    if (i < selectedItems.size()) {
                        leftCol = "    [+] " + selectedItems.get(i);
                    }

                    String rightCol = "";
                    if (i < deniedItems.size()) {
                        rightCol = "[-] " + deniedItems.get(i);
                    }
                    report.append(String.format("    %-40s %s\n", leftCol, rightCol));
                }
                report.append("\n");
            }
        }

        if (!hasSelections) {
            return "No symptoms selected.";
        }

        report.append("=========================\n\n");
        return report.toString();
    }

    private static String[] getItemsForCategory(String category) {
        return switch (category) {
            case "< General >" -> EMR_ROS_JtableDATA.General();
            case "< Vision >" -> EMR_ROS_JtableDATA.Vision();
            case "< Head_and_Neck >" -> EMR_ROS_JtableDATA.Head_and_Neck();
            case "< Pulmonary >" -> EMR_ROS_JtableDATA.Pulmonary();
            case "< Cardiovascular >" -> EMR_ROS_JtableDATA.Cardiovascular();
            case "< Gastrointestinal >" -> EMR_ROS_JtableDATA.Gastrointestinal();
            case "< Genito-Urinary >" -> EMR_ROS_JtableDATA.GenitoUrinary();
            case "< Hematology/Oncology >" -> EMR_ROS_JtableDATA.HematologyOncology();
            case "< Neurological >" -> EMR_ROS_JtableDATA.Neurological();
            case "< Endocrine >" -> EMR_ROS_JtableDATA.Endocrine();
            case "< Mental Health >" -> EMR_ROS_JtableDATA.MentalHealth();
            case "< Skin and Hair >" -> EMR_ROS_JtableDATA.SkinAndHair();
            default -> new String[0];
        };
    }
}
