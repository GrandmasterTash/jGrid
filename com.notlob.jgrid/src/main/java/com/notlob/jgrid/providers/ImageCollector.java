package com.notlob.jgrid.providers;

import java.util.ArrayList;
import java.util.List;

import com.notlob.jgrid.resources.Image;

/**
 * Passed to label providers to collect images for a given cell.
 * 
 * A grid renderer _may_ only have one collector and re-use it for each cell (clearing images between cells).
 *  
 * If a cell has multiple images, they will be rendered left-to-right.
 *  
 * @author sbolton
 *
 */
public class ImageCollector {
	
	private final List<Image> images;
	
	public ImageCollector() {
		this.images = new ArrayList<>();
	}
	
	public List<Image> getImages() {
		return images;
	}
	
	public void clear() {
		images.clear();
	}
	
	public void addImage(final Image image) {
		this.images.add(image);
	}
	
	public boolean isEmpty() {
		return this.images.isEmpty();
	}

}
