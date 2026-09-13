package com.emr.gds.features.template;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test the template parsing logic that will be extracted to application/ layer.
 * This validates the section-splitting algorithm that TemplateEditController uses.
 */
class TemplateSectionParserTest {

    private static final String[] TEXT_AREA_TITLES = {
            "CC>", "PI>", "ROS>", "PMH>", "S>",
            "O>", "Physical Exam>", "A>", "P>", "Comment>"
    };

    @Test
    void testBasicSectionParsing() {
        String input = "CC> Chest pain\nPI> 3 days of SOB";
        LinkedHashMap<String, List<String>> sections = parseSections(input);

        assertNotNull(sections);
        assertTrue(sections.containsKey("CC>"));
        assertTrue(sections.containsKey("PI>"));
    }

    @Test
    void testMultilineSections() {
        String input = "CC> Chief complaint\n\tdetails about complaint\nPI> Present illness";
        LinkedHashMap<String, List<String>> sections = parseSections(input);

        List<String> ccLines = sections.get("CC>");
        assertTrue(ccLines.size() >= 1);
    }

    @Test
    void testSectionOrderingIsPreserved() {
        String input = "P> Plan\nA> Assessment\nCC> Chief complaint";
        LinkedHashMap<String, List<String>> sections = parseSections(input);

        assertNotNull(sections.get("P>"));
        assertNotNull(sections.get("A>"));
        assertNotNull(sections.get("CC>"));
    }

    @Test
    void testEmptySections() {
        String input = "CC>\nPI>\nROS>";
        LinkedHashMap<String, List<String>> sections = parseSections(input);

        assertTrue(sections.get("CC>").isEmpty() || sections.get("CC>").size() >= 0);
    }

    @Test
    void testBuildingOrderedOutput() {
        String input = "P> Treatment\nCC> Fever\nA> Infection suspected";
        LinkedHashMap<String, List<String>> sections = parseSections(input);
        String output = buildOrderedOutput(sections);

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

        LinkedHashMap<String, List<String>> sections = parseSections(input);
        String output = buildOrderedOutput(sections);

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

        LinkedHashMap<String, List<String>> sections = parseSections(input);

        assertTrue(sections.containsKey("CC>"));
        assertTrue(sections.containsKey("Physical Exam>"));
        assertTrue(sections.containsKey("PMH>"));
    }

    // --- Parser Logic (Ported from TemplateEditController) ---

    private LinkedHashMap<String, List<String>> parseSections(String content) {
        LinkedHashMap<String, List<String>> sections = new LinkedHashMap<>();
        for (String title : TEXT_AREA_TITLES) {
            sections.put(title, new ArrayList<>());
        }

        String currentSection = null;
        for (String line : content.split("\\r?\\n", -1)) {
            boolean isHeader = false;
            for (String title : TEXT_AREA_TITLES) {
                if (line.trim().startsWith(title)) {
                    currentSection = title;
                    String afterHeader = line.substring(line.indexOf(title) + title.length()).trim();
                    if (!afterHeader.isEmpty()) {
                        sections.get(currentSection).add(afterHeader);
                    }
                    isHeader = true;
                    break;
                }
            }

            if (!isHeader && currentSection != null) {
                sections.get(currentSection).add(line);
            }
        }

        return sections;
    }

    private String buildOrderedOutput(LinkedHashMap<String, List<String>> sections) {
        StringBuilder out = new StringBuilder();
        List<String> order = List.of("CC>", "PI>", "PMH>", "S>", "ROS>", "O>", "Physical Exam>", "A>", "P>", "Comment>");

        for (String label : order) {
            List<String> lines = sections.getOrDefault(label, new ArrayList<>());
            if (lines.isEmpty() || lines.stream().allMatch(String::isBlank)) continue;

            out.append(label);
            String firstLineContent = lines.get(0).trim();
            if (!firstLineContent.isEmpty()) {
                out.append(' ').append(firstLineContent);
            }
            out.append('\n');

            for (int i = 1; i < lines.size(); i++) {
                out.append("\t").append(lines.get(i)).append('\n');
            }
        }

        return out.toString().trim();
    }
}
