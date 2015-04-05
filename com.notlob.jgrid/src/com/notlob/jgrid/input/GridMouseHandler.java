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
import com.notlob.jgrid.model.ColumnMouseOperation;
import com.notlob.jgrid.model.GridModel;
import com.notlob.jgrid.model.Row;
import com.notlob.jgrid.model.Viewport;

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
	private Column resizing;	
	private Column repositioning;
	private Column repositioningDetect;
	private Column targetColumn;	// Repositioning target.
	
	// Used for an edge-case when dragging a column to the end of the grid.
	public final static Column LAST_COLUMN = new Column("LAST.COLUMN");
	
	// Distance in pixels the scroll bar is moved each time during a column drag.
	private final static int DRAG_SCROLL_DISTANCE = 10;

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
	
	public Column getRepositioningColumn() {
		return repositioning;
	}
	
	public Column getTargetColumn() {
		return targetColumn;
	}

	public boolean isShiftHeld() {
		return shift;
	}

	public boolean isCtrlHeld() {
		return ctrl;
	}

	public boolean isAltHeld() {
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
	private boolean trackCell(final int x, final int y) {
		Column newGroupColumn = null;
		Column newGroupValue = null;

		//
		// Get the row and column indexes from the viewport.
		//
		final Column newColumn = grid.getColumnAtXY(x, y);
		final Row<T> newRow = grid.getRowAtXY(x, y);

		if ((newRow != null) && (newRow != Row.COLUMN_HEADER_ROW)) {
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

		if (newRow != row || newColumn != column || newGroupColumn != groupColumn || newGroupValue != groupValue) {
			//
			// If any of the tracked things has changed, update and return true;
			//
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
		//
		// Show a tool-tip.
		//
		if ((repositioning == null) && (column != null) && (column != Column.ROW_NUMBER_COLUMN) && (row != null) && (grid.getLabelProvider() != null)) {
			final int x = e.x;
			final int y = e.y + 16;

			if (row == Row.COLUMN_HEADER_ROW) {
				if (grid.getToolTipProvider() != null) {
					grid.getToolTipProvider().showToolTip(x, y, column, row);
				} else {
					final String toolTip = grid.getLabelProvider().getHeaderToolTip(column);
					showToolTip(x, y, column.getCaption(), (toolTip != null && !toolTip.isEmpty()) ? toolTip : "");
				}

			} else if (gridModel.isParentRow(row)) {
				if (groupColumn != null) {
					//
					// A group row's header tool-tip.
					//
					if (grid.getToolTipProvider() != null) {
						grid.getToolTipProvider().showToolTip(x, y, groupColumn, row);
					} else {
						final String toolTip = grid.getLabelProvider().getHeaderToolTip(groupColumn);
						if (toolTip != null && !toolTip.isEmpty()) {
							showToolTip(x, y, groupColumn.getCaption(), toolTip);
						}
					}

				} else if (groupValue != null) {
					//
					// A group row's value tool-tip.
					//
					if (grid.getToolTipProvider() != null) {
						grid.getToolTipProvider().showToolTip(x, y, groupValue, row);
					} else {
						final String toolTip = grid.getLabelProvider().getToolTip(groupValue, row.getElement());
						if (toolTip != null && !toolTip.isEmpty()) {
							showToolTip(x, y, groupValue.getCaption(), toolTip);
						}
					}
				}

			} else {
				//
				// Normal row's tool-tip.
				//
				if (grid.getToolTipProvider() != null) {
					grid.getToolTipProvider().showToolTip(x, y, column, row);
				} else {
					final String toolTip = grid.getLabelProvider().getToolTip(column, row.getElement());
					if (toolTip != null && !toolTip.isEmpty()) {
						showToolTip(x, y, column.getCaption(), toolTip);
					}
				}
			}
		}
	}

	@Override
	public void mouseMove(final MouseEvent e) {
		if (grid.getToolTipProvider() != null) {
			grid.getToolTipProvider().hide();
		} else {
			toolTip.setVisible(false);
		}		
		
		shift = (e.stateMask & SWT.SHIFT) == SWT.SHIFT;
		ctrl = (e.stateMask & SWT.CTRL) == SWT.CTRL;
		alt = (e.stateMask & SWT.ALT) == SWT.ALT;
		targetColumn = null;		
		
		if (resizing != null) {
			//
			// Resize the column currently being resized.
			//
			final int columnX = viewport.getColumnViewportX(gc, resizing);
			resizing.setWidth(Math.max(1, (e.x - columnX)));
			
			//
			// Cause the grid to repaint and recalculate the viewport.
			//
			gridModel.fireChangeEvent();			

		} else if (repositioningDetect != null) {
			//
			// See if the user hs initiated a drag.
			//
			if (mouseDown && (repositioning == null)) {
				repositioning = repositioningDetect;
			}			
			
			//
			// If we're repositioning a column, see where the current target is.
			//
			final int columnIndex = viewport.getColumnIndexByX(e.x, gc);
			
			if (columnIndex == -1) {
				return;
			}
			
			final Column mouseColumn = gridModel.getColumns().get(columnIndex);
			
			if (mouseColumn == repositioning) {
				return;
			}
			
			final int mouseColumnX = viewport.getColumnViewportX(gc, mouseColumn);
			if (e.x <= (mouseColumnX + (mouseColumn.getWidth() / 2))) {
				targetColumn = mouseColumn;
				
			} else if (columnIndex < gridModel.getColumns().size() - 1) {				
				targetColumn = gridModel.getColumns().get(columnIndex + 1);
				
			} else {
				targetColumn = LAST_COLUMN;
			}
			
			if (isScrollRightNeeded()) {
				//
				// Check to see if we need to scroll right.
				//
				grid.getDisplay().syncExec(new Runnable() {					
					@Override
					public void run() {
						grid.getHorizontalBar().setSelection(Math.min(grid.getHorizontalBar().getMaximum(), grid.getHorizontalBar().getSelection() + DRAG_SCROLL_DISTANCE));				
						gridModel.fireChangeEvent();
						if (isScrollRightNeeded()) {
							grid.getDisplay().timerExec(100, this);
						}
					}
				});
				
			} else if (isScrollLeftNeeded()) {
				//
				// Check to see if we need to scroll left.
				//
				grid.getDisplay().syncExec(new Runnable() {					
					@Override
					public void run() {
						grid.getHorizontalBar().setSelection(Math.max(grid.getHorizontalBar().getMinimum(), grid.getHorizontalBar().getSelection() - DRAG_SCROLL_DISTANCE));				
						gridModel.fireChangeEvent();
						if (isScrollLeftNeeded()) {
							grid.getDisplay().timerExec(100, this);
						}
					}
				});
				
			} else {
				//
				// Cause the drop-image to be rendered.
				//
				grid.redraw();
			}
			
		} else if (trackCell(e.x, e.y)) {
			//
			// Repaint the grid to show the new hovered row.
			//
			grid.redraw();
		}
		
		if ((repositioning == null) && (viewport.getColumnForMouseOperation(gc, e.x, e.y, ColumnMouseOperation.RESIZE) != null)) {
			//
			// Show the column resize mouse cursor.
			//
			if (grid.getCursor() != grid.getDisplay().getSystemCursor(SWT.CURSOR_SIZEE)) {
				grid.setCursor(grid.getDisplay().getSystemCursor(SWT.CURSOR_SIZEE));
			}
			
		} else if ((resizing == null) && (grid.getCursor() != grid.getDisplay().getSystemCursor(SWT.CURSOR_ARROW))) {
			//
			// Restore the default mouse cursor.
			//
			grid.setCursor(grid.getDisplay().getSystemCursor(SWT.CURSOR_ARROW));
		}
	}
	
	// TODO: Move this into the grid so the scroll column and scroll row logic is together.
	private boolean isScrollRightNeeded() {
		return (viewport.getLastColumnIndex() < grid.getColumns().size()) && (targetColumn == grid.getColumns().get(viewport.getLastColumnIndex()));
	}
	
	private boolean isScrollLeftNeeded() {
		return (viewport.getFirstColumnIndex() > 0) && (targetColumn == grid.getColumns().get(viewport.getFirstColumnIndex()));
	}

	@Override
	public void mouseDown(final MouseEvent e) {
		mouseDown = true;
		
		//
		// See if the mouse is near a column header boundary. If so, begin a resize drag.
		//
		resizing = viewport.getColumnForMouseOperation(gc, e.x, e.y, ColumnMouseOperation.RESIZE);
		
		//
		// If we're not resizing, maybe we're dragging a column?
		//
		if (resizing == null) {
			repositioningDetect = viewport.getColumnForMouseOperation(gc, e.x, e.y, ColumnMouseOperation.REPOSITION);
		}
	}

	@Override
	public void mouseUp(final MouseEvent e) {
		if (!mouseDown) {
			return;
		}
		
		mouseDown = false;
		repositioningDetect = null;

		if (grid.getKeyboardHandler().isEscapePressed()) {
			if (resizing != null) {
				resizing = null;
				grid.setCursor(grid.getDisplay().getSystemCursor(SWT.CURSOR_ARROW));
			}
			
			if (repositioning != null) {
				repositioning = null;
				targetColumn = null;
			}
			
			return;
		}
		
		//
		// Get the event details.
		//		
		shift = (e.stateMask & SWT.SHIFT) == SWT.SHIFT;
		ctrl = (e.stateMask & SWT.CTRL) == SWT.CTRL;
		alt = (e.stateMask & SWT.ALT) == SWT.ALT;
		
		if (!grid.isFocusControl()) {
			grid.setFocus();
		}
		
		if (resizing != null) {
			//
			// Complete the resize operation.
			//
			gridModel.fireColumnResizedEvent(resizing);
			resizing = null;
			grid.setCursor(grid.getDisplay().getSystemCursor(SWT.CURSOR_ARROW));
			return;			
		}
		
		if (repositioning != null) {						
			//
			// Reposition the column currently being dragged.
			//
			if (targetColumn != repositioning) {
				if (targetColumn == LAST_COLUMN) {
					//
					// Edge-case, we're moving the column to the end of the grid.
					//
					gridModel.getColumns().remove(repositioning);
					gridModel.getColumns().add(repositioning);
					
					//
					// Cause the grid to repaint and recalculate the scrollbars - because the 
					// v-scroll amount may need updating.
					//
					gridModel.fireChangeEvent();
					
				} else {
					//
					// Move the column now.
					//
					final int targetIndex = gridModel.getColumns().indexOf(targetColumn);				
					if (targetIndex != -1) {
						gridModel.getColumns().remove(repositioning);
						gridModel.getColumns().add(gridModel.getColumns().indexOf(targetColumn), repositioning);
						
						//
						// Cause the grid to repaint and recalculate the scrollbars - because the 
						// v-scroll amount may need updating.
						//
						gridModel.fireChangeEvent();
					}
				}
				
				gridModel.fireColumnMovedEvent(repositioning);
			}
			
			repositioning = null;
			targetColumn = null;
			return;
		}
		
		//
		// Keep the tracked cell up-to-date.
		//
		trackCell(e.x, e.y);

		//
		// Determine if there's been a mouse event we need to handle or we neeed to expose to listeners.
		//
		if (e.button == 1 || e.button == 3) { // LEFT or RIGHT
			if (e.count == 1) {
				if ((column != null) && (column != Column.ROW_NUMBER_COLUMN) && (e.y < viewport.getViewportArea(gc).y)) {
					if (row == Row.COLUMN_HEADER_ROW && e.button == 1) {
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
							if (grid.getContentProvider().isCollapsed(row.getElement())) {
								grid.expandGroups(Collections.singletonList(row.getElement()));
							} else {
								grid.collapseGroups(Collections.singletonList(row.getElement()));
							}
							
							//return; // Don't exit here - allow the group to be selected if expanding.
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
				if (row != null && (row != Row.COLUMN_HEADER_ROW)) {
					if (!(shift || ctrl)) {
						//
						// If the right mouse button is used, and the row being right-clicked is already selected, don't un-select it.
						//
						if (!((e.button == 3) && (row.isSelected()))) {
							//
							// Single row/group replace.
							//
							final List<Row<T>> rows = new ArrayList<>();
							rows.addAll(gridModel.isParentRow(row) ? gridModel.getWholeGroup(row) : Collections.singletonList(row));
							gridModel.getSelectionModel().setSelectedRows(rows);
						}

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
					if (!shift) {
						if (gridModel.isParentElement(row.getElement())) {
							if (groupColumn != null) {
								gridModel.getSelectionModel().setAnchorColumn(groupColumn);
	
							} else if (groupValue != null) {
								gridModel.getSelectionModel().setAnchorColumn(groupValue);
								
							} else if (!gridModel.getGroupByColumns().isEmpty()) {
								gridModel.getSelectionModel().setAnchorColumn(gridModel.getGroupByColumns().get(0));
							}
	
						} else if (column != Column.ROW_NUMBER_COLUMN) {
							gridModel.getSelectionModel().setAnchorColumn(column);
						}
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
						
						T anchorElement = null;
						if (gridModel.isChildElement(row.getElement())) {
							anchorElement = gridModel.getSelectionModel().getAnchorElement();
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
						
						//
						// Ensure the anchor is where the mouse was. Group children lose the anchor on double-click
						// (due to the setSelection code) so we'll restore it.
						//
						if (gridModel.isChildElement(row.getElement())) {
							gridModel.getSelectionModel().setAnchorElement(anchorElement);
						}
					}
				}
			}
		}
		
		//
		// Paint the grid.
		//
		grid.redraw();

		//
		// Notify listeners.
		//
		if (row != null && column != null && (column != Column.ROW_NUMBER_COLUMN)) {
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
