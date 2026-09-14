package com.emr.gds.features.ReferenceFile.application;

import com.emr.gds.features.ReferenceFile.persistence.ReferenceRepository;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;
import java.util.Optional;

public class ReferenceService {

    private final ReferenceRepository referenceRepository;

    public ReferenceService(ReferenceRepository referenceRepository) {
        this.referenceRepository = referenceRepository;
    }

    public ObservableList<ReferenceItem> findAllReferences() {
        List<ReferenceItem> items = referenceRepository.findAll();
        return FXCollections.observableArrayList(items);
    }

    public ReferenceItem saveReference(ReferenceItem item) {
        return referenceRepository.save(item);
    }

    public void deleteReference(ReferenceItem item) {
        referenceRepository.delete(item);
    }

    public Optional<ReferenceItem> findByCategoryAndContents(String category, String contents) {
        return referenceRepository.findByCategoryAndContents(category, contents);
    }

    public boolean existsDuplicate(String category, String contents, int excludeId) {
        return referenceRepository.existsByCategoryAndContents(category, contents, excludeId);
    }

    public List<String> findDistinctCategories() {
        return referenceRepository.findDistinctCategories();
    }

    public List<ReferenceItem> search(String query, String category) {
        return referenceRepository.search(query, category);
    }
}
