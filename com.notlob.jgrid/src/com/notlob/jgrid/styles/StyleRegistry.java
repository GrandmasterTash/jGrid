package com.notlob.jgrid.styles;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;

import com.notlob.jgrid.Grid;
import com.notlob.jgrid.model.Column;
import com.notlob.jgrid.model.Row;

// TODO: Make this lot protected / overridable.
public class StyleRegistry<T> {
	
	private final Grid<T> grid;

	protected RGB backgroundColour;
	protected int cellSpacingVertical;
	protected int cellSpacingHorizontal;

	protected final CellStyle defaultStyle;
	protected final CellStyle defaultHeaderStyle;
	protected final CellStyle rowNumberStyle;
	protected final CellStyle cornerStyle;
	protected final CellStyle groupNameStyle;
	protected final CellStyle groupValueStyle;
	protected final CellStyle noDataStyle;
	protected final CellStyle pinnedStyle;
	protected final BorderStyle groupFooterBorder;

	// Filter matches are highlight in these colours.
	protected RGB filterMatchForeground;
	protected RGB filterMatchBackground;

	// Selection styles.
	protected final CellStyle selectionStyle;
	protected final CellStyle selectionGroupStyle;
	protected final CellStyle selectionHeaderStyle;
	protected final CellStyle selectionRowNumberStyle;
	protected final CellStyle anchorStyle;
	protected RegionStyle selectionRegionStyle;

	// Hover styles.
	protected RegionStyle hoverRegionStyle;
	protected RGB hoverGroupNameForeground;
	protected RGB hoverGroupNameBackground;

	protected final static int PADDING_TOP = 3;
	protected final static int PADDING_BOTTOM = 3;

	protected final Map<String, CellStyle> customStyles;
	protected final Map<Column, CellStyle> columnStyles;
	protected final Map<Row<T>, CellStyle> rowStyles;

