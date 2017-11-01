package com.notlob.jgrid.resources;

public class FontData {
	
	public final static int NORMAL = 0;
	public final static int BOLD = 1;
	
	// TODO: All others with getter setters.
	// TODO: All others with toString.
	// TODO: Two-way converters for all.
	
	private int height;
	private int style;
	private String name;
	private String locale;
	
	public FontData() {
		this(null, 0, 0);
	}
	
	public FontData(final String name, final int height, final int style) {
		this.name = name;
		this.height = height;
		this.style = style;
	}

	public int getHeight() {
		return height;
	}
	
	public void setHeight(int height) {
		this.height = height;
	}
	
	public String getLocale() {
		return locale;
	}
	
	public void setLocale(String locale) {
		this.locale = locale;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public int getStyle() {
		return style;
	}
	public void setStyle(int style) {
		this.style = style;
	}
}
