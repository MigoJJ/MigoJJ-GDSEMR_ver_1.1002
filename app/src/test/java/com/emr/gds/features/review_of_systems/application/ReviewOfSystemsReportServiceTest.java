package com.emr.gds.features.review_of_systems.application;

import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the review-of-systems report generation service.
 * Tests pure business logic (report formatting) independently of UI.
 */
class ReviewOfSystemsReportServiceTest {

    @Test
    void generateReportWithNoSelections() {
        Map<String, List<Boolean>> categorySelections = new HashMap<>();
        String report = ReviewOfSystemsReportService.generateReport(categorySelections);

        assertEquals("No symptoms selected.", report,
                "Empty selections should produce the no-symptoms message");
    }

    @Test
    void generateReportWithGeneralSymptoms() {
        Map<String, List<Boolean>> categorySelections = new HashMap<>();
        List<Boolean> generalSelections = Arrays.asList(true, false, true, false);
        categorySelections.put("< General >", generalSelections);

        String report = ReviewOfSystemsReportService.generateReport(categorySelections);

        assertTrue(report.contains("REVIEW OF SYSTEMS"),
                "Report should contain title");
        assertTrue(report.contains("GENERAL"),
                "Report should contain General category");
        assertTrue(report.contains("[+]"),
                "Report should mark selected items with [+]");
        assertTrue(report.contains("[-]"),
                "Report should mark denied items with [-]");
        assertFalse(report.equals("No symptoms selected."),
                "Non-empty selections should not produce empty message");
    }

    @Test
    void generateReportWithMultipleCategories() {
        Map<String, List<Boolean>> categorySelections = new HashMap<>();
        categorySelections.put("< General >", Arrays.asList(true, false));
        categorySelections.put("< Vision >", Arrays.asList(false, true));
        categorySelections.put("< Cardiovascular >", Arrays.asList(true, true));

        String report = ReviewOfSystemsReportService.generateReport(categorySelections);

        assertTrue(report.contains("GENERAL"), "Should include General");
        assertTrue(report.contains("CARDIOVASCULAR"), "Should include Cardiovascular");
        assertTrue(report.contains("[+]"), "Should have selected items");
    }

    @Test
    void reportFormatting() {
        Map<String, List<Boolean>> categorySelections = new HashMap<>();
        categorySelections.put("< General >", Arrays.asList(true, false, false));

        String report = ReviewOfSystemsReportService.generateReport(categorySelections);

        assertTrue(report.contains("========================="),
                "Report should have header/footer separators");
        assertTrue(report.contains("REVIEW OF SYSTEMS"),
                "Report should have title");
    }
}
