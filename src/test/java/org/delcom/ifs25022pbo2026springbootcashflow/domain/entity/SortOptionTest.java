package org.delcom.ifs25022pbo2026springbootcashflow.domain.entity;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SortOptionTest {

    private final Transaction salary = new Transaction(1, "Gaji", new BigDecimal("5000"), TransactionType.INCOME);
    private final Transaction lunch = new Transaction(2, "Makan", new BigDecimal("25"), TransactionType.EXPENSE);
    private final Transaction bonus = new Transaction(3, "Bonus", new BigDecimal("100"), TransactionType.INCOME);
    private final Transaction rent = new Transaction(4, "Kos", new BigDecimal("900"), TransactionType.EXPENSE);

    private List<Integer> idsSortedBy(SortOption option) {
        return List.of(salary, lunch, bonus, rent).stream()
                .sorted(option.comparator())
                .map(Transaction::getId)
                .toList();
    }

    @Test
    void amountAscendingStartsFromSmallest() {
        assertEquals(List.of(2, 3, 4, 1), idsSortedBy(SortOption.AMOUNT_ASC));
    }

    @Test
    void amountDescendingStartsFromLargest() {
        assertEquals(List.of(1, 4, 3, 2), idsSortedBy(SortOption.AMOUNT_DESC));
    }

    @Test
    void incomeFirstKeepsOrderInsideEachGroup() {
        assertEquals(List.of(1, 3, 2, 4), idsSortedBy(SortOption.INCOME_FIRST));
    }

    @Test
    void expenseFirstKeepsOrderInsideEachGroup() {
        assertEquals(List.of(2, 4, 1, 3), idsSortedBy(SortOption.EXPENSE_FIRST));
    }

    @Test
    void enumHasFourOptionsAndValueOfWorks() {
        assertEquals(4, SortOption.values().length);
        assertEquals(SortOption.AMOUNT_ASC, SortOption.valueOf("AMOUNT_ASC"));
        assertEquals(TransactionType.INCOME, TransactionType.valueOf("INCOME"));
        assertEquals(2, TransactionType.values().length);
    }
}
