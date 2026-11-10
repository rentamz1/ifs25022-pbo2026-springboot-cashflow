package org.delcom.ifs25022pbo2026springbootcashflow;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Meta;

/**
 * Konfigurasi halaman induk (index.html) aplikasi Vaadin.
 * Meta description dibutuhkan oleh audit SEO Lighthouse ("Document does not have a meta description").
 */
@Meta(name = "description",
        content = "Cashflow: aplikasi web pencatat keuangan pribadi untuk mencatat pemasukan dan pengeluaran, "
                + "melihat saldo, serta mencari dan mengurutkan transaksi.")
public class AppShell implements AppShellConfigurator {
}
