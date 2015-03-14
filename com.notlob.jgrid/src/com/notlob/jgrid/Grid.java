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

import com.notlob.jgrid.input.GridKeyboardHandler;
import com.notlob.jgrid.input.GridMouseHandler;
import com.notlob.jgrid.listeners.IGridListener;
import com.notlob.jgrid.model.Column;
import com.notlob.jgrid.model.GridModel;
import com.notlob.jgrid.model.Row;
import com.notlob.jgrid.model.RowCountScope;
import com.notlob.jgrid.model.Viewport;
import com.notlob.jgrid.model.filtering.Filter;
import com.notlob.jgrid.providers.IGridContentProvider;
import com.notlob.jgrid.providers.IGridLabelProvider;
import com.notlob.jgrid.renderer.GridRenderer;
import com.notlob.jgrid.styles.StyleRegistry;

public class Grid<T> extends Composite implements GridModel.IModelListener {

	// TODO: Hide anchor / header style on lost focus (and the hover style with mouse exit as well).
	// TODO: Focus/Keyboard navigation / anchor.
	// Bug: anchorElement is used for range selection but might need a new construct now it's changed purpose.
	// Bug: CollapseFilter should opt-out of clearing selection.
	// TODO: Middle-mouse scrolling.
	// TODO: Reposition/resize columns via DnD.
	// TODO: Group row tool-tip show all group name/values even hidden ones (so if don't fit still show).
	// TODO: Column selection mode.
	// TODO: Empty data message.
	// TODO: Select next row/group if current is removed.
	// TODO: Right-click to select before raising event.
	// TODO: Column pinning.
	// TODO: In-line editing.		
	// TODO: Suppress model change events optimisation?				
	// TODO: Mouse cursor in CellStyle.
	// TODO: Ensure searches expand collapsed groups if children meet criteria.
	// TODO: Evaluate performance gain vs memory overhead of the extent cache in the renderer.
	// TODO: Make setting to toggle group name click behaviour - click vs alt + click.
	// TODO: Make setting to toggle anchor header highlight.
	// TODO: Make setting to pad group values by their column width.
	
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
	
	// Keyboard and mouse input handling.
	private final GridMouseHandler<T> mouseHandler;
	private final GridKeyboardHandler<T> keyboardHandler;

	// TODO: try..catch around all calls to listeners...
	// Things that listen to us.
	private final Collection<IGridListener<T>> listeners;

	// Used for dimension calculations.
	private final GC gc;
	private final Point computedArea;

	private final ToolTip toolTip;
	private String emptyMessage;	
	
	// Some grid behavioural flags.
	private boolean highlightHoveredRow = true;
	private boolean clickGroupFieldNameToSort = true;

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
		mouseHandler = new GridMouseHandler<T>(this, gc, listeners, toolTip);
		keyboardHandler = new GridKeyboardHandler<T>(this, gc);
		
