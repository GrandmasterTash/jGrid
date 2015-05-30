package com.notlob.jgrid.renderer;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

import com.notlob.jgrid.Grid;
import com.notlob.jgrid.model.Column;
import com.notlob.jgrid.model.Row;
import com.notlob.jgrid.model.SortDirection;
import com.notlob.jgrid.styles.AlignmentStyle;
import com.notlob.jgrid.styles.CellStyle;
import com.notlob.jgrid.styles.ContentStyle;

/**
 * Responsible for rendering 'inline' group rows. Those are rows where the values are renderer left-to-right along with
 * the field/column name and come from the group-by columns - as opposed to a normal row which only renders values
 * in columns.
 * 
 * @author Stef
 *
 * @param <T>
 */
public class GroupRowRenderer<T> extends Renderer<T> {

	protected final CellRenderer<T> cellRenderer;
	
	protected final static int PADDING__EXPAND_COLLAPSE_IMAGE = 4;
	protected final static int SPACING__GROUP_FIELD = 4;
	protected final static int PADDING__GROUP_FIELD = 8;
	
	// Indicates why we're iterating over the row's content. Are we painting, get the x location for a given column, or or we returning the column at x?
	public enum Operation {
		PAINT,
		GET_COLUMN_X,
		GET_COLUMN_HEADER_AT_X,
		GET_COLUMN_VALUE_AT_X,
		GET_EXPAND_BOUNDS
	}
	
	//
	// The references below are recycled objects - to avoid GC churn.
	//	
	protected final Point contentLocation;
	protected final Rectangle groupCellBounds;
	protected final ContentRequest paintRequest;
	
	public GroupRowRenderer(final Grid<T> grid, final CellRenderer<T> cellRenderer) {
		super(grid);
		this.cellRenderer = cellRenderer;
		paintRequest = new ContentRequest(Operation.PAINT);
		contentLocation = new Point(0, 0);
		groupCellBounds = new Rectangle(0, 0, 0, 0);
	}

	public void paintRow(final RenderContext rc, final Rectangle rowBounds, final Row<T> row) {
		try {
			final GC gc = rc.getGC();
			final Rectangle oldClipping = gc.getClipping();
			
			gc.setClipping(rowBounds);			
			iterateGroupRowContent(paintRequest, rc, rowBounds, row);			
			gc.setClipping(oldClipping);
	
			//
			// Paint any header/footer borders.
			//
			if ((row.getElement() != null) && (rc.getRenderPass() == RenderPass.FOREGROUND)) {
				setCorners(rowBounds, topLeft, topRight, bottomRight, bottomLeft);
				
				if (styleRegistry.getGroupFooterBorderTop() != null) {
					//
					// Paint a border along the top of the group row.
					//
					paintBorderLine(gc, styleRegistry.getGroupFooterBorderTop(), topLeft, topRight);
				}
				
				if (styleRegistry.getGroupFooterBorderBottom() != null) {
					//
					// Paint a border along the bottom of the group row.
					//
					paintBorderLine(gc, styleRegistry.getGroupFooterBorderBottom(), bottomLeft, bottomRight);
				}
			}
			
		} catch (Throwable t) {
			if (!rc.isErrorLogged()) {
				//
				// Print the error to the std err and ensure we only do this once to avoid log fillage.
				//
				System.err.println(String.format("Failed to paint group row: %s", t.getMessage()));
				t.printStackTrace(System.err);
				rc.setErrorLogged(true);
			}
		}
	}
	
