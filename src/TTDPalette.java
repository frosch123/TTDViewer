/*
 * This file is part of TTDViewer.
 * TTDViewer is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, version 2.
 * TTDViewer is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received a copy of the GNU General Public License along with TTDViewer. If not, see <http://www.gnu.org/licenses/>.
 */

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/** Main class for dealing with TTD related palettised stuff. */
public class TTDPalette extends Palette {
	public static final int TEMPERATE    = 0;
	public static final int ARCTIC       = 1;
	public static final int TROPIC       = 2;
	public static final int TOYLAND      = 3;
	public static final int NUM_CLIMATES = 4;

	/**
	 * This is the TTD DOS Palette.
	 * Except:
	 * <ul><li> Index 0xFF (pure white) is set to #FFFFFF instead of #FCFCFC, however these colors are equivalent
	 *          for 6-bit colordepth, which is most likely used by the DOS version. </li>
	 *     <li> All palette animation colors are set to black. </li></ul>
	 * Alpha values are not part of this table, however index 0x00 is fully transparent, the rest is opaque.
	 */
	public static final int[] DOS_PALETTE = {
		0x000000, 0x101010, 0x202020, 0x303030, 0x404040, 0x505050, 0x646464, 0x747474, // 00 - 07
		0x848484, 0x949494, 0xA8A8A8, 0xB8B8B8, 0xC8C8C8, 0xD8D8D8, 0xE8E8E8, 0xFCFCFC, // 08 - 0F
		0x343C48, 0x444C5C, 0x586070, 0x6C7484, 0x848C98, 0x9CA0AC, 0xB0B8C4, 0xCCD0DC, // 10 - 17
		0x302C04, 0x403C0C, 0x504C14, 0x605C1C, 0x787840, 0x949464, 0xB0B084, 0xCCCCA8, // 18 - 1F
		0x482C04, 0x583C14, 0x68502C, 0x7C6848, 0x98845C, 0xB8A078, 0xD4BC94, 0xF4DCB0, // 20 - 27
		0x400004, 0x580410, 0x701020, 0x882034, 0xA0384C, 0xBC546C, 0xCC687C, 0xDC8490, // 28 - 2F
		0xEC9CA4, 0xFCBCC0, 0xFCD400, 0xFCE83C, 0xFCF880, 0x4C2800, 0x603C08, 0x74581C, // 30 - 37
		0x887438, 0x9C8850, 0xB09C6C, 0xC4B488, 0x441800, 0x602C04, 0x804408, 0x9C6010, // 38 - 3F
		0xB87818, 0xD49C20, 0xE8B810, 0xFCD400, 0xFCF880, 0xFCFCC0, 0x200400, 0x401408, // 40 - 47
		0x541C10, 0x6C2C1C, 0x803828, 0x944838, 0xA85C4C, 0xB86C58, 0xC4806C, 0xD49480, // 48 - 4F
		0x083400, 0x104000, 0x205004, 0x306004, 0x40700C, 0x548414, 0x68941C, 0x80A82C, // 50 - 57
		0x1C3418, 0x2C4420, 0x3C5830, 0x50683C, 0x687C4C, 0x80945C, 0x98B06C, 0xB4CC7C, // 58 - 5F
		0x103418, 0x20482C, 0x386048, 0x4C7458, 0x60886C, 0x78A488, 0x98C0A8, 0xB8DCC8, // 60 - 67
		0x201800, 0x381C00, 0x482804, 0x58340C, 0x684018, 0x7C542C, 0x8C6C40, 0xA08058, // 68 - 6F
		0x4C2810, 0x603418, 0x744428, 0x885438, 0xA46040, 0xB87050, 0xCC8060, 0xD49470, // 70 - 77
		0xE0A880, 0xECBC94, 0x501C04, 0x642814, 0x783828, 0x8C4C40, 0xA06460, 0xB88888, // 78 - 7F
		0x242844, 0x303454, 0x404064, 0x505074, 0x646488, 0x8484A4, 0xACACC0, 0xD4D4E0, // 80 - 87
		0x281470, 0x402C90, 0x5840AC, 0x684CC4, 0x7858E0, 0x8C68FC, 0xA088FC, 0xBCA8FC, // 88 - 8F
		0x00186C, 0x002484, 0x0034A0, 0x0048B8, 0x0060D4, 0x1878DC, 0x3890E8, 0x58A8F0, // 90 - 97
		0x80C4FC, 0xBCE0FC, 0x104060, 0x18506C, 0x286078, 0x347084, 0x508CA0, 0x74ACC0, // 98 - 9F
		0x9CCCDC, 0xCCF0FC, 0xAC3434, 0xD43434, 0xFC3434, 0xFC6458, 0xFC907C, 0xFCB8A0, // A0 - A7
		0xFCD8C8, 0xFCF4EC, 0x481470, 0x5C2C8C, 0x7044A8, 0x8C64C4, 0xA888E0, 0xCCB4FC, // A8 - AF
		0xCCB4FC, 0xE8D0FC, 0x3C0000, 0x5C0000, 0x800000, 0xA00000, 0xC40000, 0xE00000, // B0 - B7
		0xFC0000, 0xFC5000, 0xFC6C00, 0xFC8800, 0xFCA400, 0xFCC000, 0xFCDC00, 0xFCFC00, // B8 - BF
		0xCC8808, 0xE49004, 0xFC9C00, 0xFCB030, 0xFCC464, 0xFCD898, 0x081858, 0x0C2468, // C0 - C7
		0x14347C, 0x1C448C, 0x285CA4, 0x3878BC, 0x4898D8, 0x64ACE0, 0x5C9C34, 0x6CB040, // C8 - CF
		0x7CC84C, 0x90E05C, 0xE0F4FC, 0xCCF0FC, 0xB4DCEC, 0x84BCD8, 0x5898AC, 0xD400D4, // D0 - D7
		0xD400D4, 0xD400D4, 0xD400D4, 0xD400D4, 0xD400D4, 0xD400D4, 0xD400D4, 0xD400D4, // D8 - DF
		0xD400D4, 0xD400D4, 0xD400D4, 0x000000, 0x000000, 0x000000, 0x000000, 0x000000, // E0 - E7
		0x000000, 0x000000, 0x000000, 0x000000, 0x000000, 0x000000, 0x000000, 0x000000, // E8 - EF
		0x000000, 0x000000, 0x000000, 0x000000, 0x000000, 0x000000, 0x000000, 0x000000, // F0 - F7
		0x000000, 0x000000, 0x000000, 0x000000, 0x000000, 0x000000, 0x000000, 0xFFFFFF  // F8 - FF
	};

