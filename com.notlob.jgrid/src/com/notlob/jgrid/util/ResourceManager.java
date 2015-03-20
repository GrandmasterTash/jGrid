package com.notlob.jgrid.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

/**
 * Used to manage SWT UI resources that need disposing when the grid is disposed.
 *
 * @author Stef
 *
 */
public class ResourceManager {

	private Display display;
	private final Map<FontData, Font> fonts;
	private final Map<String, Color> colours;
	private final Map<String, Image> images;

	public ResourceManager(final Display display) {
		this.display = display;
		fonts = new HashMap<>();
		colours = new HashMap<>();
		images = new HashMap<>();
	}
	
	public Font getFont(final FontData fontData) {
		if (!fonts.containsKey(fontData)) {
			fonts.put(fontData, new Font(display, fontData));
		}

		return fonts.get(fontData);
	}

	public Color getColour(final RGB rgb) {
		if (!colours.containsKey(rgb.toString())) {
			colours.put(rgb.toString(), new Color(display, rgb));
		}

		return colours.get(rgb.toString());
	}

	public Image getImage(final String imagePath) {
		if (!images.containsKey(imagePath)) {
			final String fullPath = "/images/" + imagePath;

			//
			// Try both so we work as an RCP plugin OR a jar.
			//
			final InputStream input = getClass().getResourceAsStream(fullPath);

			if (input == null) {
				System.err.println("Unable to locate resource " + fullPath);
			} 

			try {
				images.put(imagePath, new Image(display, input));

			} catch (SWTException ex) {
			} finally {
				try {
					input.close();
				} catch (IOException ex) {}
			}
		}

		return images.get(imagePath);
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
}
