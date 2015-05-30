package com.notlob.jgrid.renderer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

import com.notlob.jgrid.Grid;
import com.notlob.jgrid.Grid.GroupRenderStyle;
import com.notlob.jgrid.model.Column;
import com.notlob.jgrid.model.Row;
import com.notlob.jgrid.styles.CellStyle;
import com.notlob.jgrid.styles.ContentStyle;

/**
 * Responsible for painting the grid widget.
 * 
 * @author Stef
 */
public class GridRenderer<T> extends Renderer<T> implements PaintListener {
	
	// Passed to each renderer during a control paint event.
	protected final RenderContext rc;
	
	// Used to render cells.
	protected final CellRenderer<T> cellRenderer;
	
	// Used to render rows of columns.
	protected final RowRenderer<T> rowRenderer;
	
	// Used for in-line (rather than column-based) group rows.
	protected final GroupRowRenderer<T> groupRowRenderer;
	
	// Used to render the selected cells.
	protected final SelectionRenderer<T> selectionRenderer;
	
	// Set to represent the header cell when a column is being dragged to re-locate.
	protected Image columnDragImage;
	
	// Double-buffering image. Used as a key for the setData method.
	private final static String DATA__DOUBLE_BUFFER_IMAGE = "double-buffer-image"; //$NON-NLS-1$
	
	//
	// The references below are recycled objects - to avoid GC churn.
	//
	
	// Used to set each row's bounds.
	protected final Rectangle rowBounds;
	
	// Used to align the 'no data' message within the control bounds.
	protected final Point contentLocation;
	
	// Used to render the grid's borderlines.
	protected final Rectangle borderBounds;

	public GridRenderer(final Grid<T> grid) {
		super(grid);
		rc = new RenderContext();
		cellRenderer = createCellRenderer();
		rowRenderer = createRowRenderer();
		groupRowRenderer = createGroupRowRenderer();
		selectionRenderer = createSelectionRenderer();
		contentLocation = new Point(0, 0);
		rowBounds = new Rectangle(0, 0, 0, 0);
		borderBounds = new Rectangle(0, 0, 0, 0);
	}
	
	protected CellRenderer<T> createCellRenderer() {
		return new CellRenderer<T>(grid);
	}
	
	protected RowRenderer<T> createRowRenderer() {
		return new RowRenderer<T>(grid, cellRenderer);
	}
	
	protected GroupRowRenderer<T> createGroupRowRenderer() {
		return new GroupRowRenderer<T>(grid, cellRenderer);
	}
	
	protected SelectionRenderer<T> createSelectionRenderer() {
		return new SelectionRenderer<T>(grid);
	}
	
	public Column getGroupColumnForX(final GC gc, final Row<T> row, final int x, final boolean header) {
		rc.setGC(gc);
		setDefaultRowBounds(gc, row);
		return groupRowRenderer.getGroupColumnForX(rc, x, header, row, rowBounds);
	}
	
	public int getGroupColumnX(final GC gc, final Column findColumn, final Row<T> row) {
		rc.setGC(gc);
		setDefaultRowBounds(gc, row);		
		return groupRowRenderer.getGroupColumnX(rc, findColumn, row, rowBounds);
	}
	
	public Rectangle getExpandImageBounds(final GC gc, final Row<T> row) {
		rc.setGC(gc);
		setDefaultRowBounds(gc, row);		
		return groupRowRenderer.getExpandImageBounds(rc, row, rowBounds);
	}
	
	protected void setDefaultRowBounds(final GC gc, final Row<T> row) {
		rowBounds.x = styleRegistry.getCellSpacingHorizontal();
		rowBounds.y = viewport.getViewportArea(gc).y + viewport.getRowY(gc, row);
		rowBounds.width = grid.getClientArea().width - grid.getClientArea().x;
		rowBounds.height = grid.getRowHeight(row);
	}
	
	public Point getTextExtent(final String text, final GC gc, final FontData fontData) {
		rc.setGC(gc);
		return super.getTextExtent(text, rc, fontData);
	}
	
