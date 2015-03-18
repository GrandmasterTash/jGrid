package com.notlob.jgrid.examples;

import java.util.Collection;

import org.eclipse.swt.graphics.Point;

import com.notlob.jgrid.listeners.IGridListener;
import com.notlob.jgrid.model.Column;

public class GridListener implements IGridListener<Person> {
	@Override
	public void gridChanged() {
		// The grid content or structure has changed in some way.
	}

	@Override
	public void selectionChanged(Collection<Person> selectedpersons) {
		System.out.println("selectionChanged");
	}

	@Override
	public void click(Column column, Person person, Point location, int modifier) {
		System.out.println("Click");		
	}

	@Override
	public void doubleClick(Column column, Person person, Point location, int modifier) {
		System.out.println("Double-Click");		
	}

	@Override
	public void rightClick(Column column, Person person, Point location, int modifier) {
		System.out.println("Right-Click");		
	}

	@Override
	public void headerClick(Column column, Point location, int modifier) {
		System.out.println("Header-Click");		
	}

	@Override
	public void headerDoubleClick(Column column, Point location, int modifier) {
		System.out.println("Header-Double-Click");		
	}

	@Override
	public void headerRightClick(Column column, Point location, int modifier) {
		System.out.println("Header-Right-Click");	
	}

	@Override
	public void groupExpanded(Person person) {
		System.out.println("Group-Expanded");	
	}

	@Override
	public void groupCollapsed(Person person) {
		System.out.println("Group-Collapsed");	
	}
}
