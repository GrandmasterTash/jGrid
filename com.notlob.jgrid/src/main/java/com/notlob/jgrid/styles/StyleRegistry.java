package com.notlob.jgrid.styles;

import com.notlob.jgrid.Grid;
import com.notlob.jgrid.model.Column;
import com.notlob.jgrid.model.Row;
import com.notlob.jgrid.resources.FontData;
import com.notlob.jgrid.resources.RGB;

public class StyleRegistry<T> {
	
	private final Grid<T> grid;
	
	private final StyleCollector styleCollector;

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
	protected RegionStyle notActiveSelectionRegionStyle;

	// Hover styles.
	protected RegionStyle hoverRegionStyle;
	protected RGB hoverGroupNameForeground;
	protected RGB hoverGroupNameBackground;
	
	// Grid perimeter borders.
	protected BorderStyle mainBorderLeft;
	protected BorderStyle mainBorderRight;
	protected BorderStyle mainBorderTop;
	protected BorderStyle mainBorderBottom;

	public StyleRegistry(final Grid<T> grid) {
		this.grid = grid;
		this.styleCollector = new StyleCollector();
		backgroundColour = new RGB(255, 255, 255);
		
		//
		// Build a default cell style.
		//
		defaultStyle = new CellStyle();
		defaultStyle.setContentStyle(ContentStyle.IMAGE_THEN_TEXT);
		defaultStyle.setAllowContentOverlap(false);
		defaultStyle.setBackground(new RGB(255, 255, 255));
		defaultStyle.setBackgroundAlternate(new RGB(240, 240, 240));
		defaultStyle.setFontData(getDefaultFont());
		defaultStyle.setForeground(new RGB(0, 0, 0));
		defaultStyle.setForegroundOpacity(255);
		defaultStyle.setBackgroundOpacity(255);
		defaultStyle.setPaddingImageText(4);
		defaultStyle.setPaddingInnerBorder(1);
		defaultStyle.setPaddingTop(2);
		defaultStyle.setPaddingRight(2);
		defaultStyle.setPaddingBottom(2);
		defaultStyle.setPaddingLeft(2);

		//
		// Build a default header cell style.
		//
		headerStyle = defaultStyle.copy();
		headerStyle.setFontData(getDefaultFont());
		headerStyle.setContentStyle(ContentStyle.TEXT_THEN_IMAGE);
		headerStyle.setImageAlignment(AlignmentStyle.RIGHT_CENTER);
		headerStyle.setAllowContentOverlap(true);
		headerStyle.setForeground(new RGB(39, 65, 62));
		headerStyle.setBackgroundGradient1(new RGB(249, 252, 253));
		headerStyle.setBackground(new RGB(230, 235, 243));
		headerStyle.setBackgroundGradient2(new RGB(211, 219, 233));
		headerStyle.setBackgroundAlternate(null);
		headerStyle.setPaddingTop(2);
		headerStyle.setPaddingBottom(2);
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
		groupValueStyle.setFontData(new FontData("Segoe UI", 8, FontData.BOLD));

		groupNameStyle = groupValueStyle.copy();
		groupNameStyle.setForeground(new RGB(39, 65, 62));
		groupNameStyle.setFontData(new FontData("Segoe UI", 8, FontData.NORMAL));
		
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
		anchorStyle = defaultStyle.copy();
		anchorStyle.setContentStyle(null);
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
		rowNumberStyle.setPaddingTop(2);
		rowNumberStyle.setPaddingLeft(2);
		rowNumberStyle.setPaddingBottom(2);
		rowNumberStyle.setPaddingRight(2);
		rowNumberStyle.setBackgroundGradient1(null);
		rowNumberStyle.setBackgroundGradient2(null);

		selectionRowNumberStyle = selectionHeaderStyle.copy();
		selectionRowNumberStyle.setTextAlignment(AlignmentStyle.CENTER);
		selectionRowNumberStyle.setPaddingTop(2);
		selectionRowNumberStyle.setPaddingLeft(2);
		selectionRowNumberStyle.setPaddingBottom(2);
		selectionRowNumberStyle.setPaddingRight(2);

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
	
	public StyleCollector getStyleCollector() {
		return styleCollector;
	}
	
	protected FontData getDefaultFont() {
		final String OS = System.getProperty("os.name");
		
		if (OS.toLowerCase().contains("win")) {
			return new FontData("Segoe UI", 9, FontData.NORMAL);
			
		} else if (OS.toLowerCase().contains("mac")) {
			return new FontData("Arial", 9, FontData.NORMAL);
		}
		
		return new FontData("Monospaced", 8, FontData.NORMAL);
	}
	
	public CellStyle getCellStyle(final Column column, final Row<T> row) {

		styleCollector.clear();
		
		if (column != null) {
			//
			// See if there's a custom style first.
			//
			if (row == grid.getColumnHeaderRow()) {
				grid.getLabelProvider().getHeaderStyle(styleCollector, column);
			} else {
				grid.getLabelProvider().getCellStyle(styleCollector, column, row.getElement());
			}
			
			if (!styleCollector.isEmpty()) {
				//
				// If the row is selected, combine the style with the selection style.
				//
				if (row.isSelected()) {
					styleCollector.addFirst(selectionStyle);
					return styleCollector.getCellStyle();
				}
			}

			//
			// Check for a selected column header
			//
			if (grid.isFocusControl() && grid.isHighlightAnchorInHeaders() && (row == grid.getColumnHeaderRow())  && (column == grid.getGridModel().getSelectionModel().getAnchorColumn()) && !(grid.getGridRenderer().isPaintingPinned())) {
				styleCollector.add(selectionHeaderStyle);
			}
			
			if (!styleCollector.isEmpty()) {
				return styleCollector.getCellStyle();
			}
		}

		final boolean parentRow = grid.getGridModel().isParentElement(row.getElement());

		//
		// Check for a selected row.
		//
		if (row.isSelected()) {
			if (parentRow) {
				styleCollector.add(groupValueStyle);
				styleCollector.add(selectionGroupStyle);
			} else {
				styleCollector.add(defaultStyle);
				styleCollector.add(selectionStyle);
			}
			
			return styleCollector.getCellStyle();
		}

		if (parentRow) {
			return groupValueStyle;
		}

		if (row == grid.getColumnHeaderRow()) {
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
	
	public RegionStyle getNotActiveSelectionRegionStyle() {
		return notActiveSelectionRegionStyle;
	}
	
	public void setNotActiveSelectionRegionStyle(RegionStyle notActiveSelectionRegionStyle) {
		this.notActiveSelectionRegionStyle = notActiveSelectionRegionStyle;
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
	
	public BorderStyle getMainBorderBottom() {
		return mainBorderBottom;
	}
	
	public void setMainBorderBottom(BorderStyle mainBorderBottom) {
		this.mainBorderBottom = mainBorderBottom;
	}
	
	public BorderStyle getMainBorderLeft() {
		return mainBorderLeft;
	}
	
	public void setMainBorderLeft(BorderStyle mainBorderLeft) {
		this.mainBorderLeft = mainBorderLeft;
	}
	
	public BorderStyle getMainBorderRight() {
		return mainBorderRight;
	}
	
	public void setMainBorderRight(BorderStyle mainBorderRight) {
		this.mainBorderRight = mainBorderRight;
	}
	
	public BorderStyle getMainBorderTop() {
		return mainBorderTop;
	}
	
	public void setMainBorderTop(BorderStyle mainBorderTop) {
		this.mainBorderTop = mainBorderTop;
	}
}
