package com.emr.gds.features.gout;

import com.emr.gds.features.gout.application.GoutScoringService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GoutScoringServiceTest {

    @Test
    void testMsuConfirmedReturnsMaxValue() {
        int score = GoutScoringService.calculateScore(
                true,    // msuConfirmed
                0, 0, 0, false, 0, false, false, false
        );
        assertEquals(Integer.MAX_VALUE, score);
    }

    @Test
    void testNoSelectionsReturnsZero() {
        int score = GoutScoringService.calculateScore(
                false,   // msuConfirmed
                0,       // jointIndex
                0,       // clinicalFeatures
                0,       // timePatternIndex
                false,   // tophusPresent
                1,       // urateIndex (4 ~ < 6, 0점)
                false,   // synovialFluidNegative
                false,   // imagingPositive
                false    // erosionPositive
        );
        assertEquals(0, score);
    }

    @Test
    void testAllCriteriaMet() {
        int score = GoutScoringService.calculateScore(
                false,   // msuConfirmed
                2,       // jointIndex (엄지발가락 MTP1, 2점)
                3,       // clinicalFeatures (3개)
                2,       // timePatternIndex (재발성, 2점)
                true,    // tophusPresent (4점)
                4,       // urateIndex (≥ 10, 4점)
                false,   // synovialFluidNegative
                true,    // imagingPositive (4점)
                true     // erosionPositive (4점)
        );
        assertEquals(23, score);
        assertTrue(GoutScoringService.isClassified(score));
    }

    @Test
    void testIsClassifiedThreshold() {
        assertTrue(GoutScoringService.isClassified(8));
        assertTrue(GoutScoringService.isClassified(9));
        assertFalse(GoutScoringService.isClassified(7));
        assertFalse(GoutScoringService.isClassified(0));
    }

    @Test
    void testSummaryGenerationWithMsu() {
        String summary = GoutScoringService.generateSummary(
                true,    // msuConfirmed
                "", 0, "", false, "", false, false, false, 0
        );
        assertTrue(summary.contains("MSU"));
        assertTrue(summary.contains("확정적 통풍"));
    }

    @Test
    void testSummaryGenerationScored() {
        String summary = GoutScoringService.generateSummary(
                false,   // msuConfirmed
                "엄지발가락 MTP1 (2점)",
                3,
                "재발성 전형적 발작 (2점)",
                true,
                "≥ 10 (4점)",
                false,
                true,
                true,
                23
        );
        assertTrue(summary.contains("총점"));
        assertTrue(summary.contains("통풍 분류 가능"));
    }
}
