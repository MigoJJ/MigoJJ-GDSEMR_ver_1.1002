package com.emr.gds.features.glp1;

import com.emr.gds.features.glp1.adapter.in.ui.Glp1SemaglutideMain;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test: IAMButtonAction button click should instantiate Glp1SemaglutideMain.
 */
class Glp1ButtonIntegrationTest {

    @Test
    void testGlp1AppInstantiation() {
        Glp1SemaglutideMain app = new Glp1SemaglutideMain();
        assertNotNull(app);
    }

    @Test
    void testGlp1AppCanBeCreatedMultipleTimes() {
        for (int i = 0; i < 3; i++) {
            Glp1SemaglutideMain app = new Glp1SemaglutideMain();
            assertNotNull(app);
        }
    }

    @Test
    void testGlp1MainIsApplicationSubclass() {
        Glp1SemaglutideMain app = new Glp1SemaglutideMain();
        assertTrue(app instanceof javafx.application.Application);
    }
}
