package org.delcom.ifs25022pbo2026springbootcashflow.config;

import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.communication.IndexHtmlRequestListener;
import com.vaadin.flow.server.communication.IndexHtmlResponse;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BootstrapPageConfigTest {

    private static final String EMPTY_SHELL =
            "<!doctype html><html lang=\"en\"><head></head><body><div id=\"outlet\"></div></body></html>";

    private final BootstrapPageConfig config = new BootstrapPageConfig();

    @Test
    void enhanceAddsTitleLandmarkHeadingAndStyles() {
        Document document = Jsoup.parse(EMPTY_SHELL);

        config.enhance(document);

        assertEquals("Cashflow", document.title());

        Element main = document.body().child(0);
        assertEquals("main", main.tagName());
        assertEquals("cashflow-boot", main.id());
        assertEquals("Cashflow - Catatan Keuangan", main.child(0).text());
        assertEquals("h1", main.child(0).tagName());
        assertEquals("outlet", document.body().child(1).id());

        // text() tidak mengembalikan isi <style> di jsoup, jadi periksa HTML-nya.
        String css = document.head().select("style").outerHtml();
        assertTrue(css.contains("vaadin-connection-indicator[loading]{display:none !important}"));
        assertTrue(css.contains("#cashflow-boot"));
        assertTrue(css.contains("[role='main']"));
    }

    @Test
    void enhanceReplacesExistingEmptyTitle() {
        Document document = Jsoup.parse("<html><head><title></title></head><body></body></html>");

        config.enhance(document);

        assertEquals("Cashflow", document.title());
        assertEquals(1, document.select("title").size());
    }

    @Test
    void serviceInitRegistersListenerThatEnhancesIndexHtml() {
        ServiceInitEvent event = mock(ServiceInitEvent.class);

        config.serviceInit(event);

        ArgumentCaptor<IndexHtmlRequestListener> captor = ArgumentCaptor.forClass(IndexHtmlRequestListener.class);
        verify(event).addIndexHtmlRequestListener(captor.capture());

        Document document = Jsoup.parse(EMPTY_SHELL);
        IndexHtmlResponse response = mock(IndexHtmlResponse.class);
        when(response.getDocument()).thenReturn(document);
        captor.getValue().modifyIndexHtmlResponse(response);

        assertNotNull(document.selectFirst("main#cashflow-boot"));
        assertEquals("Cashflow", document.title());
    }
}
