package org.tailkeep.domain.shared;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PageQueryTest {
    private static final Sort SORT = Sort.descending("createdAt");

    @Test
    void keepsItsValues() {
        PageQuery query = new PageQuery(2, 10, SORT);

        assertThat(query.page()).isEqualTo(2);
        assertThat(query.size()).isEqualTo(10);
        assertThat(query.sort()).isEqualTo(new Sort("createdAt", Sort.Direction.DESC));
    }

    @Test
    void pagesStartAtOne() {
        assertThatThrownBy(() -> new PageQuery(0, 10, SORT)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void aPageHoldsAtLeastOneItem() {
        assertThatThrownBy(() -> new PageQuery(1, 0, SORT)).isInstanceOf(IllegalArgumentException.class);
    }
}
