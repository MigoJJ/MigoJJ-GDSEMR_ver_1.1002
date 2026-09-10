package com.emr.gds.features.medication.domain;

import java.util.List;

public record MedicationGroup(String title, List<MedicationItem> medications) {}