	/**
	 * Iterate over the group row's content. 
	 * 
	 * The Operation indicates if we're painting the row (null return type), or looking for a column (Column return 
	 * type) or a column's x location (Integer return type).
	 */
	protected void iterateGroupRowContent(final ContentRequest request, final RenderContext rc, final Rectangle rowBounds, final Row<T> row) {
		final GC gc = rc.getGC();
		final CellStyle groupValueStyle = styleRegistry.getGroupValueStyle();
		final CellStyle groupNameStyle = styleRegistry.getGroupNameStyle();
		
		if ((request.getOperation() == Operation.PAINT) && (rc.getRenderPass() == RenderPass.BACKGROUND)) {
			//
			// Paint the row background.
			//			
			gc.setBackground(getColour(rc.isAlternate() ? groupValueStyle.getBackgroundAlternate() : groupValueStyle.getBackground()));
			gc.fillRectangle(rowBounds);
		}
		
		//
		// Initialise our 'cell' bounds.
		//
		groupCellBounds.x = rowBounds.x;
		groupCellBounds.y = rowBounds.y;
		groupCellBounds.height = rowBounds.height;
		
		if (grid.isShowRowNumbers()) {
			//
			// Row number cell.
			//
			groupCellBounds.width = gridModel.getRowNumberColumn().getWidth();
								
			if (request.getOperation() == Operation.PAINT) {
				final CellStyle cellStyle = (grid.isFocusControl() && grid.isHighlightAnchorInHeaders() && doesRowHaveAnchor(row)) ? styleRegistry.getSelectionRowNumberStyle() : styleRegistry.getRowNumberStyle();
				cellRenderer.paintCell(rc, groupCellBounds, gridModel.getRowNumberColumn(), row, cellStyle);
			}
			
			groupCellBounds.x += (groupCellBounds.width + styleRegistry.getCellSpacingHorizontal());
		}
		
		//
		// Expand/collapse image.
		//
		final Image expandImage = grid.getContentProvider().isCollapsed(row.getElement()) ? getImage("plus.png") : getImage("minus.png");		
		groupCellBounds.width = PADDING__EXPAND_COLLAPSE_IMAGE + expandImage.getBounds().width + PADDING__EXPAND_COLLAPSE_IMAGE;
		align(expandImage.getBounds().width, expandImage.getBounds().height, groupCellBounds, contentLocation, AlignmentStyle.CENTER);
				
		if ((request.getOperation() == Operation.PAINT) && (rc.getRenderPass() == RenderPass.FOREGROUND)) {						
			gc.drawImage(expandImage, contentLocation.x, contentLocation.y);
			
		} else if (request.getOperation() == Operation.GET_EXPAND_BOUNDS) {
			request.setBounds(new Rectangle(contentLocation.x, contentLocation.y, expandImage.getBounds().width, expandImage.getBounds().height));
			return;
		}
		
		groupCellBounds.x += groupCellBounds.width;

		//
		// Group-by name/values.
		//		
		for (final Column column : gridModel.getGroupByColumns()) {
			if ((request.getOperation() == Operation.GET_COLUMN_X) && (request.getColumn() == column)) {
				request.setX(groupCellBounds.x);
			}
			
			final int startX = groupCellBounds.x;
			final String name = column.getCaption();
			final String providedValue = grid.getLabelProvider().getText(column, row.getElement());
			final String value = providedValue == null || providedValue.isEmpty() ? "(blank)" : providedValue;
			
			//
			// Reset the vertical bounds for each cell.
			//
			groupCellBounds.y = rowBounds.y;
			groupCellBounds.height = rowBounds.height;
			
			//
			// Sorting indicator.
			//
			final Image sortImage = getImage((column.getSortDirection() == SortDirection.ASC ? "sort_ascending.png" : "sort_descending.png"));
			groupCellBounds.y += 1; // Fudge for the image itself not being verticall centralised.
			groupCellBounds.width = sortImage.getBounds().width + SPACING__GROUP_FIELD;
			align(sortImage.getBounds().width, sortImage.getBounds().height, groupCellBounds, contentLocation, AlignmentStyle.CENTER);
			
			if ((column.getSortDirection() != SortDirection.NONE) && (request.getOperation() == Operation.PAINT) && (rc.getRenderPass() == RenderPass.FOREGROUND)) {				
				gc.drawImage(sortImage, contentLocation.x, contentLocation.y);
			}
			
			groupCellBounds.x += groupCellBounds.width;
			
			//
			// Name text.
			//
			groupCellBounds.y = rowBounds.y + groupNameStyle.getPaddingTop();
			groupCellBounds.height = rowBounds.height - groupNameStyle.getPaddingBottom();
			
			//
			// Highlight the field name if its field has the anchor.
			//
			final boolean hasAnchor = (grid.isFocusControl() && (column == grid.getAnchorColumn()) && (row.getElement() == grid.getAnchorElement()));
			if (hasAnchor && grid.isHighlightAnchorInHeaders()) {
				gc.setForeground(getColour(styleRegistry.getHoverGroupNameForeground()));
				gc.setBackground(getColour(styleRegistry.getHoverGroupNameBackground()));
			} else {
				gc.setForeground(getColour(groupNameStyle.getForeground()));
			}
						
			gc.setFont(getFont(groupNameStyle.getFontData()));
			final Point nameExtent = getTextExtent(name, rc, groupNameStyle.getFontData());
			
			if ((request.getOperation() == Operation.PAINT) && (rc.getRenderPass() == RenderPass.FOREGROUND)) {
				gc.drawText(name, groupCellBounds.x, groupCellBounds.y, (!hasAnchor || !grid.isHighlightAnchorInHeaders()));
				
			} else if ((request.getOperation() == Operation.GET_COLUMN_HEADER_AT_X) && (request.getX() >= startX) && (request.getX() < (startX + groupCellBounds.x + nameExtent.x))) {
				request.setColumn(column);
				return;
			}
			
			groupCellBounds.x += nameExtent.x + SPACING__GROUP_FIELD;
			
			//
			// Value text/image.
			//
			groupCellBounds.y = rowBounds.y + groupValueStyle.getPaddingTop();
			groupCellBounds.height = rowBounds.height - groupValueStyle.getPaddingBottom();
			
			final boolean highlightFilterMatch = doesCellHaveStyleableFilterMatch(row, column);
			if (highlightFilterMatch) {
				//
				// Use text highlighting if there's a FilterMatchRange in this column (and it's trackable).
				//
				gc.setBackground(getColour(styleRegistry.getFilterMatchBackground()));
				gc.setForeground(getColour(styleRegistry.getFilterMatchForeground()));

			} else {
				//
				// Use normal colours if we're not highlighting a filter result.
				//
				gc.setForeground(getColour(groupValueStyle.getForeground()));
			}
			
			//
			// Field value image.
			//
			if ((groupValueStyle.getContentStyle() == ContentStyle.IMAGE_THEN_TEXT) || (groupValueStyle.getContentStyle() == ContentStyle.IMAGE)) {
				final Image image = grid.getLabelProvider().getImage(column, row.getElement());
				if (image != null) {
					groupCellBounds.width = image.getBounds().width + PADDING__GROUP_FIELD;
					align(image.getBounds().width, image.getBounds().height, groupCellBounds, contentLocation, groupValueStyle.getImageAlignment());
					
					if ((request.getOperation() == Operation.PAINT) && (rc.getRenderPass() == RenderPass.FOREGROUND)) {
						gc.drawImage(image, contentLocation.x, contentLocation.y);
					
					} else if ((request.getOperation() == Operation.GET_COLUMN_VALUE_AT_X) && (request.getX() >= contentLocation.x) && (request.getX() < (contentLocation.x + image.getBounds().width))){
						request.setColumn(column);
						return;
					}
					
					groupCellBounds.x += groupCellBounds.width;
				}
			}

			//
			// Field value text.
			//
			switch (groupValueStyle.getContentStyle()) {
				case IMAGE_THEN_TEXT:
				case TEXT:
				case TEXT_THEN_IMAGE:
					gc.setFont(getFont(groupValueStyle.getFontData()));
					final Point valueExtent = getTextExtent(value, rc, groupValueStyle.getFontData());
					
					if ((request.getOperation() == Operation.PAINT) && (rc.getRenderPass() == RenderPass.FOREGROUND)) {
						gc.drawText(value, groupCellBounds.x, groupCellBounds.y, true);
						
					} else if ((request.getOperation() == Operation.GET_COLUMN_VALUE_AT_X) && (request.getX() >= groupCellBounds.x) && (request.getX() < (groupCellBounds.x + valueExtent.x))){
						request.setColumn(column);
						return;
					}
					
					groupCellBounds.x += valueExtent.x + SPACING__GROUP_FIELD;

				default:
					// No-op.
			}

			//
			// Field value image.
			//
			if (groupValueStyle.getContentStyle() == ContentStyle.TEXT_THEN_IMAGE) {
				final Image image = grid.getLabelProvider().getImage(column, row.getElement());
				if (image != null) {
					groupCellBounds.width = image.getBounds().width + PADDING__GROUP_FIELD;
					align(image.getBounds().width, image.getBounds().height, groupCellBounds, contentLocation, groupValueStyle.getImageAlignment());
					
					if ((request.getOperation() == Operation.PAINT) && (rc.getRenderPass() == RenderPass.FOREGROUND)) {
						gc.drawImage(image, contentLocation.x, contentLocation.y);
						
					} else if ((request.getOperation() == Operation.GET_COLUMN_VALUE_AT_X) && (request.getX() >= contentLocation.x) && (request.getX() < (contentLocation.x + image.getBounds().width))){
						request.setColumn(column);
						return;
					}
					
					groupCellBounds.x += groupCellBounds.width;
				}
			}
		}
		
		//
		// Assume they've clicked to the right of the last field value.
		//
		if ((request.getOperation() == Operation.GET_COLUMN_VALUE_AT_X) && !gridModel.getGroupByColumns().isEmpty() && (request.getX() >= groupCellBounds.x)) {
			request.setColumn(gridModel.getGroupByColumns().get(gridModel.getGroupByColumns().size()-1));
			return;
		}
	}
	
