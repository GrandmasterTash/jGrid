package com.notlob.jgrid.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Display;

import com.notlob.jgrid.model.filtering.CollapsedGroupFilter;
import com.notlob.jgrid.model.filtering.Filter;
import com.notlob.jgrid.model.filtering.FilterModel;
import com.notlob.jgrid.providers.IGridContentProvider;
import com.notlob.jgrid.providers.IGridLabelProvider;
import com.notlob.jgrid.styles.CellStyle;
import com.notlob.jgrid.styles.StyleRegistry;

public class GridModel<T> {

	// Visible columns and rows.
	private final List<Row<T>> rows;
	private final List<Column> columns;
	
	// All column definitions.
	private final List<Column> allColumns;

	// Rows which have been filtered out - they are not ordered.
	private final List<Row<T>> hiddenRows;
	
	// All Rows (including hidden), keyed by domain element.
	private final Map<T, Row<T>> rowsByElement;
	
	// Visible column headers, pinned rows, etc.
	private final List<Row<T>> columnHeaderRows;

	// If we're grouping data by particular column(s).
	private final List<Column> groupByColumns;

	// Selection model (also Row has a selected property).
	private final SelectionModel<T> selectionModel;

	// The sort model.
	private final SortModel<T> sortModel;

	// The filter model.
	private final FilterModel<T> filterModel;
	
	// Visible styling model.
	private final StyleRegistry<T> styleRegistry;
	
	// Show/hide the row numbers.
	private boolean showRowNumbers = false;

	// These are notified whenever something changes.
	private final List<IModelListener> listeners;

	// Providers to get / format data, images, tool-tips, etc.
	private IGridContentProvider<T> contentProvider;
	private IGridLabelProvider<T> labelProvider;
	
	public interface IModelListener {
		void modelChanged();
		void selectionChanged();
	}

	public GridModel() {
		rows = new ArrayList<>();
		rowsByElement = new LinkedHashMap<>();
		hiddenRows = new ArrayList<>();
		columns = new ArrayList<>();
		allColumns = new ArrayList<>();
		columnHeaderRows = new ArrayList<>();
		groupByColumns = new ArrayList<>();
		listeners = new ArrayList<>();
		styleRegistry = new StyleRegistry<T>();
		selectionModel = new SelectionModel<T>(this);
		sortModel = new SortModel<T>(this);
		filterModel = new FilterModel<T>(this);
	}

	public StyleRegistry<T> getStyleRegistry() {
		checkWidget();
		return styleRegistry;
	}

	public SelectionModel<T> getSelectionModel() {
		checkWidget();
		return selectionModel;
	}

	public SortModel<T> getSortModel() {
		return sortModel;
	}
	
	public FilterModel<T> getFilterModel() {
		return filterModel;
	}

	public List<Column> getColumns() {
		checkWidget();
		return columns;
	}

	public List<Column> getAllColumns() {
		checkWidget();
		return allColumns;
	}

	public List<Column> getGroupByColumns() {
		checkWidget();
		return groupByColumns;
	}
	
	/**
	 * Returns all of the visible elements in the grid. Not a performant method.
	 */
	public List<T> getElements() {
		checkWidget();
		
		//
		// To ensure the elements are in visible sequence, do this.
		//
		final List<T> elements = new ArrayList<>();
		for (Row<T> row : rows) {
			elements.add(row.getElement());
		}
		
		return elements;
	}
	
	public Collection<T> getSelection() {
		checkWidget();
		return selectionModel.getSelectedElements();
	}
	
	/**
	 * The number of VISIBLE rows.
	 */
	public int getRowCount(final boolean visible, final RowCountScope scope) {
		final Collection<Row<T>> rowsToCount = visible ? rows : hiddenRows;
		
		switch (scope) {
			case ALL:
				return rowsToCount.size();
				
			case CHILDREN:
				int childCount = 0;
				for (Row<T> row : rowsToCount) {
					if (!isParentRow(row)) {
						childCount++;
					}
				}
				return childCount;
				
			case PARENTS:
				int parentCount = 0;
				for (Row<T> row : rowsToCount) {
					if (isParentRow(row) || !isGroupRow(row)) {
						parentCount++;
					}
				}
				return parentCount;
		}
		
		return -1;
	}

	public List<Row<T>> getRows() {
		checkWidget();
		return rows;
	}
	
	public Collection<Row<T>> getHiddenRows() {
		checkWidget();
		return hiddenRows;
	}
	
