package org.delcom.ifs25022pbo2026springbootcashflow.config;

import org.delcom.ifs25022pbo2026springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25022pbo2026springbootcashflow.domain.repository.ITransactionRepository;
import org.delcom.ifs25022pbo2026springbootcashflow.usecase.FinanceUseCase;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AppConfigTest {

    @Test
    void wiresBeans() {
        AppConfig config = new AppConfig();
        ITransactionRepository repository = config.transactionRepository();
        FinanceUseCase useCase = config.financeUseCase(repository);
        FinancePresenter presenter = config.financePresenter();

        assertNotNull(repository);
        assertNotNull(useCase);
        assertNotNull(presenter);
        assertTrue(useCase.getAllTransactions().isEmpty());
    }
}
