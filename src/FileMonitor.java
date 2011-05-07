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
import javax.swing.*;
import javax.swing.event.*;

/**
 * Monitors files and notifies on modification/removal.
 * Implemented by polling the files every second.
 */
public class FileMonitor {
	/** Monitors a single file and maintains a list of listeners to notify. */
	public static class MonitorItem extends DefaultChangeEventTrigger {
		/** File to monitor */
		private File fFile;

		/** Last modification time of the file. */
		private long fLastModified;

		/**
		 * Main constructor.
		 * @param aFile File to monitor.
		 */
		public MonitorItem(File aFile)
		{
			fFile = aFile;
			fLastModified = aFile.lastModified();
		}

		/**
		 * Tests whether there are no listeners at all.
		 * @return true if there are no listeners registered.
		 */
		public boolean empty()
		{
			return fChangeEventListeners.getListenerCount() == 0;
		}

		/**
		 * Check last modification time of the file against the value of the previous call, and
		 * nofity all listeners if it changed.
		 */
		public void onTimer()
		{
			long last_modified = fFile.exists() ? fFile.lastModified() : 0;
			if (last_modified != fLastModified)
			{
				fLastModified = last_modified;
				fireChangeEvent();
			}
		}
	};

	/** Container for all monitored files. */
	protected static HashMap fItems = new HashMap();

	/** Timer checking all monitored files for modification. */
	protected static javax.swing.Timer fTimer = new javax.swing.Timer(1000, new ActionListener() {
		@Override public void actionPerformed(ActionEvent evt)
		{
			Iterator it = fItems.values().iterator();
			while (it.hasNext()) {
				((MonitorItem)it.next()).onTimer();
			}
		};
	});

	/**
	 * Register a listener to be notified on file changes.
	 * @param aFile File to monitor
	 * @param l Listener to notify.
	 */
	public static void addChangeListener(File aFile, ChangeListener l)
	{
		/* Lazily start the timer, we cannot do this statically. */
		fTimer.start();

		MonitorItem item = (MonitorItem)fItems.get(aFile);
		if (item == null) {
			item = new MonitorItem(aFile);
			fItems.put(aFile, item);
		}
		item.addChangeListener(l);
	}

	/**
	 * Unregister a listener from being notified on changes of a specific file.
	 * @param aFile File to monitor
	 * @param l Listener to unregister.
	 */
	public static void removeChangeListener(File aFile, ChangeListener l)
	{
		MonitorItem item = (MonitorItem)fItems.get(aFile);
		if (item != null) {
			item.removeChangeListener(l);
			if (item.empty()) {
				fItems.remove(aFile);
			}
		}
	}

	/**
	 * Unregister a listener from being notified on file changes.
	 * @param l Listener to unregister.
	 */
	public static void removeChangeListener(ChangeListener l)
	{
		Iterator it = fItems.values().iterator();
		while (it.hasNext()) {
			MonitorItem item = (MonitorItem)it.next();
			item.removeChangeListener(l);
			if (item.empty()) it.remove();
		}
	}
}