	public Collection<Row<T>> getAllRows() {
		checkWidget();
		return rowsByElement.values();
	}

	public List<Row<T>> getColumnHeaderRows() {
		checkWidget();
		return columnHeaderRows;
	}

	public Row<T> getRow(final T element) {
		checkWidget();
		return rowsByElement.get(element);
	}

	Map<T, Row<T>> getRowsByElement() {
		return rowsByElement;
	}

	public void setLabelProvider(final IGridLabelProvider<T> labelProvider) {
		this.labelProvider = labelProvider;
	}
	
	public void setContentProvider(final IGridContentProvider<T> contentProvider) {
		this.contentProvider = contentProvider;
		
		//
		// Add a collapsed group filter to the model.
		//
		this.filterModel.addFilters(Collections.singletonList((Filter<T>) new CollapsedGroupFilter<T>(contentProvider)));
	}	

	public IGridContentProvider<T> getContentProvider() {
		return contentProvider;
	}

	private void addColumn(final Column column) {
		//
		// Check the columnId isn't already in use.
		//
		for (final Column existing : columns) {
			if (column.getColumnId().equals(existing.getColumnId())) {
				throw new IllegalArgumentException(String.format("Duplicate column id %s", column.getColumnId()));
			}
		}

		allColumns.add(column);
		
		if (column.getSortDirection() != SortDirection.NONE) {
			getSortModel().sort(column, false, true, false);
		}
	}

	@SuppressWarnings("unchecked")
	public void addColumns(final List<Column> columns) {
		checkWidget();

		final boolean anyWereVisible = !this.columns.isEmpty();
		boolean anyNowVisible = false;

		for (final Column column : columns) {
			addColumn(column);
			anyNowVisible |= column.isVisible();
		}

		rebuildVisibleColumns();

		if (!anyWereVisible && anyNowVisible) {
			//
			// The first column should cause a row to be added for the column headers. This header row should be the first row in the header region.
			//
			columnHeaderRows.add(0, Row.COLUMN_HEADER_ROW);

			//
			// Build a row style for the header row.
			//
			styleRegistry.setCellStyle(Row.COLUMN_HEADER_ROW, styleRegistry.getDefaultHeaderStyle());
		}

		fireChangeEvent();
	}

	private void removeColumn(final Column column) {
		sortModel.removeColumn(column);
		allColumns.remove(column);
		columns.remove(column);
		groupByColumns.remove(column);
	}

	public void removeColumns(final List<Column> columns) {
		checkWidget();

		for (final Column column : columns) {
			removeColumn(column);
		}

		rebuildVisibleColumns();

		if (this.columns.isEmpty()) {
			columnHeaderRows.remove(0);
		}

		fireChangeEvent();
	}

	private void updateColumn(final Column column) {
		if (allColumns.indexOf(column) == -1) {
			throw new IllegalArgumentException("The specified column cannot be updated until it's been added.");
		}
	}

	public void updateColumns(final List<Column> columns) {
		checkWidget();

		for (final Column column : columns) {
			updateColumn(column);
		}

		rebuildVisibleColumns();
		fireChangeEvent();
	}

	private void rebuildVisibleColumns() {
		columns.clear();

		for (final Column column : allColumns) {
			if (column.isVisible()) {
				columns.add(column);
			}
		}
	}

	public Column getColumnById(final String columnId) {
		checkWidget();
		for (final Column column : allColumns) {
			if (column.getColumnId().equalsIgnoreCase(columnId)) {
				return column;
			}
		}
		return null;
	}

	public void addElements(final List<T> elements) {
		checkWidget();
		
		for (final T element : elements) {
			//
			// If this element has a parent that's not here yet ignore it - the parent will add all it's children later. Otherwise, groups grids will get
			// populated with rows that initially (until a re-sort) don't know they should be styled as a group and the rows can be spread through-out the grid
			// as children appear way before their parents.
			//
//			if (rowsByElement.containsKey(element) || (isChildElement(element) && !rowsByElement.containsKey(getParentElement(element)))) {
//				continue;
//			}
			
			//
			// Add a row for the element.
			//
			final Row<T> row = new Row<T>(element);
			row.setHeight(labelProvider.getDefaultRowHeight(element));
			addRow(row);
			
			//
			// Add any children now.
			//
//			if (isParentElement(element)) {
//				final Object[] children = contentProvider.getChildren(row.getElement());
//				for (Object child : children) {
//					final Row childRow = new Row(child);
//					childRow.setHeight(labelProvider.getDefaultRowHeight(child));
//					addRow(childRow);
//				}
//			}
		}

		fireChangeEvent();
	}
	
