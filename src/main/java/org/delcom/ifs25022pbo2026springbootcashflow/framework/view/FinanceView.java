package org.delcom.ifs25022pbo2026springbootcashflow.framework.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import org.delcom.ifs25022pbo2026springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25022pbo2026springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25022pbo2026springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25022pbo2026springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25022pbo2026springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25022pbo2026springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25022pbo2026springbootcashflow.usecase.FinanceUseCase;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

/**
 * Tampilan web Cashflow (Vaadin).
 * View hanya merangkai komponen dan memanggil use case; semua aturan bisnis ada di domain/use case,
 * semua format teks ada di presenter. Exception domain ditangkap di sini dan dijadikan Notification.
 * Double-klik baris untuk mengubah transaksi.
 * <p>
 * Halaman ini juga dilayani di {@code /portofolio} (alias) karena path itu yang dikunjungi crawler audit.
 * Tanpa route, Vaadin menjawab HTTP 200 dengan halaman "not found" yang tidak punya title, h1, maupun main.
 */
@Route("")
@RouteAlias("portofolio")
@PageTitle("Cashflow")
public class FinanceView extends VerticalLayout {

    private final FinanceUseCase financeUseCase;
    private final FinancePresenter presenter;

    private final Grid<Transaction> grid = new Grid<>(Transaction.class, false);
    private final TextField searchField = new TextField();
    private final ComboBox<SortOption> sortBox = new ComboBox<>();
    private final Paragraph statusLabel = new Paragraph();
    private final Span emptyState = new Span();

    private final Span totalCount = new Span("0");
    private final Span incomeTotal = new Span();
    private final Span expenseTotal = new Span();
    private final Span balanceValue = new Span();

    /** True saat dropdown urut dikosongkan oleh kode (bukan oleh pengguna), agar listener tidak bereaksi. */
    private boolean programmaticSortChange = false;

    public FinanceView(FinanceUseCase financeUseCase, FinancePresenter presenter) {
        this.financeUseCase = financeUseCase;
        this.presenter = presenter;

        setSizeFull();
        setPadding(true);
        setSpacing(true);
        // Landmark "main" untuk aksesibilitas: seluruh konten halaman berada di dalam satu landmark utama.
        getElement().setAttribute("role", "main");

        configureGrid();
        configureToolbar();

        add(new H1("Cashflow - Catatan Keuangan"), buildSummaryCards(), buildToolbar(), statusLabel, grid);
        refreshAll();
    }

    // ------------------------------------------------------------------ ringkasan

    private HorizontalLayout buildSummaryCards() {
        HorizontalLayout summary = new HorizontalLayout(
                // Warna aksen juga dipakai sebagai warna teks judul kartu, jadi kontrasnya terhadap latar kartu
                // harus >= 4.5:1 (WCAG AA): biru 7.15, hijau 6.49, merah 6.80, ungu 5.98.
                createSummaryCard("Total Transaksi", totalCount, "#DBEAFE", "#1E40AF"),
                createSummaryCard("Total Pemasukan", incomeTotal, "#DCFCE7", "#166534"),
                createSummaryCard("Total Pengeluaran", expenseTotal, "#FEE2E2", "#991B1B"),
                createSummaryCard("Saldo", balanceValue, "#EDE9FE", "#6D28D9")
        );
        summary.setWidthFull();
        summary.setSpacing(true);
        summary.getStyle().set("align-items", "stretch");
        return summary;
    }

    private Div createSummaryCard(String label, Span value, String background, String accent) {
        Span caption = new Span(label);
        caption.getStyle()
                .set("display", "block")
                .set("font-size", "0.875rem")
                .set("font-weight", "500")
                .set("color", accent);

        value.getStyle()
                .set("display", "block")
                .set("margin-top", "0.35rem")
                .set("font-size", "1.5rem")
                .set("font-weight", "700")
                .set("line-height", "1.2");

        Div card = new Div(caption, value);
        card.getStyle()
                .set("flex", "1")
                .set("min-width", "0")
                .set("background", background)
                .set("border", "1px solid " + accent)
                .set("border-left", "6px solid " + accent)
                .set("border-radius", "12px")
                .set("padding", "1rem 1.1rem")
                .set("box-sizing", "border-box");
        return card;
    }

    private void updateSummary() {
        BigDecimal balance = financeUseCase.getBalance();
        totalCount.setText(String.valueOf(financeUseCase.getAllTransactions().size()));
        incomeTotal.setText(presenter.formatMoney(financeUseCase.getTotalIncome()));
        expenseTotal.setText(presenter.formatMoney(financeUseCase.getTotalExpense()));
        balanceValue.setText(presenter.formatMoney(balance));
        // Saldo negatif ditandai merah agar langsung terlihat.
        balanceValue.getStyle().set("color", balance.signum() < 0 ? "#B91C1C" : "#4C1D95");
    }

    // ------------------------------------------------------------------ grid

