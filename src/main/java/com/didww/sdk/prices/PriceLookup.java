package com.didww.sdk.prices;

import java.util.Collections;
import java.util.List;

/**
 * The answer of a price lookup. It always holds at least one row: no match
 * raises {@link com.didww.sdk.exception.DidwwPricesException} with
 * {@link com.didww.sdk.exception.PricesErrorKind#PRICE_NOT_FOUND}.
 *
 * @param <T> the row type of the price list
 */
public final class PriceLookup<T> {
    private final List<T> rows;
    private final int totalRecords;

    PriceLookup(List<T> rows, int totalRecords) {
        this.rows = Collections.unmodifiableList(rows);
        this.totalRecords = totalRecords;
    }

    /**
     * Returns the matching rows.
     *
     * @return the rows, never empty
     */
    public List<T> getRows() {
        return rows;
    }

    /**
     * Returns the number of matching rows ({@code meta.total_records}).
     *
     * @return the number of rows, or 0 when the answer does not tell
     */
    public int getTotalRecords() {
        return totalRecords;
    }

    @Override
    public String toString() {
        return "PriceLookup{totalRecords=" + totalRecords + ", rows=" + rows + "}";
    }
}