	private void addRow(final Row<T> row) {
		
		//
		// Check the filter model.
		//
		if (filterModel.match(row)) {
			//
			// Make the row visible.
			//
			showRow(row);
			
		} else {
			hideRow(row);			
		}
		
		//
		// Cache the row by it's domain element.
		//
		rowsByElement.put(row.getElement(), row);
	}
	
	public void removeElements(final List<T> elements) {
		checkWidget();
		
		for (final T element : elements) {
			final Row<T> row = rowsByElement.get(element);
			rows.remove(row);
			hiddenRows.remove(row);
			rowsByElement.remove(element);
			
			if (row.isSelected()) {
				selectionModel.removeRow(row);
			}
			
			if (row.isPinned()) {
				columnHeaderRows.remove(row);
			}
		}

		fireChangeEvent();
	}
	
	public void updateElements(final List<T> elements) {
		checkWidget();

// TODO: To Ensure the row maintains it's position,	we might need it to cache it's visible index - to be fast.
// TODO: Consider groups need to compare with other groups and children with children.		
//		for (Object element : elements) {
//			final Row row = rowsByElement.get(element);
//			final int expectedIndex = sortModel.getSortedRowIndex(row);
//			final int actualIndex = 
//			
//		}
		
		// TODO: Check the filter model....
		
		fireChangeEvent();
	}
	
	public void clearElements() {
		checkWidget();
		
		//
		// Clear rows.
		//
		rows.clear();
		rowsByElement.clear();

		//
		// Clear all selections.
		//
		selectionModel.clear();	
		
		fireChangeEvent();
	}

	public void showRow(final Row<T> row) {
		final int insertIndex = sortModel.getSortedRowIndex(row);

		if (insertIndex >= 0) {
			rows.add(insertIndex, row);
		} else {
			rows.add(row);
		}
		
		row.setVisible(true);
		hiddenRows.remove(row);
	}
	
	public void hideRow(final Row<T> row) {
		rows.remove(row);
		selectionModel.removeRow(row);
		hiddenRows.add(row);
		row.setVisible(false);
	}
	
	public void groupBy(final List<Column> columns) {
		checkWidget();

		groupByColumns.addAll(columns);
		
		//
		// Hide the columns.
		//
		for (Column column : columns) {
			column.setVisible(false);
		}
		
		rebuildVisibleColumns();

//		//
//		// Sort by the value (in-future we can toggle these from the group widgets).
//		//
//		column.setSortDirection(SortDirection.ASC);
//		sortModel.sort(column, false, true);

		fireChangeEvent();
	}

	public void ungroupBy(final Column column) {
		checkWidget();

		//
		// Reveal the column again.
		//
		groupByColumns.remove(column);
		column.setVisible(true);
		rebuildVisibleColumns();
		fireChangeEvent();
	}

	public void ungroupAll() {
		checkWidget();

		//
		// Reveal the column again.
		//
		for (final Column column : groupByColumns) {
			column.setVisible(true);
		}
		rebuildVisibleColumns();

		//
		// Rebuild the model's groups
		//
		groupByColumns.clear();
		fireChangeEvent();
	}

	public void addListener(final IModelListener listener) {
		checkWidget();
		listeners.add(listener);
	}

	public void removeListener(final IModelListener listener) {
		checkWidget();
		listeners.remove(listener);
	}

	public void fireChangeEvent() {
		for (final IModelListener listener : listeners) {
			listener.modelChanged();
		}
	}
	
	void fireSelectionChangedEvent() {
		for (final IModelListener listener : listeners) {
			listener.selectionChanged();
		}
	}

	// TODO: Remove this, and ensure nothing outside of the grid access this class.
	void checkWidget() {
		if (Display.getCurrent() == null) {
			throw new SWTException(SWT.ERROR_THREAD_INVALID_ACCESS);
		}
	}
	
	public boolean isShowRowNumbers() {
		checkWidget();
		return showRowNumbers;
	}

	public void setShowRowNumbers(final boolean showRowNumbers) {
		checkWidget();
		this.showRowNumbers = showRowNumbers;
		fireChangeEvent();
	}

	public boolean isHeaderRow(final Row<T> row) {
		return columnHeaderRows.contains(row);
	}

