package com.emr.gds.features.clinicalLab.domain;

public class ClinicalLabItem {
    private int id;
    private String category;
    private String testName;
    private String unit;
    private Double maleRangeLow;
    private Double maleRangeHigh;
    private Double femaleRangeLow;
    private Double femaleRangeHigh;
    private String maleReferenceRange;
    private String femaleReferenceRange;
    private String codes;
    private String comments;

    public ClinicalLabItem(int id, String category, String testName, String unit, Double maleRangeLow, Double maleRangeHigh, Double femaleRangeLow, Double femaleRangeHigh, String maleReferenceRange, String femaleReferenceRange, String codes, String comments) {
        this.id = id;
        this.category = category;
        this.testName = testName;
        this.unit = unit;
        this.maleRangeLow = maleRangeLow;
        this.maleRangeHigh = maleRangeHigh;
        this.femaleRangeLow = femaleRangeLow;
        this.femaleRangeHigh = femaleRangeHigh;
        this.maleReferenceRange = maleReferenceRange;
        this.femaleReferenceRange = femaleReferenceRange;
        this.codes = codes;
        this.comments = comments;
    }

    // Getters
    public int getId() { return id; }
    public String getCategory() { return category; }
    public String getTestName() { return testName; }
    public String getUnit() { return unit; }
    public Double getMaleRangeLow() { return maleRangeLow; }
    public Double getMaleRangeHigh() { return maleRangeHigh; }
    public Double getFemaleRangeLow() { return femaleRangeLow; }
    public Double getFemaleRangeHigh() { return femaleRangeHigh; }
    public String getMaleReferenceRange() { return maleReferenceRange; }
    public String getFemaleReferenceRange() { return femaleReferenceRange; }
    public String getCodes() { return codes; }
    public String getComments() { return comments; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setCategory(String category) { this.category = category; }
    public void setTestName(String testName) { this.testName = testName; }
    public void setUnit(String unit) { this.unit = unit; }
    public void setMaleRangeLow(Double maleRangeLow) { this.maleRangeLow = maleRangeLow; }
    public void setMaleRangeHigh(Double maleRangeHigh) { this.maleRangeHigh = maleRangeHigh; }
    public void setFemaleRangeLow(Double femaleRangeLow) { this.femaleRangeLow = femaleRangeLow; }
    public void setFemaleRangeHigh(Double femaleRangeHigh) { this.femaleRangeHigh = femaleRangeHigh; }
    public void setMaleReferenceRange(String maleReferenceRange) { this.maleReferenceRange = maleReferenceRange; }
    public void setFemaleReferenceRange(String femaleReferenceRange) { this.femaleReferenceRange = femaleReferenceRange; }
    public void setCodes(String codes) { this.codes = codes; }
    public void setComments(String comments) { this.comments = comments; }

    @Override
    public String toString() {
        return testName + " (" + unit + ")";
    }
}
