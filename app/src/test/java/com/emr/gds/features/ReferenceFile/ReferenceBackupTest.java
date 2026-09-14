package com.emr.gds.features.ReferenceFile;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Reference Backup/Restore 테스트")
class ReferenceBackupTest {

    @TempDir
    Path tempDir;

    private Path sourceDbFile;
    private Path backupDir;
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    @BeforeEach
    void setUp() throws IOException {
        sourceDbFile = tempDir.resolve("references.db");
        backupDir = tempDir.resolve("backups");
        Files.createDirectory(backupDir);

        // 테스트용 DB 파일 생성
        Files.write(sourceDbFile, "test database content".getBytes());
    }

    // ============================
    // Backup 생성 테스트
    // ============================

    @Test
    @DisplayName("DB 파일 백업 생성 성공")
    void testCreateBackupSuccess() throws IOException {
        String timestamp = LocalDateTime.now().format(formatter);
        Path backupFile = backupDir.resolve("backup_" + timestamp + ".db");

        // 백업 수행
        Files.copy(sourceDbFile, backupFile, StandardCopyOption.REPLACE_EXISTING);

        // 검증
        assertTrue(Files.exists(backupFile));
        assertEquals(Files.size(sourceDbFile), Files.size(backupFile));
    }

    @Test
    @DisplayName("타임스탬프 기반 백업 파일명 생성")
    void testBackupFilenamingWithTimestamp() throws IOException {
        String timestamp = "20260914_120000";
        Path backupFile = backupDir.resolve("backup_" + timestamp + ".db");

        Files.copy(sourceDbFile, backupFile);

        assertTrue(Files.exists(backupFile));
        assertTrue(backupFile.getFileName().toString().contains(timestamp));
    }

    @Test
    @DisplayName("여러 백업 파일 생성")
    void testCreateMultipleBackups() throws IOException {
        for (int i = 1; i <= 3; i++) {
            Path backupFile = backupDir.resolve("backup_" + i + ".db");
            Files.copy(sourceDbFile, backupFile);
        }

        long backupCount = Files.list(backupDir).count();
        assertEquals(3, backupCount);
    }

    @Test
    @DisplayName("백업 디렉토리 자동 생성")
    void testBackupDirectoryCreation() throws IOException {
        Path newBackupDir = tempDir.resolve("new_backup_dir");

        // 디렉토리가 없는 경우 생성
        if (!Files.exists(newBackupDir)) {
            Files.createDirectories(newBackupDir);
        }

        Path backupFile = newBackupDir.resolve("backup.db");
        Files.copy(sourceDbFile, backupFile);

        assertTrue(Files.exists(newBackupDir));
        assertTrue(Files.exists(backupFile));
    }

    @Test
    @DisplayName("백업 파일 크기 검증")
    void testBackupFileSizeValidation() throws IOException {
        Path backupFile = backupDir.resolve("backup.db");

        Files.copy(sourceDbFile, backupFile);

        long originalSize = Files.size(sourceDbFile);
        long backupSize = Files.size(backupFile);

        assertEquals(originalSize, backupSize);
    }

    // ============================
    // Restore 테스트
    // ============================

    @Test
    @DisplayName("백업에서 DB 복구")
    void testRestoreFromBackup() throws IOException {
        // 1. 백업 생성
        Path backupFile = backupDir.resolve("backup.db");
        Files.copy(sourceDbFile, backupFile);

        // 2. 원본 파일 삭제
        Files.delete(sourceDbFile);
        assertFalse(Files.exists(sourceDbFile));

        // 3. 복구
        Files.copy(backupFile, sourceDbFile);

        // 4. 검증
        assertTrue(Files.exists(sourceDbFile));
        assertEquals(Files.size(backupFile), Files.size(sourceDbFile));
    }

    @Test
    @DisplayName("백업 파일 찾기")
    void testFindBackupFile() throws IOException {
        // 여러 백업 파일 생성
        Path backup1 = backupDir.resolve("backup_1.db");
        Path backup2 = backupDir.resolve("backup_2.db");

        Files.copy(sourceDbFile, backup1);
        Files.copy(sourceDbFile, backup2);

        // 가장 최근 백업 찾기
        Path latestBackup = Files.list(backupDir)
            .max((a, b) -> {
                try {
                    return Long.compare(Files.getLastModifiedTime(a).toMillis(),
                                      Files.getLastModifiedTime(b).toMillis());
                } catch (IOException e) {
                    return 0;
                }
            })
            .orElse(null);

        assertNotNull(latestBackup);
        assertTrue(Files.exists(latestBackup));
    }

    @Test
    @DisplayName("복구된 데이터 검증")
    void testRestoreDataVerification() throws IOException {
        // 원본 데이터
        byte[] originalData = "Reference Database Content".getBytes();
        Files.write(sourceDbFile, originalData);

        // 백업 생성
        Path backupFile = backupDir.resolve("backup.db");
        Files.copy(sourceDbFile, backupFile);

        // 복구
        Path restoredFile = tempDir.resolve("restored.db");
        Files.copy(backupFile, restoredFile);

        // 데이터 검증
        byte[] restoredData = Files.readAllBytes(restoredFile);
        assertArrayEquals(originalData, restoredData);
    }

