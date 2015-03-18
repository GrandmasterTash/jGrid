package com.notlob.jgrid.examples;

import org.eclipse.swt.graphics.Image;

import com.notlob.jgrid.Grid;
import com.notlob.jgrid.model.Column;
import com.notlob.jgrid.providers.IGridLabelProvider;
import com.notlob.jgrid.styles.CellStyle;
import com.notlob.jgrid.styles.ContentStyle;

/**
 * The label provider exposes formatted text, styling, images etc. for each cell in the grid.
 * 
 * @author Stef
 *
 */
public class GridLabelProvider implements IGridLabelProvider<Person> {

	private final CellStyle firstNameStyle;
	private final Image personImage;
	
	public GridLabelProvider(final Grid<Person> grid, final Image personImage) {
		this.personImage = personImage;
		
		//
		// You can override the styling in the grid. In this case we want the firstname column
		// to include an icon as well as text - but you can override pretty much anything in 
		// the cell (colours, fonts, borders, etc.). 
		//
		firstNameStyle = grid.getStyleRegistry().getDefaultStyle().copy();
		firstNameStyle.setContentStyle(ContentStyle.IMAGE_THEN_TEXT);
	}
	
	@Override
	public CellStyle getCellStyle(Column column, Person person) {
		if (column.getColumnId().equals(GridContentProvider.COLUMN_ID__FIRST_NAME)) {
			return firstNameStyle;
		}
		
		return null;
	}

	@Override
	public int getDefaultRowHeight(Person person) {
		return -1; // Allows us to override the height on a row-by-row basis. 
		           // -1 (default) means the height is calculated from the font and padding settings - 
	}

	@Override
	public Image getHeaderImage(Column column) {
		return null;
	}

	@Override
	public CellStyle getHeaderStyle(Column column) {
		return null;
	}

	@Override
	public String getHeaderToolTip(Column column) {
		return null;
	}

	@Override
	public Image getImage(Column column, Person person) {
		if (column.getColumnId().equals(GridContentProvider.COLUMN_ID__FIRST_NAME)) {
			return personImage;
		}
		
		return null;
	}

	/**
	 * Here you can format the data depending upon it's underlying data-type.
	 */
	@Override
	public String getText(Column column, Person person) {
		switch (column.getColumnId()) {
			case GridContentProvider.COLUMN_ID__FIRST_NAME:
				return person.getFirstname();
				
			case GridContentProvider.COLUMN_ID__SURNAME:
				return person.getLastname();
				
			case GridContentProvider.COLUMN_ID__AGE:
				return String.valueOf(person.getAge());			
		}
		
		return null;
	}

	@Override
	public String getToolTip(Column column, Person person) {
		return "This is a tool-tip for " + person.getFirstname();
	}

}
