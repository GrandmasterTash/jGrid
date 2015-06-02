package com.notlob.jgrid.renderer;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;

import com.notlob.jgrid.Grid;
import com.notlob.jgrid.input.GridMouseHandler;
import com.notlob.jgrid.model.Column;
import com.notlob.jgrid.model.Row;
import com.notlob.jgrid.styles.CellStyle;

/**
 * Responsible for rendering normal (non-group) rows.
 * 
 * @author Stef
 *
 * @param <T>
 */
public class RowRenderer<T> extends Renderer<T> {

	protected final CellRenderer<T> cellRenderer;
	
	protected final Image dropImage;
	
	//
	// The references below are recycled objects - to avoid GC churn.
	//	
	protected final Rectangle cellBounds;
	
	public RowRenderer(final Grid<T> grid, final CellRenderer<T> cellRenderer) {
		super(grid);
		this.cellRenderer = cellRenderer;
		dropImage = getImage("inwards_arrows.png");
		cellBounds = new Rectangle(0, 0, 0, 0);
	}
			
	public void paintRow(final RenderContext rc, final Rectangle rowBounds, final Row<T> row) {
		try {
			final GC gc = rc.getGC();
			
			//
			// Paint the row background
			//
			// TODO: This once we've fixed spacing and outer borders.
	
			cellBounds.x = rowBounds.x;
			cellBounds.y = rowBounds.y;
			cellBounds.height = rowBounds.height;

			if (grid.isShowRowNumbers()) {
				if (row == gridModel.getColumnHeaderRow()) {
					//
					// Paint the corner cell if needed.
					//
					paintCornerCell(rc);
					
				} else {				
					//
					// Paint row numbers if needed.
					//
					paintRowNumber(rc, row);
				}
			}
			
			//
			// Paint any pinned cells.
			//
			rc.setPaintingPinned(true);
			for (Column pinnedColumn : gridModel.getPinnedColumns()) {
				final CellStyle cellStyle = styleRegistry.getCellStyle(pinnedColumn, row);				
				cellBounds.width = pinnedColumn.getWidth();
				cellRenderer.paintCell(rc, cellBounds, pinnedColumn, row, cellStyle);
				cellBounds.x += (cellBounds.width + styleRegistry.getCellSpacingHorizontal());
			}
			rc.setPaintingPinned(false);
			
			//
			// Paint a vertical border - to the right of the last pinned cell.
			//
			if (!gridModel.getPinnedColumns().isEmpty()) {
				setCorners(cellBounds, topLeft, topRight, bottomRight, bottomLeft);
				paintBorderLine(gc, styleRegistry.getHeaderStyle().getBorderOuterBottom(), topLeft, bottomLeft);
			}
	
			//
			// Now paint every cell in the row.
			//
			for (int columnIndex=viewport.getFirstColumnIndex(); columnIndex<viewport.getLastVisibleColumnIndex(); columnIndex++) {
				final Column column = gridModel.getColumns().get(columnIndex);
				final CellStyle cellStyle = styleRegistry.getCellStyle(column, row);
	
				//
				// Don't paint a column header grip on the last column.
				//
				rc.setDontPaintGrip(columnIndex == (gridModel.getColumns().size() - 1));
				
				cellBounds.width = column.getWidth();
				cellRenderer.paintCell(rc, cellBounds, column, row, cellStyle);
				cellBounds.x += (cellBounds.width + styleRegistry.getCellSpacingHorizontal());
				rc.setDontPaintGrip(false);
			}
	
			//
			// Render a column-reposition indicator if we're dragging columns around.
			//
			// TODO: Move code out of RowRenderer and into paintColumnDragImage?
			if ((row == gridModel.getColumnHeaderRow()) && (rc.getRenderPass() == RenderPass.FOREGROUND) && (grid.getMouseHandler().getTargetColumn() != null)) {							
				//
				// Otherwise, move across the viewport until we get to the drag target column.
				//
				cellBounds.x = rowBounds.x;
				
				//
				// Offset by row number column width.
				//
				if (grid.isShowRowNumbers()) {
					cellBounds.x += (gridModel.getRowNumberColumn().getWidth() + styleRegistry.getCellSpacingHorizontal());
				}
				
				//
				// Offset by pinned column widths.
				//
				for (Column pinnedColumn : gridModel.getPinnedColumns()) {
					cellBounds.x += (pinnedColumn.getWidth() + styleRegistry.getCellSpacingHorizontal());
				}
				
				for (int columnIndex=viewport.getFirstColumnIndex(); columnIndex<viewport.getLastVisibleColumnIndex(); columnIndex++) {
					final Column column = gridModel.getColumns().get(columnIndex);
					if (column == grid.getMouseHandler().getTargetColumn()){ 
						gc.drawImage(dropImage, cellBounds.x - (dropImage.getBounds().width / 2) + 1, 4);					
					}
					cellBounds.width = column.getWidth();
					cellBounds.x += (cellBounds.width + styleRegistry.getCellSpacingHorizontal());
				}
				
				if (grid.getMouseHandler().getTargetColumn() == GridMouseHandler.LAST_COLUMN) {
					gc.drawImage(dropImage, cellBounds.x - (dropImage.getBounds().width / 2) + 1, 4);
				}
			}
			
		} catch (Throwable t) {
			if (!rc.isErrorLogged()) {
				//
				// Print the error to the std err and ensure we only do this once to avoid log fillage.
				//
				System.err.println(String.format("Failed to paint row: %s", t.getMessage()));
				t.printStackTrace(System.err);
				rc.setErrorLogged(true);
			}
		}
	}
	
	/**
	 * Paint the top-left corner cell.
	 */
	protected void paintCornerCell(final RenderContext rc) {
		//
		// Ensure the grip style header separator isn't used for this column header.
		//
		rc.setDontPaintGrip(true);

		//
		// Paint the background or borders.
		//
		cellBounds.width = gridModel.getRowNumberColumn().getWidth();
		cellRenderer.paintCell(rc, cellBounds, gridModel.getRowNumberColumn(), gridModel.getColumnHeaderRow(), styleRegistry.getCornerStyle());
		cellBounds.x += (cellBounds.width + styleRegistry.getCellSpacingHorizontal());
		
		//
		// Allow the grip to be used.
		//
		rc.setDontPaintGrip(false);
	}
	
	/**
	 * Paint a row number cell for the specified row.
	 */
	protected void paintRowNumber(final RenderContext rc, final Row<T> row) {		
		//
		// Get the normal row-number style or the selected style.
		//
		final CellStyle cellStyle = (grid.isFocusControl() && grid.isHighlightAnchorInHeaders() && doesRowHaveAnchor(row)) ? styleRegistry.getSelectionRowNumberStyle() : styleRegistry.getRowNumberStyle();
		
		//
		// Paint the row-number cell.
		//
		cellBounds.width = gridModel.getRowNumberColumn().getWidth();
		cellRenderer.paintCell(rc, cellBounds, gridModel.getRowNumberColumn(), row, cellStyle);
		cellBounds.x += (cellBounds.width + styleRegistry.getCellSpacingHorizontal());
	}
}
