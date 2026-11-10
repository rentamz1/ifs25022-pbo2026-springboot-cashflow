package org.delcom.ifs25022pbo2026springbootcashflow.domain.entity;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TransactionTest {

    @Test
    void exposesAllFields() {
        LocalDateTime time = LocalDateTime.of(2026, 11, 6, 8, 5);
        Transaction transaction = new Transaction(7, "Gaji", new BigDecimal("1500.50"),
                TransactionType.INCOME, time);

        assertEquals(7, transaction.getId());
        assertEquals("Gaji", transaction.getDescription());
        assertEquals(new BigDecimal("1500.50"), transaction.getAmount());
        assertEquals(TransactionType.INCOME, transaction.getType());
        assertEquals(time, transaction.getCreatedAt());
    }

    @Test
    void defaultConstructorRecordsCurrentTime() {
        LocalDateTime before = LocalDateTime.now();
        Transaction transaction = new Transaction(1, "Kopi", BigDecimal.TEN, TransactionType.EXPENSE);
        LocalDateTime after = LocalDateTime.now();

        assertFalse(transaction.getCreatedAt().isBefore(before));
        assertFalse(transaction.getCreatedAt().isAfter(after));
    }

    @Test
    void incomeAndExpenseFlagsFollowType() {
        Transaction income = new Transaction(1, "Gaji", BigDecimal.TEN, TransactionType.INCOME);
        Transaction expense = new Transaction(2, "Kopi", BigDecimal.TEN, TransactionType.EXPENSE);

        assertTrue(income.isIncome());
        assertFalse(income.isExpense());
        assertTrue(expense.isExpense());
        assertFalse(expense.isIncome());
    }

    @Test
    void signedAmountIsPositiveForIncomeAndNegativeForExpense() {
        Transaction income = new Transaction(1, "Gaji", new BigDecimal("100"), TransactionType.INCOME);
        Transaction expense = new Transaction(2, "Kopi", new BigDecimal("40"), TransactionType.EXPENSE);

        assertEquals(new BigDecimal("100"), income.signedAmount());
        assertEquals(new BigDecimal("-40"), expense.signedAmount());
    }

    @Test
    void descriptionAndAmountCanBeChanged() {
        Transaction transaction = new Transaction(1, "Lama", new BigDecimal("10"), TransactionType.EXPENSE);

        transaction.changeDescription("Baru");
        transaction.changeAmount(new BigDecimal("99"));

        assertEquals("Baru", transaction.getDescription());
        assertEquals(new BigDecimal("99"), transaction.getAmount());
        assertEquals(1, transaction.getId());
        assertEquals(TransactionType.EXPENSE, transaction.getType());
    }
}
