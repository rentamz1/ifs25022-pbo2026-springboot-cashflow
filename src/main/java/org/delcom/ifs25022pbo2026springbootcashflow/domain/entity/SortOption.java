package org.delcom.ifs25022pbo2026springbootcashflow.domain.entity;

import java.util.Comparator;

/**
 * Kriteria pengurutan transaksi.
 * Setiap opsi membawa comparator-nya sendiri sehingga aturan pengurutan
 * terpusat di domain, tidak tersebar di view atau use case.
 */
public enum SortOption {
    /** Jumlah dari yang terkecil. */
    AMOUNT_ASC(Comparator.comparing(Transaction::getAmount)),

    /** Jumlah dari yang terbesar. */
    AMOUNT_DESC(Comparator.comparing(Transaction::getAmount).reversed()),

    /** Pemasukan ditampilkan lebih dulu. */
    INCOME_FIRST(Comparator.comparing(Transaction::isExpense)),

    /** Pengeluaran ditampilkan lebih dulu. */
    EXPENSE_FIRST(Comparator.comparing(Transaction::isIncome));

    private final Comparator<Transaction> comparator;

    SortOption(Comparator<Transaction> comparator) {
        this.comparator = comparator;
    }

    /** Comparator yang dipakai untuk mengurutkan daftar transaksi. */
    public Comparator<Transaction> comparator() {
        return comparator;
    }
}
