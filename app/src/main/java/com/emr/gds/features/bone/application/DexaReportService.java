package com.emr.gds.features.bone.application;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public final class DexaReportService {

    private DexaReportService() {
    }

    public static String generateReport(
            double score,
            boolean isTScore,
            int age,
            String gender,
            boolean hasFracture,
            boolean isMenopausal,
            boolean onHrt,
            boolean hasTah,
            boolean hasStones) {

        String scoreType = isTScore ? "T-Score" : "Z-Score";
        String diagnosis = calculateDiagnosis(score, isTScore, hasFracture);
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("< DEXA Report - %s >\n", date));
        sb.append(String.format("Diagnosis: %s (%s: %.1f)\n", diagnosis, scoreType, score));
        sb.append(String.format("Patient: %d-year-old %s\n", age, gender));

        if ("Female".equals(gender)) {
            sb.append(String.format("Clinical Factors → Menopausal: %s | Fragility Fx: %s | On HRT: %s | TAH: %s | Kidney Stones: %s\n",
                    boolToYN(isMenopausal), boolToYN(hasFracture), boolToYN(onHrt), boolToYN(hasTah), boolToYN(hasStones)));
        } else {
            sb.append(String.format("Clinical Factors → Fragility Fx: %s | Kidney Stones: %s\n",
                    boolToYN(hasFracture), boolToYN(hasStones)));
        }

        sb.append("\nComment>\n");
        sb.append(String.format("# %s based on %s of %.1f.\n", diagnosis, scoreType, score));

        if (isTScore) {
            if (score <= -2.5) {
                sb.append("# Consider bisphosphonate, denosumab, or anabolic therapy.\n");
            } else if (score <= -1.0) {
                sb.append("# Lifestyle modification, calcium + vitamin D, repeat DEXA in 2–3 years.\n");
            }
        }

        return sb.toString();
    }

    private static String calculateDiagnosis(double score, boolean isTScore, boolean hasFracture) {
        if (isTScore) {
            if (score <= -2.5) {
                return hasFracture ? "Severe Osteoporosis" : "Osteoporosis";
            } else if (score < -1.0) {
                return "Osteopenia";
            } else {
                return "Normal Bone Density";
            }
        } else {
            return (score <= -2.0) ? "Below expected range for age" : "Within expected range for age";
        }
    }

    private static String boolToYN(boolean b) {
        return b ? "Yes" : "No";
    }
}
