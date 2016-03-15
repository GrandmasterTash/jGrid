package com.notlob.jgrid.examples;

import org.eclipse.swt.graphics.Image;

import com.notlob.jgrid.Grid;
import com.notlob.jgrid.model.Column;
import com.notlob.jgrid.model.Row;
import com.notlob.jgrid.providers.IGridLabelProvider;
import com.notlob.jgrid.providers.ImageCollector;
import com.notlob.jgrid.styles.CellStyle;
import com.notlob.jgrid.styles.ContentStyle;
import com.notlob.jgrid.styles.StyleCollector;
import com.notlob.jgrid.styles.StyleRegistry;

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
		
		final StyleRegistry<Person> styleRegistry = grid.getStyleRegistry();
		
		//
		// You can override the styling in the grid. In this case we want the firstname column
		// to include an icon as well as text - but you can override pretty much anything in 
		// the cell (colours, fonts, borders, etc.). 
		//
		firstNameStyle = styleRegistry.getDefaultStyle().copy();
		firstNameStyle.setContentStyle(ContentStyle.IMAGE_THEN_TEXT);
		

	}
	
	@Override
	public void getCellStyle(final StyleCollector styleCollector, final Column column, final Person element) {
		if (column.getColumnId().equals(GridContentProvider.COLUMN_ID__FIRST_NAME)) {
			styleCollector.add(firstNameStyle);
		}
	}

	@Override
	public int getDefaultRowHeight(Person person) {
		return -1; // Allows us to override the height on a row-by-row basis. 
		           // -1 (default) means the height is calculated from the font and padding settings - 
	}

	@Override
	public void getHeaderImage(final ImageCollector collector, final Column column) {
	}

	@Override
	public void getHeaderStyle(final StyleCollector styleCollector, final Column column) {
	}

	@Override
	public String getHeaderToolTip(Column column) {
		return null;
	}

	@Override
	public void getImage(final ImageCollector collector, final Column column, final Person element) {
		if (column.getColumnId().equals(GridContentProvider.COLUMN_ID__FIRST_NAME)) {
			collector.addImage(personImage);
		}
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

	@Override
	public void getAnchorStyle(StyleCollector styleCollector, Column column, Person element) {
	}

	@Override
	public boolean shouldAlternateBackground(Row<Person> previousRow, Row<Person> currentRow) {
		return false;
	}

}
