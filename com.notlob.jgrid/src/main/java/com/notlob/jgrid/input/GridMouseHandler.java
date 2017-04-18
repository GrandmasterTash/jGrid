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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.notlob.jgrid.Grid;
import com.notlob.jgrid.Grid.GroupRenderStyle;
import com.notlob.jgrid.Grid.SelectionStyle;
import com.notlob.jgrid.listeners.IGridListener;
import com.notlob.jgrid.model.Column;
import com.notlob.jgrid.model.ColumnMouseOperation;
import com.notlob.jgrid.model.GridModel;
import com.notlob.jgrid.model.Row;
import com.notlob.jgrid.model.Viewport;

/**
 * The main entry-point for mouse activity in the grid.
 * 
 * @author sbolton
 *
 * @param <T>
 */
public class GridMouseHandler<T> extends MouseAdapter implements MouseMoveListener, MouseTrackListener/*, MouseWheelListener*/ {

	protected final Grid<T> grid;
	protected final GridModel<T> gridModel;
	protected final Viewport<T> viewport;
	protected final Collection<IGridListener<T>> listeners;
	protected final GC gc;
	protected final ToolTip toolTip;

	// Track if any mouse button is in the down position.
	protected boolean mouseDown;
	protected boolean shift; // Tracked in mouseMove and mouseUp.
	protected boolean ctrl;
	protected boolean alt;
	protected boolean canReposition;
	protected Column resizing;	
	protected Column repositioning;
	protected Column repositioningDetect;
	protected Column targetColumn;	// Repositioning target.
	
	// Used for an edge-case when dragging a column to the end of the grid.
	public final static Column LAST_COLUMN = new Column("LAST.COLUMN");	
	private final static int LEFT_MOUSE_BUTTON = 1;
	private final static int RIGHT_MOUSE_BUTTON = 3;
	private final static int TOOLTIP_DELAY = 4;  // ms
	private final static int SCROLL_DELAY = 100; // ms
	
	// Track if the mouse is over a row/column.
	protected Row<T> row = null;
	protected Column column = null;
	protected Column groupColumn = null;  // << Mouse is over a group field header.
	protected Column groupValue = null;	  // << Mouse is over a group field value not the header;
	
	private final static Logger logger = LoggerFactory.getLogger(GridMouseHandler.class);
	
	public GridMouseHandler(final Grid<T> grid, final GC gc, final Collection<IGridListener<T>> listeners, final ToolTip toolTip) {
		this.grid = grid;
		this.gridModel = grid.getGridModel();
		this.viewport = grid.getViewport();
		this.listeners = listeners;
		this.toolTip = toolTip;
		this.gc = gc;
	}

	@Override
	public void mouseEnter(final MouseEvent e) {
	}

	@Override
	public void mouseExit(final MouseEvent e) {
		try {
			hideTooltip();
			
		} catch (final Throwable t) {
			logger.error("MouseExit failed.", t);
		}
	}

	@Override
	public void mouseHover(final MouseEvent e) {
		try {
			showToolTipIfRequired(e.x, e.y);
			
		} catch (final Throwable t) {
			logger.error("MouseHover failed.", t);
		}
	}
	
