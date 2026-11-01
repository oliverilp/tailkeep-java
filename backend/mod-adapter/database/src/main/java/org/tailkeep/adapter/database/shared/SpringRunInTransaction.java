package org.tailkeep.adapter.database.shared;

import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.tailkeep.domain.shared.RunInTransaction;

import java.util.function.Supplier;

@Component
public class SpringRunInTransaction implements RunInTransaction {
    private final TransactionTemplate template;

    public SpringRunInTransaction(PlatformTransactionManager transactionManager) {
        this.template = new TransactionTemplate(transactionManager);
    }

    @Override
    public <T> T execute(Supplier<T> work) {
        return template.execute(status -> work.get());
    }

    @Override
    public void execute(Runnable work) {
        template.executeWithoutResult(status -> work.run());
    }
}
