package org.delcom.ifs25022pbo2026springbootcashflow;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Memastikan file statis untuk SEO dan favicon tersedia dan valid. */
class StaticResourcesTest {

    private byte[] read(String path) throws IOException {
        try (InputStream in = getClass().getResourceAsStream(path)) {
            assertNotNull(in, "Resource tidak ditemukan: " + path);
            return in.readAllBytes();
        }
    }

    @Test
    void robotsTxtAllowsCrawlingAndIsPlainText() throws IOException {
        String text = new String(read("/META-INF/resources/robots.txt"), StandardCharsets.UTF_8);

        assertTrue(text.contains("User-agent: *"));
        assertTrue(text.contains("Allow: /"));
        assertFalse(text.toLowerCase().contains("<html"));
    }

    @Test
    void faviconIsAnIcoFileContainingOneImage() throws IOException {
        byte[] ico = read("/META-INF/resources/favicon.ico");

        assertTrue(ico.length > 22);
        assertEquals(0, ico[0]);
        assertEquals(0, ico[1]);
        assertEquals(1, ico[2]);
        assertEquals(0, ico[3]);
        assertEquals(1, ico[4]);
    }
}