	/** Color index of transparency */
	public static final int TRANSPARENT = 0x00;

	/**
	 * Color indices of grayscale.
	 * Note: TTD has no black.
	 * (Unless you cheat with the transparent color in a non-transparent drawing mode.
	 *  But that is not "supported" in OTTD anymore anyway.)
	 */
	public static final int[] GRAYSCALE = {
		0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F
	};

	/** Color indixes of magic/unused/meaningless/stupid pink */
	public static final int[] MAGIC_PINK = {
		0xD7, 0xD8, 0xD9, 0xDA, 0xDB, 0xDC, 0xDD, 0xDE, 0xDF, 0xE0, 0xE1, 0xE2
	};

	/**
	 * Color index of pure white.
	 * This color does not appear in GRFs, but is used by grfcodec for inter-sprite space.
	 */
	public static final int PURE_WHITE = 0xFF;

	/**
	 * Convert color indices from WIN palette to DOS palette.
	 * This conversion is lossless for normal colors, but some pink is lost:
	 * <ul><li> indices 03-06 (pinkF0-pinkF3 in grfcodec) are mapped to index D7 (pinkF4). </li>
	 *     <li> indices 01-02 (magic text colors) are preserved. </li></ul>
	 * This may not be intended, if you use your own recolorsprites.
	 */
	public static final Recoloring CONVERT_FROM_WIN = new Recoloring(new int[] {
		  -1, 0x01, 0x02, 0xD7, 0xD7, 0xD7, 0xD7, 0xD7, 0xD8, 0xD9,   -1,   -1,   -1,   -1,   -1,   -1, // 00 - 0F
		  -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1, // 10 - 1F
		0x06, 0x07,   -1,   -1,   -1,   -1,   -1,   -1, 0x08,   -1,   -1,   -1,   -1,   -1,   -1,   -1, // 20 - 2F
		  -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1, // 30 - 3F
		  -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1, // 40 - 4F
		  -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1, 0x04,   -1,   -1,   -1,   -1,   -1,   -1,   -1, // 50 - 5F
		  -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1, 0x05,   -1,   -1,   -1,   -1,   -1, // 60 - 6F
		  -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1, // 70 - 7F
		  -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1, 0x03,   -1,   -1,   -1,   -1,   -1,   -1,   -1, // 80 - 8F
		  -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1, // 90 - 9F
		  -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1, // A0 - AF
		  -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1, // B0 - BF
		  -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1, // C0 - CF
		  -1,   -1,   -1,   -1,   -1,   -1,   -1, 0x01, 0x02, 0xF5, 0xF6, 0xF7, 0xF8, 0xF9, 0xFA, 0xFB, // D0 - DF
		0xFC, 0xFD, 0xFE,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1, // E0 - EF
		  -1,   -1,   -1,   -1,   -1, 0x09, 0xDA, 0xDB, 0xDC, 0xDD, 0xDE, 0xDF, 0xE0, 0xE1, 0xE2,   -1  // F0 - FF
	});

