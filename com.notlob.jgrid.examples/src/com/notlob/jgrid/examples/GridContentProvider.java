package com.notlob.jgrid.examples;

import com.notlob.jgrid.model.Column;
import com.notlob.jgrid.providers.IGridContentProvider;

/**
 * The content provider exposes the structure and raw values of the domain items in the list.
 * 
 * This particular version doesn't support grouped data (getChildren and getParent return null).
 * 
 * @author Stef
 *
 */
public class GridContentProvider implements IGridContentProvider<Person> {
	
	public final static String COLUMN_ID__FIRST_NAME = "first.name";
	public final static String COLUMN_ID__SURNAME = "surname";
	public final static String COLUMN_ID__AGE = "age";
	
	@Override
	public Person[] getChildren(Person person) {
		return null;
	}

	@Override
	public String getElementId(Person person) {
		return person.getUniqueId();
	}

	@Override
	public Person getParent(Person person) {
		return null;
	}

	@Override
	public Object getValue(Column column, Person person) {
		switch (column.getColumnId()) {
			case COLUMN_ID__FIRST_NAME:
				return person.getFirstname();
				
			case COLUMN_ID__SURNAME:
				return person.getLastname();
				
			case COLUMN_ID__AGE:
				return person.getAge();			
		}
		
		return null;
	}

	@Override
	public boolean isCollapsed(Person person) {
		return false;
	}
}
