package com.notlob.jgrid.renderer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.TextLayout;

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
	
	// Used for the diagnostic panel and no-filter message.
	private TextLayout textLayout;
	private FontData debugFontData = new FontData("Consolas", 10, SWT.NORMAL);
	
	// Double-buffering image. Used as a key for the setData method.
	private final static String DATA__DOUBLE_BUFFER_IMAGE = "double-buffer-image"; //$NON-NLS-1$
	
	// Double-buffering image. Used as a key for the calculateRowHeights method.
	private final static String DATA__ROW_CALC_IMAGE = "row-height-calc-image"; //$NON-NLS-1$
	
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
		rc = new RenderContext(grid);
		cellRenderer = createCellRenderer();
		rowRenderer = createRowRenderer();
		groupRowRenderer = createGroupRowRenderer();
		selectionRenderer = createSelectionRenderer();
		contentLocation = new Point(0, 0);
		rowBounds = new Rectangle(0, 0, 0, 0);
		borderBounds = new Rectangle(0, 0, 0, 0);
	}
	
	public void dispose() {
		if (textLayout != null) {
			textLayout.dispose();
		}
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
		if (!gridModel.isParentRow(row)) {
			return null;
		}
		
		rc.setGC(gc);
		setDefaultRowBounds(gc, row);
		
		if (grid.getGroupRenderStyle() == GroupRenderStyle.INLINE) {				
			return groupRowRenderer.getExpandImageBounds(rc, row, rowBounds);
			
		} else {
			return rowRenderer.getExpandImageBounds(rc, row, rowBounds);
		}		
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
		if (rc.isPainting()) {
			throw new IllegalAccessError("Already painting");
		}
		
		GC gc = null;
		
		try {
			rc.setPainting(true);
			rc.setAnimationPending(false);
			rc.setAnyRowHeightsChanged(false);
			
			if (grid.getLabelProvider() == null) {
				throw new IllegalArgumentException("There's no IGridLabelProvider on the grid.");
			}
			
			if (grid.getContentProvider() == null) {
				throw new IllegalArgumentException("There's no IGridContentProvider on the grid.");
			}
			
			//
			// Double-buffer the paint event.
			//
			final Image image = getDoubleBufferImage(DATA__DOUBLE_BUFFER_IMAGE);

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
				// Give renderers an opportunity to alter bounds. For example, wrapped cell content may have to
				// grow the row height.
				//
				rc.setRenderPass(RenderPass.COMPUTE_SIZE);
				paintRows(rc);

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
				paintNoDataMessage(gc);
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
			// Display some diagnostics.
			//
			if (grid.isDebugPainting()) {
				paintDiagnostics(gc);
			}
			
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
						if (!grid.isDisposed()) {
							for (Row<T> row : grid.getRows()) {
								if (row.getFrame() != -1) {
									row.setFrame(row.getFrame() + row.getAnimation().getIncrement());
								}
							}
							
							grid.redraw();
						}
					}
				});
			}
			
			//
			// A COMPUTE_SIZE pass can cause the number of rows in the viewport to change.
			//
			if (rc.isAnyRowHeightsChanged()) {
				grid.updateScrollbars();
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
			
			rc.setPainting(false);
		}
	}

	/**
	 * When there's nothing to display, paint a message in the centre of the grid - the message can change if a filter
	 * has filtered-out all the data. 
	 */
	protected void paintNoDataMessage(GC gc) {
		//
		// Paint the 'no data' message. Note the filter check has to compensate for the CollapseGroupFilter. Naff.
		//
		final String text = (grid.getFilters().size() > 1) ? (grid.getEmptyFilterMessage() == null ? getDefaultFiltersHiddenDataMessage() : grid.getEmptyFilterMessage()) : (grid.getEmptyMessage() == null ? getDefaultNoDataMessage() : grid.getEmptyMessage());
		final CellStyle cellStyle = styleRegistry.getNoDataStyle();
		final Rectangle bounds = viewport.getViewportArea(gc);
		
		gc.setAlpha(cellStyle.getForegroundOpacity());
		gc.setFont(getFont(cellStyle.getFontData()));
		gc.setForeground(getColour(cellStyle.getForeground()));		
		gc.setClipping(bounds);
		
		if (textLayout == null) {
			textLayout = new TextLayout(gc.getDevice());
		}
		
		textLayout.setFont(getFont(cellStyle.getFontData()));
		textLayout.setAlignment(SWT.CENTER);
		textLayout.setWidth(bounds.width);		
		textLayout.setText(text);
		
		align(textLayout.getBounds().x, textLayout.getBounds().y, bounds, contentLocation, cellStyle.getTextAlignment());
		textLayout.draw(gc, 0, contentLocation.y);
		gc.setClipping((Rectangle) null);
	}

	/**
	 * Get or create an image with the same bounds as the grid - used to render onto - for double-buffering.
	 */
	private Image getDoubleBufferImage(final String imageKey) {
		Image image = (Image) grid.getData(imageKey);
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
			grid.setData(imageKey, image);
		}
		return image;
	}	
	
	/**
	 * Paints viewport and model details in an overlay.
	 */
	private void paintDiagnostics(GC gc) {
		try {
			final StringBuilder sb = new StringBuilder();
			sb.append(String.format("Grid Diagnostics\nColumns [%s]\tRows [%s]", 
					grid.getColumns().size(), 
					grid.getRows().size()));
			
			sb.append(String.format("\n\nCanvas\nWidth [%s]\tHeight [%s]", 
					grid.getClientArea().width, 
					grid.getClientArea().height));
			
			sb.append(String.format("\n\nViewport\nRow Idx First [%s] Last [%s] LVis [%s]", 
					viewport.getFirstRowIndex(), 
					viewport.getLastRowIndex(),
					viewport.getLastVisibleRowIndex()));
			
			sb.append(String.format("\nCol Idx First [%s] Last [%s] LVis [%s]", 
					viewport.getFirstColumnIndex(), 
					viewport.getLastColumnIndex(),
					viewport.getLastVisibleColumnIndex()));
			
			sb.append(String.format("\nLast Page Size [%s]", grid.getViewport().getRowCountLastPage(gc)));
			
			sb.append(String.format("\n\nScrollbars\nV-Max [%s] V-Cur [%s]",
					grid.getVerticalBar().getMaximum(),
					grid.getVerticalBar().getSelection()));
			
			sb.append(String.format("\nH-Max [%s] H-Cur [%s]",
					grid.getHorizontalBar().getMaximum(),
					grid.getHorizontalBar().getSelection()));
			
			sb.append(String.format("\n\nMouse\nCol Idx [%s] Row Idx [%s]", 
					grid.getColumns().indexOf(grid.getMouseHandler().getColumn()), 
					grid.getRows().indexOf(grid.getMouseHandler().getRow())));
			
			if (sb.length() > 0) {
				if (textLayout == null) {
					textLayout = new TextLayout(gc.getDevice());
				}
				textLayout.setFont(getFont(debugFontData));
				textLayout.setAlignment(SWT.LEFT);
				textLayout.setTabs(new int[] {100});
				textLayout.setWidth(300);
				
				gc.setClipping((Rectangle) null);
				gc.setAlpha(200);
				gc.setForeground(getColour(new RGB(0, 255, 0)));
				gc.setBackground(getColour(new RGB(102, 102, 102)));
				gc.fillRectangle(20, 20, textLayout.getBounds().width, textLayout.getBounds().height);
				textLayout.setText(sb.toString());				
				textLayout.draw(gc, 20, 20);
			}
		} catch (Throwable t) {
			System.err.println("Failed to diag" + t);
		}
	}

	/**
	 * Forces all rows to calculate their height.
	 */
	public void calculateRowHeights() {
		if (!rc.isPainting()) {
			GC gc = null;
			
			try {
				gc = new GC(getDoubleBufferImage(DATA__ROW_CALC_IMAGE));
				rc.setPainting(true);
				rc.setGC(gc);
				rc.setRenderPass(RenderPass.COMPUTE_SIZE);
				rc.setForceAllRows(true);
				paintRows(rc);
				rc.setForceAllRows(false);
				
			} finally {
				gc.dispose();
				rc.setPainting(false);
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
		if (grid.isShowColumnHeaders()) {
			final Row<T> row = gridModel.getColumnHeaderRow();
			rowBounds.height = grid.getRowHeight(row);
			rowRenderer.paintRow(rc, rowBounds, row);
		}
		
		//
		// Paint the main rows (including the row number column and the pinned columns).
		//
		rc.setAlternate(false);
		rowBounds.y = viewportArea.y + styleRegistry.getCellSpacingVertical();
		
		final int startRow = rc.isForceAllRows() ? 0 : viewport.getFirstRowIndex();
		final int endRow = rc.isForceAllRows() ? (gridModel.getRows().size() - 1) : viewport.getLastVisibleRowIndex();
		
		for (int rowIndex=startRow; rowIndex<endRow; rowIndex++) {
			final Row<T> row = gridModel.getRows().get(rowIndex);
			rc.setAlternate(row.isAlternateBackground());
			
			//
			// Initialise the row height used for the wrapped cell calculation.
			//
			if (rc.getRenderPass() == RenderPass.COMPUTE_SIZE) {
				rc.setComputedHeightDelta(null);
			}
			
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
			
			//
			// Advance any row animation.
			//
			if (row.getAnimation() != null) {
				row.getAnimation().postAnimate(rc, row);
			}
			
			//
			// Adjust the row height after any cell-wrapping calculations.
			//
			if ((rc.getRenderPass() == RenderPass.COMPUTE_SIZE) && (rc.getComputedHeightDelta() != null)) {
				final int newHeight = grid.getRowHeight(row) + rc.getComputedHeightDelta();
				if (newHeight > 0 && rc.getComputedHeightDelta() != 0) {
//					System.out.println(String.format("height [%s] applying delta [%s] new-height [%s]", grid.getRowHeight(row), rc.getComputedHeightDelta(), newHeight));
					row.setHeight(newHeight);
					rc.setAnyRowHeightsChanged(true);
				} else {
//					System.out.println(String.format("IGNORED height [%s] applying delta [%s] new-height [%s]", grid.getRowHeight(row), rc.getComputedHeightDelta(), newHeight));
				}
				
				rc.setComputedHeightDelta(null);
			}
			
			//
			// Move the bounds down for the next row.
			//
			rowBounds.y += (rowBounds.height + styleRegistry.getCellSpacingVertical());
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
				final RenderContext imageRC = new RenderContext(grid);
				final GC imageGC = new GC(columnDragImage);
				imageRC.setGC(imageGC);
				
				//
				// Render the column header to the imageGC.
				//
				imageRC.setRenderPass(RenderPass.BACKGROUND);
				cellRenderer.paintCell(imageRC, dragImageBounds, column, gridModel.getColumnHeaderRow(), cellStyle);
				
				imageRC.setRenderPass(RenderPass.FOREGROUND);
				cellRenderer.paintCell(imageRC, dragImageBounds, column, gridModel.getColumnHeaderRow(), cellStyle);
				
				//
				// Restore the original GC and clean-up the image GC.
				//
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
		// Ensure the standard image can also fit - if the cell could potentially render an image.
		//
		if (cellStyle.getContentStyle() != ContentStyle.TEXT) {
			//
			// Get the images for the cell.
			//
			imageCollector.clear();
			
			if (grid.getGridModel().isHeaderRow(row)) {
				grid.getLabelProvider().getHeaderImage(imageCollector, column);
				
			} else {
				grid.getLabelProvider().getImage(imageCollector, column, row.getElement());
			}
			
			if (imageCollector.isEmpty()) {
				if (grid.getGridModel().isHeaderRow(row)) {
					//
					// Even if there are no images, leave some room for the sort indicator (if this is the header).
					//
					final Image sortedImage = getImage("sort_ascending.png");
					width += (sortedImage.getBounds().width + cellStyle.getPaddingImageText());
				}
				
			} else {
				for (Image image : imageCollector.getImages()) {
					width += image.getBounds().width;
				}
				
				width += cellStyle.getPaddingImageText();
			}
		}
		
		//
		// Include any text in the width.
		//
		if (cellStyle.getContentStyle() != ContentStyle.IMAGE) {
			final String text = cellRenderer.getCellText(column, row);
			final Point point = getTextExtent(text, gc, cellStyle.getFontData());
			width += point.x;
		}
		
		width += cellStyle.getBorderOuterLeft() == null ? 0 : cellStyle.getBorderOuterLeft().getWidth();
		width += cellStyle.getBorderOuterRight() == null ? 0 : cellStyle.getBorderOuterRight().getWidth();
		width += 6; // FUDGE.
		
		return width;
	}
}
