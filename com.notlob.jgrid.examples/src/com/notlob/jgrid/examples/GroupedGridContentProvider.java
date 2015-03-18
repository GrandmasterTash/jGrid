package com.notlob.jgrid.examples;

/**
 * Exposes parent and children so the grid forms groups of people.
 * 
 * @author Stef
 *
 */
public class GroupedGridContentProvider extends GridContentProvider {
	
	@Override
	public Person[] getChildren(Person person) {
		if (person.getChildren() != null) {		
			return person.getChildren().toArray(new Person[] {});
		}
		
		return super.getChildren(person);
	}
	
	@Override
	public Person getParent(Person person) {
		return person.getParent();
	}

}
