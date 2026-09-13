package com.emr.gds.features.bone;

import com.emr.gds.features.bone.application.DexaReportService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DexaReportGenerationTest {

    @Test
    void testReportContainsAllSections() {
        String report = DexaReportService.generateReport(
                -2.0,
                true,
                65,
                "Female",
                false,
                true,
                false,
                false,
                false
        );

        assertTrue(report.contains("DEXA Report"));
        assertTrue(report.contains("Diagnosis:"));
        assertTrue(report.contains("Patient:"));
        assertTrue(report.contains("Clinical Factors"));
        assertTrue(report.contains("Comment>"));
    }

    @Test
    void testTScoreOsteopeniaRecommendations() {
        String report = DexaReportService.generateReport(
                -1.5,
                true,
                55,
                "Female",
                false,
                true,
                false,
                false,
                false
        );

        assertTrue(report.contains("Osteopenia"));
        assertTrue(report.contains("Lifestyle modification"));
        assertTrue(report.contains("calcium + vitamin D"));
        assertTrue(report.contains("2–3 years"));
    }

    @Test
    void testSevereOsteoporosisWithFractureRecommendations() {
        String report = DexaReportService.generateReport(
                -3.5,
                true,
                70,
                "Female",
                true,
                true,
                false,
                false,
                false
        );

        assertTrue(report.contains("Severe Osteoporosis"));
        assertTrue(report.contains("bisphosphonate"));
        assertTrue(report.contains("denosumab"));
        assertTrue(report.contains("anabolic therapy"));
    }

    @Test
    void testDateFormatIncluded() {
        String report = DexaReportService.generateReport(
                -1.0,
                true,
                50,
                "Female",
                false,
                false,
                false,
                false,
                false
        );

        assertTrue(report.contains("202"));
    }

    @Test
    void testMaleReportExcludesFemaleOnlyFactors() {
        String report = DexaReportService.generateReport(
                -1.5,
                true,
                70,
                "Male",
                true,
                false,
                false,
                false,
                true
        );

        assertTrue(report.contains("70-year-old Male"));
        assertFalse(report.contains("Menopausal"));
        assertFalse(report.contains("TAH"));
        assertTrue(report.contains("Fragility Fx"));
        assertTrue(report.contains("Kidney Stones"));
    }

    @Test
    void testZScoreReportFormatting() {
        String report = DexaReportService.generateReport(
                -2.5,
                false,
                25,
                "Female",
                false,
                false,
                false,
                false,
                false
        );

        assertTrue(report.contains("Z-Score"));
        assertTrue(report.contains("Below expected range"));
    }

    @Test
    void testNormalBoneDensityNoRxRecommendations() {
        String report = DexaReportService.generateReport(
                0.5,
                true,
                45,
                "Female",
                false,
                false,
                false,
                false,
                false
        );

        assertTrue(report.contains("Normal Bone Density"));
        assertFalse(report.contains("bisphosphonate"));
        assertFalse(report.contains("Lifestyle modification"));
    }

    @Test
    void testReportFormattingConsistency() {
        String report1 = DexaReportService.generateReport(
                -2.0, true, 60, "Female", false, true, false, false, false
        );
        String report2 = DexaReportService.generateReport(
                -2.0, true, 60, "Female", false, true, false, false, false
        );

        assertEquals(report1, report2);
    }
}