	/**
	 * Convert color indices from DOS palette to WIN palette.
	 * This conversion is lossy.
	 * <ul><li> Some colors (indices 20,21,28,58,6A,88) that are not present in the win palette are mapped to other colors. </li>
	 *     <li> Indices 01-02 (shared between magic text colors and dark gray) are mapped to dark gray.
	 *          The magic text colors are lost. </li>
	 *     <li> Magic pink is mapped in the sense of grfcodec. </li></ul>
	 * Index FF (pure white) is preserved, this is different to grfcodec.
	 */
	public static final Recoloring CONVERT_TO_WIN = new Recoloring(new int[] {
		  -1, 0xD7, 0xD8, 0x88, 0x58, 0x6A, 0x20, 0x21, 0x28, 0xF5,   -1,   -1,   -1,   -1,   -1,   -1, // 00 - 0F
		  -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1, // 10 - 1F
		0x35, 0x36,   -1,   -1,   -1,   -1,   -1,   -1, 0xB2,   -1,   -1,   -1,   -1,   -1,   -1,   -1, // 20 - 2F
		  -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1, // 30 - 3F
		  -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1, // 40 - 4F
		  -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1, 0x60,   -1,   -1,   -1,   -1,   -1,   -1,   -1, // 50 - 5F
		  -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1, 0x35,   -1,   -1,   -1,   -1,   -1, // 60 - 6F
		  -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1, // 70 - 7F
		  -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1, 0xAA,   -1,   -1,   -1,   -1,   -1,   -1,   -1, // 80 - 8F
		  -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1, // 90 - 9F
		  -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1, // A0 - AF
		  -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1, // B0 - BF
		  -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1, // C0 - CF
		  -1,   -1,   -1,   -1,   -1,   -1,   -1, 0x07, 0x08, 0x09, 0xF6, 0xF7, 0xF8, 0xF9, 0xFA, 0xFB, // D0 - DF
		0xFC, 0xFD, 0xFE,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1,   -1, // E0 - EF
		  -1,   -1,   -1,   -1,   -1, 0xD9, 0xDA, 0xDB, 0xDC, 0xDD, 0xDE, 0xDF, 0xE0, 0xE1, 0xE2,   -1  // F0 - FF
	});

