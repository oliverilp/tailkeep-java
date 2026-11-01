package org.tailkeep.adapter.database.shared;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.tailkeep.domain.shared.PageQuery;
import org.tailkeep.domain.shared.PageResult;
import org.tailkeep.domain.shared.Sort;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PageMapperTest {
    @Test
    void convertsTheFirstPageToSpringPageZero() {
        Pageable pageable = PageMapper.toPageable(new PageQuery(1, 10, Sort.descending("createdAt")));

        assertThat(pageable.getPageNumber()).isZero();
        assertThat(pageable.getPageSize()).isEqualTo(10);
        assertThat(pageable.getSort().getOrderFor("createdAt").getDirection()).isEqualTo(Direction.DESC);
    }

    @Test
    void keepsAnAscendingOrder() {
        Pageable pageable = PageMapper.toPageable(new PageQuery(3, 5, Sort.ascending("title")));

        assertThat(pageable.getPageNumber()).isEqualTo(2);
        assertThat(pageable.getSort().getOrderFor("title").getDirection()).isEqualTo(Direction.ASC);
    }

    @Test
    void convertsSpringPageZeroBackToPageOne() {
        Pageable pageable = PageMapper.toPageable(new PageQuery(2, 2, Sort.descending("createdAt")));
        Page<Integer> page = new PageImpl<>(List.of(3, 4), pageable, 5);

        PageResult<String> result = PageMapper.toPageResult(page, number -> "n" + number);

        assertThat(result.items()).containsExactly("n3", "n4");
        assertThat(result.page()).isEqualTo(2);
        assertThat(result.size()).isEqualTo(2);
        assertThat(result.totalItems()).isEqualTo(5);
        assertThat(result.totalPages()).isEqualTo(3);
        assertThat(result.hasNext()).isTrue();
        assertThat(result.hasPrevious()).isTrue();
    }
}
