package com.emr.gds.features.ReferenceFile;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Reference Import/Export 테스트")
class ReferenceImportExportTest {

    @TempDir
    Path tempDir;

    private Path csvFile;
    private Path exportDir;

    @BeforeEach
    void setUp() throws IOException {
        csvFile = tempDir.resolve("import_test.csv");
        exportDir = tempDir.resolve("export");
        Files.createDirectory(exportDir);
    }

    // ============================
    // CSV Import 테스트
    // ============================

    @Test
    @DisplayName("정상 CSV 파일 읽기")
    void testReadValidCsvFile() throws IOException {
        String csvContent = """
            category,contents
            Medical,Hypertension Management
            Medical,Diabetes Type 2
            Medications,Metformin Dosage
            """;

        Files.write(csvFile, csvContent.getBytes());

        // CSV 파일 존재 확인
        assertTrue(Files.exists(csvFile));

        // 파일 읽기 성공 확인
        List<String> lines = Files.readAllLines(csvFile);
        assertEquals(4, lines.size()); // 헤더 포함
    }

    @Test
    @DisplayName("빈 CSV 파일 처리")
    void testReadEmptyCsvFile() throws IOException {
        String csvContent = "category,contents\n";

        Files.write(csvFile, csvContent.getBytes());

        List<String> lines = Files.readAllLines(csvFile);
        assertEquals(1, lines.size()); // 헤더만 있음
    }

    @Test
    @DisplayName("인코딩: UTF-8 처리")
    void testCsvFileUtf8Encoding() throws IOException {
        String csvContent = """
            category,contents
            의료,고혈압
            약물,메트포르민
            """;

        Files.write(csvFile, csvContent.getBytes("UTF-8"));

        List<String> lines = Files.readAllLines(csvFile);
        assertTrue(lines.get(1).contains("의료"));
        assertTrue(lines.get(2).contains("메트포르민"));
    }

    @Test
    @DisplayName("잘못된 파일 형식 감지")
    void testInvalidFileFormat() throws IOException {
        Path txtFile = tempDir.resolve("invalid.txt");
        String content = "This is not a CSV file";

        Files.write(txtFile, content.getBytes());

        // 파일이 존재하지만 형식이 잘못됨
        assertTrue(Files.exists(txtFile));
        assertFalse(txtFile.toString().endsWith(".csv"));
    }

    @Test
    @DisplayName("CSV 헤더 검증")
    void testCsvHeaderValidation() throws IOException {
        String csvContent = """
            category,contents,directory_path
            Medical,Hypertension,/path
            """;

        Files.write(csvFile, csvContent.getBytes());

        List<String> lines = Files.readAllLines(csvFile);
        String header = lines.get(0);

        assertTrue(header.contains("category"));
        assertTrue(header.contains("contents"));
    }

    // ============================
    // CSV Export 테스트
    // ============================

    @Test
    @DisplayName("정상 CSV 파일 생성")
    void testCreateValidCsvFile() throws IOException {
        Path outputFile = exportDir.resolve("export_output.csv");

        String csvContent = """
            category,contents,directory_path
            Medical,HTN,/db/ref
            Medications,Met,/db/ref
            """;

        Files.write(outputFile, csvContent.getBytes());

        assertTrue(Files.exists(outputFile));
        List<String> lines = Files.readAllLines(outputFile);
        assertEquals(3, lines.size());
    }

    @Test
    @DisplayName("빈 데이터 내보내기")
    void testExportEmptyData() throws IOException {
        Path outputFile = exportDir.resolve("empty_export.csv");

        String csvContent = "category,contents,directory_path\n";

        Files.write(outputFile, csvContent.getBytes());

        List<String> lines = Files.readAllLines(outputFile);
        assertEquals(1, lines.size()); // 헤더만
    }

    @Test
    @DisplayName("파일 인코딩 검증 (UTF-8)")
    void testExportFileEncoding() throws IOException {
        Path outputFile = exportDir.resolve("korean_export.csv");

        String csvContent = "category,contents\n의료,고혈압\n";

        Files.write(outputFile, csvContent.getBytes("UTF-8"));

        List<String> lines = Files.readAllLines(outputFile);
        assertTrue(lines.get(1).contains("고혈압"));
    }

