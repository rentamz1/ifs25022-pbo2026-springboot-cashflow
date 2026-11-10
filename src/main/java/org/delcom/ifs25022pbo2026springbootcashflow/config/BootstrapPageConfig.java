package org.delcom.ifs25022pbo2026springbootcashflow.config;

import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

/**
 * Melengkapi HTML awal (index.html) yang dikirim server sebelum JavaScript Vaadin berjalan.
 * <p>
 * Pada mode default, Vaadin mengirim halaman kosong lalu merender view lewat JavaScript. Pemeriksa
 * aksesibilitas (Axe) yang berjalan sebelum render selesai hanya melihat halaman kosong itu, sehingga melaporkan:
 * tidak ada {@code <title>}, tidak ada landmark {@code main}, tidak ada {@code h1}, dan konten di luar landmark.
 * Konfigurasi ini menambahkan semuanya ke HTML awal.
 */
@Component
public class BootstrapPageConfig implements VaadinServiceInitListener {

    static final String TITLE = "Cashflow";
    static final String HEADING = "Cashflow - Catatan Keuangan";
    static final String PLACEHOLDER_ID = "cashflow-boot";

    /**
     * 1) Placeholder berupa lapisan tetap (position:fixed) berisi judul, langsung terlihat saat HTML pertama
     *    dimuat sehingga FCP/Speed Index tidak menunggu JavaScript Vaadin. Karena fixed, ia tidak menggeser
     *    layout (CLS tetap 0).
     * 2) Setelah view dirender (ada vaadin-vertical-layout[role=main]), placeholder dimatikan agar hanya ada
     *    satu landmark main dan satu h1.
     * 3) Bar "loading" bawaan Vaadin ditambahkan ke body di luar landmark; selama loading disembunyikan. Perlu
     *    !important karena Vaadin menyuntikkan style sendiri (vaadin-connection-indicator[popover]{display:contents})
     *    ke head setelah style ini.
     */
    static final String CSS =
            "#" + PLACEHOLDER_ID + "{position:fixed;top:0;left:0;right:0;bottom:0;z-index:1000;box-sizing:border-box;"
                    + "padding:1rem 1.5rem;background:#fff;color:#1f2937;font-family:system-ui,sans-serif}"
                    + "#" + PLACEHOLDER_ID + " h1{margin:0;font-size:2rem;line-height:1.2}"
                    + "body:has(vaadin-vertical-layout[role='main']) #" + PLACEHOLDER_ID + "{display:none}"
                    + "vaadin-connection-indicator[loading]{display:none !important}";

    @Override
    public void serviceInit(ServiceInitEvent event) {
        event.addIndexHtmlRequestListener(response -> enhance(response.getDocument()));
    }

    /** Memodifikasi DOM index.html di memori sebelum dikirim ke browser. */
    void enhance(Document document) {
        document.title(TITLE);
        document.head().appendElement("style").text(CSS);
        document.body().prependElement("main").id(PLACEHOLDER_ID)
                .appendElement("h1").text(HEADING);
    }
}
