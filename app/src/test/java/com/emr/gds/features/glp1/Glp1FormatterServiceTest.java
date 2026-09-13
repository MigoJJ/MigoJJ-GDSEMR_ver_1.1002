package com.emr.gds.features.glp1;

import com.emr.gds.features.glp1.application.Glp1FormatterService;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class Glp1FormatterServiceTest {

    @Test
    void testProblemListFormatting() {
        String result = Glp1FormatterService.formatProblemList(
                true,   // onTherapy
                true,   // forT2dm
                false,  // forObesity
                false,  // forAscvd
                "Ozempic (semaglutide, SC weekly)",
                "2.0 mg weekly",
                "",     // customDose
                LocalDate.of(2026, 10, 13),
                "Every 4 weeks",
                "Monitor weight",
                false,  // mtcMen2
                false,  // pancreatitis
                false,  // pregnancy
                false,  // severeGi
                ""      // otherContra
        );

        assertTrue(result.contains("MEDICATION - GLP-1RA (SEMAGLUTIDE)"));
        assertTrue(result.contains("On therapy"));
        assertTrue(result.contains("Indication: T2DM"));
        assertTrue(result.contains("Ozempic"));
        assertTrue(result.contains("2.0 mg weekly"));
        assertTrue(result.contains("2026-10-13"));
    }

    @Test
    void testProblemListWithContraindications() {
        String result = Glp1FormatterService.formatProblemList(
                true, true, false, false,
                "Ozempic (semaglutide, SC weekly)",
                "1.0 mg weekly",
                "",
                LocalDate.now(),
                "Every 8 weeks",
                "",
                true,   // mtcMen2
                true,   // pancreatitis
                false,
                false,
                "allergy to components"
        );

        assertTrue(result.contains("CONTRAINDICATED: Personal/family history MTC or MEN2"));
        assertTrue(result.contains("CAUTION: History of pancreatitis"));
        assertTrue(result.contains("Other: allergy to components"));
    }

    @Test
    void testAssessmentSummaryFormatting() {
        String result = Glp1FormatterService.formatAssessmentSummary(
                "Ozempic (semaglutide, SC weekly)",
                "1.0 mg weekly",
                "",
                LocalDate.of(2026, 11, 13)
        );

        assertTrue(result.contains("GLP-1RA (Semaglutide)"));
        assertTrue(result.contains("Brand: Ozempic"));
        assertTrue(result.contains("Dose: 1.0 mg weekly"));
        assertTrue(result.contains("Next visit: 2026-11-13"));
    }

    @Test
    void testAssessmentSummaryEmptyWhenNoData() {
        String result = Glp1FormatterService.formatAssessmentSummary(
                null, null, "", null
        );

        assertEquals("", result);
    }

    @Test
    void testValidationWarningMtcMen2() {
        String result = Glp1FormatterService.getValidationWarning(
                true, true, false, "Ozempic"
        );

        assertTrue(result.contains("MTC/MEN2 history"));
    }

    @Test
    void testValidationWarningPregnancy() {
        String result = Glp1FormatterService.getValidationWarning(
                true, false, true, "Ozempic"
        );

        assertTrue(result.contains("Pregnancy"));
    }

    @Test
    void testValidationWarningMissingBrand() {
        String result = Glp1FormatterService.getValidationWarning(
                true, false, false, null
        );

        assertTrue(result.contains("Please select a brand/route"));
    }

    @Test
    void testValidationNoWarningsWhenEmpty() {
        String result = Glp1FormatterService.getValidationWarning(
                false, false, false, null
        );

        assertEquals("", result);
    }

    @Test
    void testNoDoseSpecifiedMessage() {
        String result = Glp1FormatterService.formatProblemList(
                true, true, false, false,
                null, null, "", LocalDate.now(),
                "Every 4 weeks", "",
                false, false, false, false, ""
        );

        assertTrue(result.contains("[NO DOSE SPECIFIED]"));
    }
}
