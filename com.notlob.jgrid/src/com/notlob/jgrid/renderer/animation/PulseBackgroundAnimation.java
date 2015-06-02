package com.notlob.jgrid.renderer.animation;

import java.util.List;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;

import com.notlob.jgrid.Grid;
import com.notlob.jgrid.model.Row;
import com.notlob.jgrid.renderer.RenderContext;

public class PulseBackgroundAnimation<T> extends RowAnimation<T> {

	// The background will be set to this colour, then fade to the grid's background colour.
	private final List<RGB> rgbs;
	
	public PulseBackgroundAnimation(List<RGB> rgbs) {
		this.rgbs = rgbs;
	}
	
	@Override
	int getDuration() {
		return rgbs.size();
	}
	
	@Override
	public int getIncrement() {
		return 2;
	}
	
	@Override
	public void pulseBackground(final RenderContext rc, final Row<T> row) {
		if (row.getFrame() < rgbs.size()) {
			final RGB rgb = rgbs.get(row.getFrame());		
			final Grid<?> grid = rc.getGrid();		
			final Color colour = grid.getResourceManager().getColour(rgb);
			rc.getGC().setBackground(colour);
		}		
	}

}
