package com.emr.gds.features.thyroid.application;

import com.emr.gds.features.thyroid.domain.ThyroidEntry;
import com.emr.gds.features.thyroid.domain.ThyroidRiskCalculator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Builds the free-text specialist summary from a {@link ThyroidEntry}.
 * Pure text generation, no JavaFX dependency, so it's independently testable
 * from the {@code adapter/in/ui} pane that collects the entry.
 */
public final class ThyroidSummaryService {

    private static final String TSH_REF = "0.25-5 mIU/L";
    private static final String FT4_REF = "10.6-19.4 ng/L";
    private static final String FT3_REF = "2.00-4.40 pg/mL";
    private static final String T3_REF = "0.9-2.5 ng/ml";
    private static final String TPOAB_REF = "≤34.0 IU/mL";
    private static final String TG_REF = "3.50-77.00 ng/mL";
    private static final String TGAB_REF = "≤115.0 IU/mL";
    private static final String TRAB_REF = "<1.75 IU/L";
    private static final String CALCITONIN_REF = "M:≤18.2, F:≤11.5 pg/mL";
    private static final String REVT3_REF = "90-350 pg/mL";

    private ThyroidSummaryService() {
    }

    /**
     * @param e                  the thyroid entry to summarize
     * @param selectedConditions condition-checklist selections, grouped by category label
     * @param tiRadsResultText   the current TI-RADS result label text (e.g. "Score: ... TR3")
     */
    public static String buildSpecialistSummary(ThyroidEntry e, Map<String, List<String>> selectedConditions,
                                                 String tiRadsResultText) {
        List<String> lines = new ArrayList<>();
        String visit = (e.getVisitType() != null)
                ? e.getVisitType() + " visit"
                : "Thyroid specialist evaluation";
        lines.add("* Visit: " + visit);

        if (e.getCategories().isEmpty()) {
            lines.add("     | Dx: Thyroid screening/evaluation");
        } else {
            List<String> dx = e.getCategories().stream().map(Object::toString).toList();
            lines.add("     | Dx: " + String.join(", ", dx));
        }

        if (!selectedConditions.isEmpty()) {
            lines.add("     | Conditions checklist:");
            for (var entryGroup : selectedConditions.entrySet()) {
                lines.add("     |   " + entryGroup.getKey() + ": " + String.join("; ", entryGroup.getValue()));
            }
        }

        if (e.getGoiterSize() != null && !e.getGoiterSize().isBlank()) {
            String goiterSize = e.getGoiterSize();
            if (!goiterSize.toLowerCase().contains("cc")) { // If it doesn't contain "cc", process for main summary
                if (!goiterSize.toLowerCase().contains("cm")) { // If no "cm" or "cc"
                    try {
                        Double.parseDouble(goiterSize); // Check if it's a number
                        goiterSize += " CC"; // If so, append " CC"
                    } catch (NumberFormatException ex) {
                        // Not a number, leave as is (e.g., "nodular")
                    }
                }
                lines.add("     | Physical Exam: Goiter size " + goiterSize);
            }
        }
        String negatives = (e.getSymptomNegatives() != null) ? e.getSymptomNegatives().trim() : "";
        boolean hasNegatives = !negatives.isBlank();
        if (!e.getSymptoms().isEmpty() || hasNegatives) {
            List<String> syms = e.getSymptoms().stream().map(ThyroidEntry.Symptom::getLabel).toList();
            StringBuilder symptomLine = new StringBuilder("     | Symptoms: ");
            if (!syms.isEmpty()) {
                symptomLine.append(String.join("; ", syms));
            } else {
                symptomLine.append("None reported");
            }
            if (hasNegatives) {
                symptomLine.append("; Recent negatives: ").append(negatives);
            }
            lines.add(symptomLine.toString());
        }

        List<String> statusParts = new ArrayList<>();
        if (e.getCategories().contains(ThyroidEntry.MainCategory.HYPOTHYROIDISM)) {
            StringBuilder hypoLine = new StringBuilder("Hypothyroidism ");
            if (e.getHypoEtiology() != null) hypoLine.append(e.getHypoEtiology()).append(". ");
            hypoLine.append(Boolean.TRUE.equals(e.isHypoOvert()) ? "Overt." : "Subclinical.");
            if (e.getLt4DoseMcgPerDay() != null) {
                hypoLine.append(" LT4 ").append(e.getLt4DoseMcgPerDay()).append(" mcg.");
                if (e.getPatientWeightKg() != null) {
                    double est = ThyroidRiskCalculator.calculateFullReplacementDose(e.getPatientWeightKg());
                    hypoLine.append(" Est ").append((int) est).append(" mcg.");
                }
            }
            statusParts.add(hypoLine.toString());
        }

        if (e.getCategories().contains(ThyroidEntry.MainCategory.HYPERTHYROIDISM)) {
            StringBuilder hyperLine = new StringBuilder("Hyperthyroidism ");
            if (e.getHyperEtiology() != null) hyperLine.append(e.getHyperEtiology()).append(". ");
            hyperLine.append(Boolean.TRUE.equals(e.isHyperActive()) ? "Uncontrolled/Active." : "Controlled/Remission.");
            if (e.getAtdName() != null) {
                hyperLine.append(" On ").append(e.getAtdName()).append(" ").append(e.getAtdDoseMgPerDay()).append(" mg.");
            }
            statusParts.add(hyperLine.toString());
        }

        if (e.getCategories().contains(ThyroidEntry.MainCategory.CANCER)) {
            StringBuilder caLine = new StringBuilder("Thyroid Cancer ");
            if (e.getAtaRisk() != null && !e.getAtaRisk().equals("Low Risk")) {
                caLine.append(e.getAtaRisk()).append(" (path features). ");
            } else {
                caLine.append("Low Risk Stratification. ");
            }
            if (e.getTg() != null) caLine.append("Tg: ").append(e.getTg()).append(" ng/mL. ");
            statusParts.add(caLine.toString().trim());
        }
        if (!statusParts.isEmpty()) {
            lines.add("     | Status: " + statusParts.getFirst());
            for (int i = 1; i < statusParts.size(); i++) {
                lines.add("     | " + statusParts.get(i));
            }
        }

        List<String> labLines = new ArrayList<>();
        addLabLine(labLines, "TSH", e.getTsh(), TSH_REF);
        addLabLine(labLines, "fT4", e.getFreeT4(), FT4_REF);
        addLabLine(labLines, "fT3", e.getFreeT3(), FT3_REF);
        addLabLine(labLines, "T3", e.getTotalT3(), T3_REF);
        addLabLine(labLines, "TPOAb", e.getTpoAb(), TPOAB_REF);
        addLabLine(labLines, "Tg", e.getTg(), TG_REF);
        addLabLine(labLines, "TgAb", e.getTgAb(), TGAB_REF);
        addLabLine(labLines, "TRAb", e.getTrab(), TRAB_REF);
        addLabLine(labLines, "Calcitonin", e.getCalcitonin(), CALCITONIN_REF);
        addLabLine(labLines, "revT3", e.getReverseT3(), REVT3_REF);

        if (!labLines.isEmpty()) {
            String datePart = (e.getLastLabDate() != null) ? " (" + e.getLastLabDate() + ")" : "";
            lines.add("     | Labs" + datePart + ":");
            lines.addAll(labLines);
        }

        if (tiRadsResultText != null && tiRadsResultText.contains("Score")) {
            lines.add("     | Nodule/TI-RADS: " + tiRadsResultText.replace("\n", ", "));
        }

        StringBuilder plan = new StringBuilder("Plan: ");
        if (e.getFollowUpInterval() != null) {
            plan.append("Follow up in ").append(e.getFollowUpInterval()).append(". ");
        }
        if (e.getFollowUpPlanText() != null && !e.getFollowUpPlanText().isBlank()) {
            String cleanPlan = e.getFollowUpPlanText().replaceAll("\\R+", "; ").trim();
            plan.append(cleanPlan);
        }
        lines.add("     | " + plan.toString().trim());

        return String.join("\n", lines);
    }

