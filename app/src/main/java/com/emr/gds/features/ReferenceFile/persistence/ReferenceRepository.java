package com.emr.gds.features.ReferenceFile.persistence;

import com.emr.gds.features.ReferenceFile.application.ReferenceItem;
import java.util.List;
import java.util.Optional;

public interface ReferenceRepository {
    ReferenceItem save(ReferenceItem item);
    void delete(ReferenceItem item);
    List<ReferenceItem> findAll();
    Optional<ReferenceItem> findById(int id);
    Optional<ReferenceItem> findByCategoryAndContents(String category, String contents);
    boolean existsByCategoryAndContents(String category, String contents, int excludeId);
    List<String> findDistinctCategories();
    List<ReferenceItem> search(String query, String category);
}
