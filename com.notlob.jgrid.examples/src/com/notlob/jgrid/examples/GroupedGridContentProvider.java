package com.notlob.jgrid.examples;

import java.util.List;

/**
 * Exposes parent and children so the grid forms groups of people.
 * 
 * @author Stef
 *
 */
public class GroupedGridContentProvider extends GridContentProvider {
	
	@Override
	public List<Person> getChildren(Person person) {
		if (person.getChildren() != null) {		
			return person.getChildren();
		}
		
		return super.getChildren(person);
	}
	
	@Override
	public Person getParent(Person person) {
		return person.getParent();
	}

}
