package com.emr.gds.features.ReferenceFile;

import com.emr.gds.features.ReferenceFile.persistence.ReferenceRepository;
import com.emr.gds.features.ReferenceFile.application.ReferenceService;
import com.emr.gds.features.ReferenceFile.application.ReferenceItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Reference 검색/필터 테스트")
class ReferenceServiceSearchTest {

    private ReferenceService service;
    private MockReferenceRepository mockRepository;

    @BeforeEach
    void setUp() {
        mockRepository = new MockReferenceRepository();
        service = new ReferenceService(mockRepository);
    }

    // ============================
    // 검색 기능 테스트
    // ============================

    @Test
    @DisplayName("모든 Reference 항목 조회")
    void testFindAllReferences() {
        mockRepository.addItem(new ReferenceItem(1, "Medical", "Hypertension", "/db/ref"));
        mockRepository.addItem(new ReferenceItem(2, "Medical", "Diabetes", "/db/ref"));

        var results = service.findAllReferences();

        assertNotNull(results);
        assertEquals(2, results.size());
    }

    @Test
    @DisplayName("빈 Reference 목록")
    void testFindAllReferencesEmpty() {
        var results = service.findAllReferences();

        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    // ============================
    // 카테고리 관련 테스트
    // ============================

    @Test
    @DisplayName("모든 카테고리 조회")
    void testFindDistinctCategories() {
        mockRepository.addItem(new ReferenceItem(1, "Medical", "HTN", "/db/ref"));
        mockRepository.addItem(new ReferenceItem(2, "Medications", "Metformin", "/db/ref"));
        mockRepository.addItem(new ReferenceItem(3, "Medical", "DM", "/db/ref"));

        List<String> results = service.findDistinctCategories();

        assertNotNull(results);
        assertEquals(2, results.size());
        assertTrue(results.contains("Medical"));
        assertTrue(results.contains("Medications"));
    }

    @Test
    @DisplayName("카테고리 조회 - 중복 제거")
    void testFindDistinctCategoriesRemovesDuplicates() {
        mockRepository.addItem(new ReferenceItem(1, "Medical", "Item1", "/db/ref"));
        mockRepository.addItem(new ReferenceItem(2, "Medical", "Item2", "/db/ref"));
        mockRepository.addItem(new ReferenceItem(3, "Medical", "Item3", "/db/ref"));

        List<String> results = service.findDistinctCategories();

        // 같은 카테고리 항목이 3개여도 카테고리는 1개만
        assertEquals(1, results.size());
        assertEquals("Medical", results.get(0));
    }

    // ============================
    // 조회 테스트
    // ============================

    @Test
    @DisplayName("카테고리와 내용으로 Reference 찾기")
    void testFindByCategoryAndContents() {
        mockRepository.addItem(new ReferenceItem(1, "Medical", "Hypertension", "/db/ref"));

        var result = service.findByCategoryAndContents("Medical", "Hypertension");

        assertTrue(result.isPresent());
        assertEquals("Hypertension", result.get().getContents());
    }

    @Test
    @DisplayName("없는 Reference 찾기")
    void testFindByCategoryAndContentsNotFound() {
        var result = service.findByCategoryAndContents("Medical", "Not Exist");

        assertFalse(result.isPresent());
    }

    // ============================
    // 검색 테스트
    // ============================

    @Test
    @DisplayName("키워드로 Reference 검색")
    void testSearchWithKeyword() {
        mockRepository.addItem(new ReferenceItem(1, "Medical", "Type 2 Diabetes", "/db/ref"));
        mockRepository.addItem(new ReferenceItem(2, "Medical", "Diabetes Management", "/db/ref"));

        List<ReferenceItem> results = service.search("Diabetes", "Medical");

        assertNotNull(results);
        assertEquals(2, results.size());
    }

    @Test
    @DisplayName("검색 결과 없음")
    void testSearchWithNoResults() {
        mockRepository.addItem(new ReferenceItem(1, "Medical", "Hypertension", "/db/ref"));

        List<ReferenceItem> results = service.search("NonExistent", "Medical");

        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    // ============================
    // Mock Repository
    // ============================

    static class MockReferenceRepository implements ReferenceRepository {
        private java.util.Map<Integer, ReferenceItem> storage = new java.util.HashMap<>();
        private int nextId = 1;

        void addItem(ReferenceItem item) {
            storage.put(item.getId(), item);
        }

        @Override
        public ReferenceItem save(ReferenceItem item) {
            if (item.getId() == 0) {
                item.setId(nextId++);
            }
            storage.put(item.getId(), item);
            return item;
        }

        @Override
        public void delete(ReferenceItem item) {
            storage.remove(item.getId());
        }

        @Override
        public java.util.List<ReferenceItem> findAll() {
            return new java.util.ArrayList<>(storage.values());
        }

        @Override
        public java.util.Optional<ReferenceItem> findById(int id) {
            return java.util.Optional.ofNullable(storage.get(id));
        }

        @Override
        public java.util.Optional<ReferenceItem> findByCategoryAndContents(String category, String contents) {
            return storage.values().stream()
                .filter(item -> item.getCategory().equals(category) && item.getContents().equals(contents))
                .findFirst();
        }

        @Override
        public boolean existsByCategoryAndContents(String category, String contents, int excludeId) {
            return storage.values().stream()
                .anyMatch(item -> item.getId() != excludeId &&
                                 item.getCategory().equals(category) &&
                                 item.getContents().equals(contents));
        }

        @Override
        public java.util.List<String> findDistinctCategories() {
            return storage.values().stream()
                .map(ReferenceItem::getCategory)
                .distinct()
                .toList();
        }

        @Override
        public java.util.List<ReferenceItem> search(String query, String category) {
            return storage.values().stream()
                .filter(item -> (category == null || item.getCategory().equals(category)) &&
                               (query == null || item.getContents().contains(query)))
                .toList();
        }
    }
}