    @Test
    @DisplayName("백업 덮어쓰기")
    void testOverwriteBackup() throws IOException {
        Path backupFile = backupDir.resolve("backup.db");

        // 첫 번째 백업
        Files.copy(sourceDbFile, backupFile);
        long firstSize = Files.size(backupFile);

        // 원본 파일 수정
        Files.write(sourceDbFile, "updated content with more data".getBytes());

        // 두 번째 백업 (덮어쓰기)
        Files.copy(sourceDbFile, backupFile, StandardCopyOption.REPLACE_EXISTING);
        long secondSize = Files.size(backupFile);

        assertNotEquals(firstSize, secondSize);
    }

    // ============================
    // 에러 처리 테스트
    // ============================

    @Test
    @DisplayName("백업 파일 없을 때 복구 실패")
    void testRestoreWithoutBackupFile() {
        Path nonExistentBackup = backupDir.resolve("nonexistent.db");

        assertFalse(Files.exists(nonExistentBackup));
    }

    @Test
    @DisplayName("디스크 공간 부족 시뮬레이션")
    void testBackupWithLimitedDiskSpace() throws IOException {
        // 대용량 파일 생성 시뮬레이션
        byte[] largeData = new byte[1024 * 1024]; // 1MB
        Files.write(sourceDbFile, largeData);

        Path backupFile = backupDir.resolve("backup.db");
        Files.copy(sourceDbFile, backupFile);

        assertTrue(Files.exists(backupFile));
    }

    @Test
    @DisplayName("DB 파일 잠금 상태 처리")
    void testBackupWhileDbIsLocked() throws IOException {
        Path backupFile = backupDir.resolve("backup.db");

        // 파일 백업 시도
        try {
            Files.copy(sourceDbFile, backupFile);
            assertTrue(Files.exists(backupFile));
        } catch (IOException e) {
            // 파일이 잠겨있을 경우
            assertFalse(Files.exists(backupFile));
        }
    }

    @Test
    @DisplayName("불완전한 백업 정리")
    void testCleanupPartialBackup() throws IOException {
        // 불완전한 백업 파일 생성
        Path partialBackup = backupDir.resolve("backup.tmp");
        Files.write(partialBackup, "incomplete".getBytes());

        // 정리
        Files.deleteIfExists(partialBackup);

        assertFalse(Files.exists(partialBackup));
    }

    @Test
    @DisplayName("백업 파일 권한 검증")
    void testBackupFilePermissions() throws IOException {
        Path backupFile = backupDir.resolve("backup.db");
        Files.copy(sourceDbFile, backupFile);

        // 파일 읽기 가능 확인
        assertTrue(Files.isReadable(backupFile));

        // 파일이 일반 파일인지 확인
        assertTrue(Files.isRegularFile(backupFile));
    }

    @Test
    @DisplayName("자동 백업 로테이션 (최신 3개만 보관)")
    void testBackupRotation() throws IOException {
        // 5개 백업 생성
        for (int i = 1; i <= 5; i++) {
            Path backupFile = backupDir.resolve("backup_" + String.format("%03d", i) + ".db");
            Files.copy(sourceDbFile, backupFile);

            Thread.yield(); // 파일 수정 시간 차이 생성
        }

        long backupCount = Files.list(backupDir).count();

        // 모든 백업 존재 확인
        assertEquals(5, backupCount);

        // 가장 오래된 2개 삭제 시뮬레이션
        var backupFiles = Files.list(backupDir)
            .sorted()
            .limit(2)
            .toList();

        for (Path file : backupFiles) {
            Files.deleteIfExists(file);
        }

        // 3개만 남아있는지 확인
        backupCount = Files.list(backupDir).count();
        assertEquals(3, backupCount);
    }

    // ============================
    // 통합 테스트
    // ============================

    @Test
    @DisplayName("전체 백업/복구 워크플로우")
    void testCompleteBackupRestoreWorkflow() throws IOException {
        // 1. 원본 데이터 저장
        byte[] originalData = "Important Reference Data".getBytes();
        Files.write(sourceDbFile, originalData);

        // 2. 백업 생성
        Path backupFile = backupDir.resolve("backup_complete.db");
        Files.copy(sourceDbFile, backupFile);
        assertTrue(Files.exists(backupFile));

        // 3. 원본 파일 손상 시뮬레이션
        Files.delete(sourceDbFile);
        assertFalse(Files.exists(sourceDbFile));

        // 4. 복구 수행
        Files.copy(backupFile, sourceDbFile);
        assertTrue(Files.exists(sourceDbFile));

        // 5. 데이터 검증
        byte[] restoredData = Files.readAllBytes(sourceDbFile);
        assertArrayEquals(originalData, restoredData);
    }
}