	/** Palette animation that shifts some colors cyclically */
	protected class PaletteAnimationCycle extends PaletteAnimation {
		/** RGB values to insert in the palette */
		private int[] fColorPalette;

		/** Bitwise negate animation counter before usage. (TTD magic) */
		private boolean fInvertCounter;

		/** Speed of color shifting */
		private int fSpeed;

		/**
		 * Create animation that shifts some colors cyclically.
		 * The color indices must be consecutive.
		 * @param aName Name for GUI
		 * @param aFirstIndex First index in the palette to insert colors into.
		 * @param aColorPalette RGB colors to insert.
		 * @param aInvertCounter Bitwise negate animation counter before usage. (TTD magic)
		 * @param aSpeed Speed of color shifting.
		 */
		public PaletteAnimationCycle(String aName, int aFirstIndex, int[] aColorPalette, boolean aInvertCounter, int aSpeed)
		{
			super(aName, aFirstIndex, aColorPalette.length);
			fColorPalette = aColorPalette;
			fInvertCounter = aInvertCounter;
			fSpeed = aSpeed;
		}

		public void applyPalette(int[] aPalette, int aAnimationCounter, boolean aToyland)
		{
			if (!enabled) aAnimationCounter = 0;
			aAnimationCounter &= 0xFFFF;
			if (fInvertCounter) aAnimationCounter = ~aAnimationCounter;

			int len = fColorPalette.length;
			int position = (((aAnimationCounter * fSpeed) & 0xFFFF) * len) >> 16;

			for (int i = 0; i < len; i++) {
				aPalette[colors[i]] = fColorPalette[(position + i) % len] | 0xFF000000;
			}
		}
	};

	/** Palette animation that shifts some colors cyclically while only a subset of the colors is used at once. */
	protected class PaletteAnimationWaterCycle extends PaletteAnimation {
		/**
		 * RGB values to insert in the palette.
		 * See constructor for description.
		 */
		private int[][][] fColorPalette;

		/** Bitwise negate animation counter before usage. (TTD magic) */
		private boolean fInvertCounter;

		/** Speed of color shifting */
		private int fSpeed;

		/** Number of subsets (= fColorPalette[0].length) */
		private int fNumPages;

		/**
		 * Create animation that shifts some colors cyclically.
		 * The color indices must be consecutive.
		 * The color cycle consists of two cascaded cycles.
		 * There are m cycles of n colors each which are cycled themself:
		 * <table><tr><th> Step  </th><th> Colors to use         </th></tr>
		 *        <tr><td> 1     </td><td> cycle 1 in position 1 </td></tr>
		 *        <tr><td> 2     </td><td> cycle 2 in position 1 </td></tr>
		 *        <tr><td> ...   </td><td> ...                   </td></tr>
		 *        <tr><td> m     </td><td> cycle m in position 1 </td></tr>
		 *        <tr><td> m + 1 </td><td> cycle 1 in position 2 </td></tr>
		 *        <tr><td> m + 2 </td><td> cycle 2 in position 2 </td></tr>
		 *        <tr><td> ...   </td><td> ...                   </td></tr>
		 *        <tr><td> m * n </td><td> cycle m in position n </td></tr></table>
		 * @param aName Name for GUI
		 * @param aFirstIndex First index in the palette to insert colors into.
		 * @param aColorPalette RGB colors to insert.
		 *   <ul><li> Index 0: Climate (0 = Normal, 1 = Toyland) </li>
		 *       <li> Index 1: Subsets of colors to use. (shifted fast) </li>
		 *       <li> Index 2: Colors of the subset. (shifted slowly) </li></ul>
		 * @param aInvertCounter Bitwise negate animation counter before usage. (TTD magic)
		 * @param aSpeed Speed of color shifting.
		 */
		public PaletteAnimationWaterCycle(String aName, int aFirstIndex, int[][][] aColorPalette, boolean aInvertCounter, int aSpeed)
		{
			super(aName, aFirstIndex, aColorPalette[0][0].length);
			fColorPalette = aColorPalette;
			fInvertCounter = aInvertCounter;
			fSpeed = aSpeed;
			fNumPages = fColorPalette[0].length;

			if (fColorPalette.length != 2 || fColorPalette[1].length != fNumPages) throw new IllegalArgumentException();

			for (int i = 0; i < 2; i++) {
				for (int j = 0; j < fNumPages; j++) {
					if (fColorPalette[i][j].length != colors.length) throw new IllegalArgumentException();
				}
			}
		}

