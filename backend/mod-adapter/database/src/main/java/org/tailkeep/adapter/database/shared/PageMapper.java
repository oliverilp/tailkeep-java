package org.tailkeep.adapter.database.shared;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.tailkeep.domain.shared.PageQuery;
import org.tailkeep.domain.shared.PageResult;
import org.tailkeep.domain.shared.Sort;

import java.util.function.Function;

/** Translates between the domain's paging types and Spring Data's. */
public final class PageMapper {
    private PageMapper() {
    }

    public static Pageable toPageable(PageQuery query) {
        Sort sort = query.sort();
        org.springframework.data.domain.Sort.Direction direction = sort.direction() == Sort.Direction.ASC
                ? org.springframework.data.domain.Sort.Direction.ASC
                : org.springframework.data.domain.Sort.Direction.DESC;
        // Spring counts pages from 0, the domain from 1
        return PageRequest.of(query.page() - 1, query.size(),
                org.springframework.data.domain.Sort.by(direction, sort.property()));
    }

    public static <T, R> PageResult<R> toPageResult(Page<T> page, Function<T, R> mapper) {
        return new PageResult<>(
                page.getContent().stream().map(mapper).toList(),
                page.getNumber() + 1,
                page.getSize(),
                page.getTotalElements());
    }
}
