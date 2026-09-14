package com.emr.gds.features.ekg.adapter.in.ui;

import com.emr.gds.features.ekg.application.EkgReportService;
import com.emr.gds.infrastructure.service.EmrBridgeService;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class EkgSimpleReportApp extends Stage {

    private static EkgSimpleReportApp active;

    private final EkgSimpleReportView view;
    private final EkgSimpleReportController controller;

    public EkgSimpleReportApp() {
        setTitle("Simple EKG Interpretation");
        initModality(Modality.NONE);
        view = new EkgSimpleReportView();
        controller = new EkgSimpleReportController(view, new EkgReportService(new EmrBridgeService()));
        setScene(new Scene(view.createContent(), 650, 500));
    }

    public static void open() {
        if (active != null && active.isShowing()) {
            active.toFront();
            return;
        }
        active = new EkgSimpleReportApp();
        active.show();
        active.setOnHidden(e -> active = null);
    }
}
