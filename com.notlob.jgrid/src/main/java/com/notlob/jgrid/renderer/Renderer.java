package com.notlob.jgrid.renderer;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;

import com.notlob.jgrid.Grid;
import com.notlob.jgrid.model.Column;
import com.notlob.jgrid.model.GridModel;
import com.notlob.jgrid.model.Row;
import com.notlob.jgrid.model.Viewport;
import com.notlob.jgrid.model.filtering.IHighlightingFilter;
import com.notlob.jgrid.providers.ImageCollector;
import com.notlob.jgrid.styles.AlignmentStyle;
import com.notlob.jgrid.styles.BorderStyle;
import com.notlob.jgrid.styles.StyleRegistry;

/**
 * An abstract renderer with access to some utility methods.
 * 
 * @author Stef
 */
public abstract class Renderer<T> {
	
	// Core widget stuff all renderers will need.
	protected final Grid<T> grid;
	protected final GridModel<T> gridModel;
	protected final Viewport<T> viewport;
	protected final StyleRegistry<T> styleRegistry;
	protected final ImageCollector imageCollector;
	
	// Animation frequency and duration (in 'frames').
	public final static int ANIMATION_INTERVAL = 10;
		
	protected enum RenderPass {
		COMPUTE_SIZE,
		BACKGROUND,
		FOREGROUND
	}
	
	// Used for grip dots or bevelled borders.
	protected final static RGB RGB__SHADOW_DARK = new RGB(80, 80, 80);
	protected final static RGB RGB__SHADOW_HIGHLIGHT = new RGB(245, 245, 245);
	
	//
	// The references below are recycled objects - to avoid GC churn.
	//
	
	// Used to locate the corners of rectangles when drawing border lines.
	protected final Point topLeft;
	protected final Point topRight;
	protected final Point bottomLeft;
	protected final Point bottomRight;
	
	public Renderer(final Grid<T> grid) {
		this.grid = grid;
		imageCollector = new ImageCollector();
		gridModel = grid.getGridModel();
		styleRegistry = gridModel.getStyleRegistry();
		viewport = grid.getViewport();
		topLeft = new Point(0, 0);
		topRight = new Point(0, 0);
		bottomLeft = new Point(0, 0);
		bottomRight = new Point(0, 0);
	}
	
	/**
	 * Utility method to retrieve a local resource image.
	 * 
	 * The grid will dispose of the resource when disposed.
	 */
	protected Image getImage(final String imagePath) {
		return grid.getResourceManager().getImage(imagePath);
	}
	
	/**
	 * Utility method to retrieve a colour resource from an RGB.
	 * 
	 * The grid will dispose of the resource when disposed.
	 */
	protected Color getColour(final RGB rgb) {
		return grid.getResourceManager().getColour(rgb);
	}
	
	/**
	 * Utility method to retrieve a font resource from an RGB.
	 * 
	 * The grid will dispose of the resource when disposed.
	 */
	protected Font getFont(final FontData fontData) {
		return grid.getResourceManager().getFont(fontData);
	}
	
	/**
	 * Get a collector to acquire images from the label providers. 
	 */
	protected ImageCollector getImageCollector(final boolean clear) {
		if (clear) {
			imageCollector.clear();
		}
		return imageCollector;
	}
	
	/**
	 * Utility method to see if a column has the anchor.
	 */
	protected boolean doesColumnHaveAnchor(final Column column) {
		return ((column != null) && (column == gridModel.getSelectionModel().getAnchorColumn()));
	}

	/**
	 * Utility method to see if a row has the anchor.
	 */
	protected boolean doesRowHaveAnchor(final Row<T> row) {
		return ((row != null) && (row != gridModel.getColumnHeaderRow()) && (row.getElement() == grid.getAnchorElement()));
	}
	
