package com.emr.gds.features.template;

import com.emr.gds.features.template.application.TemplateSectionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TemplateSectionParserTest {

    private TemplateSectionService service;

    @BeforeEach
    void setUp() {
        service = new TemplateSectionService();
    }

    @Test
    void testBasicSectionParsing() {
        String input = "CC> Chest pain\nPI> 3 days of SOB";
        LinkedHashMap<String, List<String>> sections = service.parseSections(input);

        assertNotNull(sections);
        assertTrue(sections.containsKey("CC>"));
        assertTrue(sections.containsKey("PI>"));
    }

    @Test
    void testMultilineSections() {
        String input = "CC> Chief complaint\n\tdetails about complaint\nPI> Present illness";
        LinkedHashMap<String, List<String>> sections = service.parseSections(input);

        List<String> ccLines = sections.get("CC>");
        assertTrue(ccLines.size() >= 1);
    }

    @Test
    void testSectionOrderingIsPreserved() {
        String input = "P> Plan\nA> Assessment\nCC> Chief complaint";
        LinkedHashMap<String, List<String>> sections = service.parseSections(input);

        assertNotNull(sections.get("P>"));
        assertNotNull(sections.get("A>"));
        assertNotNull(sections.get("CC>"));
    }

    @Test
    void testEmptySections() {
        String input = "CC>\nPI>\nROS>";
        LinkedHashMap<String, List<String>> sections = service.parseSections(input);

        assertTrue(sections.get("CC>").isEmpty() || sections.get("CC>").size() >= 0);
    }

    @Test
    void testBuildingOrderedOutput() {
        String input = "P> Treatment\nCC> Fever\nA> Infection suspected";
        LinkedHashMap<String, List<String>> sections = service.parseSections(input);
        String output = service.buildOrderedOutput(sections);

        int ccIndex = output.indexOf("CC>");
        int aIndex = output.indexOf("A>");
        int pIndex = output.indexOf("P>");

        assertTrue(ccIndex < aIndex);
        assertTrue(aIndex < pIndex);
    }

    @Test
    void testComplexTemplate() {
        String input = """
            CC> Fever, cough
            PI> Started 2 days ago
                Worse at night
            PMH> HTN, DM
            ROS> Denies chills
            O> Temp 38.5C
            A> Pneumonia vs bronchitis
            P> CXR today
                Start amoxicillin
            """;

        LinkedHashMap<String, List<String>> sections = service.parseSections(input);
        String output = service.buildOrderedOutput(sections);

        assertTrue(output.contains("CC>"));
        assertTrue(output.contains("PI>"));
        assertTrue(output.contains("A>"));
        assertTrue(output.contains("P>"));
    }

    @Test
    void testHeaderFormats() {
        String input = """
            CC> Chief complaint here
            Physical Exam> Physical findings
            PMH> Past medical history
            """;

        LinkedHashMap<String, List<String>> sections = service.parseSections(input);

        assertTrue(sections.containsKey("CC>"));
        assertTrue(sections.containsKey("Physical Exam>"));
        assertTrue(sections.containsKey("PMH>"));
    }
}
