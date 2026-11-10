package org.delcom.ifs25022pbo2026springbootcashflow.adapter.presenter;

import org.delcom.ifs25022pbo2026springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25022pbo2026springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25022pbo2026springbootcashflow.domain.entity.TransactionType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FinancePresenterTest {

    private final FinancePresenter presenter = new FinancePresenter();

    private final Transaction income = new Transaction(1, "Gaji", new BigDecimal("5000000"),
            TransactionType.INCOME, LocalDateTime.of(2026, 11, 6, 8, 5));
    private final Transaction expense = new Transaction(2, "Makan siang", new BigDecimal("25000"),
            TransactionType.EXPENSE, LocalDateTime.of(2026, 11, 7, 12, 30));

    @Test
    void formatMoneyUsesThousandsSeparatorAndTwoDecimals() {
        assertEquals("Rp 1,000,000.00", presenter.formatMoney(new BigDecimal("1000000")));
        assertEquals("Rp 0.00", presenter.formatMoney(BigDecimal.ZERO));
        assertEquals("Rp 1,500.50", presenter.formatMoney(new BigDecimal("1500.5")));
    }

    @Test
    void formatMoneyPutsMinusBeforeCurrencyForNegativeValues() {
        assertEquals("-Rp 500.00", presenter.formatMoney(new BigDecimal("-500")));
    }

    @Test
    void formatTypeGivesIndonesianLabels() {
        assertEquals("Pemasukan", presenter.formatType(TransactionType.INCOME));
        assertEquals("Pengeluaran", presenter.formatType(TransactionType.EXPENSE));
    }

    @Test
    void formatSortOptionHasLabelForEveryOption() {
        assertEquals("Jumlah terkecil", presenter.formatSortOption(SortOption.AMOUNT_ASC));
        assertEquals("Jumlah terbesar", presenter.formatSortOption(SortOption.AMOUNT_DESC));
        assertEquals("Pemasukan dulu", presenter.formatSortOption(SortOption.INCOME_FIRST));
        assertEquals("Pengeluaran dulu", presenter.formatSortOption(SortOption.EXPENSE_FIRST));
    }

    @Test
    void formatCreatedAtUsesDayMonthYearAndTime() {
        assertEquals("06/11/2026 08:05", presenter.formatCreatedAt(income));
        assertEquals("07/11/2026 12:30", presenter.formatCreatedAt(expense));
    }

    @Test
    void formatBuildsOneLinePerTransaction() {
        assertEquals("[1] Gaji | Rp 5,000,000.00 | [Pemasukan]", presenter.format(income));
        assertEquals("[2] Makan siang | Rp 25,000.00 | [Pengeluaran]", presenter.format(expense));
    }

    @Test
    void formatBalanceAddsLabel() {
        assertEquals("Saldo: Rp 4,975,000.00", presenter.formatBalance(new BigDecimal("4975000")));
        assertEquals("Saldo: -Rp 10.00", presenter.formatBalance(new BigDecimal("-10")));
    }

    @Test
    void formatListShowsHeaderAndEmptyMessage() {
        assertEquals("Judul\nKosong", presenter.formatList(List.of(), "Judul", "Kosong"));
        assertEquals("Judul\n" + presenter.format(income) + "\n" + presenter.format(expense),
                presenter.formatList(List.of(income, expense), "Judul", "Kosong"));
    }

    @Test
    void showTransactionsHandlesEmptyAndFilledList() {
        assertEquals("Daftar Transaksi:\n- Data transaksi belum tersedia!",
                presenter.showTransactions(List.of()));
        assertEquals("Daftar Transaksi:\n" + presenter.format(income),
                presenter.showTransactions(List.of(income)));
    }

    @Test
    void showSearchResultsHandlesEmptyAndFilledList() {
        assertEquals("Hasil Pencarian: \"zzz\"\n- Transaksi tidak ditemukan!",
                presenter.showSearchResults(List.of(), "zzz"));
        assertEquals("Hasil Pencarian: \"gaji\"\n" + presenter.format(income),
                presenter.showSearchResults(List.of(income), "gaji"));
    }

    @Test
    void showSortedTransactionsMentionsSortLabel() {
        assertEquals("Daftar Transaksi (Jumlah terbesar):\n" + presenter.format(income),
                presenter.showSortedTransactions(List.of(income), SortOption.AMOUNT_DESC));
    }

    @Test
    void emptyStateMessages() {
        assertEquals("- Data transaksi belum tersedia!", presenter.showNoData());
        assertEquals("- Transaksi tidak ditemukan!", presenter.showNoSearchResult());
    }

    @Test
    void successAndFailureMessages() {
        assertEquals("Berhasil menambah pemasukan: [1] Gaji | Rp 5,000,000.00 | [Pemasukan]",
                presenter.showAddIncomeSuccess(income));
        assertEquals("Berhasil menambah pengeluaran: [2] Makan siang | Rp 25,000.00 | [Pengeluaran]",
                presenter.showAddExpenseSuccess(expense));
        assertEquals("Berhasil menghapus transaksi.", presenter.showRemoveSuccess());
        assertEquals("[!] Gagal menghapus transaksi dengan ID: 7.", presenter.showRemoveFailed(7));
        assertEquals("Berhasil mengubah transaksi.", presenter.showUpdateSuccess());
        assertEquals("[!] Gagal mengubah transaksi dengan ID: 7.", presenter.showUpdateFailed(7));
        assertEquals("[!] Keterangan tidak boleh kosong",
                presenter.showValidationError("Keterangan tidak boleh kosong"));
    }
}