		public void applyPalette(int[] aPalette, int aAnimationCounter, boolean aToyland)
		{
			if (!enabled) aAnimationCounter = 0;
			aAnimationCounter &= 0xFFFF;
			if (fInvertCounter) aAnimationCounter = ~aAnimationCounter;

			int position = (((aAnimationCounter * fSpeed) & 0xFFFF) * fNumPages * colors.length) >> 16;
			int[] color_values = fColorPalette[aToyland ? 1 : 0][position % fNumPages];
			position = position / fNumPages;

			for (int i = 0; i < colors.length; i++) {
				aPalette[colors[i]] = color_values[(position + i) % colors.length] | 0xFF000000;
			}
		}
	};

	/** Color cycle of the radio tower. */
	class PaletteAnimationRadioTower extends PaletteAnimation {
		/**
		 * RGB values to insert in the palette.
		 * See constructor for description.
		 */
		private int[] fColorPalette;

		/**
		 * The radio tower animation uses two color indices, which cycle through a blinking cycle.
		 * The second color has a phase shift of 180 degree wrt. the first color.
		 * @param aName Name for GUI
		 * @param aFirstIndex First index in the palette to insert colors into.
		 * @param aColorPalette Three RGB colors to use
		 *   <ul><li> Color 0: Bright color. </li>
		 *       <li> Color 1: Intermediate color shortly shown when switching between bright and dark color and back. </li>
		 *       <li> Color 2: Dark color.   </li></ul>
		 */
		public PaletteAnimationRadioTower(String aName, int aFirstIndex, int[] aColorPalette)
		{
			super(aName, aFirstIndex, 2);
			fColorPalette = aColorPalette;
		}

		public void applyPalette(int[] aPalette, int aAnimationCounter, boolean aToyland)
		{
			if (!enabled) aAnimationCounter = 0;
			aAnimationCounter = (aAnimationCounter >> 1) & 0x7F;

			int col = (aAnimationCounter < 0x40) ? 0 :
					(aAnimationCounter < 0x4A || aAnimationCounter >= 0x75) ? 1 : 2;
			aPalette[colors[0]] = fColorPalette[col] | 0xFF000000;

			aAnimationCounter = aAnimationCounter ^ 0x40;

			col = (aAnimationCounter < 0x40) ? 0 :
					(aAnimationCounter < 0x4A || aAnimationCounter >= 0x75) ? 1 : 2;
			aPalette[colors[1]] = fColorPalette[col] | 0xFF000000;
		}
	};

