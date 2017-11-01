package com.notlob.jgrid.renderer.animation;

import com.notlob.jgrid.model.Row;
import com.notlob.jgrid.renderer.CellRenderer;
import com.notlob.jgrid.renderer.RenderContext;
import com.notlob.jgrid.resources.Rectangle;

public class BounceRowAnimation<T> extends RowAnimation<T> {
	
	private final static int ANIMATION_DURATION = 500;
	private final static int ANIMATION_INCREMENT = 10;
	
	public int getDuration() {
		return ANIMATION_DURATION;
	}
	
	public int getIncrement() {
		return ANIMATION_INCREMENT;
	}

	@Override
	public void animateText(RenderContext rc, CellRenderer<T> cellRenderer, Row<T> row) {
		final Rectangle innerBounds = cellRenderer.getInnerBounds();
		cellRenderer.getContentLocation().y = (int) bounce(row.getFrame(), ANIMATION_DURATION, innerBounds.y - innerBounds.height, innerBounds.height);
	}
	
	/**
	 * Easing method to bounds cell content.
	 */
	protected float bounce(float time, final float duration, final float start, final float destination) {
		if ((time /= duration) < (1 / 2.75f)) {
			return destination * (7.5625f * time * time) + start;
			
		} else if (time < (2 / 2.75f)) {
			return destination * (7.5625f * (time -= (1.5f / 2.75f)) * time + .75f) + start;
			
		} else if (time < (2.5 / 2.75)) {
			return destination * (7.5625f * (time -= (2.25f / 2.75f)) * time + .9375f) + start;
			
		} else {
			return destination * (7.5625f * (time -= (2.625f / 2.75f)) * time + .984375f) + start;
		}
	}
	
}
