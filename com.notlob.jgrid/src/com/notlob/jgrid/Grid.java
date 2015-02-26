package com.notlob.jgrid;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackListener;
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

import com.notlob.jgrid.model.Column;
import com.notlob.jgrid.model.GridModel;
import com.notlob.jgrid.model.Row;
import com.notlob.jgrid.model.Viewport;
import com.notlob.jgrid.model.filtering.Filter;
import com.notlob.jgrid.providers.IGridContentProvider;
import com.notlob.jgrid.providers.IGridLabelProvider;
import com.notlob.jgrid.renderer.GridRenderer;
import com.notlob.jgrid.styles.StyleRegistry;

public class Grid<T> extends Composite implements GridModel.IModelListener {

	// TODO: Filtering / Searching.
	// TODO: Hook-up with existing styles.
	// TODO: Reposition/resize columns via DnD.
	// TODO: Focus/Keyboard navigation.
	// TODO: Row-group expand/collapse.
	// TODO: In-line editing.
	// TODO: Empty data message.
	// TODO: BUG: Empty grid's need to do what modelChanged does (i.e. calc viewport and scrollbars).
	// TODO: Simulate old group/column display style?	
	
	// TODO: New viewer to do progress bar and outline bar.
	
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
	private final GridMouseListener mouseListener;

	// TODO: try..catch around all calls to listeners...
	// Things that listen to us.
	private final List<IGridListener<T>> listeners;

	// Used for dimension calculations.
	private final GC gc;
	private final Point computedArea;

	private final ToolTip toolTip;
	private String emptyMessage;

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
		mouseListener = new GridMouseListener();
		listeners = new ArrayList<>();

		parent.addDisposeListener(disposeListener);
		addMouseListener(mouseListener);
		addMouseMoveListener(mouseListener);
		addMouseTrackListener(mouseListener);
		addPaintListener(gridRenderer);
		addListener(SWT.Resize, resizeListener);
		getVerticalBar().addSelectionListener(scrollListener);
		getHorizontalBar().addSelectionListener(scrollListener);
		
