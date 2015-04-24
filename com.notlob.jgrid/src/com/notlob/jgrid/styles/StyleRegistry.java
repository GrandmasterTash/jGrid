package com.notlob.jgrid.styles;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;

import com.notlob.jgrid.Grid;
import com.notlob.jgrid.model.Column;
import com.notlob.jgrid.model.Row;

public class StyleRegistry<T> {
	
	private final Grid<T> grid;

	protected RGB backgroundColour;
	protected int cellSpacingVertical;
	protected int cellSpacingHorizontal;

	protected CellStyle defaultStyle;
	protected CellStyle headerStyle;
	protected CellStyle rowNumberStyle;
	protected CellStyle cornerStyle;
	protected CellStyle groupNameStyle;
	protected CellStyle groupValueStyle;
	protected CellStyle noDataStyle;
	protected BorderStyle groupFooterBorderTop;
	protected BorderStyle groupFooterBorderBottom;
	protected BorderStyle dragDropBorder;

	// Filter matches are highlight in these colours.
	protected RGB filterMatchForeground;
	protected RGB filterMatchBackground;

	// Selection styles.
	protected CellStyle selectionStyle;
	protected CellStyle selectionGroupStyle;
	protected CellStyle selectionHeaderStyle;
	protected CellStyle selectionRowNumberStyle;
	protected CellStyle anchorStyle;
	protected RegionStyle selectionRegionStyle;

	// Hover styles.
	protected RegionStyle hoverRegionStyle;
	protected RGB hoverGroupNameForeground;
	protected RGB hoverGroupNameBackground;

	protected final static int PADDING_TOP = 3;
	protected final static int PADDING_BOTTOM = 3;

	public StyleRegistry(final Grid<T> grid) {
		this.grid = grid;
		backgroundColour = new RGB(255, 255, 255);
		
		//
		// Build a default cell style.
		//
		defaultStyle = new CellStyle();
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
		headerStyle = defaultStyle.copy();
		headerStyle.setFontData(new FontData("Segoe UI", 9, SWT.NORMAL));
		headerStyle.setContentStyle(ContentStyle.TEXT_THEN_IMAGE);
		headerStyle.setImageAlignment(AlignmentStyle.RIGHT_CENTER);
		headerStyle.setAllowContentOverlap(true);
		headerStyle.setForeground(new RGB(39, 65, 62));
		headerStyle.setBackgroundGradient1(new RGB(249, 252, 253));
		headerStyle.setBackground(new RGB(230, 235, 243));
		headerStyle.setBackgroundGradient2(new RGB(211, 219, 233));
		headerStyle.setBackgroundAlternate(null);
		headerStyle.setPaddingTop(4);
		headerStyle.setPaddingBottom(4);
		headerStyle.setPaddingLeft(4);
		headerStyle.setBorderOuterTop(new BorderStyle(1, LineStyle.SOLID, new RGB(158, 182, 206)));
		headerStyle.setBorderOuterBottom(new BorderStyle(1, LineStyle.SOLID, new RGB(158, 182, 206)));
		headerStyle.setBorderOuterRight(new BorderStyle(1, LineStyle.SOLID, new RGB(158, 182, 206)));
		headerStyle.setBorderOuterLeft(new BorderStyle(1, LineStyle.SOLID, new RGB(158, 182, 206)));

		//
		// Build a style for group (parent) rows.
		//
		groupValueStyle = headerStyle.copy();
		groupValueStyle.setBackgroundAlternate(defaultStyle.getBackgroundAlternate());
		groupValueStyle.setBackground(defaultStyle.getBackground());
		groupValueStyle.setForeground(new RGB(0, 0, 0));
		groupValueStyle.setFontData(new FontData("Segoe UI", 8, SWT.BOLD));

		groupNameStyle = groupValueStyle.copy();
		groupNameStyle.setForeground(new RGB(39, 65, 62));
		groupNameStyle.setFontData(new FontData("Segoe UI", 8, SWT.NORMAL));
		
		groupFooterBorderTop = null;//new BorderStyle(1, LineStyle.SOLID, new RGB(158, 182, 206));
		groupFooterBorderBottom = new BorderStyle(1, LineStyle.SOLID, new RGB(158, 182, 206));
		dragDropBorder = new BorderStyle(3, LineStyle.SOLID, new RGB(38, 160, 218));

		//
		// Selection cell styles
		//
		selectionStyle = new CellStyle();
		selectionGroupStyle = new CellStyle();
		selectionHeaderStyle = headerStyle.copy();
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
		anchorStyle.setForegroundOpacity(255);
		anchorStyle.setBackgroundOpacity(255);
		anchorStyle.setPaddingInnerBorder(2);
		anchorStyle.setBorderInnerTop(new BorderStyle(1, LineStyle.DASHED, new RGB(180, 180, 180)));
		anchorStyle.setBorderInnerRight(new BorderStyle(1, LineStyle.DASHED, new RGB(180, 180, 180)));
		anchorStyle.setBorderInnerBottom(new BorderStyle(1, LineStyle.DASHED, new RGB(180, 180, 180)));
		anchorStyle.setBorderInnerLeft(new BorderStyle(1, LineStyle.DASHED, new RGB(180, 180, 180)));

		//
		// Selection region style.
		//
		selectionRegionStyle = new RegionStyle();
		selectionRegionStyle.setBorder(new BorderStyle(1, LineStyle.SOLID, new RGB(0, 0, 0)));
		selectionRegionStyle.setBackground(new RGB(100, 200, 250));
		selectionRegionStyle.setBackgroundGradient1(new RGB(255, 213, 141));
		selectionRegionStyle.setBackgroundGradient2(new RGB(255, 213, 141));
		selectionRegionStyle.setForegroundOpacity(200);
		selectionRegionStyle.setBackgroundOpacity(120);

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
		rowNumberStyle = headerStyle.copy();
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

		filterMatchForeground = new RGB(0, 97, 83);
		filterMatchBackground = new RGB(198, 239, 206);
	}
	
