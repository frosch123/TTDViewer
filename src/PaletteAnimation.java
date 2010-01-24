/*
 * This file is part of TTDViewer.
 * TTDViewer is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, version 2.
 * TTDViewer is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received a copy of the GNU General Public License along with TTDViewer. If not, see <http://www.gnu.org/licenses/>.
 */

/** Base class for palette animation */
public abstract class PaletteAnimation {
	/** Name for GUI */
	public final String name;

	/** Color indexes involved */
	public final int[] colors;

	/** Enable the animation */
	public boolean enabled = true;

	/**
	 * Constructor without initialisation of {@link #colors}
	 * @param aName Name for GUI
	 */
	protected PaletteAnimation(String aName, int[] aColors)
	{
		name = aName;
		colors = aColors;
	}

	/**
	 * Constructor for palette animations using consecutive color indexes.
	 * @param aName Name for GUI
	 * @param aFirstIndex First color index of animation
	 * @param aNumIndex Number of color indexes involved
	 */
	protected PaletteAnimation(String aName, int aFirstIndex, int aNumIndex)
	{
		this(aName, new int[aNumIndex]);
		for (int i = 0; i < aNumIndex; i++) {
			colors[i] = aFirstIndex + i;
		}
	}

	/**
	 * Inserts the animatated colors into a palette.
	 * @param aPalette Palette to modify
	 * @param aAnimationCounter TTD animation counter: 16 bit, incremented by 8 every 30 ms.
	 * @param aToyland true if toyland-specific stuff should be used
	 */
	public abstract void applyPalette(int[] aPalette, int aAnimationCounter, boolean aToyland);
};