	/**
	 * Does this cell have a filter match we need to highlight?
	 */
	protected boolean doesCellHaveStyleableFilterMatch(final Row<T> row, final Column column) {
		if (row != null && row.hasFilterMatches()) {
			for (final IHighlightingFilter filterMatch : row.getFilterMatches()) {
				if (filterMatch.isColumnHighlighted(column)) {
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * Utility to populate the four points from the rectangle. The idea being, any code used to render lines along the edges should be more readable.
	 */
	protected void setCorners(final Rectangle rectangle, final Point topLeft, final Point topRight, final Point bottomRight, final Point bottomLeft) {
		topLeft.x = rectangle.x;
		topLeft.y = rectangle.y;
		topRight.x = rectangle.x + rectangle.width;
		topRight.y = rectangle.y;
		bottomLeft.x = rectangle.x;
		bottomLeft.y = rectangle.y + rectangle.height;
		bottomRight.x = rectangle.x + rectangle.width;
		bottomRight.y = rectangle.y + rectangle.height;
	}
		
	/**
	 * Utility to shrink the specified rectangle by the amount specified, the second parameter is modified rectangle.
	 * @param delta
	 */
	protected void shrinkRectangle(final Rectangle originalBounds, final Rectangle newBounds, final int delta) {
		newBounds.x = originalBounds.x + delta;
		newBounds.y = originalBounds.y + delta;
		newBounds.width = originalBounds.width - (delta * 2);
		newBounds.height = originalBounds.height - (delta * 2);
	}
	
	/**
	 * Get the SWT alignment direction from a jgrid one.
	 */
	protected int convertAlignmentToSwt(final AlignmentStyle alignment) {
		switch (alignment) {
			case BOTTOM_LEFT:
			case LEFT_CENTER:
			case TOP_LEFT:
				return SWT.LEFT;
			
			case BOTTOM_CENTER:
			case CENTER:
			case TOP_CENTER:
				return SWT.CENTER;
				
			case BOTTOM_RIGHT:
			case RIGHT_CENTER:
			case TOP_RIGHT:
				return SWT.RIGHT;
		}
		
		return SWT.LEFT;
	}
	
	/**
	 * Render a line in the specified border style.
	 */
	protected void paintBorderLine(final GC gc, final BorderStyle borderStyle, final Point point1, final Point point2) {

		if (borderStyle == null) {
			return;
		}

		switch (borderStyle.getLineStyle()) {
		case NONE:
			return;

		case BEVELED:
			// NOTE: Grips are currently only support on the right border side.
			gc.setForeground(getColour(RGB__SHADOW_HIGHLIGHT));
			gc.drawLine(point1.x, point1.y, point1.x, point2.y);

			gc.setForeground(getColour(RGB__SHADOW_DARK));
			gc.drawLine(point1.x - 1, point1.y, point1.x - 1, point2.y);
			break;

		case GRIP:
			// NOTE: Grips are currently only support on the right border side.
			final int spacer = new Double(((point2.y - point1.y) / 4.0) * 0.75).intValue();

			// Draw three dark dots in-between column headers.
			gc.setForeground(getColour(RGB__SHADOW_DARK));
			gc.drawPoint((point1.x), spacer + spacer);
			gc.drawPoint((point1.x), (spacer * 2) + spacer);
			gc.drawPoint((point1.x), (spacer * 3) + spacer);

			// Draw three light dots to make the dark ones look 3d.
			gc.setForeground(getColour(RGB__SHADOW_HIGHLIGHT));
			gc.drawPoint((point1.x), spacer + spacer + 1);
			gc.drawPoint((point1.x), (spacer * 2) + spacer + 1);
			gc.drawPoint((point1.x), (spacer * 3) + spacer + 1);
			break;

		case SOLID:
			gc.setForeground(getColour(borderStyle.getColour()));
			gc.setLineWidth(borderStyle.getWidth());
			gc.setLineStyle(SWT.LINE_SOLID);
			gc.drawLine(point1.x, point1.y, point2.x, point2.y);
			break;

		case DOTS:
			gc.setForeground(getColour(borderStyle.getColour()));
			gc.setLineWidth(borderStyle.getWidth());
			gc.setLineStyle(SWT.LINE_DOT);
			gc.drawLine(point1.x, point1.y, point2.x, point2.y);
			break;

		case DASHED:
			gc.setForeground(getColour(borderStyle.getColour()));
			gc.setLineWidth(borderStyle.getWidth());
			gc.setLineStyle(SWT.LINE_DASH);
			gc.drawLine(point1.x, point1.y, point2.x, point2.y);
			break;

		case DASH_DOT:
			gc.setForeground(getColour(borderStyle.getColour()));
			gc.setLineWidth(borderStyle.getWidth());
			gc.setLineStyle(SWT.LINE_DASHDOT);
			gc.drawLine(point1.x, point1.y, point2.x, point2.y);
			break;

		case DASH_DOT_DOT:
			gc.setForeground(getColour(borderStyle.getColour()));
			gc.setLineWidth(borderStyle.getWidth());
			gc.setLineStyle(SWT.LINE_DASHDOTDOT);
			gc.drawLine(point1.x, point1.y, point2.x, point2.y);
			break;
		}
	}
	
	/**
	 * Aligns the dimensions within the bounds specified and updates the second rectangle to content pointer with the top-left
	 * corner where the rectangle should be drawn.
	 */
	protected void align(final int width, final int height, final Rectangle bounds, final Point location, final AlignmentStyle alignment) {

		switch (alignment) {
		case BOTTOM_CENTER:
			location.x = bounds.x + ((bounds.width - width) / 2);
			location.y = bounds.y + (bounds.height - height);
			break;

		case BOTTOM_LEFT:
			location.x = bounds.x;
			location.y = bounds.y + (bounds.height - height);
			break;

		case BOTTOM_RIGHT:
			location.x = bounds.x + (bounds.width - width);
			location.y = bounds.y + (bounds.height - height);
			break;

		case CENTER:
			location.x = bounds.x + ((bounds.width - width) / 2);
			location.y = bounds.y + ((bounds.height - height) / 2);
			break;

		case LEFT_CENTER:
			location.x = bounds.x;
			location.y = bounds.y + ((bounds.height - height) / 2);
			break;

		case RIGHT_CENTER:
			location.x = bounds.x + (bounds.width - width);
			location.y = bounds.y + ((bounds.height - height) / 2);
			break;

		case TOP_CENTER:
			location.x = bounds.x + ((bounds.width - width) / 2);
			location.y = bounds.y;
			break;

		case TOP_LEFT:
			location.x = bounds.x;
			location.y = bounds.y;
			break;

		case TOP_RIGHT:
			location.x = bounds.x + (bounds.width - width);
			location.y = bounds.y;
			break;

		default:
			System.out.println("No alignment set!");
		}
	}
	
	/**
	 * Get the text extent of the string specified using the font specified.
	 */
	public Point getTextExtent(final String text, final RenderContext rc, final FontData fontData) {
		Map<String, Point> extentsByString = rc.getExtentCache().get(fontData);
		
		if (extentsByString == null) {
			extentsByString = new HashMap<String, Point>();
			rc.getExtentCache().put(fontData, extentsByString);			
		}
		
		Point extent = extentsByString.get(text);
		
		if (extent == null) {
			rc.getGC().setFont(getFont(fontData));
			extent = rc.getGC().textExtent(text);
			extentsByString.put(text, extent);
		}
		
		return extent;
	}
}
