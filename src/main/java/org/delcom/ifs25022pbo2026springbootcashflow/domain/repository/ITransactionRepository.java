package org.delcom.ifs25022pbo2026springbootcashflow.domain.repository;

import org.delcom.ifs25022pbo2026springbootcashflow.domain.entity.Transaction;

import java.math.BigDecimal;

/**
 * Port penyimpanan khusus Transaction.
 * Penomoran ID dilakukan oleh implementasi, sehingga use case cukup memberi data mentah.
 */
public interface ITransactionRepository extends IRepository<Transaction, Integer> {
    /** Menyimpan pemasukan baru dan mengembalikannya lengkap dengan ID. */
    Transaction saveIncome(String description, BigDecimal amount);

    /** Menyimpan pengeluaran baru dan mengembalikannya lengkap dengan ID. */
    Transaction saveExpense(String description, BigDecimal amount);
}
