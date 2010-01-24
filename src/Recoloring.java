/*
 * This file is part of TTDViewer.
 * TTDViewer is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, version 2.
 * TTDViewer is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received a copy of the GNU General Public License along with TTDViewer. If not, see <http://www.gnu.org/licenses/>.
 */

import java.awt.*;
import java.awt.image.*;

/** Class for mapping palette indexes to other indexes */
class Recoloring {
	/** The actual remapping */
	protected int[] fRemap = new int[256];

	/**
	 * Create the Identity.
	 */
	public Recoloring()
	{
		for (int i = 0; i < 256; i++) {
			fRemap[i] = i;
		}
	}

	/**
	 * Create a new recoloring from a (partial) remapping.
	 * If the remapping is only partial, the rest is filled with the Identity.
	 * @param aRemap Remapping of color indexes; -1 can be used for the Identity.
	 * @param aFirstIndex Actual color index, which aRemap[0] remaps.
	 */
	public Recoloring(int[] aRemap, int aFirstIndex)
	{
		this();
		for (int i = 0; i < aRemap.length; i++) {
			fRemap[aFirstIndex + i] = (aRemap[i] < 0) ? aFirstIndex + i : aRemap[i];
		}
	}

	/**
	 * Create a new recoloring from a remapping.
	 * @param aRemap Remapping of color indexes; -1 can be used for the Identity.
	 */
	public Recoloring(int[] aRemap)
	{
		this(aRemap, 0);
	}

	/**
	 * Create a new recoloring by merging a sequence of existing recolorings.
	 * The recolorings are merged by ignoring the parts that match the Identity.
	 * @param aRecoloring The recolorings to merge.
	 */
	public Recoloring(Recoloring[] aRecoloring)
	{
		this();
		for (int j = 0; j < aRecoloring.length; j++) {
			for (int i = 0; i < 256; i++) {
				int r = aRecoloring[j].fRemap[i];
				if (r != i) fRemap[i] = r;
			}
		}
	}

	/** Clone without creating a new object */
	public void copyFrom(Recoloring aRecoloring)
	{
		for (int i = 0; i < 256; i++) {
			fRemap[i] = aRecoloring.fRemap[i];
		}
	}

	/**
	 * Applies recoloring to every pixel of a raster.
	 * Input and output raster may be the same.
	 * @param aInput Input raster
	 * @param aOutput Output raster
	 */
	public void applyTo(Raster aInput, WritableRaster aOutput)
	{
		Rectangle bounds = aOutput.getBounds();
		Rectangle input_bounds = aInput.getBounds();
		if (bounds.width != input_bounds.width || bounds.height != input_bounds.height) throw new IllegalArgumentException("Source and destination raster differ in size.");

		if (bounds.width == 0 || bounds.height == 0) return;

		int[] row = new int[bounds.width];
		for (int y = 0; y < bounds.height; y++) {
			aInput.getSamples(0, y, bounds.width, 1, 0, row);
			for (int x = 0; x < bounds.width; x++) {
				row[x] = fRemap[row[x]];
			}
			aOutput.setSamples(0, y, bounds.width, 1, 0, row);
		}
	}

	/** Applies recoloring to every pixel of a raster */
	public void applyTo(WritableRaster aRaster)
	{
		applyTo(aRaster, aRaster);
	}

	/** Applies recoloring to a single color index */
	public int applyTo(int aIndex)
	{
		return fRemap[aIndex];
	}

	/**
	 * Transforms an other recoloring.
	 * That is mainly useful, when this recoloring is one of the DOS->WIN / WIN->DOS conversions.
	 * @param aInput Recoloring to transform.
	 * @return The transformed recoloring "this * aInput * inverse(this)".
	 */
	public Recoloring transform(Recoloring aInput)
	{
		int[] output = new int[256];

		/* Recolorings are usually not reversible, so just make invalid stuff transparent */
		for (int i = 0; i < 256; i++)
		{
			output[i] = 0;
		}

		for (int i = 0; i < 256; i++)
		{
			output[fRemap[i]] = fRemap[aInput.fRemap[i]];
		}

		return new Recoloring(output);
	}

	/**
	 * Transform a color palette.
	 * A raster drawn with the transformed palette will look the same as the recolored raster with the original palette.
	 * But transforming the palette is faster.
	 * @param aPalette Palette to transform.
	 * @return The transformed palette.
	 */
	public int[] transformPalette(int[] aPalette)
	{
		int[] output = new int[256];
		for (int i = 0; i < 256; i++) {
			output[i] = aPalette[fRemap[i]];
		}
		return output;
	}
}
