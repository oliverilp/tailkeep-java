package org.tailkeep.domain.shared;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PageResultTest {
    @Test
    void firstOfSeveralPages() {
        PageResult<String> page = new PageResult<>(List.of("a", "b"), 1, 2, 5);

        assertThat(page.totalPages()).isEqualTo(3);
        assertThat(page.hasNext()).isTrue();
        assertThat(page.hasPrevious()).isFalse();
    }

    @Test
    void lastPage() {
        PageResult<String> page = new PageResult<>(List.of("e"), 3, 2, 5);

        assertThat(page.hasNext()).isFalse();
        assertThat(page.hasPrevious()).isTrue();
    }

    @Test
    void anEmptyResultHasNoPages() {
        PageResult<String> page = new PageResult<>(List.of(), 1, 10, 0);

        assertThat(page.totalPages()).isZero();
        assertThat(page.hasNext()).isFalse();
        assertThat(page.hasPrevious()).isFalse();
    }

    @Test
    void anExactFitHasNoExtraPage() {
        assertThat(new PageResult<>(List.of("a", "b"), 1, 2, 4).totalPages()).isEqualTo(2);
    }
}
