package com.notlob.jgrid.examples;

import java.util.Collection;

import org.eclipse.swt.graphics.Point;

import com.notlob.jgrid.listeners.IGridListener;
import com.notlob.jgrid.model.Column;

public class GridListener implements IGridListener<Person> {

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
	public void elementsAdded(Collection<Person> elements) {
		System.out.println("elementsAdded");
	}

	@Override
	public void elementsUpdated(Collection<Person> elements) {
		System.out.println("elementsUpdated");		
	}

	@Override
	public void elementsRemoved(Collection<Person> elements) {
		System.out.println("elementsRemoved");		
	}

	@Override
	public void rowCountChanged() {
		System.out.println("rowCountChanged");		
	}

	@Override
	public void filtersChanged() {
		System.out.println("filtersChanged");	
	}

	@Override
	public void columnResized(Column column) {
		System.out.println("columnResized");	
	}

	@Override
	public void columnMoved(Column column) {
		System.out.println("columnMoved");	
	}

	@Override
	public void columnSorted(Column column) {
		System.out.println("columnSorted");	
	}

	@Override
	public void rowNumbersVisibilityChanged(boolean visible) {
		System.out.println("rowNumbersVisibilityChanged");
	}

	@Override
	public void columnAboutToSort(Column column) {
		System.out.println("columnAboutToSort");
		
	}

	@Override
	public void groupSelectorVisibilityChanged(boolean visible) {
		System.out.println("groupSelectorVisibilityChanged");
	}

	@Override
	public void cellRevealed(Column column, Person element) {
		System.out.println("cellRevealed");
	}

	@Override
	public void filtersChanging() {
		System.out.println("filtersChanging");
	}
}
