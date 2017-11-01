package com.notlob.jgrid.resources.swt;

import java.io.InputStream;

import org.eclipse.swt.widgets.Display;

import com.notlob.jgrid.resources.Color;
import com.notlob.jgrid.resources.Font;
import com.notlob.jgrid.resources.FontData;
import com.notlob.jgrid.resources.GC;
import com.notlob.jgrid.resources.Image;
import com.notlob.jgrid.resources.RGB;
import com.notlob.jgrid.resources.ResourceManager;
import com.notlob.jgrid.resources.TextLayout;

public class SWTResourceManager extends ResourceManager {

	private final Display display;
	
	public SWTResourceManager(final Display display, final GC gc) {
		super(gc);
		this.display = display;
	}

	@Override
	protected Font createFont(FontData fontData) {
		return null;
	}

	@Override
	protected Color createColour(RGB rgb) {
		return null;
	}

	@Override
	protected Image createImage(GC gc, InputStream input) {
		return null;
	}

	@Override
	protected TextLayout createTextLayout() {
		return null;
	}

	@Override
	public GC createGC(Image image) {
		return null;
	}

}