	public StyleRegistry(final Grid<T> grid) {
		this.grid = grid;
		rowStyles = new HashMap<>();
		columnStyles = new HashMap<>();
		customStyles = new HashMap<>();
		backgroundColour = new RGB(255, 255, 255);
		groupFooterBorder = new BorderStyle(2, LineStyle.SOLID, new RGB(100, 100, 100));

		//
		// Build a default cell style.
		//
		defaultStyle = new CellStyle();
		defaultStyle.setInheritanceStyle(InheritanceStyle.USE_BOTH);
		defaultStyle.setContentStyle(ContentStyle.IMAGE_THEN_TEXT);
		defaultStyle.setAllowContentOverlap(false);
		defaultStyle.setBackground(new RGB(255, 255, 255));
		defaultStyle.setBackgroundAlternate(new RGB(240, 240, 240));
		defaultStyle.setFontData(new FontData("Segoe UI", 9, SWT.NORMAL));
		defaultStyle.setForeground(new RGB(0, 0, 0));
		defaultStyle.setForegroundOpacity(255);
		defaultStyle.setBackgroundOpacity(255);
		defaultStyle.setPaddingImageText(4);
		defaultStyle.setPaddingInnerBorder(1);
		defaultStyle.setPaddingTop(PADDING_TOP);
		defaultStyle.setPaddingRight(3);
		defaultStyle.setPaddingBottom(PADDING_BOTTOM);
		defaultStyle.setPaddingLeft(3);

		//
		// Build a default header cell style.
		//
		defaultHeaderStyle = defaultStyle.copy();
		defaultHeaderStyle.setFontData(new FontData("Segoe UI", 9, SWT.NORMAL));
		defaultHeaderStyle.setContentStyle(ContentStyle.TEXT_THEN_IMAGE);
		defaultHeaderStyle.setImageAlignment(AlignmentStyle.RIGHT_CENTER);
		defaultHeaderStyle.setAllowContentOverlap(true);
		defaultHeaderStyle.setForeground(new RGB(39, 65, 62));
		defaultHeaderStyle.setBackgroundGradient1(new RGB(249, 252, 253));
		defaultHeaderStyle.setBackground(new RGB(230, 235, 243));
		defaultHeaderStyle.setBackgroundGradient2(new RGB(211, 219, 233));
		defaultHeaderStyle.setBackgroundAlternate(null);
		defaultHeaderStyle.setPaddingTop(4);
		defaultHeaderStyle.setPaddingBottom(4);
		defaultHeaderStyle.setPaddingLeft(4);
		defaultHeaderStyle.setBorderOuterTop(new BorderStyle(1, LineStyle.SOLID, new RGB(158, 182, 206)));
		defaultHeaderStyle.setBorderOuterBottom(new BorderStyle(1, LineStyle.SOLID, new RGB(158, 182, 206)));
		defaultHeaderStyle.setBorderOuterRight(new BorderStyle(1, LineStyle.SOLID, new RGB(158, 182, 206)));
		defaultHeaderStyle.setBorderOuterLeft(new BorderStyle(1, LineStyle.SOLID, new RGB(158, 182, 206)));

		//
		// Build a style for group (parent) rows.
		//
		groupValueStyle = defaultHeaderStyle.copy();
		groupValueStyle.setBackgroundAlternate(defaultStyle.getBackgroundAlternate());
		groupValueStyle.setBackground(defaultStyle.getBackground());
		groupValueStyle.setForeground(new RGB(0, 0, 0));
		groupValueStyle.setFontData(new FontData("Segoe UI", 8, SWT.BOLD));

		groupNameStyle = groupValueStyle.copy();
		groupNameStyle.setForeground(new RGB(39, 65, 62));
		groupNameStyle.setFontData(new FontData("Segoe UI", 8, SWT.NORMAL));

		//
		// Selection cell styles
		//
		selectionStyle = defaultStyle.copy();
		selectionGroupStyle = groupValueStyle.copy();
		selectionHeaderStyle = defaultHeaderStyle.copy();
		selectionHeaderStyle.setBackground(new RGB(255, 213, 141));
		selectionHeaderStyle.setBackgroundGradient1(new RGB(248, 215, 155));
		selectionHeaderStyle.setBackgroundGradient2(new RGB(241, 193, 95));
		selectionHeaderStyle.setBorderOuterTop(new BorderStyle(1, LineStyle.SOLID, new RGB(242, 149, 54)));
		selectionHeaderStyle.setBorderOuterRight(new BorderStyle(1, LineStyle.SOLID, new RGB(242, 149, 54)));
		selectionHeaderStyle.setBorderOuterBottom(new BorderStyle(1, LineStyle.SOLID, new RGB(242, 149, 54)));
		selectionHeaderStyle.setBorderOuterLeft(new BorderStyle(1, LineStyle.SOLID, new RGB(242, 149, 54)));

		//
		// Anchor style.
		//
		anchorStyle = new CellStyle();
		anchorStyle.setPaddingInnerBorder(2);
		anchorStyle.setBorderInnerTop(new BorderStyle(1, LineStyle.DASHED, new RGB(180, 180, 180)));
		anchorStyle.setBorderInnerRight(new BorderStyle(1, LineStyle.DASHED, new RGB(180, 180, 180)));
		anchorStyle.setBorderInnerBottom(new BorderStyle(1, LineStyle.DASHED, new RGB(180, 180, 180)));
		anchorStyle.setBorderInnerLeft(new BorderStyle(1, LineStyle.DASHED, new RGB(180, 180, 180)));

		//
		// Selection region style.
		//
		selectionRegionStyle = new RegionStyle();
		selectionRegionStyle.setBorder(new BorderStyle(2, LineStyle.SOLID, new RGB(0, 0, 0)));
		selectionRegionStyle.setBackground(new RGB(100, 200, 250));
		selectionRegionStyle.setBackgroundGradient1(new RGB(255, 213, 141));
		selectionRegionStyle.setBackgroundGradient2(new RGB(255, 213, 141));
		selectionRegionStyle.setForegroundOpacity(200);
		selectionRegionStyle.setBackgroundOpacity(100);

		//
		// Mouse hover region style.
		//
		hoverRegionStyle = new RegionStyle();
		hoverRegionStyle.setBorder(new BorderStyle(1, LineStyle.SOLID, new RGB(38, 160, 218)));
		hoverRegionStyle.setBackground(new RGB(189, 223, 241));
		hoverRegionStyle.setBackgroundGradient1(new RGB(189, 223, 241));
		hoverRegionStyle.setBackgroundGradient2(new RGB(189, 223, 241));
		hoverRegionStyle.setForegroundOpacity(200);
		hoverRegionStyle.setBackgroundOpacity(100);
		hoverGroupNameForeground = new RGB(0, 0, 0);
		hoverGroupNameBackground = new RGB(255, 213, 141);

		//
		// Builds the row number cell style.
		//
		rowNumberStyle = defaultHeaderStyle.copy();
		rowNumberStyle.setContentStyle(ContentStyle.TEXT);
		rowNumberStyle.setTextAlignment(AlignmentStyle.CENTER);
		rowNumberStyle.setPaddingTop(PADDING_TOP);
		rowNumberStyle.setPaddingBottom(PADDING_BOTTOM);
		rowNumberStyle.setBackgroundGradient1(null);
		rowNumberStyle.setBackgroundGradient2(null);

		selectionRowNumberStyle = selectionHeaderStyle.copy();
		selectionRowNumberStyle.setTextAlignment(AlignmentStyle.CENTER);
		selectionRowNumberStyle.setPaddingTop(PADDING_TOP);
		selectionRowNumberStyle.setPaddingBottom(PADDING_BOTTOM);

		//
		// Builds the corner cell style.
		//
		cornerStyle = rowNumberStyle.copy();
		cornerStyle.setContentStyle(ContentStyle.IMAGE);
		cornerStyle.setBackground(new RGB(169, 196, 233));
		cornerStyle.setBackgroundGradient1(null);
		cornerStyle.setBackgroundGradient2(null);
		cornerStyle.setBorderInnerTop(new BorderStyle(1, LineStyle.SOLID, new RGB(213, 228, 242)));
		cornerStyle.setBorderInnerRight(new BorderStyle(1, LineStyle.SOLID, new RGB(176, 208, 247)));
		cornerStyle.setBorderInnerBottom(new BorderStyle(1, LineStyle.SOLID, new RGB(176, 208, 247)));
		cornerStyle.setBorderInnerLeft(new BorderStyle(1, LineStyle.SOLID, new RGB(213, 228, 242)));

		//
		// Text style for the no-data message.
		//
		noDataStyle = defaultStyle.copy();
		noDataStyle.setTextAlignment(AlignmentStyle.CENTER);

		pinnedStyle = defaultStyle.copy();
		pinnedStyle.setForeground(new RGB(39, 65, 62));
		pinnedStyle.setBackground(new RGB(230, 235, 243));

		filterMatchForeground = new RGB(0, 97, 83);
		filterMatchBackground = new RGB(198, 239, 206);
	}

