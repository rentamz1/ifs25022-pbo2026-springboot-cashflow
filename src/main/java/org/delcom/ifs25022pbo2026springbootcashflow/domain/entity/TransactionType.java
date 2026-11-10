package org.delcom.ifs25022pbo2026springbootcashflow.domain.entity;

/**
 * Jenis transaksi keuangan.
 * INCOME menambah saldo, EXPENSE mengurangi saldo.
 */
public enum TransactionType {
    /** Pemasukan. */
    INCOME,

    /** Pengeluaran. */
    EXPENSE
}
