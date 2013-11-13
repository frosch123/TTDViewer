/*
 * This file is part of TTDViewer.
 * TTDViewer is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, version 2.
 * TTDViewer is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received a copy of the GNU General Public License along with TTDViewer. If not, see <http://www.gnu.org/licenses/>.
 */

import java.awt.*;
import java.awt.image.*;
import javax.swing.event.*;

/**
 * Base class for indexed palettes.
 * ChangeEvents are triggered on changes to the palette or the global recoloring.
 */
public class Palette extends DefaultChangeEventTrigger {
	/** Current RGBA palette. */
	protected int[] fCurrentPalette = new int[256];

	/**
	 * Selected global recoloring.
	 * Note: It is final, so everyone can refer to it. But the actualy remapping changes nevertheless.
	 */
	public final Recoloring global_recoloring = new Recoloring();

	/** Protected dummy constructor to prevent public construction */
	protected Palette()
	{
	}

	/**
	 * Switch the global recoloring.
	 * The new recoloring will be copied into {@link #global_recoloring} without changing {@link #global_recoloring} itself.
	 * @param aRecoloring recoloring to use globally
	 */
	public void setGlobalRecoloring(Recoloring aRecoloring)
	{
		global_recoloring.copyFrom(aRecoloring);
		fireChangeEvent();
	}

	/** Build a IndexColorModel with the current palette and a certain recoloring applied. */
	public IndexColorModel getColorModel(Recoloring aRecoloring, boolean aTransparentAsBlue)
	{
		int[] raw_palette;
		if (aTransparentAsBlue) {
			raw_palette = new int[256];
			for (int i = 0; i < 256; i++) {
				int c = fCurrentPalette[i];
				if (c >> 24 == 0) c = 0xFF0000FF;
				raw_palette[i] = c;
			}
		} else {
			raw_palette = fCurrentPalette;
		}
		int[] palette = (aRecoloring != null) ? aRecoloring.transformPalette(raw_palette) : raw_palette;

		return new IndexColorModel(8, 256, palette, 0, !aTransparentAsBlue, -1, DataBuffer.TYPE_BYTE);
	}

	/** Build a IndexColorModel with the current palette. */
	public IndexColorModel getColorModel(boolean aTransparentAsBlue)
	{
		return getColorModel(null, aTransparentAsBlue);
	}

	/** Get the RGBA value of a certain color of the current palette. */
	public Color getColor(int i)
	{
		return new Color(fCurrentPalette[i], true);
	}
}
