/*
 * This file is part of TTDViewer.
 * TTDViewer is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, version 2.
 * TTDViewer is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received a copy of the GNU General Public License along with TTDViewer. If not, see <http://www.gnu.org/licenses/>.
 */

import java.awt.*;
import javax.swing.*;

public class MultiLineToolTip extends JToolTip {
	String[] lines = null;

	public void setTipText(String tipText)
	{
		lines = tipText.split("\n");
		Insets insets = getInsets();
		Font font = getFont();
		FontMetrics fm = getFontMetrics(font);

		int h = lines.length * fm.getHeight();
		int w = 0;
		for (int i = 0; i < lines.length; i++) {
			int lw = fm.stringWidth(lines[i]);
			if (lw > w) w = lw;
		}
		setPreferredSize(new Dimension(insets.left + insets.right + w + 6, insets.top + insets.bottom + h));
	}

	public void paint(Graphics g)
	{
		super.paint(g);

		Insets insets = getInsets();
		Font font = getFont();
		FontMetrics fm = getFontMetrics(font);

		int y = fm.getAscent() + insets.top;
		for (int i = 0; i < lines.length; i++) {
			g.drawString(lines[i], insets.left + 3, y);
			y += fm.getHeight();
		}
	}
};