    private static void addLabLine(List<String> lines, String name, Double value, String ref) {
        if (value == null) return;
        String indicator = getLabIndicator(value, ref);
        lines.add(String.format("          %-15s\t%-10.2f\t%-2s\t(%s)", name, value, indicator, ref));
    }

    private static String getLabIndicator(Double value, String ref) {
        if (value == null || ref == null) return "";

        // Special handling for complex cases like Calcitonin
        if (ref.contains("M:") || ref.contains("F:")) {
            return "";
        }

        String[] parts = ref.split(" ");
        if (parts.length == 0) return "";
        String numericRef = parts[0];

        if (numericRef.contains("-")) {
            String[] rangeParts = numericRef.split("-");
            if (rangeParts.length < 2) return "";
            try {
                double low = Double.parseDouble(rangeParts[0]);
                double high = Double.parseDouble(rangeParts[1]);
                if (value < low) return "▽";
                if (value > high) return "▲";
            } catch (NumberFormatException e) {
                return ""; // Or log error
            }
        } else if (numericRef.startsWith("≤")) {
            try {
                double high = Double.parseDouble(numericRef.substring(1));
                if (value > high) return "▲";
            } catch (NumberFormatException e) {
                return "";
            }
        } else if (numericRef.startsWith("<")) {
            try {
                double high = Double.parseDouble(numericRef.substring(1));
                if (value >= high) return "▲";
            } catch (NumberFormatException e) {
                return "";
            }
        }
        return "";
    }
}
