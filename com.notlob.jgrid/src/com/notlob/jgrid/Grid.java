package com.notlob.jgrid;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.ToolTip;

import com.notlob.jgrid.listeners.IGridListener;
import com.notlob.jgrid.model.Column;
import com.notlob.jgrid.model.GridModel;
import com.notlob.jgrid.model.Row;
import com.notlob.jgrid.model.Viewport;
import com.notlob.jgrid.model.filtering.Filter;
import com.notlob.jgrid.mouse.GridMouseListener;
import com.notlob.jgrid.providers.IGridContentProvider;
import com.notlob.jgrid.providers.IGridLabelProvider;
import com.notlob.jgrid.renderer.GridRenderer;
import com.notlob.jgrid.styles.StyleRegistry;

public class Grid<T> extends Composite implements GridModel.IModelListener {

	// TODO: Filtering / Searching.
	// TODO: Row updates to repaint.
	// TODO: Reposition/resize columns via DnD.
	// TODO: Focus/Keyboard navigation.
	// TODO: Row-group expand/collapse.
	// TODO: In-line editing.
	// TODO: Empty data message.
	// TODO: BUG: Empty grid's need to do what modelChanged does (i.e. calc viewport and scrollbars).
	// TODO: Column selection mode.
	// TODO: Group sorting cell tool-tips.	
	
	// Models.
	private final GridModel<T> gridModel;
	private IGridLabelProvider<T> labelProvider;
	private IGridContentProvider<T> contentProvider;
	private GridRenderer<T> gridRenderer;

	// Helpers.
	private final Viewport<T> viewport;

	// Things we listen to.
	private final ScrollListener scrollListener;
	private final ResizeListener resizeListener;
	private final DisposeListener disposeListener;
	private final GridMouseListener<T> mouseListener;

	// TODO: try..catch around all calls to listeners...
	// Things that listen to us.
	private final Collection<IGridListener<T>> listeners;

	// Used for dimension calculations.
	private final GC gc;
	private final Point computedArea;

	private final ToolTip toolTip;
	private String emptyMessage;
//	private Shell filterShell;

	public Grid(final Composite parent) {
		super(parent, SWT.V_SCROLL | SWT.H_SCROLL | SWT.DOUBLE_BUFFERED /*| SWT.NO_BACKGROUND | SWT.NO_REDRAW_RESIZE*/);
		gc = new GC(this);
		computedArea = new Point(-1, -1);

		gridModel = new GridModel<T>();
		gridModel.addListener(this);
		viewport = new Viewport<T>(this);
		gridRenderer = new GridRenderer<T>(this);
		disposeListener = new GridDisposeListener();
		resizeListener = new ResizeListener();
		scrollListener = new ScrollListener();		
		listeners = new ArrayList<>();		
				
		toolTip = new ToolTip(parent.getShell(), SWT.NONE);
		toolTip.setAutoHide(true);
		
		mouseListener = new GridMouseListener<T>(this, gc, listeners, toolTip);
		
		parent.addDisposeListener(disposeListener);
		addMouseListener(mouseListener);
		addMouseMoveListener(mouseListener);
		addMouseTrackListener(mouseListener);
		addPaintListener(gridRenderer);
		addListener(SWT.Resize, resizeListener);
		getVerticalBar().addSelectionListener(scrollListener);
		getHorizontalBar().addSelectionListener(scrollListener);
	}
		
	@Override
	public void dispose() {
		toolTip.dispose();
		
//		if (filterShell != null) {
//			filterShell.dispose();
//		}
		
		// Remove listeners.		
		removeMouseListener(mouseListener);
		removePaintListener(gridRenderer);
		removeListener(SWT.Resize, resizeListener);
		getVerticalBar().removeSelectionListener(scrollListener);
		getHorizontalBar().removeSelectionListener(scrollListener);
		gridModel.removeListener(this);

		// Dispose of UI handles.
		gc.dispose();
		super.dispose();
	}

