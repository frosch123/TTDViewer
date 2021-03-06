/*
 * This file is part of TTDViewer.
 * TTDViewer is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, version 2.
 * TTDViewer is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received a copy of the GNU General Public License along with TTDViewer. If not, see <http://www.gnu.org/licenses/>.
 */

import java.io.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.imageio.*;

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
			@Override public void stateChanged(ChangeEvent e)
			{
				repaint();
			}
		});

		MouseInputAdapter listener = new MouseInputAdapter() {
			Point2D.Float fStartPixel;
			@Override public void mousePressed(MouseEvent e)
			{
				fStartPixel = pixelFromViewport(e.getPoint());
			}

			@Override public void mouseDragged(MouseEvent e)
			{
				scrollPixelToScreen(fStartPixel, screenFromViewport(e.getPoint()));
			}

			@Override public void mouseReleased(MouseEvent e)
			{
				mouseDragged(e);
			}
		};
		addMouseListener(listener);
		addMouseMotionListener(listener);

		addMouseWheelListener(new MouseWheelListener() {
			@Override public void mouseWheelMoved(MouseWheelEvent e)
			{
				setZoomAtScreen(getZoom() - e.getWheelRotation(), screenFromViewport(e.getPoint()));
			}
		});

		fImage = TTDImage.createBlank(aPalette, 1, 1);
		updateSize();
	}

	/**
	 * Load new image from File.
	 * @param aFile File to read from
	 */
	public void loadFrom(File aFile) throws Exception
	{
		fImage = TTDImage.createFrom(fPalette, aFile);
		updateSize();
	}

	/**
	 * Save the image to File.
	 * @param aFile File to write to
	 * @param aSaveTransparentAsBlue Save transparante pixels as blue pixels.
	 * @param aSaveRecolored Save recolored
	 * @param aSaveZoomed Save zoomed
	 * @param aSaveAnimState Use the current animation state; else use a fixed state
	 */
	public void saveTo(File aFile, String aFileFormat, boolean aSaveTransparentAsBlue, boolean aSaveRecolored, boolean aSaveZoomed, boolean aSaveAnimState) throws Exception
	{
		Palette pal = aSaveAnimState ? fPalette : fPalette.getUnanimatedPalette();
		IndexColorModel color_model = pal.getColorModel(aSaveTransparentAsBlue);

		WritableRaster pixel_data = fImage.getRaster();
		int width = pixel_data.getWidth();
		int height = pixel_data.getHeight();

		if (aSaveRecolored) {
			WritableRaster recolored = color_model.createCompatibleWritableRaster(width, height);
			fPalette.global_recoloring.applyTo(pixel_data, recolored);
			pixel_data = recolored;
		}

		if (aSaveZoomed && fZoom > 1) {
			WritableRaster zoomed = color_model.createCompatibleWritableRaster(width * fZoom, height * fZoom);
			int[] row_in = new int[width];
			int[] row_out = new int[width * fZoom];
			for (int y = 0; y < height; y++) {
				pixel_data.getSamples(0, y, width, 1, 0, row_in);
				for (int x = 0; x < width; x++) {
					for (int i = 0; i < fZoom; i++) {
						row_out[x * fZoom + i] = row_in[x];
					}
				}
				for (int i = 0; i < fZoom; i++) {
					zoomed.setSamples(0, y * fZoom + i, width * fZoom, 1, 0, row_out);
				}
			}
			pixel_data = zoomed;
		}

		BufferedImage output_image = new BufferedImage(color_model, pixel_data, false, null);
		if (!ImageIO.write(output_image, aFileFormat, aFile)) {
			throw new Exception("No writer for this file format available.");
		}
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

	/** Get the JViewport this display is part of */
	protected JViewport findViewport()
	{
		Component maybe_viewport = getParent();
		while (maybe_viewport != null && !JViewport.class.isInstance(maybe_viewport)) {
			maybe_viewport = maybe_viewport.getParent();
		}
		return (JViewport)maybe_viewport;
	}

	/** Get pixel position from viewport position */
	public Point2D.Float pixelFromViewport(Point p)
	{
		Point2D.Float pixel = new Point2D.Float((p.x + 0.0f) / fZoom, (p.y + 0.0f) / fZoom);
		return pixel;
	}

	/** Get viewport position from pixel position */
	public Point viewportFromPixel(Point2D.Float p)
	{
		Point viewport = new Point(Math.round(p.x * fZoom), Math.round(p.y * fZoom));
		return viewport;
	}

	/** Get viewport position from screen position */
	public Point viewportFromScreen(Point s)
	{
		JViewport viewport = findViewport();
		Point p = new Point(s);
		if (viewport != null) {
			Point topleft = viewport.getViewPosition();
			p.translate(topleft.x, topleft.y);
		}
		return p;
	}

	/** Get viewport position from screen position */
	public Point screenFromViewport(Point v)
	{
		JViewport viewport = findViewport();
		Point p = new Point(v);
		if (viewport != null) {
			Point topleft = viewport.getViewPosition();
			p.translate(-topleft.x, -topleft.y);
		}
		return p;
	}

	/** Get pixel position from screen position */
	public Point2D.Float pixelFromScreen(Point p)
	{
		return pixelFromViewport(viewportFromScreen(p));
	}

	/** Get screen position from pixel position */
	public Point screenFromPixel(Point2D.Float p)
	{
		return screenFromViewport(viewportFromPixel(p));
	}

	/**
	 * Scroll the viewport to a certain viewport position.
	 * The position is clamped to the valid range.
	 * @param p viewport position to make the new topleft.
	 */
	public void scrollViewport(Point v)
	{
		JViewport viewport = findViewport();
		if (viewport == null) return;

		Dimension view_size = viewport.getViewSize();
		Dimension extent_size = viewport.getExtentSize();

		Point p = new Point(v);
		if (p.x + extent_size.width  > view_size.width)  p.x = view_size.width  - extent_size.width;
		if (p.y + extent_size.height > view_size.height) p.y = view_size.height - extent_size.height;
		if (p.x < 0) p.x = 0;
		if (p.y < 0) p.y = 0;

		viewport.setViewPosition(p);
	}

	/**
	 * Scroll a certain viewport position to a certain screen position.
	 * The position is clamped to the valid range.
	 * @param v viewport position
	 * @param s screen position
	 */
	public void scrollViewportToScreen(Point v, Point s)
	{
		Point p = new Point(v);
		p.translate(-s.x, -s.y);
		scrollViewport(p);
	}

	/**
	 * Scroll a certain pixel position to a certain screen position.
	 * The position is clamped to the valid range.
	 * @param p pixel position
	 * @param s screen position
	 */
	public void scrollPixelToScreen(Point2D.Float p, Point s)
	{
		scrollViewportToScreen(viewportFromPixel(p), s);
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

	/** Set current zoomlevel while retaining the pixel position at a certain screen position. */
	public void setZoomAtScreen(int aZoom, Point s)
	{
		if (aZoom == fZoom) return;
		Point2D.Float pixel = pixelFromScreen(s);
		setZoom(aZoom);
		scrollPixelToScreen(pixel, s);
	}

	/** Set current zoomlevel while retaining the pixel position at the center of the screen. */
	public void setZoomAtCenter(int aZoom)
	{
		if (aZoom == fZoom) return;
		JViewport viewport = findViewport();
		if (viewport != null) {
			Dimension extent_size = viewport.getExtentSize();
			setZoomAtScreen(aZoom, new Point(extent_size.width / 2, extent_size.height / 2));
		} else {
			setZoom(aZoom);
		}
	}

	@Override protected void paintComponent(Graphics g)
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

		ColorModel color_model = fPalette.getColorModel(fPalette.global_recoloring, false);
		g.drawImage(fImage.getImage(color_model, x, y, width, height), output_x, output_y, output_width, output_height, Color.WHITE, null);
	}
}
