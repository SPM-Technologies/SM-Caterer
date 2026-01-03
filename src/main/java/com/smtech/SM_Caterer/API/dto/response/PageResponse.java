package com.smtech.SM_Caterer.API.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Paginated response wrapper.
 * Contains page content and pagination metadata.
 *
 * @param <T> Type of content items
 * @author CloudCaters Team
 * @version 1.0
 * @since Phase 2
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {

    /**
     * List of items in current page.
     */
    private List<T> content;

    /**
     * Current page number (0-based).
     */
    private int page;

    /**
     * Page size.
     */
    private int size;

    /**
     * Total number of elements across all pages.
     */
    private long totalElements;

    /**
     * Total number of pages.
     */
    private int totalPages;

    /**
     * Whether this is the first page.
     */
    private boolean first;

    /**
     * Whether this is the last page.
     */
    private boolean last;

    /**
     * Whether there are more pages after this one.
     */
    private boolean hasNext;

    /**
     * Whether there are pages before this one.
     */
    private boolean hasPrevious;

    /**
     * Creates PageResponse from Spring Data Page.
     *
     * @param page Spring Data Page
     * @param <T>  Content type
     * @return PageResponse
     */
    public static <T> PageResponse<T> from(Page<T> page) {
        return PageResponse.<T>builder()
                .content(page.getContent())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .build();
    }

    /**
     * Creates PageResponse with mapped content.
     *
     * @param page    Spring Data Page (original type)
     * @param content Mapped content list
     * @param <T>     Mapped content type
     * @param <S>     Original content type
     * @return PageResponse
     */
    public static <T, S> PageResponse<T> from(Page<S> page, List<T> content) {
        return PageResponse.<T>builder()
                .content(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .build();
    }
}
