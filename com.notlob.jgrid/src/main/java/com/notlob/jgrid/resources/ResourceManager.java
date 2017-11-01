package com.notlob.jgrid.resources;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Used to manage UI resources that need disposing when the grid is disposed.
 *
 * @author Stef
 *
 */
public abstract class ResourceManager {

	private GC gc; // The main GC used by the grid for various calculations and the creation of other resources.
	private final Map<FontData, Font> fonts;
	private final Map<String, Image> images;
	private final Map<String, Color> colours;
	private final List<TextLayout> textLayouts;
	
	private final static Logger logger = LoggerFactory.getLogger(ResourceManager.class);

	public ResourceManager(final GC gc) {
		this.gc = gc;
		fonts = new HashMap<>();
		colours = new HashMap<>();
		images = new HashMap<>();
		textLayouts = new ArrayList<>();
	}
	
	public Font getFont(final FontData fontData) {
		if (!fonts.containsKey(fontData)) {
			fonts.put(fontData, createFont(fontData));
		}

		return fonts.get(fontData);
	}
	
	protected abstract Font createFont(final FontData fontData);

	public Color getColour(final RGB rgb) {
		if (!colours.containsKey(rgb.toString())) {
			colours.put(rgb.toString(), createColour(rgb));
		}

		return colours.get(rgb.toString());
	}
	
	protected abstract Color createColour(final RGB rgb);

	public Image getImage(final String imagePath) {
		if (!images.containsKey(imagePath)) {
			final String fullPath = "/images/" + imagePath;

			//
			// Try both so we work as an RCP plugin OR a jar.
			//
			final InputStream input = getClass().getResourceAsStream(fullPath);

			if (input == null) {
				logger.error("Unable to locate resource " + fullPath);
			} 

			try {
				images.put(imagePath, createImage(gc, input));

			} catch (Exception ex) {
				ex.printStackTrace();
				
			} finally {
				try {
					input.close();
				} catch (IOException ex) {}
			}
		}

		return images.get(imagePath);
	}
	
	protected abstract Image createImage(final GC gc, final InputStream input);

	public TextLayout getTextLayout() {
		final TextLayout textLayout = createTextLayout();
		textLayouts.add(textLayout);
		return textLayout;
	}
	
	protected abstract TextLayout createTextLayout();

	/**
	 * The main GC used for measurements.
	 */
	public GC getGC() {
		return gc;
	}
	
	/**
	 * Create a GC from the image specified - the lifecycle is NOT managed by the resourcemanager.
	 */
	public abstract GC createGC(final Image image);
	
	public void dispose() {
		gc = null;

		for (final FontData fontData : fonts.keySet()) {
			fonts.get(fontData).dispose();
		}

		for (final String rgb : colours.keySet()) {
			colours.get(rgb).dispose();
		}

		for (final String imagePath : images.keySet()) {
			images.get(imagePath).dispose();
		}
		
		for (final TextLayout textLayout : textLayouts) {
			textLayout.dispose();
		}

		fonts.clear();
		colours.clear();
		images.clear();
		textLayouts.clear();
	}
}
