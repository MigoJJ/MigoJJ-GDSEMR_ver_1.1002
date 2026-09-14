package com.emr.gds.features.ReferenceFile.application;

import javafx.beans.property.SimpleStringProperty;

public class ReferenceItem {
    private int id;
    private final SimpleStringProperty category;
    private final SimpleStringProperty contents;
    private final SimpleStringProperty directoryPath;

    public ReferenceItem() {
        this(0, "", "", "");
    }

    public ReferenceItem(String category, String contents, String directoryPath) {
        this(0, category, contents, directoryPath);
    }

    public ReferenceItem(int id, String category, String contents, String directoryPath) {
        this.id = id;
        this.category = new SimpleStringProperty(category);
        this.contents = new SimpleStringProperty(contents);
        this.directoryPath = new SimpleStringProperty(directoryPath);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCategory() {
        return category.get();
    }

    public SimpleStringProperty categoryProperty() {
        return category;
    }

    public void setCategory(String category) {
        this.category.set(category);
    }

    public String getContents() {
        return contents.get();
    }

    public SimpleStringProperty contentsProperty() {
        return contents;
    }

    public void setContents(String contents) {
        this.contents.set(contents);
    }

    public String getDirectoryPath() {
        return directoryPath.get();
    }

    public SimpleStringProperty directoryPathProperty() {
        return directoryPath;
    }

    public void setDirectoryPath(String directoryPath) {
        this.directoryPath.set(directoryPath);
    }
}