	public GridModel<T> getGridModel() {
		checkWidget();
		return gridModel;
	}
	
	public StyleRegistry<T> getStyleRegistry() {
		checkWidget();
		return gridModel.getStyleRegistry();
	}
	
	public void addColumns(final List<Column> columns) {
		checkWidget();
		gridModel.addColumns(columns);
	}
	
	public Column getColumn(final int columnIndex) {
		checkWidget();
		return gridModel.getColumns().get(columnIndex);
	}
	
	public Column getColumnById(final String columnId) {
		checkWidget();
		return gridModel.getColumnById(columnId);
	}
	
	public void groupBy(final List<Column> columns) {
		checkWidget();
		gridModel.groupBy(columns);
	}
	
	public void addElements(final List<T> elements) {
		checkWidget();
		gridModel.addElements(elements);
	}
	
	public void removeElements(final List<T> elements) {
		checkWidget();
		gridModel.removeElements(elements);
	}
	
	public void updateElements(final List<T> elements) {
		checkWidget();
		gridModel.updateElements(elements);
	}
	
	public void clearElements() {
		checkWidget();
		gridModel.clearElements();
	}
	
	public int getRowCount() {
		checkWidget();
		return gridModel.getRows().size();
	}
	
	public int getGroupCount() {
		checkWidget();
		// TODO: maintain group count via add/remove rows.
		return -1;
	}
	
	public void addFilters(final Collection<Filter<T>> filters) {
		checkWidget();
		gridModel.getFilterModel().addFilters(filters);
	}

	@Override
	public void modelChanged() {
		invalidateComputedArea();
		updateScrollbars();
		redraw();

		for (final IGridListener<T> listener : listeners) {
			listener.modelChanged(gridModel);
		}
	}
	
	@Override
	public void selectionChanged() {
		redraw();
		
		for (final IGridListener<T> listener : listeners) {
			listener.selectionChanged(gridModel.getSelectionModel().getSelectedElements());
		}
	}

	public void setGridRenderer(final GridRenderer<T> gridRenderer) {
		removePaintListener(this.gridRenderer);
		this.gridRenderer = gridRenderer;
		addPaintListener(gridRenderer);
	}

	public Viewport<T> getViewport() {
		return viewport;
	}

	public IGridLabelProvider<T> getLabelProvider() {
		return labelProvider;
	}

	public void setLabelProvider(final IGridLabelProvider<T> labelProvider) {
		this.labelProvider = labelProvider;
		
		if (this.gridModel != null) {
			this.gridModel.setLabelProvider(labelProvider);
		}
		redraw();
	}

	public void setContentProvider(final IGridContentProvider<T> contentProvider) {
		this.contentProvider = contentProvider;

		if (this.gridModel != null) {
			this.gridModel.setContentProvider(contentProvider);
		}
	}

	public IGridContentProvider<T> getContentProvider() {
		return contentProvider;
	}
	
//	public void setFilterRowVisible(final boolean visible) {
//		checkWidget();
//		gridModel.setFilterRowVisible(visible);
//	}
//	
//	public boolean isFilterRowVisible() {
//		checkWidget();
//		return gridModel.isFilterRowVisible();
//	}

	private void updateScrollbars() {
		viewport.invalidate();

		final Point computedArea = getComputedArea();
		final Rectangle viewportArea = viewport.getViewportArea(gc);
		viewport.calculateVisibleCellRange(gc);

		final int nextRowHeight = (viewport.getLastRowIndex() >= 0 && (viewport.getLastRowIndex() + 1) < gridModel.getRows().size()) ? gridModel.getRowHeight(gc, gridModel.getRows().get(viewport.getLastRowIndex())) : 0;
		final int nextColumnWidth = (viewport.getLastColumnIndex() >= 0 && (viewport.getLastColumnIndex() + 1) < gridModel.getColumns().size()) ? gridModel.getColumns().get(viewport.getLastColumnIndex()).getWidth() : 0;

		updateScrollbar(getVerticalBar(), viewportArea.height, computedArea.y, nextRowHeight);
		updateScrollbar(getHorizontalBar(), viewportArea.width, computedArea.x, nextColumnWidth);
	}

