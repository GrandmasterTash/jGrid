package com.notlob.jgrid.examples;

import java.util.ArrayList;
import java.util.List;

/**
 * Person is an example domain item - one we can all relate to. 
 *  
 * @author Stef
 *
 */
public class Person {
	
	private String uniqueId;
	private String firstname;
	private String lastname;
	private int age;
	
	private Person parent;
	private List<Person> children;
	
	public Person(final String uniqueId, final String firstname, final String lastname, final int age) {
		this(uniqueId, firstname, lastname, age, null);
	}
	
	public Person(final String uniqueId, final String firstname, final String lastname, final int age, final Person parent) {
		this.uniqueId = uniqueId;
		this.firstname = firstname;
		this.lastname = lastname;
		this.age = age;
		this.parent = parent;
		
		if (parent != null) {
			parent.addChild(this);
		}
	}
	
	public String getUniqueId() {
		return uniqueId;
	}
	
	public int getAge() {
		return age;
	}
	
	public String getFirstname() {
		return firstname;
	}
	
	public String getLastname() {
		return lastname;
	}	

	void addChild(final Person child) {
		if (children == null) {
			children = new ArrayList<>();
		}
		
		children.add(child);
	}
	
	public Person getParent() {
		return parent;
	}
	
	public List<Person> getChildren() {
		return children;
	}
}