		parent.addDisposeListener(disposeListener);
		addKeyListener(keyboardHandler);
		addMouseListener(mouseHandler);
		addMouseMoveListener(mouseHandler);
		addMouseTrackListener(mouseHandler);
		addPaintListener(gridRenderer);
		addListener(SWT.Resize, resizeListener);
		getVerticalBar().addSelectionListener(scrollListener);
		getHorizontalBar().addSelectionListener(scrollListener);
	}
		
	@Override
	public void dispose() {
		toolTip.dispose();
		
		// Remove listeners.
		removeKeyListener(keyboardHandler);
		removeMouseListener(mouseHandler);
		removeMouseTrackListener(mouseHandler);
		removeMouseMoveListener(mouseHandler);
		removePaintListener(gridRenderer);
		removeListener(SWT.Resize, resizeListener);
		getVerticalBar().removeSelectionListener(scrollListener);
		getHorizontalBar().removeSelectionListener(scrollListener);
		gridModel.removeListener(this);

		// Dispose of UI handles.
		gc.dispose();
		super.dispose();
	}
	
	public void setHighlightHoveredRow(boolean highlightHoveredRow) {
		this.highlightHoveredRow = highlightHoveredRow;
	}
	
	public boolean isHighlightHoveredRow() {
		return highlightHoveredRow;
	}
	
	public void setClickGroupFieldNameToSort(boolean clickGroupFieldNameToSort) {
		this.clickGroupFieldNameToSort = clickGroupFieldNameToSort;
	}
	
	public boolean isClickGroupFieldNameToSort() {
		return clickGroupFieldNameToSort;				
	}
	
	public void checkWidget() {
		super.checkWidget();
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
	
	public Collection<Column> getColumns() {
		checkWidget();
		return gridModel.getColumns();
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
	
	public Column getGroupColumn(final int columnIndex) {
		checkWidget();
		return gridModel.getGroupByColumns().get(columnIndex);
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
	
	public Collection<T> getSelection() {
		checkWidget();
		return gridModel.getSelectionModel().getSelectedElements();
	}
	
	public int getRowCount(final boolean visible, final RowCountScope scope) {
		checkWidget();
		return gridModel.getRowCount(visible, scope);
	}
	
	public void applyFilters() {
		checkWidget();
		gridModel.getFilterModel().applyFilters();
	}
	
	public Collection<Filter<T>> getFilters() {
		checkWidget();
		return gridModel.getFilterModel().getFilters();
	}
	
	public void addFilters(final Collection<Filter<T>> filters) {
		checkWidget();
		gridModel.getFilterModel().addFilters(filters);
	}
	
	public void removeFilters(final Collection<Filter<T>> filters) {
		checkWidget();
		gridModel.getFilterModel().removeFilters(filters);
	}

	@Override
	public void modelChanged() {
		invalidateComputedArea();
		updateScrollbars();
		redraw();

		for (final IGridListener<T> listener : listeners) {
			listener.gridChanged();
		}
	}
	
	@Override
	public void selectionChanged() {
		redraw();
		
		for (final IGridListener<T> listener : listeners) {
			listener.selectionChanged(gridModel.getSelectionModel().getSelectedElements());
		}
	}
	
	public GridMouseHandler<T> getMouseHandler() {
		return mouseHandler;
	}
	
	public GridRenderer<T> getGridRenderer() {		
		return gridRenderer;
	}

	public void setGridRenderer(final GridRenderer<T> gridRenderer) {
		removePaintListener(this.gridRenderer);
		this.gridRenderer = gridRenderer;
		addPaintListener(gridRenderer);
	}

	public Viewport<T> getViewport() {
		checkWidget();
		return viewport;
	}

	public IGridLabelProvider<T> getLabelProvider() {
		return labelProvider;
	}

	public void setLabelProvider(final IGridLabelProvider<T> labelProvider) {
		checkWidget();
		this.labelProvider = labelProvider;
		
		if (this.gridModel != null) {
			this.gridModel.setLabelProvider(labelProvider);
		}
		redraw();
	}

	public void setContentProvider(final IGridContentProvider<T> contentProvider) {
		checkWidget();
		this.contentProvider = contentProvider;

		if (this.gridModel != null) {
			this.gridModel.setContentProvider(contentProvider);
		}
	}

	public IGridContentProvider<T> getContentProvider() {
		checkWidget();
		return contentProvider;
	}

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

	public String getEmptyMessage() {
		return emptyMessage;
	}

	public void setEmptyMessage(final String emptyMessage) {
		this.emptyMessage = emptyMessage;
	}
	
	public boolean isShowRowNumbers() {
		checkWidget();
		return gridModel.isShowRowNumbers();
	}
	
	public void setShowRowNumbers(boolean show) {
		checkWidget();
		gridModel.setShowRowNumbers(show);		
	}
	
	public boolean isHideNoneHighlightedRows() {		
		checkWidget();
		return gridModel.getFilterModel().isHideNoneHighlightedRows();
	}
	
	public void setHideNoneHighlightedRows(final boolean hideNoneHighlightedRows) {
		checkWidget();
		gridModel.getFilterModel().setHideNoneHighlightedRows(hideNoneHighlightedRows);
	}

	public void addListener(final IGridListener<T> listener) {
		this.listeners.add(listener);
	}

	public void removeListener(final IGridListener<T> listener) {
		this.listeners.remove(listener);
	}
	
	public void reveal(final Column column, final T element) {
		checkWidget();
		viewport.reveal(gc, column, gridModel.getRow(element));
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
}