package com.emr.gds.features.gout.application;

public final class GoutScoringService {

    private GoutScoringService() {
    }

    public static int calculateScore(
            boolean msuConfirmed,
            int jointIndex,
            int clinicalFeatures,
            int timePatternIndex,
            boolean tophusPresent,
            int urateIndex,
            boolean synovialFluidNegative,
            boolean imagingPositive,
            boolean erosionPositive) {

        if (msuConfirmed) {
            return Integer.MAX_VALUE;
        }

        int score = 0;
        score += jointIndex;
        score += clinicalFeatures;
        score += timePatternIndex;

        if (tophusPresent) {
            score += 4;
        }

        switch (urateIndex) {
            case 0 -> score -= 4;
            case 2 -> score += 2;
            case 3 -> score += 3;
            case 4 -> score += 4;
        }

        if (synovialFluidNegative) {
            score -= 2;
        }

        if (imagingPositive) {
            score += 4;
        }

        if (erosionPositive) {
            score += 4;
        }

        return score;
    }

    public static boolean isClassified(int score) {
        return score >= 8;
    }

    public static String generateSummary(
            boolean msuConfirmed,
            String jointDesc,
            int clinicalFeatures,
            String timePatternDesc,
            boolean tophusPresent,
            String urateDesc,
            boolean synovialFluidNegative,
            boolean imagingPositive,
            boolean erosionPositive,
            int score) {

        StringBuilder summary = new StringBuilder();
        summary.append("--- 통풍(Gout) 진단 평가 상세 내역 ---\n");

        if (msuConfirmed) {
            summary.append("[확정적 기준] 증상 관절/점액낭에서 요산 결정(MSU) 확인됨\n");
            summary.append("\n최종 판정: 확정적 통풍 (Sufficient Criterion 충족)");
            return summary.toString();
        }

        summary.append(String.format("- 관절 침범: %s\n", jointDesc));
        summary.append(String.format("- 임상 특징(발적, 압통, 보행장애): %d개 특징 관찰\n", clinicalFeatures));
        summary.append(String.format("- 발작 양상: %s\n", timePatternDesc));

        if (tophusPresent) {
            summary.append("- 통풍 결절(Tophus): 존재 (+4점)\n");
        }

        summary.append(String.format("- 혈청 요산 농도: %s\n", urateDesc));

        if (synovialFluidNegative) {
            summary.append("- 관절액 검사: MSU 미검출 (-2점)\n");
        }

        if (imagingPositive) {
            summary.append("- 영상(US/DECT): 요산 침착 확인 (+4점)\n");
        }

        if (erosionPositive) {
            summary.append("- 영상(X-ray): 골미란 확인 (+4점)\n");
        }

        String diagnosis = isClassified(score) ? "[통풍 분류 가능 (Gout Classified)]" : "[통풍 아님 (Not Classified)]";
        summary.append("\n-----------------------------------\n");
        summary.append(String.format("총점: %d점\n최종 판정: %s", score, diagnosis));

        return summary.toString();
    }
}
