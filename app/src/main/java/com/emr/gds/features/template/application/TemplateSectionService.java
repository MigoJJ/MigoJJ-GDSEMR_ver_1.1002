package com.emr.gds.features.template.application;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TemplateSectionService {

    public static final String[] TEXT_AREA_TITLES = {
            "CC>", "PI>", "ROS>", "PMH>", "S>",
            "O>", "Physical Exam>", "A>", "P>", "Comment>"
    };

    private static final Pattern HEADER_PATTERN = Pattern.compile(
            "^\\s*(CC>|PI>|ROS>|PMH>|S>|O>|Physical Exam>|A>|P>|Comment>)\\s*(.*)$"
    );

    public LinkedHashMap<String, List<String>> parseSections(String content) {
        LinkedHashMap<String, List<String>> sections = new LinkedHashMap<>();
        for (String title : TEXT_AREA_TITLES) {
            sections.put(title, new ArrayList<>());
        }
        String currentSection = null;
        for (String line : content.split("\\r?\\n", -1)) {
            Matcher m = HEADER_PATTERN.matcher(line);
            if (m.matches()) {
                currentSection = m.group(1);
                String afterHeader = m.group(2).trim();
                if (!afterHeader.isEmpty()) {
                    sections.get(currentSection).add(afterHeader);
                }
            } else if (currentSection != null) {
                sections.get(currentSection).add(line);
            } else {
                if (sections.containsKey("Comment>")) {
                    sections.get("Comment>").add(line);
                }
            }
        }
        return sections;
    }

    public String buildOrderedOutput(LinkedHashMap<String, List<String>> sections) {
        StringBuilder out = new StringBuilder();
        List<String> order = Arrays.asList("CC>", "PI>", "PMH>", "S>", "ROS>", "O>", "Physical Exam>", "A>", "P>", "Comment>");

        for (String label : order) {
            List<String> lines = sections.getOrDefault(label, Collections.emptyList());
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
