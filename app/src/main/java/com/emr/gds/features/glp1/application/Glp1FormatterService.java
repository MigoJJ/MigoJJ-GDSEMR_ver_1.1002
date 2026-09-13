package com.emr.gds.features.glp1.application;

import java.time.LocalDate;

public final class Glp1FormatterService {

    private Glp1FormatterService() {
    }

    public static String formatProblemList(
            boolean onTherapy,
            boolean forT2dm,
            boolean forObesity,
            boolean forAscvd,
            String brand,
            String dosePreset,
            String customDose,
            LocalDate nextVisit,
            String interval,
            String followUpNotes,
            boolean mtcMen2,
            boolean pancreatitis,
            boolean pregnancy,
            boolean severeGi,
            String otherContra) {

        StringBuilder sb = new StringBuilder();
        sb.append("MEDICATION - GLP-1RA (SEMAGLUTIDE)\n");

        sb.append("   Status / Indication:\n");
        if (onTherapy) {
            sb.append("      - On therapy\n");
        } else {
            sb.append("      - NOT currently on therapy\n");
        }
        if (forT2dm)    sb.append("      - Indication: T2DM\n");
        if (forObesity) sb.append("      - Indication: Obesity / overweight\n");
        if (forAscvd)   sb.append("      - Indication: ASCVD risk reduction\n");

        sb.append("   Dose:\n");
        if (brand != null && !brand.isEmpty()) {
            sb.append("      - Brand / Route: ").append(brand).append("\n");
        }
        if (dosePreset != null && !dosePreset.isEmpty()) {
            sb.append("      - Dose: ").append(dosePreset).append("\n");
        }
        if (customDose != null && !customDose.isEmpty()) {
            sb.append("      - Custom dose: ").append(customDose).append("\n");
        }
        if ((brand == null || brand.isEmpty()) &&
            (dosePreset == null || dosePreset.isEmpty()) &&
            (customDose == null || customDose.isEmpty())) {
            sb.append("      - [NO DOSE SPECIFIED]\n");
        }

        sb.append("   Follow-up:\n");
        if (nextVisit != null) {
            sb.append("      - Next visit: ").append(nextVisit).append("\n");
        }
        if (interval != null && !interval.isEmpty()) {
            sb.append("      - Interval: ").append(interval).append("\n");
        }
        if (followUpNotes != null && !followUpNotes.isEmpty()) {
            sb.append("      - Notes: ").append(followUpNotes).append("\n");
        }

        sb.append("   Contraindications / Cautions:\n");
        boolean hasContra = false;
        if (mtcMen2) {
            sb.append("      - CONTRAINDICATED: Personal/family history MTC or MEN2\n");
            hasContra = true;
        }
        if (pancreatitis) {
            sb.append("      - CAUTION: History of pancreatitis\n");
            hasContra = true;
        }
        if (pregnancy) {
            sb.append("      - CONTRAINDICATED: Pregnancy or planning pregnancy\n");
            hasContra = true;
        }
        if (severeGi) {
            sb.append("      - CAUTION: Severe GI disease or gastroparesis\n");
            hasContra = true;
        }
        if (otherContra != null && !otherContra.isEmpty()) {
            sb.append("      - Other: ").append(otherContra).append("\n");
            hasContra = true;
        }
        if (!hasContra) {
            sb.append("      - None documented\n");
        }

        return sb.toString();
    }

    public static String formatAssessmentSummary(
            String brand,
            String presetDose,
            String customDose,
            LocalDate nextVisit) {

        if ((brand == null || brand.isEmpty()) &&
            (presetDose == null || presetDose.isEmpty()) &&
            (customDose == null || customDose.isEmpty()) &&
            nextVisit == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder("GLP-1RA (Semaglutide): ");
        boolean hasEntry = false;

        if (brand != null && !brand.isEmpty()) {
            sb.append("Brand: ").append(brand);
            hasEntry = true;
        }

        String dose = (presetDose != null && !presetDose.isEmpty()) ? presetDose : customDose;
        if (dose != null && !dose.isEmpty()) {
            if (hasEntry) sb.append(" | ");
            sb.append("Dose: ").append(dose);
            hasEntry = true;
        }

        if (nextVisit != null) {
            if (hasEntry) sb.append(" | ");
            sb.append("Next visit: ").append(nextVisit);
        }

        return sb.toString();
    }

    public static String getValidationWarning(
            boolean onTherapy,
            boolean mtcMen2,
            boolean pregnancy,
            String brand) {

        StringBuilder warnings = new StringBuilder();

        if (mtcMen2) {
            warnings.append("Warning: MTC/MEN2 history - GLP-1RA is contraindicated. ");
        }
        if (pregnancy) {
            warnings.append("Warning: Pregnancy - GLP-1RA is contraindicated. ");
        }
        if (onTherapy && (brand == null || brand.isEmpty())) {
            warnings.append("Warning: Please select a brand/route. ");
        }

        return warnings.toString();
    }
}
