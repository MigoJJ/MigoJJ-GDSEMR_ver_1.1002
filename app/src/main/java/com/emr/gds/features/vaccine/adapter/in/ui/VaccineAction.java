package com.emr.gds.features.vaccine.adapter.in.ui;

import com.emr.gds.infrastructure.service.EmrBridgeService;
import com.emr.gds.features.vaccine.application.VaccineService;

public class VaccineAction {

    private static VaccineController controller;

    public static void open() {
        if (controller == null) {
            controller = new VaccineController(new VaccineService(new EmrBridgeService()));
        }
        controller.show();
    }
}
