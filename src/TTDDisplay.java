/*
 * This file is part of TTDViewer.
 * TTDViewer is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, version 2.
 * TTDViewer is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received a copy of the GNU General Public License along with TTDViewer. If not, see <http://www.gnu.org/licenses/>.
 */

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.swing.*;
import javax.swing.event.*;

/** Displays a TTDImage */
public class TTDDisplay extends JPanel {
	/** Shown image */
	protected TTDImage fImage;

	/** Palette to use for creating images */
	protected TTDPalette fPalette;

	/** Current zoomlevel */
	protected int fZoom = 1;

	/** Listeners to notify on changes in zoom level, file loading, etc. */
	private DefaultChangeEventTrigger fChangeEventListeres = new DefaultChangeEventTrigger();

	/**
	 * Only constructor starting with an empty image.
	 * @param aPalette Palette to use when loading images.
	 */
	public TTDDisplay(TTDPalette aPalette)
	{
		fPalette = aPalette;
		fPalette.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e)
			{
				repaint();
			}
		});

		fImage = TTDImage.createBlank(aPalette, 1, 1);
		fImage.recoloring = aPalette.global_recoloring;
		updateSize();
	}

	/**
	 * Load new image from File.
	 * @param aFile File to read from
	 */
	public void loadFrom(File aFile) throws Exception
	{
		fImage = TTDImage.createFrom(fPalette, aFile);
		fImage.recoloring = fPalette.global_recoloring;
		updateSize();
	}

	/** The listener will be notified on changes to the zoom level, file loading, etc. */
	public void addChangeListener(ChangeListener l)
	{
		fChangeEventListeres.addChangeListener(l);
	}

	public void removeChangeListener(ChangeListener l)
	{
		fChangeEventListeres.removeChangeListener(l);
	}

	protected void fireChangeEvent()
	{
		fChangeEventListeres.fireChangeEvent();
	}

	/** Resize this to match image bounds and zoomlevel */
	protected void updateSize()
	{
		Dimension size = fImage.getSize();
		Dimension new_size = new Dimension(size.width * fZoom, size.height * fZoom);
		setPreferredSize(new_size);
		setSize(new_size);
		fireChangeEvent();
	}

	/** Get current zoomlevel */
	public int getZoom()
	{
		return fZoom;
	}

	/** Set current zoomlevel */
	public void setZoom(int aZoom)
	{
		if (aZoom == fZoom) return;
		if (aZoom < 1) aZoom = 1;
		fZoom = aZoom;
		updateSize();
	}

	protected void paintComponent(Graphics g)
	{
		Rectangle display_bounds = g.getClipBounds();
		int x = display_bounds.x / fZoom;
		int y = display_bounds.y / fZoom;
		int width = display_bounds.width / fZoom + 2;
		int height = display_bounds.height / fZoom + 2;

		Dimension size = fImage.getSize();
		if (x + width > size.width) width = size.width - x;
		if (y + height > size.height) height = size.height - y;

		if (width <= 0 || height <= 0) return;

		int output_x = x * fZoom;
		int output_y = y * fZoom;
		int output_width = width * fZoom;
		int output_height = height * fZoom;

		/* Clear background if not completely covered by image */
		if (output_x > display_bounds.x || output_y > display_bounds.y
				|| (output_x + output_width  < display_bounds.x + display_bounds.width )
				|| (output_y + output_height < display_bounds.y + display_bounds.height)) {
			g.setColor(getBackground());
			g.fillRect(display_bounds.x, display_bounds.y, display_bounds.width, display_bounds.height);
		}

		g.drawImage(fImage.getImage(x, y, width, height), output_x, output_y, output_width, output_height, Color.WHITE, null);
	}
}
