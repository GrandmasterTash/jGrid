package com.notlob.jgrid.renderer.animation;

import com.notlob.jgrid.model.Row;
import com.notlob.jgrid.renderer.CellRenderer;
import com.notlob.jgrid.renderer.RenderContext;

public abstract class RowAnimation<T> {
	
	// How many frames does the animation run for?
//	private final int duration;
//	
//	public RowAnimation(final int duration) {
//		this.duration = duration;
//	}
//	
//	public int getDuration() {
//		return duration;
//	}
	
	abstract int getDuration();
	
	public int getIncrement() {
		return 1;
	}

	/**
	 * Modify properties of the GC, renderer, bounds, etc.
	 */
	public void animateText(final RenderContext rc, final CellRenderer<T> cellRenderer, final Row<T> row) {		
	}
	
	/**
	 * Modify the row's background colour.
	 */
	public void pulseBackground(final RenderContext rc, final Row<T> row) {		
	}
	
	/**
	 * Stop the animation if we're at the last frame, otherwise set a flag to continue animiations.
	 */
	public void postAnimate(final RenderContext rc, final Row<T> row) {
		if (row.getFrame() >= 0) {
			if (row.getFrame() < getDuration()) {
				//
				// Ensure the grid knows at least one row 
				//
				rc.setAnimationPending(true);
				
			} else {
				//
				// The row has finished it's animation.
				//
				row.setFrame(-1);
				row.setAnimation(null);
			}
		}
	}

}
