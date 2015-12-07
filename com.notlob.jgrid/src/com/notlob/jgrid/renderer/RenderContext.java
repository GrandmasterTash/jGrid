package com.notlob.jgrid.renderer;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;

import com.notlob.jgrid.Grid;
import com.notlob.jgrid.renderer.Renderer.RenderPass;

/**
 * A single instance is created for each grid and passed to the individual renderers during a paint cycle.
 * 
 * @author Stef
 *
 */
public class RenderContext {
	
	protected final Grid<?> grid;
	
	// The current graphics context being used for the paint.
	protected GC gc;
	
	// True whilst we're in a paint job - used to prevent interleaving calls from corrupting the render context.
	protected boolean painting;
	
	// We paint in multiple 'passes'. This allows us, for example, to render the selection background
	// rectangle, THEN render the cell foreground without it being affected by the background.
	protected RenderPass renderPass;
	
	// Are we painting an alternate background row?
	protected boolean alternate;
	
	// Stops the grid painting the anchor in a pinned column.
	protected boolean paintingPinned;
	
	// Stops the grip column header separator style being painted for the corner cell and the last column header cell.
	protected boolean dontPaintGrip;
	
	// Ensure a paint operation only logs an exception once - otherwise grid painting could fill-up log files.
	protected boolean errorLogged;
	
	// True if one or more rows has not finished it's throb animation.
	protected boolean animationPending;
	
	// Used by the text-wrapping calculation in a COMPUTE_SIZE pass to grow the row if required. 
	protected Integer computedHeightDelta;
	
	// Sets a flag to force a paint to use all rows - not just what's in the viewport - used forced COMPUTED_SIZE calls.
	protected boolean forceAllRows;
	
	// Cache the width of any text rendered by the font data.
	protected final Map<FontData, Map<String, Point>> extentCache;
		
	public RenderContext(final Grid<?> grid) {
		this.grid = grid;
		this.extentCache = new HashMap<>();
		this.computedHeightDelta = null;
	}
	
	public Grid<?> getGrid() {
		return grid;
	}
	
	public GC getGC() {
		return gc;
	}

	public void setGC(final GC gc) {
		this.gc = gc;
	}
	
	public void setPainting(final boolean painting) {
		this.painting = painting;
	}
	
	public boolean isPainting() {
		return this.painting;
	}

	public boolean isPaintingPinned() {
		return paintingPinned;
	}

	public void setPaintingPinned(final boolean paintingPinned) {
		this.paintingPinned = paintingPinned;
	}

	public RenderPass getRenderPass() {
		return renderPass;
	}

	public void setRenderPass(final RenderPass renderPass) {
		this.renderPass = renderPass;
	}

	public boolean isErrorLogged() {
		return errorLogged;
	}

	public void setErrorLogged(final boolean errorLogged) {
		this.errorLogged = errorLogged;
	}
	
	public boolean isAnimationPending() {
		return animationPending;
	}

	public void setAnimationPending(final boolean animationPending) {
		this.animationPending = animationPending;
	}

	public boolean isAlternate() {
		return alternate;
	}

	public void setAlternate(final boolean alternate) {
		this.alternate = alternate;
	}

	public boolean isDontPaintGrip() {
		return dontPaintGrip;
	}

	public void setDontPaintGrip(final boolean dontPaintGrip) {
		this.dontPaintGrip = dontPaintGrip;
	}
	
	public boolean isForceAllRows() {
		return forceAllRows;
	}
	
	public void setForceAllRows(final boolean forceAllRows) {
		this.forceAllRows = forceAllRows;
	}

	public Map<FontData, Map<String, Point>> getExtentCache() {
		return extentCache;
	}
	
	public Integer getComputedHeightDelta() {
		return computedHeightDelta;
	}
	
	public void setComputedHeightDelta(final Integer computedHeightDelta) {
		this.computedHeightDelta = computedHeightDelta;
	}
}