/*
 * This file is part of TTDViewer.
 * TTDViewer is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, version 2.
 * TTDViewer is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received a copy of the GNU General Public License along with TTDViewer. If not, see <http://www.gnu.org/licenses/>.
 */

import java.io.*;
import java.awt.*;
import java.awt.image.*;
import javax.imageio.stream.*;

/**
 * Simple class to load 256 color indexed .pcx files.
 *
 * TODO Add stuff to make it register to ImageIO itself.
 */
public class PCX {
	/** Helper class to read/write headers of 256 color indexed .pcs files */
	protected static class Header {
		/** Size of the image */
		public final Dimension size;

		/** Bytes per scanline */
		public final int bytes_per_line;

		/**
		 * Only public constructor for you anyway.
		 * @param aSize Image dimension
		 */
		public Header(Dimension aSize)
		{
			size = aSize;
			bytes_per_line = (aSize.width + 1) & ~1; // should be even
		}

		/**
		 * Constructor to enforce certain bytes_per_line.
		 * @param aSize Image dimension
		 * @param aBytesPerLine Bytes per scanline
		 */
		protected Header(Dimension aSize, int aBytesPerLine)
		{
			size = aSize;
			bytes_per_line = aBytesPerLine;
		}

		/**
		 * Read a .pcx header from a stream.
		 * Only 256 color indexed .pcx are allowed.
		 * @param aStream Stream to read from
		 * @return The read header, or null if no valid header was found.
		 */
		public static Header loadFrom(ImageInputStream aStream)
		{
			try {
				aStream.setByteOrder(java.nio.ByteOrder.LITTLE_ENDIAN);
				if (aStream.readByte() != 10) return null; // always 10
				if (aStream.readByte() != 5)  return null; // version: 5 for 256 colors
				if (aStream.readByte() != 1)  return null; // always 1
				if (aStream.readByte() != 8)  return null; // bits per plane: 8 for 256 colors
				int xmin = aStream.readUnsignedShort();
				int ymin = aStream.readUnsignedShort();
				int xmax = aStream.readUnsignedShort();
				int ymax = aStream.readUnsignedShort();
				aStream.readUnsignedShort();               // horizontal dpi
				aStream.readUnsignedShort();               // vertical dpi
				aStream.skipBytes(48);                     // 16 color palette: unused
				if (aStream.readByte() != 0) return null;  // always 0
				if (aStream.readByte() != 1) return null;  // number of planes: 1 for 256 colors
				int bytes_per_line_and_plane = aStream.readUnsignedShort();
				aStream.readUnsignedShort();               // palette info: deprecated
				aStream.readUnsignedShort();               // horizontal screen size: deprecated
				aStream.readUnsignedShort();               // vertical screen size: deprecated
				aStream.skipBytes(54);                     // fill to 128 bytes
				int width = xmax - xmin + 1;
				int height = ymax - ymin + 1;
				if (width <= 0 || height <= 0 || width > bytes_per_line_and_plane) return null;
				aStream.flush();
				return new Header(new Dimension(width, height), bytes_per_line_and_plane);
			} catch (Exception e) {
				return null;
			}
		}

		/**
		 * Append the .pcx header to a stream.
		 * @param aStream Stream to write to
		 */
		public void saveTo(ImageOutputStream aStream)
		{
			try {
				aStream.setByteOrder(java.nio.ByteOrder.LITTLE_ENDIAN);
				aStream.writeByte(10);                             // always 10
				aStream.writeByte(5);                              // version: 5 for 256 colors
				aStream.writeByte(1);                              // always 1
				aStream.writeByte(8);                              // bits per plane: 8 for 256 colors
				aStream.writeShort(0);                             // xmin
				aStream.writeShort(0);                             // ymin
				aStream.writeShort(size.width - 1);                // xmax
				aStream.writeShort(size.height - 1);               // ymax
				aStream.writeShort(96);                            // horizontal dpi
				aStream.writeShort(96);                            // vertical dpi
				for (int i = 0; i < 48; i++) aStream.writeByte(0); // 16 color palette: unused
				aStream.writeByte(0);                              // always 0
				aStream.writeByte(1);                              // number of planes: 1 for 256 colors
				aStream.writeShort(bytes_per_line);                // actually also per plane
				aStream.writeShort(1);                             // palette info: deprecated, set to 1
				aStream.writeShort(0);                             // horizontal screen size: deprecated, set to 0
				aStream.writeShort(0);                             // vertical screen size: deprecated, set to 0
				for (int i = 0; i < 54; i++) aStream.writeByte(0); // fill to 128 bytes
				aStream.flush();
			} catch (Exception e) {
			}
		}
	}

	/**
	 * Read a .pcx image from an ImageInputStream.
	 * Only 256 color indexed .pcx are allowed.
	 * @param aStream Stream to read from
	 * @return The read image, or null if no valid image was found.
	 */
	public static BufferedImage loadFrom(ImageInputStream aStream)
	{
		try {
			Header header = Header.loadFrom(aStream);
			if (header == null) return null;

			WritableRaster raster = Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE, header.size.width, header.size.height, 1, null);
			int[] scanline = new int[header.bytes_per_line];

			/* TODO this is quite slow */
			for (int y = 0; y < header.size.height; y++) {
				int x = 0;
				while (x < scanline.length) {
					int b = aStream.readByte();
					if ((b & 0xC0) == 0xC0) {
						int count = b & 0x3F;
						int val = aStream.readByte();
						if (x + count > scanline.length) return null;
						for (int i = 0; i < count; i++) {
							scanline[x++] = val;
						}
					} else {
						scanline[x++] = b;
					}
				}
				raster.setSamples(0, y, header.size.width, 1, 0, scanline);
				aStream.flush();
			}

			if (aStream.readByte() != 12) return null; // always 12

			byte[] palette = new byte[256 * 3];
			if (aStream.read(palette) != palette.length) return null;
			ColorModel color_model = new IndexColorModel(8, 256, palette, 0, false);
			return new BufferedImage(color_model, raster, false, null);
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Read a .pcx image from a File.
	 * Only 256 color indexed .pcx are allowed.
	 * @param aFile File to read from
	 * @return The read image, or null if no valid image was found.
	 */
	public static BufferedImage loadFrom(File aFile)
	{
		try {
			return loadFrom(new FileImageInputStream(aFile));
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Read a .pcx image from a Stream.
	 * Only 256 color indexed .pcx are allowed.
	 * @param aStream Stream to read from
	 * @return The read image, or null if no valid image was found.
	 */
	public static BufferedImage loadFrom(InputStream aStream)
	{
		return loadFrom(new MemoryCacheImageInputStream(aStream));
	}
}