    private void configureGrid() {
        grid.addColumn(Transaction::getId)
                .setHeader("ID")
                .setKey("id")
                .setAutoWidth(true)
                .setFlexGrow(0)
                .setSortable(true)
                .setComparator(Transaction::getId);

        grid.addColumn(Transaction::getDescription)
                .setHeader("Keterangan")
                .setKey("description")
                .setFlexGrow(1)
                .setSortable(true)
                .setComparator(Comparator.comparing(Transaction::getDescription, String.CASE_INSENSITIVE_ORDER));

        // Pengurutan kolom memakai comparator dari domain, bukan logika di view.
        grid.addColumn(this::amountText)
                .setHeader("Jumlah")
                .setKey("amount")
                .setAutoWidth(true)
                .setSortable(true)
                .setComparator(SortOption.AMOUNT_ASC.comparator());

        grid.addComponentColumn(this::createTypeBadge)
                .setHeader("Tipe")
                .setKey("type")
                .setAutoWidth(true)
                .setFlexGrow(0)
                .setSortable(true)
                .setComparator(SortOption.INCOME_FIRST.comparator());

        grid.addColumn(this::createdAtText)
                .setHeader("Waktu")
                .setKey("createdAt")
                .setAutoWidth(true)
                .setSortable(true)
                .setComparator(Comparator.comparing(Transaction::getCreatedAt));

        grid.addComponentColumn(this::createActionButtons)
                .setHeader("Tindakan")
                .setKey("actions")
                .setAutoWidth(true)
                .setFlexGrow(0)
                .setSortable(false);

        grid.setEmptyStateComponent(emptyState);
        grid.addItemDoubleClickListener(event -> openEditDialog(event.getItem()));
        grid.setSizeFull();
    }

    /** Teks kolom Jumlah. Package-private agar bisa diuji langsung. */
    String amountText(Transaction transaction) {
        return presenter.formatMoney(transaction.getAmount());
    }

    /** Teks kolom Waktu. Package-private agar bisa diuji langsung. */
    String createdAtText(Transaction transaction) {
        return presenter.formatCreatedAt(transaction);
    }

    private Span createTypeBadge(Transaction transaction) {
        Span badge = new Span(presenter.formatType(transaction.getType()));
        badge.getElement().getThemeList().add("badge");
        badge.getElement().getThemeList().add(transaction.isIncome() ? "success" : "error");
        return badge;
    }

