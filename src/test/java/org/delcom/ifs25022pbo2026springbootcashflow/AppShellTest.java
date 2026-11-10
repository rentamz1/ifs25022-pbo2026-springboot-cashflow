package org.delcom.ifs25022pbo2026springbootcashflow;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Meta;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class AppShellTest {

    @Test
    void appShellIsConfiguratorWithMetaDescription() {
        assertInstanceOf(AppShellConfigurator.class, new AppShell());

        Meta meta = AppShell.class.getAnnotation(Meta.class);

        assertNotNull(meta);
        assertEquals("description", meta.name());
        assertFalse(meta.content().isBlank());
    }
}
