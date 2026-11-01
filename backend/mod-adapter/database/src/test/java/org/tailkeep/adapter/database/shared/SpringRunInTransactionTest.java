package org.tailkeep.adapter.database.shared;

import org.junit.jupiter.api.Test;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SpringRunInTransactionTest {
    private final PlatformTransactionManager manager = mock(PlatformTransactionManager.class);
    private final TransactionStatus status = mock(TransactionStatus.class);
    private final SpringRunInTransaction transaction = new SpringRunInTransaction(manager);

    SpringRunInTransactionTest() {
        when(manager.getTransaction(any(TransactionDefinition.class))).thenReturn(status);
    }

    @Test
    void commitsAndReturnsTheResult() {
        String result = transaction.execute(() -> "done");

        assertThat(result).isEqualTo("done");
        verify(manager).commit(status);
        verify(manager, never()).rollback(any());
    }

    @Test
    void commitsWorkWithoutAResult() {
        int[] runs = {0};

        transaction.execute(() -> runs[0]++);

        assertThat(runs[0]).isEqualTo(1);
        verify(manager).commit(status);
    }

    @Test
    void rollsBackWhenTheWorkFails() {
        assertThatThrownBy(() -> transaction.execute(() -> {
            throw new IllegalStateException("boom");
        })).isInstanceOf(IllegalStateException.class).hasMessage("boom");

        verify(manager).rollback(status);
        verify(manager, never()).commit(any());
    }
}
