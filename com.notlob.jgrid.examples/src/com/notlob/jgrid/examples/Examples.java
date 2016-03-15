package com.notlob.jgrid.examples;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.notlob.jgrid.Grid;
import com.notlob.jgrid.Grid.SelectionStyle;
import com.notlob.jgrid.model.Column;
import com.notlob.jgrid.model.SortDirection;
import com.notlob.jgrid.model.filtering.Filter;
import com.notlob.jgrid.styles.AlignmentStyle;

/**
 * To run these examples you will have to export the com.notlob.jgrid project as a jar and add to the classpath.
 * 
 * You'll also have to add SWT to the classpath.
 * 
 * @author Stef
 *
 */
public class Examples {

	public static void main(String[] args) {
		try {
			//
			// Create the domain model.
			//
			final List<Person> persons = createElements();
			
			//
			// Shell layout.
			//
			final GridLayout shellLayout = new GridLayout(1, true);
			final Display display = new Display();				
			final Shell shell = new Shell(display);
			
			shell.setSize(700, 500);
			shell.setLayout(shellLayout);
			shell.setText("JGrid Examples");
						
			//
			// One of the grid cells will display an icon.			
			//
			final InputStream input = Examples.class.getResourceAsStream("person.png");
			final Image personImage = new Image(display, input);
			input.close();
			
			//
			// Create a JGrid to display Person domain items.
			//
			final Grid<Person> grid = new Grid<Person>(shell);
			grid.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
					
			//
			// The mandatory things required for the JGrid to function are the content provider, label provider, one or more columns and one or more elements (rows of data).
			//
			grid.setContentProvider(new GridContentProvider(persons));
			grid.setLabelProvider(new GridLabelProvider(grid, personImage));
			grid.addColumns(createColumns());
			grid.addElements(createElements());						
			grid.addListener(new GridListener()); // You can add basic listeners to the grid to respond to user input. If the IGridListener doesn't provider
                                                  // what you need, just add standard SWT listeners to the widget.
			
			//
			// Filters can be added/removed to show/hide elements. 
			//
			// We'll add a special IHighlighting filter to each column which will highlight cells that match a search term enter in the text box.
			//
			grid.addFilters(createFilters(grid));
			
			//
			// Various optional, behavioural settings.
			//
			grid.setEmptyMessage("There's no data");
			grid.setSelectionStyle(SelectionStyle.ROW_BASED);
			grid.setHighlightAnchorCellBorder(true);        // Draw a focus border in the current cell.
			grid.setHighlightAnchorInHeaders(true);         // Change the row/column header for the current cell.
			grid.setHighlightHoveredRow(true);              // Highlight the row the mouse is over.
			grid.setSelectGroupIfAllChildrenSelected(true); // Select the parent row of a group if all it's children are selected.
			grid.setShowRowNumbers(true);                   // Toggles the visibility of row numbers.
			grid.setHideNoneHighlightedRows(false);         // See IHighlightingFilter - toggles whether a IHighlightingFilter 
			                                                // hides non-matching elements (like a normal filter) or doesn't 
			                                                // hide elements, but instead highlights matching elements.
			
			//
			// There's plenty of customisation that can be done to the grid's appearance (see GridLabelProvider.getCellStyle).
			// But if thats not enough, you can always override the renderer for the grid.
			//
			//grid.setGridRenderer(new GridRenderer<Person>(grid) {
			//	// override paint<blah> methods.
			//});
						
			final Composite buttonComposite = new Composite(shell, SWT.NONE);
			buttonComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			
			final FillLayout buttonLayout = new FillLayout();
			buttonLayout.type = SWT.HORIZONTAL;
			buttonComposite.setLayout(buttonLayout);
			
			final Button notGroupedButton = new Button(buttonComposite, SWT.PUSH);
			notGroupedButton.setText("Un-Grouped");
			notGroupedButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent arg0) {
					//
					// Reset the data and done group it.
					//
					grid.clearElements();
					grid.ungroupBy(grid.getGroupByColumns());
					grid.setContentProvider(new GridContentProvider(persons)); // Normal - not grouped.
					grid.addElements(persons);
				}
			});
			
			final Button groupedButton = new Button(buttonComposite, SWT.PUSH);
			groupedButton.setText("Grouped");
			groupedButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent arg0) {
					//
					// Reset the data and group it.
					//
					grid.clearElements();					
					grid.setContentProvider(new GroupedGridContentProvider(persons));
					grid.groupBy(Collections.singletonList(createColumns().get(1))); // Surname column. Use a new instance
					                                                                 // as grouping makes a column invisible.
					grid.addElements(persons);	
				}
			});
			
			final Composite filterComposite = new Composite(shell, SWT.NONE);
			filterComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			filterComposite.setLayout(new GridLayout(2, false));
			
			final Label filterLabel = new Label(filterComposite, SWT.NONE);
			filterLabel.setText("Search:");
			filterLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
			
			final Text filterText = new Text(filterComposite, SWT.BORDER);
			filterText.setMessage("<enter search text>");
			filterText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			filterText.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent arg0) {
					applyFilters(grid, filterText.getText());
				}
			});
						
			//
			// Start the app.
			//
			shell.open();

			while (!shell.isDisposed()) {
				try {
					if (!display.readAndDispatch()) {
						display.sleep();
					}
				} catch (final Exception e) {
					e.printStackTrace();
				}
			}

			personImage.dispose();
			display.dispose();

		} catch (final Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Create the initial columns (left-to-right).
	 */
	private static List<Column> createColumns() {
		final List<Column> columns = new ArrayList<>();
		
		//
		// Firstname.
		//
		final Column firstNameColumn = new Column(GridContentProvider.COLUMN_ID__FIRST_NAME);
		firstNameColumn.setCaption("First Name");
		firstNameColumn.setTextAlignment(AlignmentStyle.LEFT_CENTER);
		firstNameColumn.setWidth(150);
		columns.add(firstNameColumn);		
		
		//
		// Surname.
		//
		final Column surnameColumn = new Column(GridContentProvider.COLUMN_ID__SURNAME);
		surnameColumn.setCaption("Surname");
		surnameColumn.setTextAlignment(AlignmentStyle.LEFT_CENTER);
		surnameColumn.setWidth(150);
		surnameColumn.setSortDirection(SortDirection.ASC);		
		columns.add(surnameColumn);
		
		//
		// Age.
		//
		final Column ageColumn = new Column(GridContentProvider.COLUMN_ID__AGE);
		ageColumn.setCaption("Age");
		ageColumn.setTextAlignment(AlignmentStyle.RIGHT_CENTER);
		ageColumn.setWidth(50);
		
		//
		// You can use custom comparators for sorting if non-alphabetical ordering is required.
		//
		ageColumn.setComparator(new IntegerComparator());		
		columns.add(ageColumn);
		
		//
		// Note, custom properties can be added to columns via setData.
		//
		//ageColumn.setData(key, data);
		
		return columns;
	}
	
	/**
	 * Create a directory of Person domain items.
	 */
	private static List<Person> createElements() {
		final List<Person> elements = new ArrayList<>();
		
		//
		// Add some individuals.
		//
		elements.add(new Person("P01", "John", "Smith", 25));
		elements.add(new Person("P02", "Doris", "Day", 22));
		elements.add(new Person("P03", "Judy", "Finnegan", 35));
		elements.add(new Person("P04", "Mumm-Ra", "The Ever-Living", 120));
		elements.add(new Person("P05", "Joanne", "Oliphant", 23));
		elements.add(new Person("P06", "Tamiko", "Hoffer", 34));
		elements.add(new Person("P07", "Etha", "Plaisance", 36));
		elements.add(new Person("P08", "Amy", "Karnes", 13));
		elements.add(new Person("P09", "Catherin", "Rawlins", 45));
		elements.add(new Person("P10", "Brady", "Priest", 65));
		elements.add(new Person("P11", "Katie", "Swint", 23));
		elements.add(new Person("P12", "Caren", "Brickman", 22));
		elements.add(new Person("P13", "Jacelyn", "Brumit", 40));
		elements.add(new Person("P14", "Vaughn", "Rossiter", 34));
		elements.add(new Person("P15", "Glennis", "Wallace", 23));
		
		//
		// Group a few people together into families.
		//
		final Person parent1 = new Person("P16", "Deshawn", "Hilderbrand", 37);
		elements.add(parent1);
		elements.add(new Person("P17", "Clemmie", "Hilderbrand", 42, parent1));
		elements.add(new Person("P18", "Dayna", "Hilderbrand", 19, parent1));
		
		final Person parent2 = new Person("P19", "Sibyl", "Morrissey", 24); 
		elements.add(parent2);
		elements.add(new Person("P20", "Leisha", "Morrissey", 28, parent2));
		elements.add(new Person("P21", "Moira", "Morrissey", 75, parent2));
		elements.add(new Person("P22", "Bob", "Morrissey", 123, parent2));
		
		elements.add(new Person("P23", "Angelica", "Waits", 18));
		elements.add(new Person("P24", "Horacio", "Knights", 17));
		
		return elements;
	}
	
	/**
	 * Build a PersonFilter for every column in the grid.
	 */
	private static List<Filter<Person>> createFilters(final Grid<Person> grid) {
		final List<Filter<Person>> filters = new ArrayList<>();
		
		for (Column column : grid.getColumns()) {
			filters.add(new PersonFilter(column, grid.getLabelProvider()));
		}
		
		return filters;
	}
	
	/**
	 * Set each PersonFilter to the text specified and apply to the grid to highlight matches.
	 */
	private static void applyFilters(final Grid<Person> grid, final String filterText) {
		for (Filter<Person> filter : grid.getFilters()) {
			if (filter instanceof PersonFilter) {
				((PersonFilter) filter).setFilterText(filterText);
			}
		}
		
		grid.applyFilters();		
	}
	
	/**
	 * A non alphabetic comparator.
	 */
	public static class IntegerComparator implements Comparator<Integer> {
		@Override
		public int compare(Integer o1, Integer o2) {
			if (o1 == null && o2 == null) {
				return 0;
			}
			
			if (o1 == null && o2 != null) {
				return -1;
			}
			
			if (o1 == null && o2 == null) {
				return 1;
			}
			
			return o1.compareTo(o2);
		}
	}
}