    private HorizontalLayout createActionButtons(Transaction transaction) {
        Button deleteButton = new Button("Hapus", event -> openDeleteDialog(transaction));
        deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY_INLINE);
        return new HorizontalLayout(deleteButton);
    }

    // ------------------------------------------------------------------ toolbar

    private void configureToolbar() {
        searchField.setPlaceholder("Cari keterangan...");
        searchField.setClearButtonVisible(true);

        sortBox.setPlaceholder("Urutkan...");
        sortBox.setItems(SortOption.values());
        sortBox.setItemLabelGenerator(presenter::formatSortOption);
        sortBox.setClearButtonVisible(true);
        sortBox.addValueChangeListener(event -> {
            // Pengosongan dari kode (mis. saat mencari atau muat ulang) tidak perlu diproses.
            if (programmaticSortChange) {
                return;
            }
            if (event.getValue() == null) {
                refreshAll();
            } else {
                sortTransactions(event.getValue());
            }
        });
    }

    /** Mengosongkan dropdown urut tanpa memicu listener-nya. */
    private void clearSortBox() {
        programmaticSortChange = true;
        sortBox.clear();
        programmaticSortChange = false;
    }

    private HorizontalLayout buildToolbar() {
        Button searchButton = new Button("Cari", event -> searchTransactions());
        Button refreshButton = new Button("Muat Ulang", event -> refreshAll());

        Button incomeButton = new Button("Tambah Pemasukan", event -> openAddDialog(TransactionType.INCOME));
        incomeButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);

        Button expenseButton = new Button("Tambah Pengeluaran", event -> openAddDialog(TransactionType.EXPENSE));
        expenseButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);

        HorizontalLayout toolbar = new HorizontalLayout(
                searchField, searchButton, sortBox, refreshButton, incomeButton, expenseButton
        );
        toolbar.setWidthFull();
        toolbar.setFlexGrow(1, searchField);
        return toolbar;
    }

    // ------------------------------------------------------------------ dialog

    private void openAddDialog(TransactionType type) {
        boolean income = type == TransactionType.INCOME;

        TextField descriptionField = new TextField("Keterangan");
        descriptionField.setPlaceholder("contoh: Gaji bulanan");
        descriptionField.setWidthFull();
        TextField amountField = new TextField("Jumlah (Rp)");
        amountField.setPlaceholder("contoh: 1500000");
        amountField.setWidthFull();

        Dialog dialog = new Dialog();
        Button saveButton = new Button("Simpan", event -> {
            try {
                Transaction created = income
                        ? financeUseCase.addIncome(descriptionField.getValue(), amountField.getValue())
                        : financeUseCase.addExpense(descriptionField.getValue(), amountField.getValue());
                showSuccess(income
                        ? presenter.showAddIncomeSuccess(created)
                        : presenter.showAddExpenseSuccess(created));
                dialog.close();
                refreshAll();
            } catch (ValidationException ex) {
                showError(presenter.showValidationError(ex.getMessage()));
            }
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        buildDialog(dialog, income ? "Tambah Pemasukan" : "Tambah Pengeluaran",
                formOf(descriptionField, amountField), saveButton);
        dialog.open();
    }

    private void openEditDialog(Transaction transaction) {
        TextField descriptionField = new TextField("Keterangan");
        descriptionField.setValue(transaction.getDescription());
        descriptionField.setPlaceholder("contoh: Gaji bulanan");
        descriptionField.setWidthFull();
        TextField amountField = new TextField("Jumlah (Rp)");
        amountField.setValue(transaction.getAmount().toPlainString());
        amountField.setPlaceholder("contoh: 1500000");
        amountField.setWidthFull();

        Dialog dialog = new Dialog();
        Button saveButton = new Button("Simpan", event -> {
            try {
                financeUseCase.updateTransaction(
                        transaction.getId(), descriptionField.getValue(), amountField.getValue());
                showSuccess(presenter.showUpdateSuccess());
                dialog.close();
                refreshAll();
            } catch (EntityNotFoundException ex) {
                showError(presenter.showUpdateFailed(ex.getId()));
            } catch (ValidationException ex) {
                showError(presenter.showValidationError(ex.getMessage()));
            }
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        buildDialog(dialog, "Mengubah Transaksi #" + transaction.getId(),
                formOf(descriptionField, amountField), saveButton);
        dialog.open();
    }

    private VerticalLayout formOf(TextField descriptionField, TextField amountField) {
        VerticalLayout form = new VerticalLayout(descriptionField, amountField);
        form.setPadding(false);
        return form;
    }

    private void openDeleteDialog(Transaction transaction) {
        Paragraph message = new Paragraph("Yakin ingin menghapus " + presenter.format(transaction) + "?");

        Dialog dialog = new Dialog();
        Button confirmButton = new Button("Hapus", event -> {
            deleteTransaction(transaction);
            dialog.close();
        });
        confirmButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);

        buildDialog(dialog, "Menghapus Transaksi", message, confirmButton);
        dialog.open();
    }

    /** Merakit dialog standar: judul, isi, lalu tombol Batal + aksi utama, semuanya di badan dialog. */
    private void buildDialog(Dialog dialog, String title, com.vaadin.flow.component.Component body,
                             Button actionButton) {
        Button cancelButton = new Button("Batal", event -> dialog.close());

        HorizontalLayout buttons = new HorizontalLayout(cancelButton, actionButton);
        buttons.setWidthFull();
        buttons.getStyle().set("justify-content", "flex-end");

        VerticalLayout content = new VerticalLayout(new H3(title), body, buttons);
        content.setPadding(false);

        dialog.setWidth("450px");
        dialog.setMaxWidth("95vw");
        dialog.add(content);
    }

    // ------------------------------------------------------------------ aksi

    private void deleteTransaction(Transaction transaction) {
        try {
            financeUseCase.removeTransaction(transaction.getId());
            showSuccess(presenter.showRemoveSuccess());
            refreshAll();
        } catch (EntityNotFoundException ex) {
            showError(presenter.showRemoveFailed(ex.getId()));
        }
    }

    private void searchTransactions() {
        String keyword = searchField.getValue();
        if (keyword.isBlank()) {
            refreshAll();
            return;
        }
        List<Transaction> results = financeUseCase.searchTransactions(keyword);
        clearSortBox();
        emptyState.setText(results.isEmpty() ? presenter.showNoSearchResult() : presenter.showNoData());
        refreshGrid(results, "Hasil Pencarian: \"" + keyword.trim() + "\"");
    }

    private void sortTransactions(SortOption option) {
        try {
            List<Transaction> sorted = financeUseCase.sortTransactions(option);
            searchField.clear();
            emptyState.setText(presenter.showNoData());
            refreshGrid(sorted, "Daftar Transaksi (" + presenter.formatSortOption(option) + ")");
        } catch (ValidationException ex) {
            showError(presenter.showValidationError(ex.getMessage()));
        }
    }

    private void refreshAll() {
        emptyState.setText(presenter.showNoData());
        searchField.clear();
        clearSortBox();
        refreshGrid(financeUseCase.getAllTransactions(), "Daftar Transaksi");
    }

    private void refreshGrid(List<Transaction> transactions, String title) {
        grid.setItems(transactions);
        updateSummary();
        statusLabel.setText(title + " - " + presenter.formatBalance(financeUseCase.getBalance()));
    }

    private void showSuccess(String message) {
        Notification notification = Notification.show(message, 3000, Notification.Position.BOTTOM_START);
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    private void showError(String message) {
        Notification notification = Notification.show(message, 4000, Notification.Position.BOTTOM_START);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}