	/**
	 * Return the x co-ordinate of the specified group-by column. 
	 */
	public int getGroupColumnX(final RenderContext rc, final Column findColumn, final Row<T> row, final Rectangle rowBounds) {
		final ContentRequest request = new ContentRequest(Operation.GET_COLUMN_X);
		request.setColumn(findColumn);
		iterateGroupRowContent(request, rc, rowBounds, row);
		return request.getX();
	}

	/**
	 * Return the header or value (whichever is requested) at the x coordinate for the specified row.
	 */
	public Column getGroupColumnForX(final RenderContext rc, final int x, final boolean header, final Row<T> row, final Rectangle rowBounds) {
		final ContentRequest request = new ContentRequest(header ? Operation.GET_COLUMN_HEADER_AT_X : Operation.GET_COLUMN_VALUE_AT_X);
		request.setX(x);
		iterateGroupRowContent(request, rc, rowBounds, row);
		return request.getColumn();
	}
	
	/**
	 * Return the expand/collapse group row image bounds for the specified group row.
	 */
	public Rectangle getExpandImageBounds(final RenderContext rc, final Row<T> row, final Rectangle rowBounds) {
		final ContentRequest request = new ContentRequest(Operation.GET_EXPAND_BOUNDS);
		iterateGroupRowContent(request, rc, rowBounds, row);
		return request.getBounds();
	}
	
	/**
	 * A multi-purpose request object.
	 * 
	 * Allows the iterateGroupRowContent method to be used for different reasons.
	 */
	public class ContentRequest {
		// The type of thing we're doing with the content.
		private final Operation operation;
		
		// Used to locate a column's x coordinate, or return the column at an x coordinate.
		private Column column = null;
		
		// Used to return some bounds information to the caller.
		private Rectangle bounds = null;
		
		// Used to return the x coordinate of a column or check a colum at an x coorderin.
		private int x = -1;
		
		public ContentRequest(final Operation operation) {
			this.operation = operation;
		}
		
		public Operation getOperation() {
			return operation;
		}
		
		public Rectangle getBounds() {
			return bounds;
		}
		
		public void setBounds(final Rectangle bounds) {
			this.bounds = bounds;
		}
		
		public int getX() {
			return x;
		}
		
		public void setX(int x) {
			this.x = x;
		}
		
		public Column getColumn() {
			return column;
		}
		
		public void setColumn(Column column) {
			this.column = column;
		}
	}
}
