package com.notlob.jgrid.util;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

/**
 * Singleton to manage SWT UI resources that need disposing when the app terminates.
 * 
 * @author Stef
 *
 */
public class ResourceManager {
	
	private Display display;
	private final Map<FontData, Font> fonts;
	private final Map<String, Color> colours;
	private final Map<String, Image> images;
	
	public Font getFont(final FontData fontData) {
		if (!fonts.containsKey(fontData)) {
			fonts.put(fontData, new Font(display, fontData));
		}
		
		return fonts.get(fontData);
	}
	static int index= 0;
	public Color getColour(final RGB rgb) {
		if (!colours.containsKey(rgb.toString())) {
			colours.put(rgb.toString(), new Color(display, rgb));
		}
		
		return colours.get(rgb.toString());
	}
	
	public Image getImage(final String imagePath) {
		if (!images.containsKey(imagePath)) {
			final File fullPath = new File("/images", imagePath);
			final InputStream input = getClass().getClassLoader().getResourceAsStream(fullPath.getPath());
			images.put(imagePath, new Image(display, input));
		}
		
		return images.get(imagePath);
	}
	
	public void setDisplay(final Display display) {
		this.display = display;
	}
	
	public void dispose() {
		display = null;
		
		for (final FontData fontData : fonts.keySet()) {
			fonts.get(fontData).dispose();
		}
		
		for (final String rgb : colours.keySet()) {
			colours.get(rgb).dispose();
		}
		
		for (final String imagePath : images.keySet()) {
			images.get(imagePath).dispose();
		}
		
		fonts.clear();
		colours.clear();
		images.clear();
	}
	
	private ResourceManager() {
		fonts = new HashMap<>();
		colours = new HashMap<>();
		images = new HashMap<>();
	}
 
	private static class SingletonHolder { 
		private static final ResourceManager INSTANCE = new ResourceManager();
	}
 
	public static ResourceManager getInstance() {
		return SingletonHolder.INSTANCE;
	}
}
