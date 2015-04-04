package com.notlob.jgrid.model.filtering;

import com.notlob.jgrid.model.Column;

/**
 * A flag on the FilterModel can be set so that rows that don't meet highlighting filters can still be shown.
 *
 * This allows us to use Highlighting filters to highlight cells in columns that match the filter.
 *
 * @author Stef
 *
 */
public interface IHighlightingFilter {

	boolean isColumnHighlighted(final Column column);

}
