package com.notlob.jgrid.examples;

import com.notlob.jgrid.model.Column;
import com.notlob.jgrid.model.Row;
import com.notlob.jgrid.model.filtering.Filter;
import com.notlob.jgrid.model.filtering.IHighlightingFilter;
import com.notlob.jgrid.providers.IGridLabelProvider;

/**
 * Matches against rows where the column specified contains the text specified.
 * 
 * Note: If you wanted to filter on raw, unformatted values - you would use the content provider instead of the label provider.
 * 
 * @author Stef
 *
 */
public class PersonFilter extends Filter<Person> implements IHighlightingFilter {
	
	private final Column column;
	private final IGridLabelProvider<Person> labelProvider;
	private String filterText;
	
	public PersonFilter(final Column column, final IGridLabelProvider<Person> labelProvider) {
		this.column = column;
		this.labelProvider = labelProvider;
	}

	@Override
	public Column getColumn() {
		return column;
	}

	@Override
	public boolean matches(Row<Person> row) {
		if ((filterText == null) || (filterText.isEmpty())) {
			return true;
		}
		
		if (labelProvider.getText(column, row.getElement()).equalsIgnoreCase(filterText)) {
			row.addFilterMatch(this);
			return true;
		}
		
		return false;
	}

	@Override
	public String toReadableString(boolean includeStyleTokens) {
		return String.format("Filtering %s on '%s'", column.getCaption(), filterText);
	}
	
	public void setFilterText(final String filterText) {
		this.filterText = filterText;
	}

}