	public Map<String, CellStyle> getCustomStyles() {
		return customStyles;
	}

	public CellStyle getDefaultHeaderStyle() {
		return defaultHeaderStyle;
	}

	public CellStyle getDefaultStyle() {
		return defaultStyle;
	}

	public CellStyle getRowNumberStyle() {
		return rowNumberStyle;
	}

	public CellStyle getCornerStyle() {
		return cornerStyle;
	}

	public void setFilterMatchBackground(final RGB filterMatchBackground) {
		this.filterMatchBackground = filterMatchBackground;
	}

	public void setFilterMatchForeground(final RGB filterMatchForeground) {
		this.filterMatchForeground = filterMatchForeground;
	}

	public RGB getFilterMatchBackground() {
		return filterMatchBackground;
	}

	public RGB getFilterMatchForeground() {
		return filterMatchForeground;
	}

	public CellStyle getSelectionStyle() {
		return selectionStyle;
	}

	public RegionStyle getSelectionRegionStyle() {
		return selectionRegionStyle;
	}

	public CellStyle getSelectionHeaderStyle() {
		return selectionHeaderStyle;
	}

	public CellStyle getSelectionRowNumberStyle() {
		return selectionRowNumberStyle;
	}

	public RegionStyle getHoverRegionStyle() {
		return hoverRegionStyle;
	}

	public RGB getHoverGroupNameBackground() {
		return hoverGroupNameBackground;
	}

	public RGB getHoverGroupNameForeground() {
		return hoverGroupNameForeground;
	}

	public CellStyle getNoDataStyle() {
		return noDataStyle;
	}

	public CellStyle getGroupNameStyle() {
		return groupNameStyle;
	}

	public CellStyle getGroupValueStyle() {
		return groupValueStyle;
	}

	public CellStyle getPinnedStyle() {
		return pinnedStyle;
	}

	public CellStyle getAnchorStyle() {
		return anchorStyle;
	}

	public CellStyle getCellStyle(final Column column, final Row<T> row) {

		if (row.isPinned()) {
			return pinnedStyle;
		}

		if (column != null) {
			//
			// See if there's a custom style first.
			//
			final CellStyle customStyle = (row == Row.COLUMN_HEADER_ROW) ? grid.getLabelProvider().getHeaderStyle(column) : grid.getLabelProvider().getCellStyle(column, row.getElement());
			if (customStyle != null) {
				return customStyle;
			}

			//
			// Check for a selected column header
			//
			if (grid.isFocusControl() && grid.isHighlightAnchorInHeaders() && (row == Row.COLUMN_HEADER_ROW)  && (column == grid.getGridModel().getSelectionModel().getAnchorColumn())) {
				return selectionHeaderStyle;
			}
		}

		final boolean parentRow = grid.getGridModel().isParentElement(row.getElement());

		//
		// Check for a selected row.
		//
		if (row.isSelected()) {
			if (parentRow) {
				return selectionGroupStyle;
			}

			return selectionStyle;
		}

		if (parentRow) {
			return groupValueStyle;
		}

		if (rowStyles.containsKey(row)) {
			return rowStyles.get(row);
		}

		if (columnStyles.containsKey(column)) {
			return columnStyles.get(column);
		}

		return defaultStyle;
	}

	public CellStyle getCellStyle(final Row<T> row) {
		if (rowStyles.containsKey(row)) {
			return rowStyles.get(row);
		}

		return defaultStyle;
	}

	public void setCellStyle(final Column column, final CellStyle cellStyle) {
		columnStyles.put(column, cellStyle);
	}

	public void setCellStyle(final Row<T> row, final CellStyle cellStyle) {
		rowStyles.put(row, cellStyle);
	}

	public int getCellSpacingHorizontal() {
		return cellSpacingHorizontal;
	}

	public void setCellSpacingHorizontal(final int cellSpacingHorizontal) {
		this.cellSpacingHorizontal = cellSpacingHorizontal;
	}

	public int getCellSpacingVertical() {
		return cellSpacingVertical;
	}

	public void setCellSpacingVertical(final int cellSpacingVertical) {
		this.cellSpacingVertical = cellSpacingVertical;
	}

	public RGB getBackgroundColour() {
		return backgroundColour;
	}

	public BorderStyle getGroupFooterBorder() {
		return groupFooterBorder;
	}

}
