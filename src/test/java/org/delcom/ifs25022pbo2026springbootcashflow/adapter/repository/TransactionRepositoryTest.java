package org.delcom.ifs25022pbo2026springbootcashflow.adapter.repository;

import org.delcom.ifs25022pbo2026springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25022pbo2026springbootcashflow.domain.entity.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TransactionRepositoryTest {

    private TransactionRepository repository;

    @BeforeEach
    void setUp() {
        repository = new TransactionRepository();
    }

    @Test
    void startsEmpty() {
        assertTrue(repository.findAll().isEmpty());
    }

    @Test
    void saveIncomeAndExpenseAssignSequentialIdsAndTypes() {
        Transaction income = repository.saveIncome("Gaji", new BigDecimal("100"));
        Transaction expense = repository.saveExpense("Kopi", new BigDecimal("5"));

        assertEquals(1, income.getId());
        assertEquals(TransactionType.INCOME, income.getType());
        assertEquals(2, expense.getId());
        assertEquals(TransactionType.EXPENSE, expense.getType());
        assertEquals(2, repository.findAll().size());
    }

    @Test
    void findAllReturnsDefensiveCopy() {
        repository.saveIncome("Gaji", BigDecimal.TEN);

        repository.findAll().clear();

        assertEquals(1, repository.findAll().size());
    }

    @Test
    void findByIdReturnsMatchOrEmpty() {
        Transaction saved = repository.saveIncome("Gaji", BigDecimal.TEN);

        assertSame(saved, repository.findById(1).orElseThrow());
        assertTrue(repository.findById(99).isEmpty());
    }

    @Test
    void deleteByIdRemovesOnlyExistingItem() {
        repository.saveIncome("Gaji", BigDecimal.TEN);
        repository.saveExpense("Kopi", BigDecimal.ONE);

        assertTrue(repository.deleteById(1));
        assertFalse(repository.deleteById(1));
        assertEquals(1, repository.findAll().size());
        assertEquals(2, repository.findAll().get(0).getId());
    }

    @Test
    void idsAreNotReusedAfterDelete() {
        repository.saveIncome("A", BigDecimal.TEN);
        repository.deleteById(1);

        assertEquals(2, repository.saveIncome("B", BigDecimal.TEN).getId());
    }

    @Test
    void findByFiltersWithPredicate() {
        repository.saveIncome("Gaji", BigDecimal.TEN);
        repository.saveExpense("Kopi", BigDecimal.ONE);

        List<Transaction> expenses = repository.findBy(Transaction::isExpense);

        assertEquals(1, expenses.size());
        assertEquals("Kopi", expenses.get(0).getDescription());
    }

    @Test
    void updateKeepsChangesMadeOnStoredObject() {
        Transaction saved = repository.saveIncome("Lama", BigDecimal.TEN);
        saved.changeDescription("Baru");

        repository.update(saved);

        assertEquals("Baru", repository.findById(1).orElseThrow().getDescription());
    }
}
