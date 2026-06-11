package com.yordanos_bekele.righthand.architecture;

import com.yordanos_bekele.righthand.RightHandApplication;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

class ModulithStructureTest {

    @Test
    void verifiesSpringModulithBoundaries() {
        ApplicationModules.of(RightHandApplication.class).verify();
    }
}
