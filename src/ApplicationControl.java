/*
 * This file is part of TTDViewer.
 * TTDViewer is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, version 2.
 * TTDViewer is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received a copy of the GNU General Public License along with TTDViewer. If not, see <http://www.gnu.org/licenses/>.
 */

import java.awt.*;
import java.awt.event.*;
import java.util.*;

/**
 * Class to keep track of open window instances.
 * When the last window is closed the application is terminated.
 */
public class ApplicationControl {

	private static HashSet fWindows = new HashSet();

	/**
	 * Deregister a window.
	 * When the last window is deregistered, the application is closed.
	 */
	protected static void removeWindow(Window aWindow)
	{
		fWindows.remove(aWindow);
		if (fWindows.isEmpty()) {
			System.exit(0);
		}
	}

	/**
	 * Register a newly opened window.
	 * The window is deregistered automatically on closure.
	 */
	public static void addWindow(Window aWindow)
	{
		if (fWindows.add(aWindow)) {
			aWindow.addWindowListener(new WindowAdapter() {
				@Override public void windowClosed(WindowEvent e)
				{
					removeWindow(e.getWindow());
				}
			});
		}
	}
}