	@Override
	public void paintControl(final PaintEvent e) {
		GC gc = null;
		rc.setAnimationPending(false);
		
		try {
			if (grid.getLabelProvider() == null) {
				throw new IllegalArgumentException("There's no IGridLabelProvider on the grid.");
			}
			
			if (grid.getContentProvider() == null) {
				throw new IllegalArgumentException("There's no IGridContentProvider on the grid.");
			}
			
			//
			// Double-buffer the paint event.
			//
			Image image = (Image) grid.getData(DATA__DOUBLE_BUFFER_IMAGE);
		    if ((image == null) || (image.getBounds().width != grid.getSize().x) || (image.getBounds().height != grid.getSize().y)) {
		    	//
		    	// If the old image no longer fits the bounds, trash it.
		    	//
		    	if (image != null) {
		    		image.dispose();
		    	}

		    	//
		    	// Store the double-buffer image in the data of the canvas.
		    	//
		    	image = new Image(grid.getDisplay(), grid.getSize().x, grid.getSize().y);
		    	grid.setData(DATA__DOUBLE_BUFFER_IMAGE, image);
		    }

		    //
		    // Set-up a GC for this paint event.
		    //
		    gc = new GC(image);
			gc.setBackground(getColour(styleRegistry.getBackgroundColour()));
			gc.fillRectangle(grid.getClientArea());
			gc.setAntialias(SWT.ON);
			gc.setTextAntialias(SWT.ON);
			
			//
			// Ensure our RC has this GC. Nicey.
			//
			rc.setGC(gc);

			if (gridModel != null && !gridModel.getColumns().isEmpty()) {
				//
				// Calculate the viewport ranges.
				//
				viewport.calculateVisibleCellRange(gc);

				//
				// Paint the grid and cell backgrounds.
				//
				rc.setRenderPass(RenderPass.BACKGROUND);
				paintRows(rc);
				selectionRenderer.paintSelectionRegion(rc);

				//
				// Paint the grid and cell foregrounds.
				//
				rc.setRenderPass(RenderPass.FOREGROUND);
				paintRows(rc);
				selectionRenderer.paintSelectionRegion(rc);
				
				//
				// Paint a drag image if we're dragging a column.
				//
				paintColumnDragImage(rc);
			} 
			
			if (gridModel.getRows().isEmpty()) {
				//
				// Paint the 'no data' message. Note the filter check has to compensate for the CollapseGroupFilter. Naff.
				//
				final String text = grid.getEmptyMessage() == null ? (grid.getFilters().size() > 1 ? getDefaultFiltersHiddenDataMessage() : getDefaultNoDataMessage()) : grid.getEmptyMessage();
				final CellStyle cellStyle = styleRegistry.getNoDataStyle();
				final Point point = getTextExtent(text, rc, cellStyle.getFontData());
				final Rectangle bounds = viewport.getViewportArea(gc);
				final Rectangle oldBounds = gc.getClipping();
				
				gc.setAlpha(cellStyle.getForegroundOpacity());
				gc.setFont(getFont(cellStyle.getFontData()));
				gc.setForeground(getColour(cellStyle.getForeground()));
				align(point.x, point.y, bounds, contentLocation, cellStyle.getTextAlignment());
				gc.setClipping(bounds);
				gc.drawText(text, contentLocation.x, contentLocation.y, SWT.DRAW_TRANSPARENT);
				gc.setClipping(oldBounds);
			}

			//
			// Paint a main border if required (column header outer-top border can be used for the top border along the grid).
			//
			borderBounds.x = grid.getClientArea().x;
			borderBounds.y = grid.getClientArea().y;
			borderBounds.width = grid.getClientArea().width - 1;
			borderBounds.height = grid.getClientArea().height - 1;
			setCorners(borderBounds, topLeft, topRight, bottomRight, bottomLeft);
			paintBorderLine(gc, styleRegistry.getMainBorderLeft(), topLeft, bottomLeft);
			paintBorderLine(gc, styleRegistry.getMainBorderRight(), topRight, bottomRight);
			paintBorderLine(gc, styleRegistry.getMainBorderTop(), topLeft, topRight);
			paintBorderLine(gc, styleRegistry.getMainBorderBottom(), bottomLeft, bottomRight);
			
			//
			// Paint the image to the real GC now.
			//
			e.gc.drawImage(image, 0, 0);
			
			//
			// Schedule another paint if we're animating.
			//
			if (rc.isAnimationPending()) {
				grid.getDisplay().timerExec(ANIMATION_INTERVAL, new Runnable() {
					@Override
					public void run() {
						grid.redraw();
					}
				});
			}

		} catch (final Throwable t) {
			if (!rc.isErrorLogged()) {
				//
				// Print the error to the std err and ensure we only do this once to avoid log fillage.
				//
				System.err.println(String.format("Failed to paint control: %s", t.getMessage()));
				t.printStackTrace(System.err);
				rc.setErrorLogged(true);
			}

		} finally {
			if (gc != null) {
				gc.dispose();
			}
		}
	}

