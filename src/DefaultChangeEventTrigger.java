/*
 * This file is part of TTDViewer.
 * TTDViewer is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, version 2.
 * TTDViewer is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received a copy of the GNU General Public License along with TTDViewer. If not, see <http://www.gnu.org/licenses/>.
 */

import java.awt.*;
import javax.swing.event.*;

/** Class for managing ChangeListeners and firing them. */
public class DefaultChangeEventTrigger {
	/** Objects to notify. */
	protected EventListenerList fChangeEventListeners = new EventListenerList();

	public void addChangeListener(ChangeListener l)
	{
		fChangeEventListeners.add(ChangeListener.class, l);
	}

	public void removeChangeListener(ChangeListener l)
	{
		fChangeEventListeners.remove(ChangeListener.class, l);
	}

	protected void fireChangeEvent()
	{
		ChangeEvent event = new ChangeEvent(this);
		Object[] listeners = fChangeEventListeners.getListenerList();
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == ChangeListener.class) {
				((ChangeListener)listeners[i + 1]).stateChanged(event);
			}
		}
	}
}