		toolTip = new ToolTip(parent.getShell(), SWT.NONE);
		toolTip.setAutoHide(true);
	}
		
	@Override
	public void dispose() {
		toolTip.dispose();
		
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
	
	public void setFilterRowVisible(final boolean visible) {
		checkWidget();
		gridModel.setFilterRowVisible(visible);
	}
	
	public boolean isFilterRowVisible() {
		checkWidget();
		return gridModel.isFilterRowVisible();
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
	
	// TODO: Break-up into protected methods handleClick and handleDoubleClick?
	private class GridMouseListener extends MouseAdapter implements MouseMoveListener, MouseTrackListener {
		private boolean mouseDown;
		private Row<T> row = null;
		private Column column = null;

		private void trackCell(final int x, final int y) {
			//
			// Track the row and column.
			//
			final int rowIndex = viewport.getRowIndexByY(y, gc);
			final int columnIndex = viewport.getColumnIndexByX(x, gc);
			
			if (rowIndex != -1) {
				row = gridModel.getRows().get(rowIndex);				
			} else {
				row = null;
			}
			
			if (columnIndex != -1) {
				column = gridModel.getColumns().get(columnIndex);
			} else {
				column = null;
			}
			
			//System.out.println(String.format("%s, %s", columnIndex, rowIndex));
		}
		
		@Override
		public void mouseEnter(MouseEvent e) {
		}
		
		@Override
		public void mouseExit(MouseEvent e) {
		}
		
		@Override
		public void mouseHover(final MouseEvent e) {
			if ((column != null) && (row != null)) {
				//
				// For now, ignore tool-tips on group rows.
				//
				if (labelProvider != null && !gridModel.isParentRow(row)) {
					final String toolTip = labelProvider.getToolTip(column, row.getElement());
					if (toolTip != null && !toolTip.isEmpty()) {
						//
						// Build a slight delay otherwise the tool-tip would swallow clicks meant for the grid.
						//
						Grid.this.getDisplay().timerExec(4, new Runnable() {
							@Override
							public void run() {
								final Point location = Grid.this.toDisplay(e.x, e.y + 16);
								Grid.this.toolTip.setLocation(location);
								Grid.this.toolTip.setText(column.getCaption());
								Grid.this.toolTip.setMessage(toolTip);
								Grid.this.toolTip.setVisible(true);								
							}
						});
					}
				}
			}
		}
		
		@Override
		public void mouseMove(MouseEvent e) {
			Grid.this.toolTip.setVisible(false);
			trackCell(e.x, e.y);
		}
		
		@Override
		public void mouseDown(final MouseEvent e) {
			mouseDown = true;
		}

		@Override
		public void mouseUp(final MouseEvent e) {
			if (!mouseDown) {
				return;
			}

			mouseDown = false;

			//
			// Get the event details.
			//
			trackCell(e.x, e.y);
			final boolean shift = (e.stateMask & SWT.SHIFT) == SWT.SHIFT;
			boolean ctrl = (e.stateMask & SWT.CTRL) == SWT.CTRL;

			if (e.button == 1) { // LEFT
				if (e.count == 1) {
					//
					// Column sorting.
					//
					if ((row == null) && (e.y < viewport.getViewportArea(gc).y)) {
						if (column != null) {
							gridModel.getSortModel().sort(column, true, ctrl);
						}
					}

					//
					// Handle the selection.
					//
					if (row != null) {
						// If it's the row-number cell, pretend ctrl is used for sticky selectins.
						if (e.x < viewport.getViewportArea(gc).x) {
							ctrl = true;
						}

						if (!(shift || ctrl)) {
							//
							// Single row/group replace.
							//
							final List<Row<T>> rows = new ArrayList<>();
							rows.addAll(gridModel.isParentRow(row) ? gridModel.getWholeGroup(row) : Collections.singletonList(row));
							gridModel.getSelectionModel().setSelectedRows(rows);

						} else if (ctrl && !shift) {
							//
							// Single row/group toggle.
							//
							final List<Row<T>> rows = new ArrayList<>();
							rows.addAll(gridModel.isParentRow(row) ? gridModel.getWholeGroup(row) : Collections.singletonList(row));
							gridModel.getSelectionModel().toggleRowSelections(rows);

						} else if (!ctrl && shift) {
							//
							// Range replace.
							//
							gridModel.getSelectionModel().selectRange(row, false);

						} else if (ctrl && shift) {
							//
							// Range addition.
							//
							gridModel.getSelectionModel().selectRange(row, true);
						}
					}

					//
					// Select All - the corner has been clicked.
					//
					if ((e.x < viewport.getViewportArea(gc).x) && (e.y < viewport.getViewportArea(gc).y)) {
						gridModel.getSelectionModel().selectAll();
					}

				} else if (e.count > 1) {
					//
					// Double-click.
					//
					if (row != null) {
						//
						// Get the rows group.
						//
						if (gridModel.isGroupRow(row)) {
							final List<Row<T>> group = gridModel.getWholeGroup(row);
							boolean selected = true;
							for (final Row<T> member : group) {
								if (!member.isSelected()) {
									selected = false;
									break;
								}
							}

							//
							// If any are unselected, select the group, otherwise, if they are all selected,
							// just the select the row that was double-clicked (as long as it's not a parent row).
							//
							if (!selected) {
								gridModel.getSelectionModel().setSelectedRows(group);

							} else if (!gridModel.isParentRow(row)) {
								gridModel.getSelectionModel().setSelectedRows(Collections.singletonList(row));
							}
						}
					}
				}
			}

			//
			// Notify listeners.
			//
			if (row != null && column != null) {
				for (final IGridListener<T> listener : listeners) {
					if (e.button == 1) {
						if (e.count == 1) {
							listener.click(column, row.getElement(), e.stateMask);
						} else if (e.count > 1) {
							listener.doubleClick(column, row.getElement(), e.stateMask);
						}
	
					} else if (e.button == 3) {
						listener.rightClick(column, row.getElement(), e.stateMask);
					}
				}
			}
		}
	}
}