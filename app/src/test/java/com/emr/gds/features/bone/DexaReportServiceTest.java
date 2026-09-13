package com.emr.gds.features.bone;

import com.emr.gds.features.bone.application.DexaReportService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DexaReportServiceTest {

    @Test
    void testTScoreSevereOsteoporosisWithFracture() {
        String report = DexaReportService.generateReport(
                -3.5,    // T-Score: severe
                true,    // isTScore
                65,      // age
                "Female",
                true,    // hasFracture
                true,    // isMenopausal
                false,   // onHrt
                false,   // hasTah
                false    // hasStones
        );
        assertTrue(report.contains("Severe Osteoporosis"));
        assertTrue(report.contains("bisphosphonate"));
    }

    @Test
    void testTScoreOsteoporosisWithoutFracture() {
        String report = DexaReportService.generateReport(
                -2.8,    // T-Score: osteoporosis without fracture
                true,    // isTScore
                60,
                "Female",
                false,   // hasFracture
                true,
                false,
                false,
                false
        );
        assertTrue(report.contains("Osteoporosis"));
        assertFalse(report.contains("Severe"));
    }

    @Test
    void testTScoreOsteopenia() {
        String report = DexaReportService.generateReport(
                -1.5,    // T-Score: osteopenia
                true,    // isTScore
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
        assertTrue(report.contains("2–3 years"));
    }

    @Test
    void testTScoreNormalBoneDensity() {
        String report = DexaReportService.generateReport(
                -0.5,    // T-Score: normal
                true,    // isTScore
                50,
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
    void testZScoreBelowExpectedRange() {
        String report = DexaReportService.generateReport(
                -2.5,    // Z-Score: below expected
                false,   // isZScore
                35,
                "Female",
                false,
                false,
                false,
                false,
                false
        );
        assertTrue(report.contains("Below expected range"));
        assertTrue(report.contains("Z-Score"));
    }

    @Test
    void testZScoreWithinExpectedRange() {
        String report = DexaReportService.generateReport(
                -1.0,    // Z-Score: within range
                false,   // isZScore
                40,
                "Female",
                false,
                false,
                false,
                false,
                false
        );
        assertTrue(report.contains("Within expected range"));
    }

    @Test
    void testMaleReport() {
        String report = DexaReportService.generateReport(
                -2.0,
                true,
                70,
                "Male",
                true,    // hasFracture
                false,   // isMenopausal (ignored for males)
                false,
                false,   // hasTah (ignored for males)
                true     // hasStones
        );
        assertTrue(report.contains("70-year-old Male"));
        assertTrue(report.contains("Fragility Fx: Yes"));
        assertTrue(report.contains("Kidney Stones: Yes"));
        assertFalse(report.contains("Menopausal"));
    }

    @Test
    void testReportContainsScore() {
        String report = DexaReportService.generateReport(
                -1.8,
                true,
                60,
                "Female",
                false,
                false,
                false,
                false,
                false
        );
        assertTrue(report.contains("-1.8"));
        assertTrue(report.contains("DEXA Report"));
    }
}
