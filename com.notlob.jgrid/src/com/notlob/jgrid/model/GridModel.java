package com.notlob.jgrid.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.graphics.GC;

import com.notlob.jgrid.Grid;
import com.notlob.jgrid.model.filtering.CollapsedGroupFilter;
import com.notlob.jgrid.model.filtering.Filter;
import com.notlob.jgrid.model.filtering.FilterModel;
import com.notlob.jgrid.providers.IGridContentProvider;
import com.notlob.jgrid.providers.IGridLabelProvider;
import com.notlob.jgrid.styles.CellStyle;
import com.notlob.jgrid.styles.StyleRegistry;
import com.notlob.jgrid.util.ResourceManager;

/**
 *
 * 
 * NOTE: This is an internal class not to be manipulated by client code.
 * 
 * @author Stef
 */
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

	// These external listeners are notified whenever something changes.
	private final List<IModelListener<T>> listeners;

	// Providers to get / format data, images, tool-tips, etc.
	private IGridContentProvider<T> contentProvider;
	private IGridLabelProvider<T> labelProvider;
	
	// Used in row height calculations.
	private final ResourceManager resourceManager;
	private final GC gc;
	
	// A reference count which, if greater than zero means the grid will stop redrawing, recalculating scrollbars/viewport,
	// and stop firing rowCount-change notifications to any listeners.
	private int suppressedEvents = 0;

	// An internal listener so the grid can broker events to public listeners or react to internal changes.
	public interface IModelListener<T> {
		void modelChanged();
		void selectionChanged();
		void heightChanged(final int delta);
		void rowCountChanged();
		void filtersChanged();
		void elementsAdded(final Collection<T> elements);
		void elementsUpdated(final Collection<T> elements);
		void elementsRemoved(final Collection<T> elements);
		void columnResized(final Column column);		
		void columnMoved(final Column column);		
		void columnSorted(final Column column);
	}

	public GridModel(final Grid<T> grid, final ResourceManager resourceManager, final GC gc) {
		this.resourceManager = resourceManager;
		this.gc = gc; 
		rows = new ArrayList<>();
		rowsByElement = new LinkedHashMap<>();
		hiddenRows = new ArrayList<>();
		columns = new ArrayList<>();
		allColumns = new ArrayList<>();
		columnHeaderRows = new ArrayList<>();
		groupByColumns = new ArrayList<>();
		listeners = new ArrayList<>();
		styleRegistry = new StyleRegistry<T>(grid);
		selectionModel = new SelectionModel<T>(this);
		sortModel = new SortModel<T>(this);
		filterModel = new FilterModel<T>(this);
	}
	
	public void enableEvents(final boolean enable) {
		if (enable) {
			suppressedEvents--;
			
		} else {
			suppressedEvents++;
		}
		
		if (suppressedEvents < 0) {
			throw new IllegalArgumentException("Suppressed event count already " + suppressedEvents);
			
		} else if (suppressedEvents == 0) {
			//
			// Re-index the rows.
			//
			reindex();
			
			//
			// If we're no-longer suppressing events then trigger a redraw.
			//
			fireChangeEvent();
			fireRowCountChangedEvent();
		}		
	}
	
	public boolean isEventsSuppressed() {
		return (suppressedEvents > 0);
	}

	public StyleRegistry<T> getStyleRegistry() {
		return styleRegistry;
	}

	public SelectionModel<T> getSelectionModel() {
		return selectionModel;
	}

	public SortModel<T> getSortModel() {
		return sortModel;
	}

	public FilterModel<T> getFilterModel() {
		return filterModel;
	}

	public List<Column> getColumns() {
		return columns;
	}

	public List<Column> getAllColumns() {
		return allColumns;
	}

	public List<Column> getGroupByColumns() {
		return groupByColumns;
	}

	/**
	 * Returns all of the visible elements in the grid. Not a performant method.
	 */
	public List<T> getElements() {		
		//
		// To ensure the elements are in visible sequence, do this.
		//
		final List<T> elements = new ArrayList<>(rows.size());
		for (final Row<T> row : rows) {
			elements.add(row.getElement());
		}

		return elements;
	}

	public Collection<T> getSelection() {
		return selectionModel.getSelectedElements();
	}

	/**
	 * The number of visible or hidden rows.
	 */
	public int getDetailedRowCount(final boolean visible, final RowCountScope scope) {

		final Collection<Row<T>> rowsToCount = visible ? rows : hiddenRows;
		switch (scope) {
			case ALL:
				return rowsToCount.size();

			case CHILDREN:
				int childCount = 0;
				for (final Row<T> row : rowsToCount) {
					if (!isParentRow(row)) {
						childCount++;
					}
				}
				return childCount;

			case PARENTS:
				int parentCount = 0;
				for (final Row<T> row : rowsToCount) {
					if (isParentRow(row)) {
						parentCount++;
					}
				}
				return parentCount;
		}

		return -1;
	}

	public List<Row<T>> getRows() {
		return rows;
	}

	public Collection<Row<T>> getHiddenRows() {
		return hiddenRows;
	}

	public Collection<Row<T>> getAllRows() {
		return rowsByElement.values();
	}

	public List<Row<T>> getColumnHeaderRows() {
		return columnHeaderRows;
	}

	public Row<T> getRow(final T element) {
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
		// The mandatory filter needs the contentprovider.
		//
		clearFilters();
	}
	
	public void clearFilters() {
		filterModel.clear();
		
		//
		// Add a collapsed group filter to the model. It provides the ability to collapse/expand groups.
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
	
	public void clearColumns() {
		removeColumns(allColumns);
	}

	private void removeColumn(final Column column) {
		sortModel.removeColumn(column);
		allColumns.remove(column);
		columns.remove(column);
		groupByColumns.remove(column);
	}

	public void removeColumns(final List<Column> columns) {
		for (final Column column : columns) {
			removeColumn(column);
		}

		rebuildVisibleColumns();

		if (this.columns.isEmpty() && !columnHeaderRows.isEmpty()) {
			columnHeaderRows.clear();
		}

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
		for (final Column column : allColumns) {
			if (column.getColumnId().equalsIgnoreCase(columnId)) {
				return column;
			}
		}
		return null;
	}

	public void addElements(final Collection<T> elements) {
		int heightDelta = 0;
		
		for (final T element : elements) {
			//
			// Add a row for the element.
			//
			final Row<T> row = new Row<T>(element);
			row.setHeight(labelProvider.getDefaultRowHeight(element));
			
			if (addRow(row)) {
				heightDelta += getRowHeight(row);
			}
		}
		
		//
		// Reseed the row-indexes if there's been any move or show/hiding.
		//
		reindex();

		if (heightDelta != 0) {
			fireHeightChangeEvent(heightDelta);
		}
		
		fireElementsAddedEvent(elements);		
		fireRowCountChangedEvent();
	}

	private boolean addRow(final Row<T> row) {

		//
		// Cache the row by it's domain element.
		//
		rowsByElement.put(row.getElement(), row);
		
		//
		// Check the filter model.
		//
		if (filterModel.match(row)) {
			//
			// Make the row visible.
			//
			showRow(row);
			return true;

		} else {
			hideRow(row);
		}

		return false;
	}

	public void removeElements(final Collection<T> elements) {
		int heightDelta = 0;
		int lastSelectedIndex = -1;
		
		for (final T element : elements) {
			final Row<T> row = rowsByElement.get(element);
			heightDelta -= getRowHeight(row);			
			rows.remove(row);
			hiddenRows.remove(row);
			rowsByElement.remove(element);

			if (row.isSelected()) {
				selectionModel.removeRow(row);
				lastSelectedIndex = Math.max(lastSelectedIndex, row.getRowIndex());
			}

			if (row.isPinned()) {
				columnHeaderRows.remove(row);
			}
		}
		
		//
		// If there WAS a selection and now there is NONE then select the row or group AFTER the last 
		// previously selected row or group.
		//
		if ((lastSelectedIndex != -1) && (selectionModel.getSelectedElements().isEmpty())) {
			final int nextIndex = lastSelectedIndex - elements.size() + 1;
			
			if ((nextIndex >= 0) && (nextIndex <= (rows.size()-1))) {
				final Row<T> row = rows.get(nextIndex);
				final List<Row<T>> rowsToSelect = isGroupRow(row) ? getWholeGroup(row) : Collections.singletonList(row); 
				selectionModel.setSelectedRows(rowsToSelect);
			}
		}
		
		//
		// Reseed the row-indexes if there's been any move or show/hiding.
		//
		reindex();

		if (heightDelta != 0) {
			fireHeightChangeEvent(heightDelta);
		}
		
		fireElementsRemovedEvent(elements);
		fireRowCountChangedEvent();
	}

	public void updateElements(final Collection<T> elements) {
		int heightDelta = 0;
		
		for (Object element : elements) {
			final Row<T> row = rowsByElement.get(element);
			
			if (row != null) {
				//
				// Should the row be shown/hidden?
				//
				final boolean visible = filterModel.match(row);
				
				if (visible && row.isVisible()) {
					//
					// Should the row move?
					//
					final int expectedIndex = Math.abs(sortModel.getSortedRowIndex(row));
					final int actualIndex = row.getRowIndex();
					
					if (expectedIndex != actualIndex) {
						//
						// Move the row to the correct position.
						//										
						rows.remove(row);
						final int newEexpectedIndex = sortModel.getSortedRowIndex(row);					
						rows.add(newEexpectedIndex, row);
						row.setRowIndex(newEexpectedIndex);
					}
					
				} else if (visible && !row.isVisible()) {
					//
					// Reveal the row.
					//
					showRow(row);
					heightDelta += getRowHeight(row);
					
				} else if (!visible && row.isVisible()) {
					//
					// Hide the row.
					//
					hideRow(row);				
					heightDelta -= getRowHeight(row);
				}
			}
		}
		
		//
		// Reseed the row-indexes if there's been any move or show/hiding.
		//
		reindex();

		//
		// If the height of the rows has changed, adjust the grid's scroll-bars.
		//
		if (heightDelta != 0) {
			fireHeightChangeEvent(heightDelta);
			fireElementsUpdatedEvent(elements);
			fireRowCountChangedEvent();
			
		} else {
			fireElementsUpdatedEvent(elements);
			fireChangeEvent();
		}
	}
	
	public void reindex() {
		if (!isEventsSuppressed()) {
			int rowIndex = 0;
			for (Row<T> row : rows) {
				row.setRowIndex(rowIndex++);
			}
		}
	}

	public void clearElements() {
		//
		// Clear all selections.
		//
		selectionModel.clear(false);
		
		//
		// Clear rows.
		//
		rows.clear();
		hiddenRows.clear();
		rowsByElement.clear();

		fireChangeEvent();
	}

	public void showRow(final Row<T> row) {
		final int insertIndex = sortModel.getSortedRowIndex(row);

		if (insertIndex >= 0) {
			rows.add(insertIndex, row);
			row.setRowIndex(insertIndex);
			
		} else {
			rows.add(row);
			row.setRowIndex(rows.size()-1);
		}
		
		row.setVisible(true);
		hiddenRows.remove(row);
	}

	public void hideRow(final Row<T> row) {
		rows.remove(row);
		selectionModel.removeRow(row);
		hiddenRows.add(row);
		row.setVisible(false);
		row.setRowIndex(-1);
	}

	public void groupBy(final List<Column> columns) {
		groupByColumns.addAll(columns);

		//
		// Hide the columns.
		//
		for (final Column column : columns) {
			column.setVisible(false);
		}

		rebuildVisibleColumns();
		fireChangeEvent();
	}

	public void ungroupBy(final List<Column> columns) {
		//
		// Reveal the column again.
		//
		for (Column column : columns) {
			column.setVisible(true);
		}
		
		groupByColumns.removeAll(columns);		
		rebuildVisibleColumns();
		fireChangeEvent();
	}

	public void ungroupAll() {
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

	public void addListener(final IModelListener<T> listener) {
		listeners.add(listener);
	}

	public void removeListener(final IModelListener<T> listener) {
		listeners.remove(listener);
	}

	/**
	 * Causes the grid to rebuild the viewport and scrollbars, redraw, then notify clients.
	 */
	public void fireChangeEvent() {
		for (final IModelListener<T> listener : listeners) {
			listener.modelChanged();
		}
	}
	
	/**
	 * Causes the grid to resize the vertical scroll bar and redraw.
	 */
	public void fireHeightChangeEvent(final int delta) {
		for (final IModelListener<T> listener : listeners) {
			listener.heightChanged(delta);
		}
	}
	
	/**
	 * Causes the grid to notify clients the rows, or filtered row counts *may* have changed.
	 */
	public void fireRowCountChangedEvent() {
		for (final IModelListener<T> listener : listeners) {
			listener.rowCountChanged();
		}
	}
	
	public void fireElementsAddedEvent(final Collection<T> elements) {
		for (final IModelListener<T> listener : listeners) {
			listener.elementsAdded(elements);
		}
	}
	
	public void fireElementsUpdatedEvent(final Collection<T> elements) {
		for (final IModelListener<T> listener : listeners) {
			listener.elementsUpdated(elements);
		}
	}
	
	public void fireElementsRemovedEvent(final Collection<T> elements) {
		for (final IModelListener<T> listener : listeners) {
			listener.elementsRemoved(elements);
		}
	}
	
	void fireSelectionChangedEvent() {
		for (final IModelListener<T> listener : listeners) {
			listener.selectionChanged();
		}
	}
	
	public void fireFiltersChangedEvent() {
		for (final IModelListener<T> listener : listeners) {
			listener.filtersChanged();
		}
	}
	
	public void fireColumnMovedEvent(Column column) {
		for (final IModelListener<T> listener : listeners) {
			listener.columnMoved(column);
		}
	}
	
	public void fireColumnResizedEvent(Column column) {
		for (final IModelListener<T> listener : listeners) {
			listener.columnResized(column);
		}
	}
	
	public void fireColumnSortedEvent(Column column) {
		for (final IModelListener<T> listener : listeners) {
			listener.columnSorted(column);
		}
	}

	public boolean isShowRowNumbers() {
		return showRowNumbers;
	}

	public void setShowRowNumbers(final boolean showRowNumbers) {
		this.showRowNumbers = showRowNumbers;
		fireChangeEvent();
	}

	public boolean isHeaderRow(final Row<T> row) {
		return columnHeaderRows.contains(row);
	}

	public int getRowHeight(final Row<T> row) {
		final CellStyle cellStyle = styleRegistry.getCellStyle(row);
		return row.getHeight(resourceManager, gc, cellStyle);
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
		final List<T> childElements = contentProvider.getChildren(row.getElement());
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
				if (childRow != null) {
					group.addAll(getAllChildren(childRow));
				}
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
		final List<T> childElements = contentProvider.getChildren(row.getElement());

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

	public void pinRows(final List<Row<T>> rows) {
		for (final Row<T> row : rows) {
			row.setPinned(true);
			columnHeaderRows.add(row);
		}

		fireChangeEvent();
	}
}
