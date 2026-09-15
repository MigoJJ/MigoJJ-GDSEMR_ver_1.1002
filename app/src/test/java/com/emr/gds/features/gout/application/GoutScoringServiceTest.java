package com.emr.gds.features.gout.application;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for gout scoring algorithm.
 * ACR/EULAR 2015 gout classification criteria calculator.
 */
class GoutScoringServiceTest {

    @Test
    void calculateScoreWithMsuConfirmed() {
        int score = GoutScoringService.calculateScore(
                true,   // msuConfirmed
                0,      // jointIndex
                0,      // clinicalFeatures
                0,      // timePatternIndex
                false,  // tophusPresent
                0,      // urateIndex
                false,  // synovialFluidNegative
                false,  // imagingPositive
                false   // erosionPositive
        );

        assertEquals(Integer.MAX_VALUE, score,
                "MSU crystal confirmation should return MAX_VALUE (definite gout diagnosis)");
    }

    @Test
    void calculateScoreWithoutMsuConfirmed() {
        int score = GoutScoringService.calculateScore(
                false,  // msuConfirmed
                2,      // jointIndex
                2,      // clinicalFeatures
                1,      // timePatternIndex
                false,  // tophusPresent
                2,      // urateIndex
                false,  // synovialFluidNegative
                false,  // imagingPositive
                false   // erosionPositive
        );

        assertTrue(score > 0, "Score should accumulate from non-confirmed findings");
    }

    @Test
    void calculateScoreWithTophusPresent() {
        int score1 = GoutScoringService.calculateScore(
                false, 0, 0, 0, false, 0, false, false, false);

        int score2 = GoutScoringService.calculateScore(
                false, 0, 0, 0, true, 0, false, false, false);

        assertEquals(score2 - score1, 4,
                "Tophus presence should add 4 points");
    }

    @Test
    void calculateScoreWithHighUrateLevel() {
        int score = GoutScoringService.calculateScore(
                false,  // msuConfirmed
                0,      // jointIndex
                0,      // clinicalFeatures
                0,      // timePatternIndex
                false,  // tophusPresent
                4,      // urateIndex (highest)
                false,  // synovialFluidNegative
                false,  // imagingPositive
                false   // erosionPositive
        );

        assertEquals(4, score, "Highest urate level (index 4) should contribute 4 points");
    }

    @Test
    void calculateScoreWithSynovialFluidNegative() {
        int score1 = GoutScoringService.calculateScore(
                false, 0, 0, 0, false, 0, false, false, false);

        int score2 = GoutScoringService.calculateScore(
                false, 0, 0, 0, false, 0, true, false, false);

        assertEquals(score2 - score1, -2,
                "Negative synovial fluid should subtract 2 points");
    }

    @Test
    void calculateScoreAccumulatesMultipleFactors() {
        int score = GoutScoringService.calculateScore(
                false,  // msuConfirmed
                2,      // jointIndex
                2,      // clinicalFeatures
                2,      // timePatternIndex
                true,   // tophusPresent
                4,      // urateIndex
                false,  // synovialFluidNegative
                true,   // imagingPositive
                true    // erosionPositive
        );

        assertTrue(score > 10, "Multiple positive factors should produce high score");
    }
}