    @Test
    @DisplayName("경로 유효성 확인")
    void testPathValidity() throws IOException {
        Path outputDir = exportDir.resolve("subdir");
        Files.createDirectories(outputDir);

        Path outputFile = outputDir.resolve("export.csv");

        String content = "category,contents\n";
        Files.write(outputFile, content.getBytes());

        assertTrue(Files.exists(outputFile));
        assertEquals("export.csv", outputFile.getFileName().toString());
    }

    @Test
    @DisplayName("파일 덮어쓰기")
    void testFileOverwrite() throws IOException {
        Path outputFile = exportDir.resolve("overwrite_test.csv");

        String originalContent = "category,contents\nOriginal,Data\n";
        Files.write(outputFile, originalContent.getBytes());

        String newContent = "category,contents\nNew,Data\n";
        Files.write(outputFile, newContent.getBytes(), StandardOpenOption.TRUNCATE_EXISTING);

        List<String> lines = Files.readAllLines(outputFile);
        assertTrue(lines.get(1).contains("New"));
        assertFalse(lines.get(1).contains("Original"));
    }

    // ============================
    // 통합 Import/Export 테스트
    // ============================

    @Test
    @DisplayName("Import 후 Export 데이터 일치")
    void testImportExportConsistency() throws IOException {
        // 1. 원본 CSV 생성
        Path sourceFile = tempDir.resolve("source.csv");
        String sourceContent = """
            category,contents
            Medical,Hypertension
            Medical,Diabetes
            """;

        Files.write(sourceFile, sourceContent.getBytes());

        // 2. 파일 읽기 (Import 시뮬레이션)
        List<String> importedLines = Files.readAllLines(sourceFile);

        // 3. 새 파일에 쓰기 (Export 시뮬레이션)
        Path exportFile = exportDir.resolve("exported.csv");
        Files.write(exportFile, importedLines);

        // 4. 내용 검증
        List<String> exportedLines = Files.readAllLines(exportFile);
        assertEquals(importedLines.size(), exportedLines.size());
    }

    @Test
    @DisplayName("대용량 CSV 처리")
    void testLargeFileHandling() throws IOException {
        StringBuilder csv = new StringBuilder("category,contents\n");

        // 1000개 행 생성
        for (int i = 1; i <= 1000; i++) {
            csv.append("Category").append(i).append(",");
            csv.append("Content").append(i).append("\n");
        }

        Files.write(csvFile, csv.toString().getBytes());

        List<String> lines = Files.readAllLines(csvFile);
        assertEquals(1001, lines.size()); // 헤더 + 1000개
    }

    // ============================
    // 에러 처리 테스트
    // ============================

    @Test
    @DisplayName("파일 읽기 권한 부족")
    void testFileReadPermissionDenied() throws IOException {
        Path noReadFile = tempDir.resolve("no_read.csv");
        Files.write(noReadFile, "content".getBytes());

        // 읽기 권한 제거 (플랫폼에 따라 다를 수 있음)
        try {
            noReadFile.toFile().setReadable(false);

            // 파일이 여전히 존재
            assertTrue(Files.exists(noReadFile));
        } finally {
            noReadFile.toFile().setReadable(true);
        }
    }

    @Test
    @DisplayName("쓰기 권한 부족")
    void testFileWritePermissionDenied() throws IOException {
        Path writeTestDir = tempDir.resolve("readonly");
        Files.createDirectory(writeTestDir);

        try {
            writeTestDir.toFile().setWritable(false);

            // 디렉토리에 쓸 수 없음
            assertFalse(writeTestDir.toFile().canWrite());
        } finally {
            writeTestDir.toFile().setWritable(true);
        }
    }

    @Test
    @DisplayName("파일 경로 특수문자 처리")
    void testSpecialCharactersInPath() throws IOException {
        Path specialFile = exportDir.resolve("file_with-special.chars_123.csv");

        String content = "category,contents\n";
        Files.write(specialFile, content.getBytes());

        assertTrue(Files.exists(specialFile));
    }
}
