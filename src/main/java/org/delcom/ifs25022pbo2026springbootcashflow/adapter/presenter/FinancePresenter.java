package org.delcom.ifs25022pbo2026springbootcashflow.adapter.presenter;

import org.delcom.ifs25022pbo2026springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25022pbo2026springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25022pbo2026springbootcashflow.domain.entity.TransactionType;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Presenter: mengubah data domain menjadi teks siap tampil (uang, tipe, baris transaksi, pesan).
 * Memisahkan urusan format dari entity, use case, dan view.
 */
public class FinancePresenter {

    private static final DateTimeFormatter DATE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /** Label tiap opsi pengurutan; memakai peta agar tidak ada percabangan. */
    private static final Map<SortOption, String> SORT_LABELS = new EnumMap<>(Map.of(
            SortOption.AMOUNT_ASC, "Jumlah terkecil",
            SortOption.AMOUNT_DESC, "Jumlah terbesar",
            SortOption.INCOME_FIRST, "Pemasukan dulu",
            SortOption.EXPENSE_FIRST, "Pengeluaran dulu"
    ));

    /** Memformat uang dengan pemisah ribuan koma dan dua desimal, mis. Rp 1,000,000.00. */
    public String formatMoney(BigDecimal amount) {
        String sign = amount.signum() < 0 ? "-" : "";
        return String.format(Locale.US, "%sRp %,.2f", sign, amount.abs());
    }

    /** Label tipe transaksi. */
    public String formatType(TransactionType type) {
        return type == TransactionType.INCOME ? "Pemasukan" : "Pengeluaran";
    }

    /** Label opsi pengurutan. */
    public String formatSortOption(SortOption option) {
        return SORT_LABELS.get(option);
    }

    /** Waktu transaksi dicatat. */
    public String formatCreatedAt(Transaction transaction) {
        return transaction.getCreatedAt().format(DATE_TIME_FORMAT);
    }

    /** Satu baris transaksi: [id] keterangan | Rp ... | [tipe]. */
    public String format(Transaction transaction) {
        return String.format(
                "[%d] %s | %s | [%s]",
                transaction.getId(),
                transaction.getDescription(),
                formatMoney(transaction.getAmount()),
                formatType(transaction.getType())
        );
    }

    /** Teks saldo, mis. Saldo: Rp 4,975,000.00. */
    public String formatBalance(BigDecimal balance) {
        return "Saldo: " + formatMoney(balance);
    }

    /** Daftar transaksi multi-baris dengan header; pesan kosong jika tidak ada data. */
    public String formatList(List<Transaction> transactions, String header, String emptyMessage) {
        if (transactions.isEmpty()) {
            return header + "\n" + emptyMessage;
        }
        String body = transactions.stream()
                .map(this::format)
                .collect(Collectors.joining("\n"));
        return header + "\n" + body;
    }

    /** Pesan saat belum ada transaksi. */
    public String showNoData() {
        return "- Data transaksi belum tersedia!";
    }

    /** Pesan saat pencarian tidak menemukan apa pun. */
    public String showNoSearchResult() {
        return "- Transaksi tidak ditemukan!";
    }

    /** Semua transaksi. */
    public String showTransactions(List<Transaction> transactions) {
        return formatList(transactions, "Daftar Transaksi:", showNoData());
    }

    /** Hasil pencarian. */
    public String showSearchResults(List<Transaction> transactions, String keyword) {
        return formatList(transactions, "Hasil Pencarian: \"" + keyword + "\"", showNoSearchResult());
    }

    /** Transaksi yang sudah diurutkan. */
    public String showSortedTransactions(List<Transaction> transactions, SortOption option) {
        return formatList(transactions,
                "Daftar Transaksi (" + formatSortOption(option) + "):", showNoData());
    }

    public String showAddIncomeSuccess(Transaction transaction) {
        return "Berhasil menambah pemasukan: " + format(transaction);
    }

    public String showAddExpenseSuccess(Transaction transaction) {
        return "Berhasil menambah pengeluaran: " + format(transaction);
    }

    public String showRemoveSuccess() {
        return "Berhasil menghapus transaksi.";
    }

    public String showRemoveFailed(int id) {
        return String.format("[!] Gagal menghapus transaksi dengan ID: %d.", id);
    }

    public String showUpdateSuccess() {
        return "Berhasil mengubah transaksi.";
    }

    public String showUpdateFailed(int id) {
        return String.format("[!] Gagal mengubah transaksi dengan ID: %d.", id);
    }

    public String showValidationError(String message) {
        return "[!] " + message;
    }
}
