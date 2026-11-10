package org.delcom.ifs25022pbo2026springbootcashflow.usecase;

import org.delcom.ifs25022pbo2026springbootcashflow.adapter.repository.TransactionRepository;
import org.delcom.ifs25022pbo2026springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25022pbo2026springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25022pbo2026springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25022pbo2026springbootcashflow.domain.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FinanceUseCaseTest {

    private FinanceUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new FinanceUseCase(new TransactionRepository());
    }

    private List<Integer> idsOf(List<Transaction> transactions) {
        return transactions.stream().map(Transaction::getId).toList();
    }

    // ---------------------------------------------------------------- tambah

    @Test
    void addIncomeTrimsDescriptionAndKeepsTwoDecimals() throws Exception {
        Transaction saved = useCase.addIncome("  Gaji  ", "5000000");

        assertEquals(1, saved.getId());
        assertEquals("Gaji", saved.getDescription());
        assertEquals(new BigDecimal("5000000.00"), saved.getAmount());
        assertTrue(saved.isIncome());
    }

    @Test
    void addExpenseStoresExpenseAndRoundsHalfUp() throws Exception {
        Transaction saved = useCase.addExpense("Kopi", "1500.555");

        assertTrue(saved.isExpense());
        assertEquals(new BigDecimal("1500.56"), saved.getAmount());
    }

    @Test
    void addRejectsBlankOrNullDescription() {
        ValidationException blank = assertThrows(ValidationException.class, () -> useCase.addIncome("   ", "100"));
        ValidationException none = assertThrows(ValidationException.class, () -> useCase.addExpense(null, "100"));

        assertEquals("Keterangan tidak boleh kosong", blank.getMessage());
        assertEquals("Keterangan tidak boleh kosong", none.getMessage());
        assertTrue(useCase.getAllTransactions().isEmpty());
    }

    @Test
    void addRejectsBlankOrNullAmount() {
        ValidationException blank = assertThrows(ValidationException.class, () -> useCase.addIncome("Gaji", " "));
        ValidationException none = assertThrows(ValidationException.class, () -> useCase.addIncome("Gaji", null));

        assertEquals("Jumlah tidak boleh kosong", blank.getMessage());
        assertEquals("Jumlah tidak boleh kosong", none.getMessage());
    }

    @Test
    void addRejectsNonNumericAmount() {
        ValidationException letters = assertThrows(ValidationException.class, () -> useCase.addIncome("Gaji", "abc"));
        ValidationException separator = assertThrows(ValidationException.class, () -> useCase.addIncome("Gaji", "1,000"));

        assertTrue(letters.getMessage().startsWith("Jumlah harus berupa angka"));
        assertTrue(separator.getMessage().startsWith("Jumlah harus berupa angka"));
    }

    @Test
    void addRejectsZeroNegativeAndRoundedToZeroAmounts() {
        for (String input : List.of("0", "-5", "0.001")) {
            ValidationException exception = assertThrows(ValidationException.class,
                    () -> useCase.addExpense("Kopi", input));
            assertEquals("Jumlah harus lebih besar dari 0", exception.getMessage());
        }
        assertTrue(useCase.getAllTransactions().isEmpty());
    }

    @Test
    void addRejectsHugeAmountWithoutHanging() {
        ValidationException exception = assertThrows(ValidationException.class,
                () -> useCase.addIncome("Gaji", "1e999999999"));

        assertEquals("Jumlah terlalu besar", exception.getMessage());
    }

    // ---------------------------------------------------------------- hapus

    @Test
    void removeTransactionDeletesExistingAndFailsForUnknownId() throws Exception {
        useCase.addIncome("Gaji", "100");

        useCase.removeTransaction(1);

        assertTrue(useCase.getAllTransactions().isEmpty());
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> useCase.removeTransaction(1));
        assertEquals(1, exception.getId());
    }

    // ---------------------------------------------------------------- ubah

    @Test
    void updateTransactionChangesDescriptionAndAmount() throws Exception {
        useCase.addExpense("Makan siang", "25000");

        useCase.updateTransaction(1, "  Makan malam ", "30000");

        Transaction updated = useCase.getAllTransactions().get(0);
        assertEquals("Makan malam", updated.getDescription());
        assertEquals(new BigDecimal("30000.00"), updated.getAmount());
        assertTrue(updated.isExpense());
    }

    @Test
    void updateTransactionFailsForUnknownId() {
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> useCase.updateTransaction(99, "A", "1"));

        assertEquals(99, exception.getId());
    }

    @Test
    void updateTransactionDoesNotChangeAnythingWhenInputIsInvalid() throws Exception {
        useCase.addExpense("Makan siang", "25000");

        assertThrows(ValidationException.class, () -> useCase.updateTransaction(1, "", "30000"));
        assertThrows(ValidationException.class, () -> useCase.updateTransaction(1, "Baru", "abc"));

        Transaction untouched = useCase.getAllTransactions().get(0);
        assertEquals("Makan siang", untouched.getDescription());
        assertEquals(new BigDecimal("25000.00"), untouched.getAmount());
    }

    // ---------------------------------------------------------------- cari

    @Test
    void searchIgnoresCaseAndOnlyMatchesDescription() throws Exception {
        useCase.addIncome("Gaji Bulanan", "100");
        useCase.addExpense("Makan siang", "25");

        assertEquals(List.of(1), idsOf(useCase.searchTransactions("  gAJi ")));
        assertEquals(List.of(2), idsOf(useCase.searchTransactions("SIANG")));
        assertTrue(useCase.searchTransactions("zzz").isEmpty());
        assertTrue(useCase.searchTransactions("100").isEmpty());
    }

    @Test
    void searchWithBlankOrNullKeywordReturnsEverything() throws Exception {
        useCase.addIncome("Gaji", "100");
        useCase.addExpense("Kopi", "5");

        assertEquals(2, useCase.searchTransactions("  ").size());
        assertEquals(2, useCase.searchTransactions(null).size());
    }

    // ---------------------------------------------------------------- urut

    @Test
    void sortTransactionsFollowsEachSortOption() throws Exception {
        useCase.addIncome("Gaji", "5000000");
        useCase.addExpense("Makan", "25000");
        useCase.addIncome("Bunga", "1500.56");

        assertEquals(List.of(3, 2, 1), idsOf(useCase.sortTransactions(SortOption.AMOUNT_ASC)));
        assertEquals(List.of(1, 2, 3), idsOf(useCase.sortTransactions(SortOption.AMOUNT_DESC)));
        assertEquals(List.of(1, 3, 2), idsOf(useCase.sortTransactions(SortOption.INCOME_FIRST)));
        assertEquals(List.of(2, 1, 3), idsOf(useCase.sortTransactions(SortOption.EXPENSE_FIRST)));
        assertEquals(List.of(1, 2, 3), idsOf(useCase.getAllTransactions()));
    }

    @Test
    void sortTransactionsRejectsNullOption() {
        ValidationException exception = assertThrows(ValidationException.class,
                () -> useCase.sortTransactions(null));

        assertEquals("Pilihan pengurutan tidak valid", exception.getMessage());
    }

    // ---------------------------------------------------------------- saldo

    @Test
    void totalsAndBalanceFollowTransactions() throws Exception {
        assertEquals(new BigDecimal("0"), useCase.getBalance());

        useCase.addIncome("Gaji", "5000000");
        useCase.addExpense("Makan", "25000");

        assertEquals(new BigDecimal("5000000.00"), useCase.getTotalIncome());
        assertEquals(new BigDecimal("25000.00"), useCase.getTotalExpense());
        assertEquals(new BigDecimal("4975000.00"), useCase.getBalance());
    }

    @Test
    void balanceCanBecomeNegativeAndIsRestoredAfterDelete() throws Exception {
        useCase.addIncome("Gaji", "100");
        useCase.addExpense("Beli laptop", "250");

        assertEquals(new BigDecimal("-150.00"), useCase.getBalance());

        useCase.removeTransaction(2);

        assertEquals(new BigDecimal("100.00"), useCase.getBalance());
    }
}