	@Override
	public void mouseMove(final MouseEvent e) {
		try {
			//
			// Track the modifiers
			//
			shift = (e.stateMask & SWT.SHIFT) == SWT.SHIFT;
			ctrl = (e.stateMask & SWT.CTRL) == SWT.CTRL;
			alt = (e.stateMask & SWT.ALT) == SWT.ALT;
			targetColumn = null;
			
			hideTooltip();
			
			if (resizing != null) {
				resizeColumn(e.x);			
	
			} else if (repositioningDetect != null) {
				checkForColumnMove(e.x);
				
				if (isScrollRightNeeded()) {
					scrollRight();
					
				} else if (isScrollLeftNeeded()) {
					scrollLeft();
					
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
			
			updateCursor(e.x, e.y);
	
			//
			// Fix an issue where, when dragging a column, the mouse leaves the grid area, mouse up, then re-enters the grid area.
			//
			if (targetColumn == null && !mouseDown) {
				repositioning = null;
			}
			
		} catch (final Throwable t) {
			logger.error("MouseMove failed", t);
		}
	}
	
	@Override
	public void mouseDown(final MouseEvent e) {
		try {
			if (e.button == LEFT_MOUSE_BUTTON) {			
				mouseDown = true;
				
				//
				// See if the mouse is near a column header boundary. If so, begin a resize drag.
				//
				resizing = viewport.getColumnForMouseOperation(gc, e.x, e.y, ColumnMouseOperation.RESIZE);
				
				//
				// If we're not resizing, maybe we're dragging a column?
				//
				if (resizing == null && isColumnMovingEnabled()) {
					repositioningDetect = viewport.getColumnForMouseOperation(gc, e.x, e.y, ColumnMouseOperation.REPOSITION);
				}
				
			} else if (e.button == RIGHT_MOUSE_BUTTON) {
				//
				// Keep the tracked cell up-to-date.
				//
				trackCell(e.x, e.y);
	
				//
				// Move the anchor to the thing under the mouse.
				//
				switch (gridModel.getSelectionModel().getSelectionStyle()) {
					case MULTI_COLUMN_BASED:
					case SINGLE_COLUMN_BASED:
						if (column != null && (!column.isSelected())) {
							gridModel.getSelectionModel().setSelectedColumns(Collections.singletonList(column));
						}
						break;
						
					default:
						if ((row != null) && (row != gridModel.getColumnHeaderRow())) {
							if (!row.isSelected()) {
								gridModel.getSelectionModel().setSelectedRows(Collections.singletonList(row));
							}
							
							gridModel.getSelectionModel().setAnchorElement(row.getElement());
						}
						break;
				}				
				
				if ((column != null) && (column != gridModel.getGroupSelectorColumn()) && (column != gridModel.getRowNumberColumn())) {
					gridModel.getSelectionModel().setAnchorColumn(column);
				}			
			}
			
		} catch (final Throwable t) {
			logger.error("MouseDown failed", t);
		}
	}

	@Override
	public void mouseUp(final MouseEvent e) {
		try {		
			if (!mouseDown) {
				return;
			}
			
			mouseDown = false;
			repositioningDetect = null;
	
			if (grid.getKeyboardHandler().isEscapePressed()) {
				//
				// If ESC is pressed whilst resizing a column or repositioning a column, abort it.
				//
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
			// If the event is outside the widget bounds - ignore it.
			//
			if (!grid.getClientArea().contains(e.x, e.y)) {
				return;
			}
			
			//
			// Get the event details.
			//		
			shift = (e.stateMask & SWT.SHIFT) == SWT.SHIFT;
			ctrl = (e.stateMask & SWT.CTRL) == SWT.CTRL;
			alt = (e.stateMask & SWT.ALT) == SWT.ALT;
			
			if (!grid.isFocusControl()) {
				//
				// Ensure the grid has the focus.
				//
				grid.setFocus();
			}
			
			if (handleColumnResized(e.count)) {
				return;
			}		
			
			if (handleColumnRepositioned()) {
				return;
			}
			
			//
			// Keep the tracked cell up-to-date.
			//
			trackCell(e.x, e.y);
	
			//
			// Determine if there's been a mouse event we need to handle or we need to expose to listeners.
			//
			if (e.button == LEFT_MOUSE_BUTTON || e.button == RIGHT_MOUSE_BUTTON) {
				if (e.count == 1) {
					handleSingleClick(e.x, e.y, e.button);
	
				} else if (e.count > 1) {				
					handleDoubleClick();
				}
			}
			
			//
			// Paint the grid.
			//
			grid.redraw();
	
			//
			// Notify any listeners.
			//
			notifyListeners(e.x, e.y, e.button, e.stateMask, e.count);
			
		} catch (final Throwable t) {
			logger.error("MouseUp Failed.", t);
		}
	}
	
	/**
	 * Notify any listeners about click events.  
	 */
	protected void notifyListeners(final int mouseX, final int mouseY, final int button, final int stateMask, final int clickCount) {
		if (row != null && column != null && (column != gridModel.getGroupSelectorColumn()) && (column != gridModel.getRowNumberColumn())) {
			for (final IGridListener<T> listener : listeners) {
				if (button == LEFT_MOUSE_BUTTON) {
					if (clickCount == 1) {
						if (row == gridModel.getColumnHeaderRow()) {
							listener.headerClick(column, new Point(mouseX, mouseY), stateMask);
						} else {
							listener.click(column, row.getElement(), new Point(mouseX, mouseY), stateMask);
						}

					} else if (clickCount > 1) {
						if (row == gridModel.getColumnHeaderRow()) {
							listener.headerDoubleClick(column, new Point(mouseX, mouseY), stateMask);
						} else {
							listener.doubleClick(column, row.getElement(), new Point(mouseX, mouseY), stateMask);
						}
					}

				} else if (button == RIGHT_MOUSE_BUTTON) {
					if (row == gridModel.getColumnHeaderRow()) {
						listener.headerRightClick(column, new Point(mouseX, mouseY), stateMask);
					} else {
						listener.rightClick(column, row.getElement(), new Point(mouseX, mouseY), stateMask);
					}
				}
			}
		}
	}
	
	/**
	 * Handle a single click (left OR right) - updating the selection model, expanding/collapsing groups, performing column sorts.
	 */
	protected void handleSingleClick(final int mouseX, final int mouseY, final int button) {
		if ((column != null) && (column != gridModel.getGroupSelectorColumn()) && (column != gridModel.getRowNumberColumn()) && (mouseY < viewport.getViewportArea(gc).y)) {
			// TODO: Investigate/debug the last part of the above statement, it looks dubious to me.
			if (grid.isSortedEnabled() && (row == gridModel.getColumnHeaderRow()) && (button == LEFT_MOUSE_BUTTON)) {
				//
				// Column sorting.
				//
				gridModel.getSortModel().sort(column, true, ctrl, true);
				return;
			}
		}

		//
		// Check for group row hot-spots.
		//
		if ((column != null) && (row != null) && gridModel.isParentRow(row)) {
			//
			// Expand/collapse toggle.
			//
			final Rectangle bounds = grid.getGridRenderer().getExpandImageBounds(gc, row);
			
			if (bounds.contains(mouseX,  mouseY)) {							
				if (grid.getContentProvider().isCollapsed(row.getElement())) {
					grid.expandGroups(Collections.singletonList(row.getElement()));
					
				} else {
					grid.collapseGroups(Collections.singletonList(row.getElement()));
				}
				
				//return; // Don't exit here - allow the group to be selected if expanding.
			}

			if (grid.isSortedEnabled() && alt && (groupColumn != null)) {
				//
				// Toggle the sort on the group column.
				//
				gridModel.getSortModel().sort(groupColumn, true, ctrl, true);
				return;
			}
		}
		
		//
		// Handle the selection.
		//
		if (row != null && (row != gridModel.getColumnHeaderRow())) {
			//
			// Update the anchor column - before triggering selection changed events.
			//
			if (!shift) {
				if (isRenderGroupInline() && gridModel.isParentElement(row.getElement())) {
					if (groupColumn != null) {
						gridModel.getSelectionModel().setAnchorColumn(groupColumn);

					} else if (groupValue != null) {
						gridModel.getSelectionModel().setAnchorColumn(groupValue);
						
					} else if (!gridModel.getGroupByColumns().isEmpty()) {
						gridModel.getSelectionModel().setAnchorColumn(gridModel.getGroupByColumns().get(0));
					}

				} else if ((column != gridModel.getRowNumberColumn()) && (column != gridModel.getGroupSelectorColumn())) {
					gridModel.getSelectionModel().setAnchorColumn(column);
				}
			}
			
			//
			// If we're not selecting columns we're selecting rows (by default).
			//
			final boolean selectColumn = (gridModel.getSelectionModel().getSelectionStyle() == SelectionStyle.MULTI_COLUMN_BASED || gridModel.getSelectionModel().getSelectionStyle() == SelectionStyle.SINGLE_COLUMN_BASED); 
			
			if (!(shift || ctrl)) {				
				//
				// If the right mouse button is used, and the row being right-clicked is already selected, don't un-select it.
				//
				if (!((button == RIGHT_MOUSE_BUTTON) && (row.isSelected()))) {
					//
					// Single row/group or column replace.
					//
					if (selectColumn) {
						gridModel.getSelectionModel().setSelectedColumns(Collections.singletonList(column));						
					} else {
						final List<Row<T>> rows = new ArrayList<>();
						rows.addAll((gridModel.isParentRow(row) || (gridModel.getGroupSelectorColumn() == column)) ? gridModel.getWholeGroup(row) : Collections.singletonList(row));
						gridModel.getSelectionModel().setSelectedRows(rows);
					}
				}

			} else if (ctrl && !shift) {
				//
				// Single row/group or column toggle.
				//
				if (selectColumn) {
					gridModel.getSelectionModel().toggleColumnSelections(Collections.singletonList(column));
				} else {					
					gridModel.getSelectionModel().toggleRowSelections(Collections.singletonList(row));
				}

			} else if (!ctrl && shift) {
				//
				// Range replace.
				//
				if (selectColumn) {
					gridModel.getSelectionModel().selectRange(column, false);
				} else {
					gridModel.getSelectionModel().selectRange(row, false);
				}

			} else if (ctrl && shift) {
				//
				// Range addition.
				//
				if (selectColumn) {
					gridModel.getSelectionModel().selectRange(column, true);
				} else {
					gridModel.getSelectionModel().selectRange(row, true);
				}
			}
		}

		//
		// Select All - the corner has been clicked.
		//
		if ((mouseX < viewport.getViewportArea(gc).x) && (mouseY < viewport.getViewportArea(gc).y) && (viewport.getColumnIndexByX(mouseX, gc) == -1)) {
			gridModel.getSelectionModel().selectAll();
		}
	}
	
	/**
	 * Handle a double-click - selecting the entire group if it's a child row.
	 */
	protected void handleDoubleClick() {
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
	
	/**
	 * Check, if the user was resizing a column, if they've completed the resize. 
	 */
	protected boolean handleColumnResized(final int clickCount) {
		if (resizing != null) {
			final Column wasResizing = resizing;
			
			//
			// Complete the resize operation.
			//
			gridModel.fireColumnResizedEvent(resizing);
			resizing = null;
			grid.setCursor(grid.getDisplay().getSystemCursor(SWT.CURSOR_ARROW));
						
			if (clickCount > 1) {
				//
				// The user has double-clicked on a resize border - execute the auto-resize-a-tron.
				//
				grid.autoSizeColumn(wasResizing);
			}
			return true;			
		}
		
		return false;
	}
	
	/**
	 * Check, if the user was repositioning a column, if they've completed the move. 
	 */
	protected boolean handleColumnRepositioned() {
		if (repositioning != null && isColumnMovingEnabled()) {						
			//
			// Reposition the column currently being dragged.
			//
			if (targetColumn != repositioning) {
				//
				// Move the column now.
				//
				gridModel.moveColumn(repositioning, targetColumn);
			}
			
			repositioning = null;
			targetColumn = null;
			return true;
		}
		
		return false;
	}

	/**
	 * return the column currently under the mouse pointer.
	 */
	public Column getColumn() {
		return column;
	}

	/**
	 * return the row currently under the mouse pointer.
	 */
	public Row<T> getRow() {
		return row;
	}

	/**
	 * return the group column (or value) currently under the mouse pointer.
	 */
	public Column getGroupColumn() {
		return groupColumn;
	}

	/**
	 * If we're in the middle of a column drag, this is the column being dragged.
	 */
	public Column getRepositioningColumn() {
		return repositioning;
	}

	/**
	 * This is where the current (if any) column move will end up.
	 */
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
	
	/**
	 * Make the code more readable.
	 */
	protected boolean isRenderGroupInline() {
		return (grid.getGroupRenderStyle() == GroupRenderStyle.INLINE);
	}

	protected boolean isColumnMovingEnabled() {
		return grid.isColumnMovingEnabled();
	}
	
	/**
	 * Tracks the column and row under the mouse as it moves.
	 *
	 * Returns true if the cell or row changes.
	 */
	protected boolean trackCell(final int x, final int y) {
		Column newGroupColumn = null;
		Column newGroupValue = null;

		//
		// Get the row and column indexes from the viewport.
		//
		final Column newColumn = grid.getColumnAtXY(x, y);
		final Row<T> newRow = grid.getRowAtXY(x, y);

		if ((newRow != null) && (newRow != gridModel.getColumnHeaderRow())) {
			//
			// If this is a group row.
			//
			if (isRenderGroupInline() && gridModel.isParentElement(newRow.getElement())) {
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
			// If any of the tracked things has changed, update out tracked references and return true;
			//
			row = newRow;
			column = newColumn;
			groupColumn = newGroupColumn;
			groupValue = newGroupValue;
			return true;
		}

		//
		// The mouse isn't over anything we need to track.
		//
		return false;
	}

	/**
	 * Scroll the grid right one column - after a short delay.
	 */
	protected void scrollLeft() {
		//
		// Check to see if we need to scroll left.
		//
		grid.getDisplay().syncExec(new Runnable() {					
			@Override
			public void run() {
				grid.getHorizontalBar().setSelection(Math.max(grid.getHorizontalBar().getMinimum(), grid.getHorizontalBar().getSelection() - 1));				
				gridModel.fireChangeEvent();
				if (isScrollLeftNeeded()) {
					grid.getDisplay().timerExec(SCROLL_DELAY, this);
				}
			}
		});
	}

	/**
	 * Scroll the grid right one column - after a short delay.
	 */
	protected void scrollRight() {
		//
		// Check to see if we need to scroll right.
		//
		grid.getDisplay().syncExec(new Runnable() {					
			@Override
			public void run() {
				grid.getHorizontalBar().setSelection(Math.min(grid.getHorizontalBar().getMaximum(), grid.getHorizontalBar().getSelection() + 1));				
				gridModel.fireChangeEvent();
				if (isScrollRightNeeded()) {
					grid.getDisplay().timerExec(SCROLL_DELAY, this);
				}
			}
		});
	}

	/**
	 * See if the user is starting - or is in the middle of a column move.
	 */
	protected void checkForColumnMove(final int mouseX) {
		//
		// See if the user has initiated a drag column move drag.
		//
		if (mouseDown && (repositioning == null) && isColumnMovingEnabled()) {
			repositioning = repositioningDetect;
		}			
		
		//
		// If we're repositioning a column, see where the current target is.
		//
		final int columnIndex = viewport.getColumnIndexByX(mouseX, gc);
		if (columnIndex == -1) {
			//
			// We're moving a column, but we can't currently tell where to...
			//
			return;
		}
		
		final Column mouseColumn = gridModel.getColumns().get(columnIndex);			
		if (mouseColumn == repositioning) {
			targetColumn = mouseColumn;
		}
					
		final int mouseColumnX = viewport.getColumnViewportX(gc, mouseColumn);
		if (mouseX <= (mouseColumnX + (mouseColumn.getWidth() / 2))) {
			//
			// We're dragging to the left of the hovered column.
			//
			targetColumn = mouseColumn;
			
		} else if (columnIndex < gridModel.getColumns().size() - 1) {
			//
			// We're dragging to the right of the hovered column.
			//
			targetColumn = gridModel.getColumns().get(columnIndex + 1);
			
		} else {
			//
			// Something mad has occurred.
			//
			targetColumn = LAST_COLUMN;
		}
	}
	
	/**
	 * Resize the current column being resize - using the mouses current X position.
	 */
	protected void resizeColumn(final int mouseX) {
		//
		// Resize the column currently being resized.
		//
		final int columnX = viewport.getColumnViewportX(gc, resizing);
		resizing.setWidth(Math.max(1, (mouseX - columnX)));
		
		//
		// Cause the grid to repaint and recalculate the viewport.
		//
		gridModel.fireChangeEvent();
	}
		
	/**
	 * returns true if the targetColumn (of a column move operation) is off the right-hand edge of the viewport.
	 */
	protected boolean isScrollRightNeeded() {
		return (viewport.getLastColumnIndex() < grid.getColumns().size()) && (targetColumn == grid.getColumns().get(viewport.getLastColumnIndex()));
	}

	/**
	 * returns true if the targetColumn (of a column move operation) is off the left-hand edge of the viewport.
	 */
	protected boolean isScrollLeftNeeded() {
		return (viewport.getFirstColumnIndex() > 0) && (targetColumn == grid.getColumns().get(viewport.getFirstColumnIndex()));
	}

	/**
	 * Update the mouse cursor if we're resizing or about to resize a column.
	 */
	protected void updateCursor(final int mouseX, final int mouseY) {
		if ((repositioning == null) && (viewport.getColumnForMouseOperation(gc, mouseX, mouseY, ColumnMouseOperation.RESIZE) != null)) {
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
	
	/**
	 * Regardless of whether we're using the grid's own tool-tip provider or our built in one, hide it.
	 */
	protected void hideTooltip() {
		if (grid.getToolTipProvider() != null) {
			grid.getToolTipProvider().hide();
			
		} else {
			toolTip.setVisible(false);
		}
	}

	/**
	 * Show a tool-tip if: -
	 *  - we're not moving a column
	 *  - there IS a column and row under the mouse
	 *  - It's not the group selector or row number column
	 *  - We have a label provider on the grid.
	 * 
	 * Specify whether it's a header tool-tip (if false, it'll be a body tool-tip).
	 */
	protected void showToolTipIfRequired(final int mouseX, final int mouseY) {
		if ((repositioning == null) && (column != null) && (column != gridModel.getGroupSelectorColumn()) && (column != gridModel.getRowNumberColumn()) && (row != null) && (grid.getLabelProvider() != null)) {
			final int x = mouseX;
			final int y = mouseY + 16; // er wut?

			if (row == gridModel.getColumnHeaderRow()) {
				if (grid.getToolTipProvider() != null) {
					grid.getToolTipProvider().showToolTip(x, y, column, row);
				} else {
					final String toolTip = grid.getLabelProvider().getHeaderToolTip(column);
					showToolTip(x, y, column.getCaption(), (toolTip != null && !toolTip.isEmpty()) ? toolTip : "");
				}

			} else if (isRenderGroupInline() && gridModel.isParentRow(row)) {
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
	
	/**
	 * If there's no customised tool-tip provider, use the grid's own tool-tip provider to show the tool-tip
	 * at the location specified.
	 */
	protected void showToolTip(final int x, final int y, final String boldText, final String message) {
		//
		// Build a slight delay otherwise the tool-tip would swallow clicks meant for the grid.
		//
		grid.getDisplay().timerExec(TOOLTIP_DELAY, new Runnable() {
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
