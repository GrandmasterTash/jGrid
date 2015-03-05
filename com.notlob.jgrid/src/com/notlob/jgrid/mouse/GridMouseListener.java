package com.notlob.jgrid.mouse;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.ToolTip;

import com.notlob.jgrid.Grid;
import com.notlob.jgrid.listeners.IGridListener;
import com.notlob.jgrid.model.Column;
import com.notlob.jgrid.model.GridModel;
import com.notlob.jgrid.model.Row;
import com.notlob.jgrid.model.Viewport;

// TODO: Expose more mouse events for cells.

public class GridMouseListener<T> extends MouseAdapter implements MouseMoveListener, MouseTrackListener {
	
	private final Grid<T> grid;
	private final GridModel<T> gridModel;
	private final Viewport<T> viewport;
	private final Collection<IGridListener<T>> listeners;
	private final GC gc;
	private final ToolTip toolTip;
	
	// Track if any mouse button is in the down position.
	private boolean mouseDown;
	
	// Track if the mouse is over a row/column.
	private Row<T> row = null;
	private Column column = null;
	
	public GridMouseListener(final Grid<T> grid, final GC gc, final Collection<IGridListener<T>> listeners, final ToolTip toolTip) {
		this.grid = grid;
		this.gridModel = grid.getGridModel();
		this.viewport = grid.getViewport();
		this.listeners = listeners;
		this.toolTip = toolTip;
		this.gc = gc;
	}

	@SuppressWarnings("unchecked")
	private void trackCell(final int x, final int y) {
		//
		// Track the row and column.
		//
		final int rowIndex = viewport.getRowIndexByY(y, gc);
		final int columnIndex = viewport.getColumnIndexByX(x, gc);
		
		if (rowIndex == -1) {
			//
			// See if it's the column header or filter row.
			//
			if (y >= 0 ) {
				row = null;
				final int headerHeight = gridModel.getRowHeight(gc, Row.COLUMN_HEADER_ROW);
				
				if (y < headerHeight) {
					row = Row.COLUMN_HEADER_ROW;
					
//				} else if (y < (headerHeight + gridModel.getRowHeight(gc, Row.FILTER_HEADER_ROW))) {
//					row = Row.FILTER_HEADER_ROW;
				}
			} 
			
		} else {
			row = gridModel.getRows().get(rowIndex);				
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
			final int x = e.x;
			final int y = e.y + 16;
			
			if (row == Row.COLUMN_HEADER_ROW) {
				// TODO: allow label provider to specify (example merged cols show source cols).
				showToolTip(x, y, "", column.getCaption());
				
//			} else  if (row == Row.FILTER_HEADER_ROW) {
//				// TODO: allow label provider to specify (example additional non-quick filters on column...).
//				final QuickFilter<T> quickFilter = gridModel.getFilterModel().getQuickFilterForColumn(column);
//				showToolTip(x, y, column.getCaption(), (quickFilter == null) ? "(not filtered)" : quickFilter.getToolTip());
				
			} else if (grid.getLabelProvider() != null && !gridModel.isParentRow(row)) {
				//
				// For now, ignore tool-tips on group rows.
				//
				final String toolTip = grid.getLabelProvider().getToolTip(column, row.getElement());
				if (toolTip != null && !toolTip.isEmpty()) {
					showToolTip(x, y, column.getCaption(), toolTip);
				}
			}
		}
	}
	
	@Override
	public void mouseMove(MouseEvent e) {
		toolTip.setVisible(false);
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
				if ((column != null) && (e.y < viewport.getViewportArea(gc).y)) {						
					if (row == Row.COLUMN_HEADER_ROW) {
						//
						// Column sorting.
						//
						gridModel.getSortModel().sort(column, true, ctrl);
						
//					} else if (row == Row.FILTER_HEADER_ROW) {
//						//
//						// Quick filtering.
//						//
//						grid.showQuickFilterPicker(column, grid.toDisplay(e.x, e.y));
					}
				}

				//
				// Handle the selection.
				//
				if (row != null && (row != Row.COLUMN_HEADER_ROW) /*&& (row != Row.FILTER_HEADER_ROW)*/) {
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
						listener.click(column, row.getElement(), new Point(e.x, e.y), e.stateMask);
						
					} else if (e.count > 1) {
						listener.doubleClick(column, row.getElement(), new Point(e.x, e.y), e.stateMask);
					}

				} else if (e.button == 3) {
					listener.rightClick(column, row.getElement(), new Point(e.x, e.y), e.stateMask);
				}
			}
		}
	}
	
	private void showToolTip(final int x, final int y, final String boldText, final String message) {
		//
		// Build a slight delay otherwise the tool-tip would swallow clicks meant for the grid.
		//
		grid.getDisplay().timerExec(4, new Runnable() {
			@Override
			public void run() {
				final Point location = grid.toDisplay(x, y);
				toolTip.setLocation(location);
				toolTip.setText(boldText);
				toolTip.setMessage(message);
				toolTip.setVisible(true);								
			}
		});
	}
}
