package org.delcom.ifs25022pbo2026springbootcashflow.usecase;

import org.delcom.ifs25022pbo2026springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25022pbo2026springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25022pbo2026springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25022pbo2026springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25022pbo2026springbootcashflow.domain.repository.ITransactionRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Use case pencatatan keuangan.
 * Hanya memproses data dan melempar checked exception domain; tidak ada I/O ke UI
 * (tanpa Vaadin, tanpa System.out), sehingga bisa diuji tanpa framework.
 */
public class FinanceUseCase {
    /** Batas nominal per transaksi, mencegah angka yang tidak masuk akal. */
    private static final BigDecimal MAX_AMOUNT = new BigDecimal("1000000000000");

    /** Port repository yang di-inject dari luar (Dependency Inversion). */
    private final ITransactionRepository transactionRepository;

    public FinanceUseCase(ITransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    /** Mengambil semua transaksi. */
    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    /** Menambah pemasukan setelah keterangan dan jumlah divalidasi. */
    public Transaction addIncome(String description, String amountInput) throws ValidationException {
        String cleanDescription = validateDescription(description);
        BigDecimal amount = parseAmount(amountInput);
        return transactionRepository.saveIncome(cleanDescription, amount);
    }

    /** Menambah pengeluaran setelah keterangan dan jumlah divalidasi. */
    public Transaction addExpense(String description, String amountInput) throws ValidationException {
        String cleanDescription = validateDescription(description);
        BigDecimal amount = parseAmount(amountInput);
        return transactionRepository.saveExpense(cleanDescription, amount);
    }

    /** Menghapus transaksi; melempar exception jika ID tidak ditemukan. */
    public void removeTransaction(int id) throws EntityNotFoundException {
        if (!transactionRepository.deleteById(id)) {
            throw new EntityNotFoundException(id);
        }
    }

    /**
     * Mengubah keterangan dan jumlah transaksi. Semua input divalidasi lebih dulu
     * sehingga data tidak berubah setengah-setengah jika salah satunya tidak valid.
     */
    public void updateTransaction(int id, String description, String amountInput)
            throws EntityNotFoundException, ValidationException {
        Optional<Transaction> found = transactionRepository.findById(id);
        if (found.isEmpty()) {
            throw new EntityNotFoundException(id);
        }

        String cleanDescription = validateDescription(description);
        BigDecimal amount = parseAmount(amountInput);

        Transaction transaction = found.get();
        transaction.changeDescription(cleanDescription);
        transaction.changeAmount(amount);
        transactionRepository.update(transaction);
    }

    /** Mencari transaksi berdasarkan keterangan (case-insensitive); kata kunci kosong = semua. */
    public List<Transaction> searchTransactions(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return transactionRepository.findAll();
        }
        String lowerKeyword = keyword.trim().toLowerCase();
        return transactionRepository.findBy(
                transaction -> transaction.getDescription().toLowerCase().contains(lowerKeyword));
    }

    /** Mengurutkan transaksi sesuai {@link SortOption}. */
    public List<Transaction> sortTransactions(SortOption option) throws ValidationException {
        if (option == null) {
            throw new ValidationException("Pilihan pengurutan tidak valid");
        }
        return transactionRepository.findAll().stream()
                .sorted(option.comparator())
                .toList();
    }

    /** Total seluruh pemasukan. */
    public BigDecimal getTotalIncome() {
        return sum(Transaction::isIncome);
    }

    /** Total seluruh pengeluaran. */
    public BigDecimal getTotalExpense() {
        return sum(Transaction::isExpense);
    }

    /** Saldo saat ini = total pemasukan - total pengeluaran. */
    public BigDecimal getBalance() {
        return getTotalIncome().subtract(getTotalExpense());
    }

    private BigDecimal sum(Predicate<Transaction> filter) {
        return transactionRepository.findAll().stream()
                .filter(filter)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private String validateDescription(String description) throws ValidationException {
        if (description == null || description.isBlank()) {
            throw new ValidationException("Keterangan tidak boleh kosong");
        }
        return description.trim();
    }

    private BigDecimal parseAmount(String input) throws ValidationException {
        if (input == null || input.isBlank()) {
            throw new ValidationException("Jumlah tidak boleh kosong");
        }

        BigDecimal value;
        try {
            value = new BigDecimal(input.trim());
        } catch (NumberFormatException ex) {
            throw new ValidationException("Jumlah harus berupa angka (contoh: 1000000 atau 1500.50)");
        }

        // Dicek sebelum dibulatkan agar input seperti 1e999999999 tidak membebani memori.
        if (value.compareTo(MAX_AMOUNT) > 0) {
            throw new ValidationException("Jumlah terlalu besar");
        }

        BigDecimal rounded = value.setScale(2, RoundingMode.HALF_UP);
        if (rounded.signum() <= 0) {
            throw new ValidationException("Jumlah harus lebih besar dari 0");
        }
        return rounded;
    }
}
