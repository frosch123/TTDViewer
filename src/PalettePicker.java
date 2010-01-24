/*
 * This file is part of TTDViewer.
 * TTDViewer is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, version 2.
 * TTDViewer is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received a copy of the GNU General Public License along with TTDViewer. If not, see <http://www.gnu.org/licenses/>.
 */

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.swing.*;
import javax.swing.event.*;

/** GUI class to show some colors of a palette */
class PalettePicker extends JPanel {
	/** Palette to get original colors from */
	protected Palette fPalette;

	/** Color indexes to show, and where to show them */
	protected int[][] fColors;

	/** Recoloring to apply before drawing */
	protected Recoloring fRecoloring;

	/** Hide certain color indexes (indexes as before recoloring) */
	public boolean[] hide_color;

	/** Draw a checkered rectangle representing transparency */
	protected static void drawTransparency(Graphics g, int x, int y, int width, int height)
	{
		Shape old_clip = g.getClip();
		g.clipRect(x, y, width, height);

		for (int i = 0; i < height; i += 4) {
			for (int j = 0; j < width; j += 4) {
				g.setColor(((i + j) & 4) != 0 ? Color.WHITE : Color.LIGHT_GRAY);
				g.fillRect(x + j, y + i, 4, 4);
			}
		}

		g.setClip(old_clip);
	}

	/**
	 * Create new PalettePicker showing certain color indexes in a matrix layout.
	 * @param aPalette Palette to use colors from
	 * @param aRecoloring Recoloring to apply before drawing
	 * @param aColors Color indexes in the matrix
	 */
	public PalettePicker(Palette aPalette, Recoloring aRecoloring, int[][] aColors)
	{
		fPalette = aPalette;
		fColors = aColors;
		fRecoloring = aRecoloring;

		fPalette.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e)
			{
				repaint();
			}
		});

		/* Show all colors by default */
		hide_color = new boolean[256];
		for (int i = 0; i < 256; i++) {
			hide_color[i] = false;
		}

		setPreferredSize(new Dimension(fColors[0].length * 16, fColors.length * 16));
		setMinimumSize(new Dimension(fColors[0].length * 16, fColors.length * 16));
		setMaximumSize(new Dimension(fColors[0].length * 16, fColors.length * 16));
		setBackground(Color.WHITE);
	}

	/**
	 * Create new PalettePicker showing certain colors in a single row.
	 * @param aPalette Palette to use colors from
	 * @param aRecoloring Recoloring to apply before drawing
	 * @param aColors Color indexes in the row
	 */
	public PalettePicker(Palette aPalette, Recoloring aRecoloring, int[] aColors)
	{
		this(aPalette, aRecoloring, new int[][] {aColors});
	}

	protected void paintComponent(Graphics g)
	{
		Dimension size = getSize();
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, size.width, size.height);

		int rows = fColors.length;
		int cols = fColors[0].length;

		int sizex = size.width / cols;
		int sizey = size.height / rows;
		int offsx = (size.width - sizex * cols) / 2;
		int offsy = (size.height - sizey * rows) / 2;

		for (int y = 0; y < rows; y++) {
			for (int x = 0; x < cols; x++) {
				int cellx = offsx + x * sizex;
				int celly = offsy + y * sizey;

				/* Skip hidden colors */
				int color = fColors[y][x];
				if (hide_color[color]) continue;

				/* Recolor */
				color = fRecoloring.fRemap[color];

				/* Draw the color */
				Color real_color = fPalette.getColor(color);
				if (real_color.getAlpha() != 255) {
					drawTransparency(g, cellx + 1, celly + 1, sizex - 2, sizey - 2);
				}
				if (real_color.getAlpha() != 0) {
					g.setColor(real_color);
					g.fillRect(cellx + 1, celly + 1, sizex - 2, sizey - 2);
				}

				/* Draw border */
				g.setColor(Color.BLACK);
				g.drawRect(cellx + 1, celly + 1, sizex - 2, sizey - 2);
			}
		}
	}
}
