package com.notlob.jgrid.input;

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
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.ToolTip;

import com.notlob.jgrid.Grid;
import com.notlob.jgrid.listeners.IGridListener;
import com.notlob.jgrid.model.Column;
import com.notlob.jgrid.model.GridModel;
import com.notlob.jgrid.model.Row;
import com.notlob.jgrid.model.Viewport;

// TODO: Expose more mouse events for cells.

public class GridMouseHandler<T> extends MouseAdapter implements MouseMoveListener, MouseTrackListener/*, MouseWheelListener*/ {

	private final Grid<T> grid;
	private final GridModel<T> gridModel;
	private final Viewport<T> viewport;
	private final Collection<IGridListener<T>> listeners;
	private final GC gc;
	private final ToolTip toolTip;

	// Track if any mouse button is in the down position.
	private boolean mouseDown;
	private boolean shift; // Tracked in mouseMove and mouseUp.
	private boolean ctrl;
	private boolean alt;

	// Track if the mouse is over a row/column.
	private Row<T> row = null; // TODO: Rename hovered_....
	private Column column = null;
	private Column groupColumn = null;  // << Mouse is over a group field header.
	private Column groupValue = null;	// << Mouse is over a group field value not the header;

	public GridMouseHandler(final Grid<T> grid, final GC gc, final Collection<IGridListener<T>> listeners, final ToolTip toolTip) {
		this.grid = grid;
		this.gridModel = grid.getGridModel();
		this.viewport = grid.getViewport();
		this.listeners = listeners;
		this.toolTip = toolTip;
		this.gc = gc;
	}

	public Column getColumn() {
		return column;
	}

	public Row<T> getRow() {
		return row;
	}

	public Column getGroupColumn() {
		return groupColumn;
	}

	public boolean isShift() {
		return shift;
	}

	public boolean isCtrl() {
		return ctrl;
	}

	public boolean isAlt() {
		return alt;
	}

	public void setAlt(final boolean alt) {
		this.alt = alt;
	}

	/**
	 * Tracks the column and row under the mouse as it moves.
	 *
	 * Returns true if the cell or row changes.
	 */
	@SuppressWarnings("unchecked")
	private boolean trackCell(final int x, final int y) {
		Row<T> newRow = null;
		Column newColumn = null;
		Column newGroupColumn = null;
		Column newGroupValue = null;

		//
		// Get the row and column indexes from the viewport.
		//
		final int rowIndex = viewport.getRowIndexByY(y, gc);
		final int columnIndex = viewport.getColumnIndexByX(x, gc);

		if (rowIndex == -1) {
			//
			// See if it's the column header or filter row.
			//
			if (y >= 0 ) {
				newRow = null;
				final int headerHeight = gridModel.getRowHeight(gc, Row.COLUMN_HEADER_ROW);

				if (y < headerHeight) {
					newRow = Row.COLUMN_HEADER_ROW;
				}
			}

		} else {
			newRow = gridModel.getRows().get(rowIndex);

			//
			// If this is a group row.
			//
			if (gridModel.isParentElement(newRow.getElement())) {
				//
				// If the mouse is over a group field header - track it.
				//
				newGroupColumn = grid.getGridRenderer().getGroupColumnForX(gc, newRow, x, true);

				//
				// If the mouse is over a group field value - track it.
				//
				if (newGroupColumn == null) {
					newGroupValue = grid.getGridRenderer().getGroupColumnForX(gc, newRow, x, false);
				}
			}
		}

		if (columnIndex != -1) {
			newColumn = gridModel.getColumns().get(columnIndex);

		} else {
			newColumn = null;
		}

		if (newRow != row || newColumn != column || newGroupColumn != groupColumn || newGroupValue != groupValue) {
			row = newRow;
			column = newColumn;
			groupColumn = newGroupColumn;
			groupValue = newGroupValue;
			return true;
		}

		return false;
	}

	@Override
	public void mouseEnter(final MouseEvent e) {
	}

	@Override
	public void mouseExit(final MouseEvent e) {
	}