	public int getRowHeight(final GC gc, final Row<T> row) {
		final CellStyle cellStyle = styleRegistry.getCellStyle(row);
		return row.getHeight(gc, cellStyle);
	}

	/**
	 * Indicates if the row is in a group. Either if it has a parent, or if it has children (or could have children).
	 */
	public boolean isGroupRow(final Row<T> row) {
		if (row.getElement() != null) {
			//
			// The row has a parent.
			//
			if (contentProvider.getParent(row.getElement()) != null) {
				return true;
			}

			//
			// The row has (or could have) children.
			//
			if (isParentRow(row)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * If the row has a child list (even if it's empty) it's a parent row.
	 */
	public boolean isParentRow(final Row<T> row) {
		return isParentElement(row.getElement());
	}

	/**
	 * If the row has a child list (even if it's empty) it's a parent row.
	 */
	public boolean isParentElement(final T element) {
		return ((element != null) && (contentProvider.getChildren(element) != null));
	}
	
	public boolean isChildElement(final T element) {
		return (contentProvider.getParent(element) != null);
	}
	
	public T getParentElement(final T element) {
		return contentProvider.getParent(element);
	}

	/**
	 * Return the row's parent, or the row itself if it has none.
	 */
	public T getParentOrOwnElement(final Row<T> row) {
		final T parent = getContentProvider().getParent(row.getElement());
		return parent == null ? row.getElement() : parent;
	}

	public boolean isSameGroup(final Row<T> row1, final Row<T> row2) {
		return (isGroupRow(row1) && isGroupRow(row2) && (getParentOrOwnElement(row1) == getParentOrOwnElement(row2)));
	}

	/**
	 * If the row is in a group return the entire group. Only the immediate group or below is returned.
	 *
	 * Parent groups are not included.
	 */
	public List<Row<T>> getWholeGroup(final Row<T> row) {
		final List<Row<T>> group = new ArrayList<>();

		final T parentElement = contentProvider.getParent(row.getElement());
		final T[] childElements = contentProvider.getChildren(row.getElement());
		if (parentElement != null) {
			//
			// If this row has a parent. Include all the parent's children/grand-children.
			//
			final Row<T> parentRow = rowsByElement.get(parentElement);
			group.addAll(getAllChildren(parentRow));

		} else if (childElements != null) {
			//
			// If this row has any children, ensure they (and their grand-children are included.
			//
			group.add(row);
			for (final Row<T> childRow : getChildren(row)) {
				group.addAll(getAllChildren(childRow));
			}
		}

		return group;
	}

	/**
	 * Return the row and all children and grandchildren for this row.
	 */
	private List<Row<T>> getAllChildren(final Row<T> row) {
		final List<Row<T>> group = new ArrayList<>();
		group.add(row);

		for (final Row<T> childRow : getChildren(row)) {
			group.addAll(getAllChildren(childRow));
		}
		return group;
	}

	/**
	 * Return immediate children from this row.
	 */
	private List<Row<T>> getChildren(final Row<T> row) {
		final List<Row<T>> children = new ArrayList<>();
		final T[] childElements = contentProvider.getChildren(row.getElement());

		if (childElements != null) {
			for (final T childElement : childElements) {
				children.add(rowsByElement.get(childElement));
			}
		}

		return children;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append(String.format("%s\n", this.getClass().getSimpleName()));
		sb.append(String.format("Show Row Numbers: %s\n", showRowNumbers));
		sb.append(String.format("Rows: %s (%s selected)\n", rowsByElement.size(), selectionModel.getSelectedElements().size()));

		sb.append(String.format("Columns (%s total %s shown)\n", allColumns.size(), columns.size()));
		for (final Column column : allColumns) {
			sb.append(String.format("\t%s\n", column));
		}

		sb.append(String.format("Grouping By\n"));

		if (groupByColumns.isEmpty()) {
			sb.append("\t(none)\n");
		} else {
			for (final Column column : groupByColumns) {
				sb.append(String.format("\t%s\n", column.getColumnId()));
			}
		}



		return sb.toString();
	}

	public List<Row<T>> getRowsForElements(final Set<T> elements) {
		checkWidget();

		final List<Row<T>> rows = new ArrayList<>();
		for (final Object element : elements) {
			rows.add(rowsByElement.get(element));
		}
		return rows;
	}

	public void pinRows(final List<Row<T>> rows) {
		checkWidget();

		for (final Row<T> row : rows) {
			row.setPinned(true);
			columnHeaderRows.add(row);
		}

		fireChangeEvent();
	}

}