	public CellStyle getCellStyle(final Column column, final Row<T> row) {

		if (column != null) {
			//
			// See if there's a custom style first.
			//
			final CellStyle customStyle = (row == Row.COLUMN_HEADER_ROW) ? grid.getLabelProvider().getHeaderStyle(column) : grid.getLabelProvider().getCellStyle(column, row.getElement());
			if (customStyle != null) {
				//
				// If the row is selected, combine the style with the selection style.
				//
				if (row.isSelected()) {
					final CompositeCellStyle compositeCellStyle = new CompositeCellStyle();
					compositeCellStyle.add(selectionStyle);
					compositeCellStyle.add(customStyle);
					return compositeCellStyle;
				}
				
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
			final CompositeCellStyle compositeCellStyle = new CompositeCellStyle();			
			
			if (parentRow) {
				compositeCellStyle.add(groupValueStyle);
				compositeCellStyle.add(selectionGroupStyle);
			} else {
				compositeCellStyle.add(defaultStyle);
				compositeCellStyle.add(selectionStyle);
			}
			
			return compositeCellStyle;
		}

		if (parentRow) {
			return groupValueStyle;
		}

		if (row == Row.COLUMN_HEADER_ROW) {
			return headerStyle;
		}

		return defaultStyle;
	}

	public CellStyle getHeaderStyle() {
		return headerStyle;
	}
	
	public void setHeaderStyle(CellStyle headerStyle) {
		this.headerStyle = headerStyle;
	}

	public CellStyle getDefaultStyle() {
		return defaultStyle;
	}
	
	public void setDefaultStyle(CellStyle defaultStyle) {
		this.defaultStyle = defaultStyle;
	}

	public CellStyle getRowNumberStyle() {
		return rowNumberStyle;
	}
	
	public void setRowNumberStyle(CellStyle rowNumberStyle) {
		this.rowNumberStyle = rowNumberStyle;
	}

	public CellStyle getCornerStyle() {
		return cornerStyle;
	}
	
	public void setCornerStyle(CellStyle cornerStyle) {
		this.cornerStyle = cornerStyle;
	}

	public RGB getFilterMatchBackground() {
		return filterMatchBackground;
	}
	
	public void setFilterMatchBackground(final RGB filterMatchBackground) {
		this.filterMatchBackground = filterMatchBackground;
	}

	public RGB getFilterMatchForeground() {
		return filterMatchForeground;
	}
	
	public void setFilterMatchForeground(final RGB filterMatchForeground) {
		this.filterMatchForeground = filterMatchForeground;
	}

	public CellStyle getSelectionStyle() {
		return selectionStyle;
	}
	
	public void setSelectionStyle(CellStyle selectionStyle) {
		this.selectionStyle = selectionStyle;
	}

	public RegionStyle getSelectionRegionStyle() {
		return selectionRegionStyle;
	}
	
	public void setSelectionRegionStyle(RegionStyle selectionRegionStyle) {
		this.selectionRegionStyle = selectionRegionStyle;
	}

	public CellStyle getSelectionHeaderStyle() {
		return selectionHeaderStyle;
	}
	
	public void setSelectionHeaderStyle(CellStyle selectionHeaderStyle) {
		this.selectionHeaderStyle = selectionHeaderStyle;
	}

	public CellStyle getSelectionRowNumberStyle() {
		return selectionRowNumberStyle;
	}
	
	public void setSelectionRowNumberStyle(CellStyle selectionRowNumberStyle) {
		this.selectionRowNumberStyle = selectionRowNumberStyle;
	}
	
	public CellStyle getSelectionGroupStyle() {
		return selectionGroupStyle;
	}
	
	public void setSelectionGroupStyle(CellStyle selectionGroupStyle) {
		this.selectionGroupStyle = selectionGroupStyle;
	}

	public RegionStyle getHoverRegionStyle() {
		return hoverRegionStyle;
	}
	
	public void setHoverRegionStyle(RegionStyle hoverRegionStyle) {
		this.hoverRegionStyle = hoverRegionStyle;
	}

	public RGB getHoverGroupNameBackground() {
		return hoverGroupNameBackground;
	}
	
	public void setHoverGroupNameBackground(RGB hoverGroupNameBackground) {
		this.hoverGroupNameBackground = hoverGroupNameBackground;
	}

	public RGB getHoverGroupNameForeground() {
		return hoverGroupNameForeground;
	}
	
	public void setHoverGroupNameForeground(RGB hoverGroupNameForeground) {
		this.hoverGroupNameForeground = hoverGroupNameForeground;
	}

	public CellStyle getNoDataStyle() {
		return noDataStyle;
	}
	
	public void setNoDataStyle(CellStyle noDataStyle) {
		this.noDataStyle = noDataStyle;
	}

	public CellStyle getGroupNameStyle() {
		return groupNameStyle;
	}
	
	public void setGroupNameStyle(CellStyle groupNameStyle) {
		this.groupNameStyle = groupNameStyle;
	}

	public CellStyle getGroupValueStyle() {
		return groupValueStyle;
	}
	
	public void setGroupValueStyle(CellStyle groupValueStyle) {
		this.groupValueStyle = groupValueStyle;
	}

	public CellStyle getAnchorStyle() {
		return anchorStyle;
	}
	
	public void setAnchorStyle(CellStyle anchorStyle) {
		this.anchorStyle = anchorStyle;
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
	
	public void setBackgroundColour(RGB backgroundColour) {
		this.backgroundColour = backgroundColour;
	}

	public BorderStyle getGroupFooterBorderTop() {
		return groupFooterBorderTop;
	}
	
	public void setGroupFooterBorderTop(BorderStyle groupFooterBorderTop) {
		this.groupFooterBorderTop = groupFooterBorderTop;
	}
	
	public BorderStyle getGroupFooterBorderBottom() {
		return groupFooterBorderBottom;
	}
	
	public void setGroupFooterBorderBottom(BorderStyle groupFooterBorderBottom) {
		this.groupFooterBorderBottom = groupFooterBorderBottom;
	}
	
	public BorderStyle getDragDropBorder() {
		return dragDropBorder;
	}
	
	public void setDragDropBorder(BorderStyle dragDropBorder) {
		this.dragDropBorder = dragDropBorder;
	}
	
}