	@Override
	public void mouseHover(final MouseEvent e) {
		if ((column != null) && (row != null) && (grid.getLabelProvider() != null)) {
			final int x = e.x;
			final int y = e.y + 16;

			if (row == Row.COLUMN_HEADER_ROW) {
				final String toolTip = grid.getLabelProvider().getHeaderToolTip(column);
				showToolTip(x, y, column.getCaption(), (toolTip != null && !toolTip.isEmpty()) ? toolTip : "");

			} else if (gridModel.isParentRow(row)) {
				if (groupColumn != null) {
					//
					// A group row's header tool-tip.
					//
					final String toolTip = grid.getLabelProvider().getHeaderToolTip(groupColumn);
					if (toolTip != null && !toolTip.isEmpty()) {
						showToolTip(x, y, groupColumn.getCaption(), toolTip);
					}

				} else if (groupValue != null) {
					//
					// A group row's value tool-tip.
					//
					final String toolTip = grid.getLabelProvider().getToolTip(groupValue, row.getElement());
					if (toolTip != null && !toolTip.isEmpty()) {
						showToolTip(x, y, groupValue.getCaption(), toolTip);
					}
				}

			} else {
				//
				// Normal row's tool-tip.
				//
				final String toolTip = grid.getLabelProvider().getToolTip(column, row.getElement());
				if (toolTip != null && !toolTip.isEmpty()) {
					showToolTip(x, y, column.getCaption(), toolTip);
				}
			}
		}
	}

	@Override
	public void mouseMove(final MouseEvent e) {
		toolTip.setVisible(false);
		shift = (e.stateMask & SWT.SHIFT) == SWT.SHIFT;
		ctrl = (e.stateMask & SWT.CTRL) == SWT.CTRL;
		alt = (e.stateMask & SWT.ALT) == SWT.ALT;

		if (trackCell(e.x, e.y)) {
			//
			// Repaint the grid to show the hovered row.
			//
			grid.redraw();
		}
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

		if (grid.getCursor() == grid.getDisplay().getSystemCursor(SWT.CURSOR_SIZEALL)) {
			grid.setCursor(grid.getDisplay().getSystemCursor(SWT.CURSOR_ARROW));
		}

		//
		// Get the event details.
		//
		trackCell(e.x, e.y);
		shift = (e.stateMask & SWT.SHIFT) == SWT.SHIFT;
		ctrl = (e.stateMask & SWT.CTRL) == SWT.CTRL;
		alt = (e.stateMask & SWT.ALT) == SWT.ALT;

		if (e.button == 1) { // LEFT
			if (e.count == 1) {
				if ((column != null) && (e.y < viewport.getViewportArea(gc).y)) {
					if (row == Row.COLUMN_HEADER_ROW) {
						//
						// Column sorting.
						//
						gridModel.getSortModel().sort(column, true, ctrl, true);
						return;
					}
				}

				if (column != null && row != null) {
					//
					// Check for group row hot-spots.
					//
					if (gridModel.isParentRow(row)) {
						//
						// Expand/collapse toggle.
						//
						final Rectangle bounds = grid.getGridRenderer().getExpandImageBounds(gc, row);
						if (bounds.contains(e.x,  e.y)) {
							for (final IGridListener<T> listener : listeners) {
								if (grid.getContentProvider().isCollapsed(row.getElement())) {
									listener.groupExpanded(row.getElement());
								} else {
									listener.groupCollapsed(row.getElement());
								}
							}

							// Refresh filters.
							gridModel.getFilterModel().applyFilters();
							return;
						}

						if (alt && (groupColumn != null)) {
							//
							// Toggle the sort on the group column.
							//
							gridModel.getSortModel().sort(groupColumn, true, ctrl, true);
							return;
						}
					}
				}

				//
				// Handle the selection.
				//
				if (row != null && (row != Row.COLUMN_HEADER_ROW) /*&& (row != Row.FILTER_HEADER_ROW)*/) {
					//
					// If it's the row-number cell, pretend ctrl is used for sticky selections.
					//
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

					//
					// Update the anchor column.
					//
					if (gridModel.isParentElement(row.getElement())) {
						if (groupColumn != null) {
							gridModel.getSelectionModel().setAnchorColumn(groupColumn);

						} else if (groupValue != null) {
							gridModel.getSelectionModel().setAnchorColumn(groupValue);
							
						} else if (!gridModel.getGroupByColumns().isEmpty()) {
							gridModel.getSelectionModel().setAnchorColumn(gridModel.getGroupByColumns().get(0));
						}

					} else {
						gridModel.getSelectionModel().setAnchorColumn(column);
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
						if (row == Row.COLUMN_HEADER_ROW) {
							listener.headerClick(column, new Point(e.x, e.y), e.stateMask);
						} else {
							listener.click(column, row.getElement(), new Point(e.x, e.y), e.stateMask);
						}

					} else if (e.count > 1) {
						if (row == Row.COLUMN_HEADER_ROW) {
							listener.headerDoubleClick(column, new Point(e.x, e.y), e.stateMask);
						} else {
							listener.doubleClick(column, row.getElement(), new Point(e.x, e.y), e.stateMask);
						}
					}

				} else if (e.button == 3) {
					if (row == Row.COLUMN_HEADER_ROW) {
						listener.headerRightClick(column, new Point(e.x, e.y), e.stateMask);
					} else {
						listener.rightClick(column, row.getElement(), new Point(e.x, e.y), e.stateMask);
					}
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
