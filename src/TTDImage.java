/*
 * This file is part of TTDViewer.
 * TTDViewer is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, version 2.
 * TTDViewer is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received a copy of the GNU General Public License along with TTDViewer. If not, see <http://www.gnu.org/licenses/>.
 */

import java.io.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.imageio.*;

/** Class to hold a TTD related image, recolour it and draw it or subsets of it. */
public class TTDImage {
	/** Image with indexed colors */
	protected WritableRaster fPixelData = null;

	/** Palette to use */
	protected TTDPalette fPalette;

	/** Recoloring to apply before drawing */
	public Recoloring recoloring = null;

	/**
	 * Create an image.
	 * Note there are more useful static methods to create an image.
	 * @param aPalette Palette to use.
	 * @param aRaster Image with indexed colors.
	 * @see #createBlank
	 * @see #createFrom
	 */
	public TTDImage(TTDPalette aPalette, WritableRaster aRaster)
	{
		fPalette = aPalette;
		fPixelData = aRaster;
	}

	/**
	 * Create empty (transparent) image.
	 * @param aPalette Palette to use.
	 * @param aWidth Image width.
	 * @param aHeight Image height.
	 * @return new image.
	 */
	public static TTDImage createBlank(TTDPalette aPalette, int aWidth, int aHeight)
	{
		IndexColorModel color_model = aPalette.getColorModel();
		WritableRaster pixel_data = color_model.createCompatibleWritableRaster(aWidth, aHeight);
		return new TTDImage(aPalette, pixel_data);
	}

	/**
	 * Compares two colors and decides whether they are sufficient close to each other to be considered equal.
	 * This is used to detect WIN/DOS palette.
	 */
	private static boolean mayColorsBeEqual(int aColor1, int aColor2)
	{
		return Math.abs((aColor1 & 0xFF) - (aColor2 & 0xFF)) < 7
				&& Math.abs(((aColor1 >> 8) & 0xFF) - ((aColor2 >> 8) & 0xFF)) < 7
				&& Math.abs(((aColor1 >> 16) & 0xFF) - ((aColor2 >> 16) & 0xFF)) < 7;
	}

	/**
	 * Create image from a BufferedImage.
	 * The given image must use indexed colors from either TTD's DOS or WIN palette.
	 * The function tests the grayscale colors to detect DOS/WIN palette.
	 * @param aPalette Palette to use.
	 * @param aImage Image to read.
	 * @return new image, or null if the source is not valid.
	 */
	public static TTDImage createFrom(TTDPalette aPalette, BufferedImage aImage)
	{
		if (aImage.getType() == BufferedImage.TYPE_BYTE_INDEXED) {
			ColorModel color_model = aImage.getColorModel();
			boolean maybe_dos = true;
			boolean maybe_win = true;
			try {
				/* Traverse gray scale */
				for (int i = 0; i < aPalette.GRAYSCALE.length; i++) {
					int test_index = aPalette.GRAYSCALE[i];
					int test_color = aPalette.DOS_PALETTE[test_index];

					int dos_color = color_model.getRGB(test_index);
					maybe_dos &= mayColorsBeEqual(test_color, dos_color);

					int win_color = color_model.getRGB(aPalette.CONVERT_TO_WIN.fRemap[test_index]);
					maybe_win &= mayColorsBeEqual(test_color, win_color);
				}
			} catch (Exception e) {
				System.out.println("Invalid indexed image.");
				return null;
			}
			if (maybe_dos == maybe_win) {
				System.out.println("Palette not detected.");
				return null;
			}

			TTDImage result = createBlank(aPalette, aImage.getWidth(), aImage.getHeight());
			result.fPixelData.setRect(aImage.getRaster());
			if (maybe_win) {
				System.out.println("WIN palette detected. Converting.");
				aPalette.CONVERT_FROM_WIN.applyTo(result.fPixelData);
			} else {
				System.out.println("DOS palette detected.");
			}
			return result;
		} else {
			System.out.println("No indexed image.");
			return null;
		}
	}

	/**
	 * Create image from File.
	 * The given image must use indexed colors from either TTD's DOS or WIN palette.
	 * The function tests the grayscale colors to detect DOS/WIN palette.
	 * Supported are .pcx files and everything Java knows itself.
	 * @param aPalette Palette to use.
	 * @param aFile File to read from.
	 * @return new image, or null if the source is not valid.
	 */
	public static TTDImage createFrom(TTDPalette aPalette, File aFile)
	{
		try {
			/* First try our own PCX thingie */
			BufferedImage image = PCX.loadFrom(aFile);
			if (image == null) image = ImageIO.read(aFile);
			return createFrom(aPalette, image);
		} catch (Exception e) {
			System.out.println(e);
			return null;
		}
	}

	/**
	 * Create image from Stream.
	 * The given image must use indexed colors from either TTD's DOS or WIN palette.
	 * The function tests the grayscale colors to detect DOS/WIN palette.
	 * Supported are .pcx files and everything Java knows itself.
	 * @param aPalette Palette to use.
	 * @param aStream Stream to read from.
	 * @return new image, or null if the source is not valid.
	 */
	public static TTDImage createFrom(TTDPalette aPalette, InputStream aStream)
	{
		try {
			/* First try our own PCX thingie */
			BufferedImage image = PCX.loadFrom(aStream);
			if (image == null) image = ImageIO.read(aStream);
			return createFrom(aPalette, image);
		} catch (Exception e) {
			return null;
		}
	}

	/** Get image dimension. */
	public Dimension getSize()
	{
		return fPixelData.getBounds().getSize();
	}

	/** Return a BufferedImage of the image using the current palette and recoloring. */
	public BufferedImage getImage()
	{
		return new BufferedImage(fPalette.getColorModel(recoloring), fPixelData, false, null);
	}

	/**
	 * Return a BufferedImage of a part of the image using the current palette and recoloring.
	 * If the selected rectangle extents the image dimension it is clipped at its border.
	 */
	public BufferedImage getImage(Rectangle rect)
	{
		Rectangle valid = rect.intersection(fPixelData.getBounds());
		WritableRaster sub_raster = fPixelData.createWritableChild(valid.x, valid.y, valid.width, valid.height, 0, 0, null);
		return new BufferedImage(fPalette.getColorModel(recoloring), sub_raster, false, null);
	}

	/**
	 * Return a BufferedImage of a part of the image using the current palette and recoloring.
	 * If the selected rectangle extents the image dimension it is clipped at its border.
	 */
	public BufferedImage getImage(int x, int y, int width, int height)
	{
		return getImage(new Rectangle(x, y, width, height));
	}
}