	private void updateScrollbar(final ScrollBar scrollBar, final int visible, final int maximum, final int increment) {
		scrollBar.setMaximum(maximum);
		scrollBar.setThumb(visible);
		scrollBar.setPageIncrement(Math.min(scrollBar.getThumb(), scrollBar.getMaximum()));
		scrollBar.setIncrement(increment);
		scrollBar.setVisible(maximum > visible);
	}

	private void invalidateComputedArea() {
		computedArea.x = -1;
		computedArea.y = -1;
	}

	private Point getComputedArea() {
		if (computedArea.x == -1 || computedArea.y == -1) {
			computedArea.x = 0;
			computedArea.y = 0;

			for (final Column column : gridModel.getColumns()) {
				computedArea.x += column.getWidth();
			}

			for (final Row<T> row : gridModel.getRows()) {
				computedArea.y += gridModel.getRowHeight(gc, row);
			}
		}

		return computedArea;
	}
	
//	/**
//	 * Build a quick-picker dialog to select values from the column specified and apply a QuickFilter with those values.
//	 */
//	public void showQuickFilterPicker(final Column column, final Point location) {
//		checkWidget();
//		
//		// TODO: Text box to filter table view. REQUIRES JFACE so externalise QuickFilters.
//		
//		//
//		// Tear-down any existing popup.
//		//
//		if (filterShell != null) {
//			filterShell.dispose();			
//		}
//		
//		//
//		// Locate or create a filter for the column.
//		//
//		QuickFilter<T> quickFilter = gridModel.getFilterModel().getQuickFilterForColumn(column);
//		if (quickFilter == null) {
//			quickFilter = new QuickFilter<T>(column, labelProvider, contentProvider);			
//		}
//		
//		//
//		// Build a list of all rows (hidden and shown).
//		//
//		final List<Row<T>> allRows = new ArrayList<>(gridModel.getAllRows());
//		
//		//
//		// Sort the values by their column's backing comparator rather than an alphabetical sort.
//		// This allows dates / numbers to be sorted chronologically in the filter picker.
//		//
//		Collections.sort(allRows, new Comparator<Row<T>>() {
//			@Override
//			public int compare(final Row<T> row1, final Row<T> row2) {
//				//
//				// Use a type-specific comparator on the underlying value.
//				//
//				final Object value1 = contentProvider.getValue(column, row1.getElement());
//				final Object value2 = contentProvider.getValue(column, row2.getElement());
//				
//				if ((value1 != null) && (value2 != null) && (value1.getClass() == value2.getClass())) {				
//					return column.getComparator().compare(value1, value2);				
//
//				} else {
//					//
//					// Mixing datatypes in a merged column means we need to sort on the displayed string value.
//					//
//					final String displayText1 = labelProvider.getText(column, row1.getElement());
//					final String displayText2 = labelProvider.getText(column, row2.getElement());
//					return displayText1.compareTo(displayText2);
//				}
//			}
//		});		
//		
//		//
//		// Build a small View-model for the picker which tracks whether a value is in a visible or hidden row and also whether the value has already
//		// been picked in the quick-filter.
//		//
//		final Map<String, QuickFilterValueState<T>> uniqueValues = new LinkedHashMap<>();
//		uniqueValues.put(QuickFilter.QUICK_FILTER__BLANKS, new QuickFilterValueState<T>(null, quickFilter.getFilterValues().contains(QuickFilter.QUICK_FILTER__BLANKS), true));
//		uniqueValues.put(QuickFilter.QUICK_FILTER__NOT_BLANK, new QuickFilterValueState<T>(null, quickFilter.getFilterValues().contains(QuickFilter.QUICK_FILTER__NOT_BLANK), true));		
//		
//		for (Row<T> row : allRows) {
//			//
//			// Add all non-null values to the list of available values to filter on.
//			//
//			if (contentProvider.getValue(column, row.getElement()) != null) {
//				final String text = labelProvider.getText(column, row.getElement());
//				final boolean checked = (quickFilter.getFilterValues().contains(text));
//				final QuickFilterValueState<T> state = new QuickFilterValueState<T>(row, checked, row.isVisible());
//				uniqueValues.put(text, state);
//			}
//		}
//		
//		//
//		// Now the view-model is constructed, build the view...
//		//
//		filterShell = new Shell(getShell(), SWT.TOOL);
//		filterShell.setBackground(getShell().getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));;
//		filterShell.setLayout(new GridLayout(2, false));
//		filterShell.setLocation(location);	
//		filterShell.addShellListener(new ShellAdapter() {
//			@Override
//			public void shellDeactivated(ShellEvent e) {
//				filterShell.close();
//			}
//		});
//		
//		final GridLayout tableLayout = new GridLayout(1, true);
//		tableLayout.marginWidth = 0;
//		tableLayout.marginHeight = 0;
//		
//		final Composite tableComposite = new Composite(filterShell, SWT.NONE);
//		tableComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
//		tableComposite.setLayout(tableLayout);
//		
//		final Text filterText = new Text(tableComposite, SWT.BORDER);
//		filterText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));		
//		
//		final Table filterTable = new Table(tableComposite, SWT.CHECK | SWT.V_SCROLL | SWT.FULL_SELECTION);						
//		filterTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
//		((GridData) filterTable.getLayoutData()).heightHint = 180;		
//		filterText.addModifyListener(new FilterFilterListener(filterTable));
//		
//		final GridLayout buttonLayout = new GridLayout(1, true);
//		buttonLayout.marginWidth = 0;
//		buttonLayout.marginHeight = 0;
//		
//		final Composite buttonComposite = new Composite(filterShell, SWT.NONE);
//		buttonComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true));
//		buttonComposite.setLayout(buttonLayout);
//
//		final Button spacer1 = new Button(buttonComposite, SWT.PUSH);
//		spacer1.setVisible(false);
//		spacer1.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true));
//		
//		final Button selectNoneButton = new Button(buttonComposite, SWT.PUSH);
//		selectNoneButton.setText("Select &None");
//		selectNoneButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
//		selectNoneButton.addSelectionListener(new SelectNoneListener(filterTable));
//		
//		final Button selectAllButton = new Button(buttonComposite, SWT.PUSH);
//		selectAllButton.setText("Select &All");
//		selectAllButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
//		selectAllButton.addSelectionListener(new SelectAllListener(filterTable));
//		
//		final Button spacer2 = new Button(buttonComposite, SWT.PUSH);
//		spacer2.setVisible(false);
//		spacer2.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
//		((GridData) spacer2.getLayoutData()).heightHint = 8;
//		
//		//
//		// Allow implementors to add an 'advanced filter...' button for example.
//		//
//		createAdditionalFilterButtons(buttonComposite);
//		
//		final Button filterButton = new Button(buttonComposite, SWT.PUSH);
//		filterButton.setText("&Filter");
//		filterButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
//		filterButton.addSelectionListener(new QuickFilterListener(filterTable, quickFilter));
//		
//		final Button cancelButton = new Button(buttonComposite, SWT.PUSH);
//		cancelButton.setText("&Cancel");
//		cancelButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
//		cancelButton.addSelectionListener(new CancelFilterListener());
//		
//		//
//		// Populate the picker with values to filter on.
//		//
//		int index = 0;
//		filterTable.setItemCount(uniqueValues.size());
//		for (String text : uniqueValues.keySet()) {
//			final QuickFilterValueState<T> state = uniqueValues.get(text);
//			final TableItem tableItem = filterTable.getItem(index++);
//			tableItem.setText(text);
//			tableItem.setChecked(state.isChecked());
//			tableItem.setForeground(state.isAvailable() ? filterShell.getDisplay().getSystemColor(SWT.COLOR_LIST_FOREGROUND) : filterShell.getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));
//		}
//		
//		filterShell.setDefaultButton(filterButton);
//		filterShell.pack();
//		filterShell.setVisible(true);
//		filterText.forceFocus();
//	}
	
