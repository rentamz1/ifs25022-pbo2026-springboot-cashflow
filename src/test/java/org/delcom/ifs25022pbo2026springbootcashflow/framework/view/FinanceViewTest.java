package org.delcom.ifs25022pbo2026springbootcashflow.framework.view;

import com.github.mvysny.kaributesting.v10.MockVaadin;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.SortDirection;
import org.delcom.ifs25022pbo2026springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25022pbo2026springbootcashflow.adapter.repository.TransactionRepository;
import org.delcom.ifs25022pbo2026springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25022pbo2026springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25022pbo2026springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25022pbo2026springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25022pbo2026springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25022pbo2026springbootcashflow.usecase.FinanceUseCase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static com.github.mvysny.kaributesting.v10.GridKt._doubleClickItem;
import static com.github.mvysny.kaributesting.v10.GridKt._getCellComponent;
import static com.github.mvysny.kaributesting.v10.LocatorJ._click;
import static com.github.mvysny.kaributesting.v10.LocatorJ._get;
import static com.github.mvysny.kaributesting.v10.LocatorJ._setValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FinanceViewTest {

    private static final String INCOME_BUTTON = "Tambah Pemasukan";
    private static final String EXPENSE_BUTTON = "Tambah Pengeluaran";

    /** Use case asli yang kegagalannya bisa dipaksa, untuk menguji jalur penanganan exception di view. */
    private static class FlakyUseCase extends FinanceUseCase {
        private boolean failRemove;
        private boolean failUpdate;
        private boolean failSort;

        FlakyUseCase() {
            super(new TransactionRepository());
        }

        @Override
        public void removeTransaction(int id) throws EntityNotFoundException {
            if (failRemove) {
                throw new EntityNotFoundException(id);
            }
            super.removeTransaction(id);
        }

        @Override
        public void updateTransaction(int id, String description, String amountInput)
                throws EntityNotFoundException, ValidationException {
            if (failUpdate) {
                throw new EntityNotFoundException(id);
            }
            super.updateTransaction(id, description, amountInput);
        }

        @Override
        public List<Transaction> sortTransactions(SortOption option) throws ValidationException {
            if (failSort) {
                throw new ValidationException("urutan gagal");
            }
            return super.sortTransactions(option);
        }
    }

    private FlakyUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new FlakyUseCase();
        MockVaadin.setup();
        UI.getCurrent().add(new FinanceView(useCase, new FinancePresenter()));
    }

    @AfterEach
    void tearDown() {
        MockVaadin.tearDown();
    }

    // ------------------------------------------------------------------ tampilan awal

    @Test
    void startsEmptyWithZeroSummary() {
        assertEquals("Daftar Transaksi - Saldo: Rp 0.00", statusText());
        assertEquals(List.of("0", "Rp 0.00", "Rp 0.00", "Rp 0.00"), summaryValues());
        assertEquals(0, grid().getListDataView().getItemCount());
    }

    @Test
    void viewIsExposedAsMainLandmarkWithSingleHeading() {
        FinanceView view = _get(FinanceView.class);

        assertEquals("main", view.getElement().getAttribute("role"));
        assertEquals("Cashflow - Catatan Keuangan", _get(view, H1.class).getText());
    }

    @Test
    void columnTextHelpersFormatAmountAndTime() {
        FinanceView view = _get(FinanceView.class);
        Transaction transaction = new Transaction(9, "Contoh", new BigDecimal("1234.5"),
                TransactionType.INCOME, LocalDateTime.of(2026, 1, 2, 3, 4));

        assertEquals("Rp 1,234.50", view.amountText(transaction));
        assertEquals("02/01/2026 03:04", view.createdAtText(transaction));
    }

    // ------------------------------------------------------------------ tambah

    @Test
    void addIncomeAndExpenseUpdatesGridAndSummary() {
        addTransaction(INCOME_BUTTON, "Gaji", "5000000");
        addTransaction(EXPENSE_BUTTON, "Makan siang", "25000");

        assertEquals(2, useCase.getAllTransactions().size());
        assertEquals(2, grid().getListDataView().getItemCount());
        assertEquals("Daftar Transaksi - Saldo: Rp 4,975,000.00", statusText());
        assertEquals(List.of("2", "Rp 5,000,000.00", "Rp 25,000.00", "Rp 4,975,000.00"), summaryValues());
    }

    @Test
    void typeColumnShowsColoredBadges() {
        addTransaction(INCOME_BUTTON, "Gaji", "5000000");
        addTransaction(EXPENSE_BUTTON, "Makan siang", "25000");
        Grid<Transaction> grid = grid();

        Span incomeBadge = (Span) _getCellComponent(grid, indexOfDescription(grid, "Gaji"), "type");
        Span expenseBadge = (Span) _getCellComponent(grid, indexOfDescription(grid, "Makan siang"), "type");

        assertEquals("Pemasukan", incomeBadge.getText());
        assertTrue(incomeBadge.getElement().getThemeList().contains("success"));
        assertEquals("Pengeluaran", expenseBadge.getText());
        assertTrue(expenseBadge.getElement().getThemeList().contains("error"));
    }

    @Test
    void invalidInputKeepsAddDialogOpenAndAddsNothing() {
        clickButton(INCOME_BUTTON);
        Dialog dialog = _get(Dialog.class);

        clickDialogButton(dialog, "Simpan");
        fillDialog(dialog, "Gaji", "");
        clickDialogButton(dialog, "Simpan");
        fillDialog(dialog, "Gaji", "abc");
        clickDialogButton(dialog, "Simpan");
        fillDialog(dialog, "Gaji", "-5");
        clickDialogButton(dialog, "Simpan");
        fillDialog(dialog, "   ", "100");
        clickDialogButton(dialog, "Simpan");

        assertTrue(useCase.getAllTransactions().isEmpty());
        assertEquals(dialog, _get(Dialog.class));
        clickDialogButton(dialog, "Batal");

        clickButton(EXPENSE_BUTTON);
        Dialog expenseDialog = _get(Dialog.class);
        fillDialog(expenseDialog, "Kopi", "nol");
        clickDialogButton(expenseDialog, "Simpan");
        assertTrue(useCase.getAllTransactions().isEmpty());
        clickDialogButton(expenseDialog, "Batal");
    }

    @Test
    void negativeBalanceIsShownWithMinusAndRedColor() {
        addTransaction(EXPENSE_BUTTON, "Beli buku", "50000");

        assertEquals(List.of("1", "Rp 0.00", "Rp 50,000.00", "-Rp 50,000.00"), summaryValues());
        assertEquals("Daftar Transaksi - Saldo: -Rp 50,000.00", statusText());
        assertEquals("#B91C1C", balanceSpan().getStyle().get("color"));

        addTransaction(INCOME_BUTTON, "Gaji", "100000");

        assertEquals("Rp 50,000.00", summaryValues().get(3));
        assertEquals("#4C1D95", balanceSpan().getStyle().get("color"));
    }

    // ------------------------------------------------------------------ cari

    @Test
    void searchFiltersByDescriptionAndBlankSearchShowsEverything() {
        addSampleData();
        TextField searchField = searchField();

        _setValue(searchField, "tidakada");
        clickButton("Cari");
        assertEquals(0, grid().getListDataView().getItemCount());
        assertEquals("Hasil Pencarian: \"tidakada\" - Saldo: Rp 5,075,000.00", statusText());

        _setValue(searchField, " gaji ");
        clickButton("Cari");
        assertEquals(List.of("Gaji"), descriptions(grid()));
        assertEquals("Hasil Pencarian: \"gaji\" - Saldo: Rp 5,075,000.00", statusText());

        _setValue(searchField, "   ");
        clickButton("Cari");
        assertEquals(3, grid().getListDataView().getItemCount());
        assertEquals("Daftar Transaksi - Saldo: Rp 5,075,000.00", statusText());
    }

    @Test
    void refreshButtonClearsSearchAndSort() {
        addSampleData();
        _setValue(searchField(), "gaji");
        clickButton("Cari");
        sortBox().setValue(SortOption.AMOUNT_ASC);

        clickButton("Muat Ulang");

        assertEquals(3, grid().getListDataView().getItemCount());
        assertEquals("", searchField().getValue());
        assertNull(sortBox().getValue());
        assertEquals("Daftar Transaksi - Saldo: Rp 5,075,000.00", statusText());
    }

    // ------------------------------------------------------------------ urut

    @Test
    void sortDropdownOrdersRowsWithDomainSortOptions() {
        addSampleData();
        ComboBox<SortOption> sortBox = sortBox();

        sortBox.setValue(SortOption.AMOUNT_DESC);
        assertEquals(List.of("Gaji", "Bonus", "Makan siang"), descriptions(grid()));
        assertEquals("Daftar Transaksi (Jumlah terbesar) - Saldo: Rp 5,075,000.00", statusText());

        sortBox.setValue(SortOption.AMOUNT_ASC);
        assertEquals(List.of("Makan siang", "Bonus", "Gaji"), descriptions(grid()));

        sortBox.setValue(SortOption.INCOME_FIRST);
        assertEquals(List.of("Gaji", "Bonus", "Makan siang"), descriptions(grid()));

        sortBox.setValue(SortOption.EXPENSE_FIRST);
        assertEquals(List.of("Makan siang", "Gaji", "Bonus"), descriptions(grid()));
    }

    @Test
    void choosingSortClearsSearchAndSearchingClearsSort() {
        addSampleData();
        _setValue(searchField(), "gaji");

        sortBox().setValue(SortOption.AMOUNT_ASC);
        assertEquals("", searchField().getValue());
        assertEquals(3, grid().getListDataView().getItemCount());

        _setValue(searchField(), "bonus");
        clickButton("Cari");
        assertNull(sortBox().getValue());
        assertEquals(List.of("Bonus"), descriptions(grid()));
    }

    @Test
    void clearingSortDropdownShowsAllTransactionsAgain() {
        addSampleData();
        sortBox().setValue(SortOption.AMOUNT_DESC);

        sortBox().clear();

        assertEquals(3, grid().getListDataView().getItemCount());
        assertEquals("Daftar Transaksi - Saldo: Rp 5,075,000.00", statusText());
    }

    @Test
    void sortFailureIsReportedWithoutChangingGrid() {
        addSampleData();
        useCase.failSort = true;

        sortBox().setValue(SortOption.AMOUNT_ASC);

        assertEquals("Daftar Transaksi - Saldo: Rp 5,075,000.00", statusText());
        assertEquals(List.of("Gaji", "Makan siang", "Bonus"), descriptions(grid()));
    }

    @Test
    void gridHeadersSortWithDomainComparators() {
        addSampleData();
        Grid<Transaction> grid = grid();

        sortBy(grid, "amount", SortDirection.ASCENDING);
        assertEquals(List.of("Makan siang", "Bonus", "Gaji"), descriptions(grid));
        sortBy(grid, "amount", SortDirection.DESCENDING);
        assertEquals(List.of("Gaji", "Bonus", "Makan siang"), descriptions(grid));

        sortBy(grid, "type", SortDirection.ASCENDING);
        assertEquals("Makan siang", descriptions(grid).get(2));
        sortBy(grid, "type", SortDirection.DESCENDING);
        assertEquals("Makan siang", descriptions(grid).get(0));

        sortBy(grid, "description", SortDirection.ASCENDING);
        assertEquals(List.of("Bonus", "Gaji", "Makan siang"), descriptions(grid));

        sortBy(grid, "id", SortDirection.DESCENDING);
        assertEquals(List.of("Bonus", "Makan siang", "Gaji"), descriptions(grid));

        sortBy(grid, "createdAt", SortDirection.ASCENDING);
        assertEquals(3, grid.getListDataView().getItemCount());
    }

    // ------------------------------------------------------------------ ubah

    @Test
    void doubleClickOpensEditDialogAndSavesChanges() {
        addTransaction(INCOME_BUTTON, "Gaji", "5000000");
        Grid<Transaction> grid = grid();

        _doubleClickItem(grid, 0);
        Dialog dialog = _get(Dialog.class);
        assertEquals("Gaji", descriptionField(dialog).getValue());
        assertEquals("5000000.00", amountField(dialog).getValue());

        fillDialog(dialog, "Gaji Bulanan", "6000000");
        clickDialogButton(dialog, "Simpan");

        Transaction updated = useCase.getAllTransactions().get(0);
        assertEquals("Gaji Bulanan", updated.getDescription());
        assertEquals(new BigDecimal("6000000.00"), updated.getAmount());
        assertEquals("Daftar Transaksi - Saldo: Rp 6,000,000.00", statusText());
    }

    @Test
    void invalidEditKeepsDialogOpenAndDataUntouched() {
        addTransaction(EXPENSE_BUTTON, "Makan siang", "25000");

        _doubleClickItem(grid(), 0);
        Dialog dialog = _get(Dialog.class);
        fillDialog(dialog, "", "30000");
        clickDialogButton(dialog, "Simpan");
        fillDialog(dialog, "Makan malam", "abc");
        clickDialogButton(dialog, "Simpan");

        Transaction untouched = useCase.getAllTransactions().get(0);
        assertEquals("Makan siang", untouched.getDescription());
        assertEquals(new BigDecimal("25000.00"), untouched.getAmount());
        assertEquals(dialog, _get(Dialog.class));
        clickDialogButton(dialog, "Batal");
    }

    @Test
    void editFailureIsReportedWhenTransactionDisappeared() {
        addTransaction(EXPENSE_BUTTON, "Makan siang", "25000");
        useCase.failUpdate = true;

        _doubleClickItem(grid(), 0);
        Dialog dialog = _get(Dialog.class);
        clickDialogButton(dialog, "Simpan");

        assertEquals("Makan siang", useCase.getAllTransactions().get(0).getDescription());
        clickDialogButton(dialog, "Batal");
    }

    // ------------------------------------------------------------------ hapus

    @Test
    void deleteAsksConfirmationAndCanBeCancelled() {
        addSampleData();
        Grid<Transaction> grid = grid();

        clickRowButton(grid, indexOfDescription(grid, "Makan siang"), "Hapus");
        Dialog cancelDialog = _get(Dialog.class);
        assertEquals("Yakin ingin menghapus [2] Makan siang | Rp 25,000.00 | [Pengeluaran]?",
                _get(cancelDialog, Paragraph.class).getText());
        clickDialogButton(cancelDialog, "Batal");
        assertEquals(3, useCase.getAllTransactions().size());

        clickRowButton(grid, indexOfDescription(grid, "Makan siang"), "Hapus");
        Dialog confirmDialog = _get(Dialog.class);
        clickDialogButton(confirmDialog, "Hapus");

        assertEquals(2, useCase.getAllTransactions().size());
        assertEquals(List.of("Gaji", "Bonus"), descriptions(grid));
        assertEquals(List.of("2", "Rp 5,100,000.00", "Rp 0.00", "Rp 5,100,000.00"), summaryValues());
    }

    @Test
    void deleteFailureIsReportedWhenTransactionDisappeared() {
        addTransaction(INCOME_BUTTON, "Gaji", "5000000");
        useCase.failRemove = true;

        clickRowButton(grid(), 0, "Hapus");
        clickDialogButton(_get(Dialog.class), "Hapus");

        assertEquals(1, useCase.getAllTransactions().size());
    }

    // ------------------------------------------------------------------ helper

    /** Data contoh: 1 Gaji (masuk 5.000.000), 2 Makan siang (keluar 25.000), 3 Bonus (masuk 100.000). */
    private void addSampleData() {
        addTransaction(INCOME_BUTTON, "Gaji", "5000000");
        addTransaction(EXPENSE_BUTTON, "Makan siang", "25000");
        addTransaction(INCOME_BUTTON, "Bonus", "100000");
    }

    private void addTransaction(String buttonCaption, String description, String amount) {
        clickButton(buttonCaption);
        Dialog dialog = _get(Dialog.class);
        fillDialog(dialog, description, amount);
        clickDialogButton(dialog, "Simpan");
    }

    private void fillDialog(Dialog dialog, String description, String amount) {
        _setValue(descriptionField(dialog), description);
        _setValue(amountField(dialog), amount);
    }

    private TextField descriptionField(Dialog dialog) {
        return _get(dialog, TextField.class, spec -> spec.withPlaceholder("contoh: Gaji bulanan"));
    }

    private TextField amountField(Dialog dialog) {
        return _get(dialog, TextField.class, spec -> spec.withPlaceholder("contoh: 1500000"));
    }

    private TextField searchField() {
        return _get(TextField.class, spec -> spec.withPlaceholder("Cari keterangan..."));
    }

    @SuppressWarnings("unchecked")
    private ComboBox<SortOption> sortBox() {
        return _get(ComboBox.class);
    }

    @SuppressWarnings("unchecked")
    private Grid<Transaction> grid() {
        return _get(Grid.class);
    }

    private String statusText() {
        return _get(_get(FinanceView.class), Paragraph.class).getText();
    }

    private List<String> descriptions(Grid<Transaction> grid) {
        return grid.getListDataView().getItems().map(Transaction::getDescription).toList();
    }

    private int indexOfDescription(Grid<Transaction> grid, String description) {
        List<String> all = descriptions(grid);
        int index = all.indexOf(description);
        if (index < 0) {
            throw new AssertionError("Transaksi tidak ditemukan di grid: " + description);
        }
        return index;
    }

    private void sortBy(Grid<Transaction> grid, String columnKey, SortDirection direction) {
        grid.sort(List.of(new GridSortOrder<>(grid.getColumnByKey(columnKey), direction)));
    }

    private void clickRowButton(Grid<Transaction> grid, int row, String caption) {
        HorizontalLayout actions = (HorizontalLayout) _getCellComponent(grid, row, "actions");
        _click(_get(actions, Button.class, spec -> spec.withCaption(caption)));
    }

    private void clickButton(String caption) {
        _click(_get(Button.class, spec -> spec.withCaption(caption)));
    }

    private void clickDialogButton(Dialog dialog, String caption) {
        _click(_get(dialog, Button.class, spec -> spec.withCaption(caption)));
    }

    /** Nilai keempat kartu ringkasan: total transaksi, pemasukan, pengeluaran, saldo. */
    private List<String> summaryValues() {
        return summaryCards().stream()
                .map(card -> ((Span) card.getChildren().skip(1).findFirst().orElseThrow()).getText())
                .toList();
    }

    private Span balanceSpan() {
        Div balanceCard = summaryCards().get(3);
        return (Span) balanceCard.getChildren().skip(1).findFirst().orElseThrow();
    }

    private List<Div> summaryCards() {
        FinanceView view = _get(FinanceView.class);
        HorizontalLayout summary = (HorizontalLayout) view.getChildren()
                .filter(HorizontalLayout.class::isInstance)
                .findFirst()
                .orElseThrow();
        return summary.getChildren().map(Div.class::cast).toList();
    }
}