	/** Palette animations of TTD */
	public final PaletteAnimation[] palette_animations = {
		new PaletteAnimationCycle("Fizzy Drinks", 0xE3, new int[] {
			0xD49480, 0x4C1808, 0x6C2C18, 0x904834, 0xB06C54
		}, true, 512),
		new PaletteAnimationCycle("Fire Cycle", 0xE8, new int[] {
			0xFCC400, 0xFC3C00, 0xFC5400, 0xFC6C00, 0xFC7C00, 0xFC9400, 0xFCAC00
		}, true, 512),
		new PaletteAnimationCycle("Light House", 0xF1, new int[] {
			0xF0D000, 0x000000, 0x000000, 0x000000
		}, false, 256),
		new PaletteAnimationRadioTower("Radio Tower", 0xEF, new int[] {
			0xFF0000, 0x800000, 0x140000
		}),
		new PaletteAnimationWaterCycle("Dark Water", 0xF5, new int[][][] {
			{ { 0x204470, 0x244874, 0x284C78, 0x2C507C, 0x305480 } },
			{ { 0x1C6C7C, 0x207080, 0x247484, 0x287888, 0x2C7C8C } }
		}, false, 320),
		new PaletteAnimationWaterCycle("Glittery Water", 0xFA, new int[][][] {
			{ { 0xD8F4FC, 0x6484A8, 0x486490, 0x486490, 0x6484A8 },
			  { 0xACD0E0, 0x486490, 0x486490, 0x486490, 0x84ACC4 },
			  { 0x84ACC4, 0x486490, 0x486490, 0x486490, 0xACD0E0 } },
			{ { 0xD8F4FC, 0x74B4C4, 0x5CA4B8, 0x5CA4B8, 0x74B4C4 },
			  { 0xB4DCE8, 0x5CA4B8, 0x5CA4B8, 0x5CA4B8, 0x94C8D8 },
			  { 0x94C8D8, 0x5CA4B8, 0x5CA4B8, 0x5CA4B8, 0xB4DCE8 } }
		}, false, 128)
	};

	/**
	 * Toyland uses different colors in the water cycles.
	 * The transparency recolor map also recolors the water colors to different/matching colors.
	 */
	protected int fClimate = TEMPERATE;

	/** Current palette animation counter. Incremented by 8 every 30 ms unless paused. */
	protected int fAnimationCounter = 0;

	/** Timer for palette animation */
	protected Timer fAnimationTimer;

	/** Setup palette, palette animation and global recoloring. */
	public TTDPalette()
	{
		buildPalette();

		ActionListener anim_trigger = new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				fAnimationCounter = (fAnimationCounter + 8) & 0xFFFF;
				buildPalette();
			}
		};

		fAnimationTimer = new Timer(30, anim_trigger);
	}

	/**
	 * Returns the current climate setting.
	 * @return TEMPERATE, ARCTIC, TROPIC or TOYLAND
	 */
	public int getClimate()
	{
		return fClimate;
	}

	/**
	 * Sets the current climate setting.
	 * @param aClimate TEMPERATE, ARCTIC, TROPIC or TOYLAND
	 */
	public void setClimate(int aClimate)
	{
		fClimate = aClimate;
		buildPalette();
	}

	/** Start palette animation */
	public void startPaletteAnimation()
	{
		fAnimationTimer.start();
	}

	/** Pause palette animation */
	public void pausePaletteAnimation()
	{
		fAnimationTimer.stop();
	}

	/** Stop palette animation and reset the animation counter */
	public void stopPaletteAnimation()
	{
		fAnimationTimer.stop();
		fAnimationCounter = 0;
		buildPalette();
	}

	/** Build {@link #fCurrentPalette} from scratch and apply palette animation. */
	protected void buildPalette()
	{
		for (int i = 0; i < 256; i++) {
			fCurrentPalette[i] = DOS_PALETTE[i] | (i != TRANSPARENT ? 0xFF000000 : 0x00000000);
		}
		for (int i = 0; i < palette_animations.length; i++) {
			palette_animations[i].applyPalette(fCurrentPalette, fAnimationCounter, fClimate == TOYLAND);
		}
		fireChangeEvent();
	}

	/** Build the raw palette with fixed animation state */
	public Palette getUnanimatedPalette()
	{
		Palette pal = new Palette();
		for (int i = 0; i < 256; i++) {
			pal.fCurrentPalette[i] = DOS_PALETTE[i] | (i != TRANSPARENT ? 0xFF000000 : 0x00000000);
		}
		for (int i = 0; i < palette_animations.length; i++) {
			palette_animations[i].applyPalette(pal.fCurrentPalette, 0, fClimate == TOYLAND);
		}
		return pal;
	}
};