	protected void createAdditionalFilterButtons(final Composite buttonComposite) {		
	}

	public String getEmptyMessage() {
		return emptyMessage;
	}

	public void setEmptyMessage(final String emptyMessage) {
		this.emptyMessage = emptyMessage;
	}

	public void addListener(final IGridListener<T> listener) {
		this.listeners.add(listener);
	}

	public void removeListener(final IGridListener<T> listener) {
		this.listeners.remove(listener);
	}

	private class ScrollListener extends SelectionAdapter {
		@Override
		public void widgetSelected(final SelectionEvent e) {
			viewport.invalidate();
			redraw();
		}
	}

	private class ResizeListener implements Listener {
		@Override
		public void handleEvent(final Event event) {
			if (event.type == SWT.Resize) {
				updateScrollbars();
			}
		}
	}

	private class GridDisposeListener implements DisposeListener {
		@Override
		public void widgetDisposed(final DisposeEvent e) {
			dispose();
		}
	}
	
//	private class SelectAllListener extends SelectionAdapter {
//		private final Table table;
//		
//		public SelectAllListener(final Table table) {
//			this.table = table;
//		}
//		
//		@Override
//		public void widgetSelected(SelectionEvent e) {
//			for (int index=0; index<table.getItemCount(); index++) {
//				table.getItem(index).setChecked(true);
//			}
//		}
//	}
//	
//	private class SelectNoneListener extends SelectionAdapter {
//		private final Table table;
//		
//		public SelectNoneListener(final Table table) {
//			this.table = table;
//		}
//		
//		@Override
//		public void widgetSelected(SelectionEvent e) {
//			for (int index=0; index<table.getItemCount(); index++) {
//				table.getItem(index).setChecked(false);
//			}
//		}
//	}
//	
//	private class CancelFilterListener extends SelectionAdapter {
//		@Override
//		public void widgetSelected(SelectionEvent e) {
//			filterShell.dispose();
//		}
//	}
//	
//	private class QuickFilterListener extends SelectionAdapter {
//		private final Table table;
//		private final QuickFilter<T> quickFilter;
//				
//		public QuickFilterListener(final Table table, final QuickFilter<T> quickFilter) {
//			this.table = table;
//			this.quickFilter = quickFilter;
//		}
//		
//		@Override
//		public void widgetSelected(SelectionEvent e) {
//			//
//			// Update the values in the filter from the table.
//			//
//			quickFilter.getFilterValues().clear();
//			for (int index=0; index<table.getItemCount(); index++) {
//				final TableItem item = table.getItem(index);
//				
//				if (item.getChecked()) {
//					quickFilter.getFilterValues().add(item.getText());
//				}
//			}
//			
//			if (quickFilter.getFilterValues().isEmpty()) {
//				//
//				// Remove the filter if there's no values in to.
//				//
//				gridModel.getFilterModel().removeFilters(Collections.singletonList((Filter<T>) quickFilter));
//				
//			} else if (gridModel.getFilterModel().getFilters().contains(quickFilter)) {
//				//
//				// Re-apply the existing filter changes to the grid.
//				//
//				gridModel.getFilterModel().applyFilters();
//
//			} else {
//				// 
//				// Add the filter if it's not already added.
//				//
//				gridModel.getFilterModel().addFilters(Collections.singletonList((Filter<T>) quickFilter));
//			}
//			
//			//
//			// Close the filter picker.
//			//
//			filterShell.close();
//		}
//	}
//	
//	private class FilterFilterListener implements ModifyListener {
//		private final Table filterTable;
//		
//		public FilterFilterListener(final Table filterTable) {
//			this.filterTable = filterTable;
//		}
//		
//		@Override
//		public void modifyText(ModifyEvent e) {
////			filterTable.setF
//			
//		}
//	}
}