	/**
	 * Iterate over the header row(s) then body rows and render each row in turn.
	 */
	protected void paintRows(final RenderContext rc) {
		final GC gc = rc.getGC();
		final Rectangle viewportArea = viewport.getViewportArea(gc);

		//
		// Set-up the initial row's bounds.
		//
		rowBounds.x = styleRegistry.getCellSpacingHorizontal();
		rowBounds.y = styleRegistry.getCellSpacingVertical();
		rowBounds.width = grid.getClientArea().width - grid.getClientArea().x;
		
		//
		// Paint the column header row(s).
		//
		Row<T> row = gridModel.getColumnHeaderRow();
		rowBounds.height = grid.getRowHeight(row);
		rowRenderer.paintRow(rc, rowBounds, row);
		
		//
		// Paint the main rows (including the row number column and the pinned columns).
		//
		rc.setAlternate(false);
		rowBounds.y = viewportArea.y;
		
		for (int rowIndex=viewport.getFirstRowIndex(); rowIndex<viewport.getLastVisibleRowIndex(); rowIndex++) {
			row = gridModel.getRows().get(rowIndex);
			rowBounds.height = grid.getRowHeight(row);
			
			if (gridModel.isParentRow(row) && (grid.getGroupRenderStyle() == GroupRenderStyle.INLINE)) {
				//
				// Paint the group row, by using the groupBy columns from left-to-right.
				//
				groupRowRenderer.paintRow(rc, rowBounds, row);
				
			} else {
				//
				// Just paint the row like any normal row - with columns.
				//
				rowRenderer.paintRow(rc, rowBounds, row);
			}				

			rowBounds.y += (rowBounds.height + styleRegistry.getCellSpacingVertical());

			//
			// If there's a next row, and it's in the same group, don't flip the alternate background.
			//
			final int nextIndex = rowIndex + 1;
			if (!((nextIndex < viewport.getLastRowIndex()) && (nextIndex < gridModel.getRows().size()) && (gridModel.isSameGroup(row, gridModel.getRows().get(nextIndex))))) {
				rc.setAlternate(!rc.isAlternate());
			}
		}		
	}
	
	/**
	 * If there's a column being repositioned with the mouse, render a 'drag image' representing the column
	 * header at the mouse location.
	 */
	protected void paintColumnDragImage(final RenderContext rc) {		
		final Column column = grid.getMouseHandler().getRepositioningColumn();
		
		if ((column == null) && (columnDragImage != null)) {
			//
			// If we're not dragging then dispose any previous image.
			//
			columnDragImage.dispose();
			columnDragImage = null;
			
		} else if (column != null) {
			final GC gc = rc.getGC();
			
			if (columnDragImage == null) {				
				//
				// Create a column drag image.
				//
				final CellStyle cellStyle = styleRegistry.getCellStyle(column, gridModel.getColumnHeaderRow());
				final int height = grid.getRowHeight(gridModel.getColumnHeaderRow());
				columnDragImage = new Image(grid.getDisplay(), column.getWidth(), height);
				final Rectangle dragImageBounds = new Rectangle(columnDragImage.getBounds().x, columnDragImage.getBounds().y, columnDragImage.getBounds().width - 1, columnDragImage.getBounds().height - 1);
				
				//
				// Create a new GC to create an image of the column header in and back-up the actual GC.
				//
				final GC imageGC = new GC(columnDragImage);
				final GC oldGC = rc.getGC();
				
				//
				// Render the column header to the imageGC.
				//
				cellRenderer.paintCell(rc, dragImageBounds, column, gridModel.getColumnHeaderRow(), cellStyle);
				
				//
				// Restor the original GC and clean-up the image GC.
				//
				rc.setGC(oldGC);
				imageGC.dispose();
			}
			
			//
			// Render the column drag image at the mouses location on the main GC.
			//
			final Point mouseLocation = grid.toControl(new Point(
					grid.getDisplay().getCursorLocation().x - (columnDragImage.getBounds().width / 2), 
					grid.getDisplay().getCursorLocation().y));

			gc.setAlpha(220);
			gc.drawImage(columnDragImage, mouseLocation.x, mouseLocation.y);
		}
	}
	
	protected String getDefaultFiltersHiddenDataMessage() {
		return "No data matches your filter criteria.";
	}

	protected String getDefaultNoDataMessage() {
		return "There is no data to display.";
	}
	
	public boolean isPaintingPinned() {
		return rc.isPaintingPinned();
	}
	
	public int getMinimumWidth(final GC gc, final Column column) {
		//
		// Get the column header style and caption. 
		//
		int minWidth = getCellMinimumWidth(gc, column, gridModel.getColumnHeaderRow());
		
		//
		// Iterate over each cell in the column getting style, content, images, padding, text extents, etc....
		//
		for (Row<T> row : grid.getRows()) {
			minWidth = Math.max(minWidth, getCellMinimumWidth(gc, column, row));
		}
		
		//
		// Don't allow auto-resize to zap a column out of existence.
		//
		return Math.max(minWidth, 5);
	}
	
	protected int getCellMinimumWidth(final GC gc, final Column column, final Row<T> row) {
		final CellStyle cellStyle = styleRegistry.getCellStyle(column, row);
		gc.setFont(getFont(cellStyle.getFontData()));
		
		//
		// Include cell padding.
		//
		int width = cellStyle.getPaddingLeft() + cellStyle.getPaddingRight();

		//
		// Ensure the standard image can also fit.
		//
		if (cellStyle.getContentStyle() != ContentStyle.TEXT) {
			width += (16 + cellStyle.getPaddingImageText());
		}
		
		//
		// Include any text in the width.
		//
		if (cellStyle.getContentStyle() != ContentStyle.IMAGE) {
			final String text = cellRenderer.getCellText(column, row);
			final Point point = getTextExtent(text, gc, cellStyle.getFontData());
			width += point.x;
		}
		
		return width;
	}
}
