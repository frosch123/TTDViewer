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
import java.text.*;
import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.event.*;
import javax.xml.*;
import javax.xml.parsers.*;
import javax.xml.validation.*;
import org.xml.sax.*;
import org.w3c.dom.*;

/**
 * Ah, well, the thingie on the right.
 * It displays the palette animations from TTDPalette and the recolorings from recolor.xml (validated using recolor.xsd),
 * and allows you to select stuff from it.
 *
 * TODO the mouse handling in this is really a mess.
 */
public class RecolorBrowser extends JTree {
	/** Base class for the nodes and leafs in the treeview. */
	protected static class TreeItem implements TreeNode {
		/** Text to appear in the tree. */
		public final String name;

		/** Text to appear in some tooltip. */
		public final String description;

		/** Parent node, null for root. */
		public final TreeItem parent;

		/** Climates in which the item is available. */
		public final boolean climates[] = new boolean[TTDPalette.NUM_CLIMATES];

		/** The component to draw in the treeview */
		protected JPanel fDisplay = new JPanel();

		/** The component containing the text and selection state */
		protected JComponent fText = null;

		/**
		 * Defines the effect on the main palette.
		 * @param aSeparated is set to true for the colors to hide.
		 * @return the recoloring to use, or null if identity will do.
		 */
		public Recoloring getRecoloring(boolean[] aSeparated)
		{
			return null;
		}

		/**
		 * Called when the climate changes.
		 * @param aClimate new climate
		 */
		public void setClimate(int aClimate)
		{
			boolean enabled = (aClimate >= 0 && aClimate < climates.length && climates[aClimate]);
			if (fText != null) fText.setEnabled(enabled);
		}

		/**
		 * Checks whether the item is enabled.
		 * @return true if enabled.
		 */
		public boolean isEnabled()
		{
			return fText == null || fText.isEnabled();
		}

		/**
		 * Determines whether the item is selected.
		 * @return true if selected
		 */
		public boolean isSelected()
		{
			/* If we have some checkbox/radiobutton use its state. other stuff (e.g. labels) are always selected. */
			return (fText instanceof AbstractButton) ? ((AbstractButton)fText).isSelected() : true;
		}

		/**
		 * Select the item (if it is selectable).
		 */
		public void doSelect()
		{
			if (fText instanceof AbstractButton) {
				((AbstractButton)fText).setSelected(true);
			}
		}

		/**
		 * Set up needed stuff for {@link #fText}.
		 * @param aText the component to use. If null, a JLabel using {@link #name} will be created.
		 */
		protected void setupText(JComponent aText)
		{
			if (aText == null) {
				if (fText != null) return;
				aText = new JLabel(name);
			}
			if (fText != null) {
				fDisplay.remove(fText);
				fText = null;
			}
			fText = aText;
			fText.setBackground(Color.WHITE);
			fDisplay.add(fText, BorderLayout.CENTER);

			/* dispatchEvent does not work here, no idea, see below */
			fText.addMouseListener(new MouseInputAdapter() {
				public void mouseClicked(MouseEvent e)
				{
					if (isEnabled()) doSelect();
				}
			});
		}

		/**
		 * Set up the item as radiobutton.
		 * @param aGroup The group the button to add to.
		 */
		protected void addToButtonGroup(ButtonGroup aGroup)
		{
			JRadioButton radio = new JRadioButton(name, aGroup.getButtonCount() == 0);
			aGroup.add(radio);
			setupText(radio);
		}

		/**
		 * Set up the component displaying the item.
		 */
		private void setupDisplay()
		{
			fDisplay.setBackground(Color.WHITE);
			if (description.length() > 0) fDisplay.setToolTipText(description);
			fDisplay.setLayout(new BorderLayout());
			setupText(null);
		}

		/**
		 * Create new treeitem with name and description from an XML element.
		 * @param aParent Parent item in the tree
		 * @param aElement Element to take name and description from
		 */
		protected TreeItem(TreeItem aParent, Element aElement)
		{
			parent = aParent;
			name = aElement.getAttribute("name");
			description = aElement.getAttribute("desc");

			for (int i = 0; i < climates.length; i++) {
				climates[i] = false;
			}
			String climate_list = aElement.getAttribute("climates");
			String split_climates[] = climate_list.split(" ");
			for (int i = 0; i < split_climates.length; i++) {
				if (split_climates[i].equals("temperate")) climates[TTDPalette.TEMPERATE] = true;
				if (split_climates[i].equals("arctic"))    climates[TTDPalette.ARCTIC]    = true;
				if (split_climates[i].equals("tropic"))    climates[TTDPalette.TROPIC]    = true;
				if (split_climates[i].equals("toyland"))   climates[TTDPalette.TOYLAND]   = true;
			}

			setupDisplay();
		}

		/**
		 * Create new treeitem.
		 * @param aParent Parent item in the tree
		 * @param aName Name to use
		 */
		protected TreeItem(TreeItem aParent, String aName)
		{
			parent = aParent;
			name = aName;
			description = "";
			for (int i = 0; i < climates.length; i++) {
				climates[i] = true;
			}
			setupDisplay();
		}

		/** Get the component to display in the treeview. */
		public JPanel getDisplay()
		{
			return fDisplay;
		}

		public Enumeration children()
		{
			return null;
		}

		public boolean getAllowsChildren()
		{
			return false;
		}

		public TreeNode getChildAt(int childIndex)
		{
			return null;
		}

		public int getChildCount()
		{
			return 0;
		}

		public int getIndex(TreeNode node)
		{
			return -1;
		}

		public TreeNode getParent()
		{
			return parent;
		}

		public boolean isLeaf()
		{
			return true;
		}
	}

	/** Leaf node representing a recoloring */
	static class RecolorItem extends TreeItem {
		/** Color indexes to separate from the main palette. */
		public int fSeparate[];

		/** Recoloring of the item */
		public Recoloring fRecoloring;

		/** Sprite number of the recolor sprite; -1 if none */
		public int fSprite = -1;

		/**
		 * Get the recoloring to the item.
		 * @param aSeparated separated color indices are set to 'true' in the array; other indices stay unmodified.
		 * @return recoloring of the item
		 */
		public Recoloring getRecoloring(boolean[] aSeparated)
		{
			for (int i = 0; i < fSeparate.length; i++) {
				aSeparated[fSeparate[i]] = true;
			}
			return fRecoloring;
		}

		/**
		 * Parse a space-separated list of numbers.
		 * @param aList list of numbers
		 * @param aRadix Base of the numbers (decimal, hexadecimal, ...)
		 * @return array containing the numbers
		 */
		private int[] readNumberList(String aList, int aRadix)
		{
			String[] split_indices = aList.split(" ");
			int[] indices = new int[split_indices.length];
			for (int i = 0; i < split_indices.length; i++) {
				indices[i] = Integer.parseInt(split_indices[i], aRadix);
			}
			return indices;
		}

		/**
		 * Construct a new RecolorItem by reading its content from an Element.
		 * @param aParent Parent item in the tree
		 * @param aPalette Palette to use for the PalettePicker
		 * @param aElement Element to use data from
		 */
		public RecolorItem(TreeItem aParent, Palette aPalette, Element aElement)
		{
			super(aParent, aElement);

			/* Read 'sprite' */
			String sprite = aElement.getAttribute("sprite");
			if (sprite.length() > 0) {
				fSprite = Integer.parseInt(sprite);
				fDisplay.setToolTipText(description + (description.length() == 0 ? "(" : " (")
					+ Integer.toString(fSprite) + "/0x" + Integer.toHexString(fSprite) + ")");
			}

			/* Read 'indices' */
			int[] indices;
			String all_indices = aElement.getAttribute("indices");
			if (all_indices.equals("none")) {
				indices = new int[0];
			} else if (all_indices.equals("all")) {
				indices = new int[256];
				for (int i = 0; i < 256; i++) {
					indices[i] = i;
				}
			} else {
				indices = readNumberList(all_indices, 16);
			}

			/* Read 'separateable' */
			String separateable = aElement.getAttribute("separateable");
			if (separateable.equals("none")) {
				fSeparate = new int[0];
			} else if (separateable.equals("all")) {
				fSeparate = indices;
			} else {
				fSeparate = readNumberList(separateable, 16);
			}

			/* Read content */
			String content = new String();
			for (Node node = aElement.getFirstChild(); node != null; node = node.getNextSibling()) {
				if (node.getNodeType() == Node.TEXT_NODE) {
					content += node.getNodeValue();
				}
			}

			String[] split_content;
			if (content.length() > 0) {
				split_content = content.split(" ");
			} else {
				split_content = new String[0];
			}
			if (split_content.length != indices.length) {
				System.out.println("<recolor> " + name + ": Length of 'indices' (" + String.valueOf(indices.length)
						+ ") and 'content' (" + String.valueOf(split_content.length) + ") do not match.");
				System.exit(1);
			}

			/* Set up recoloring */
			int[] remap = new int[256];
			for (int i = 0; i < 256; i++) {
				remap[i] = i;
			}

			for (int i = 0; i < indices.length; i++)
			{
				if (!split_content[i].equals("__")) {
					remap[indices[i]] = Integer.parseInt(split_content[i], 16);
				}
			}

			fRecoloring = new Recoloring(remap);

			/* Set up palette picker, if colors get separated */
			if (fSeparate.length > 0) {
				PalettePicker picker = new PalettePicker(aPalette, fRecoloring, fSeparate);
				fDisplay.add(picker, BorderLayout.EAST);
			}
		}
	}

	/** Leaf node representing a palette animation cycle, including a CheckBox to enable/disable it. */
	static class CycleItem extends TreeItem {
		/** PaletteAnimation of the item */
		public PaletteAnimation fCycle;

		/** Handler of the CheckBox */
		public void doSelect()
		{
			JCheckBox checkbox = (JCheckBox)fText;
			checkbox.setSelected(!checkbox.isSelected());
			fCycle.enabled = checkbox.isSelected();
		}

		/** Set up the checkbox */
		protected void setupText(JComponent aText)
		{
			super.setupText(new JCheckBox(name, true));
		}

		/**
		 * Create a new CycleItem.
		 * @param aParent Parent item in the tree
		 * @param aPalette Palette to use for the PalettePicker
		 * @param aCycle PaletteAnimation to represent
		 */
		public CycleItem(TreeItem aParent, Palette aPalette, PaletteAnimation aCycle)
		{
			super(aParent, aCycle.name);
			fCycle = aCycle;
			PalettePicker picker = new PalettePicker(aPalette, aPalette.global_recoloring, aCycle.colors);
			fDisplay.add(picker, BorderLayout.EAST);
		}
	}

	/** Basic branch item in the tree */
	static class BranchItem extends TreeItem {
		/** Children */
		protected Vector fSubItems = new Vector();

		public void setClimate(int aClimate)
		{
			super.setClimate(aClimate);
			for (int i = 0; i < fSubItems.size(); i++) {
				TreeItem sub = (TreeItem)fSubItems.get(i);
				sub.setClimate(aClimate);
			}
		}

		/**
		 * Recursively merge selected child recolorings.
		 * @param aSeparated separated color indices are set to 'true' in the array; other indices stay unmodified.
		 * @return recoloring of the item
		 */
		public Recoloring getRecoloring(boolean[] aSeparated)
		{
			Recoloring recolor = null;
			for (int i = 0; i < fSubItems.size(); i++) {
				TreeItem sub = (TreeItem)fSubItems.get(i);
				Recoloring sub_recolor = sub.getRecoloring(aSeparated);
				if (sub_recolor != null) {
					if (recolor == null) {
						recolor = sub_recolor;
					} else {
						recolor = new Recoloring(new Recoloring[] {recolor, sub_recolor});
					}
				}
			}
			return recolor;
		}

		/** Append child item */
		public void append(TreeItem aItem)
		{
			fSubItems.add(aItem);
		}

		/**
		 * Recursively construct a matching item to an Element and append it as child.
		 * @param aPalette Palette to use for potential PalettePickers.
		 * @param aElement Element to read data from
		 */
		public void append(Palette aPalette, Element aElement)
		{
			String tag = aElement.getTagName();
			if (tag.equals("recolor")) {
				append(new RecolorItem(this, aPalette, aElement));
			} else if (tag.equals("choice")) {
				append(new ChoiceItem(this, aPalette, aElement));
			} else if (tag.equals("sequence")) {
				append(new SequenceItem(this, aPalette, aElement));
			}
		}

		/**
		 * Construct matching items for all child Elements and append them as chilren.
		 * @param aPalette Palette to use for potential PalettePickers.
		 * @param aElement Element to read child elements from
		 */
		public void appendChildren(Palette aPalette, Node aElement)
		{
			for (Node node = aElement.getFirstChild(); node != null; node = node.getNextSibling()) {
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					append(aPalette, (Element)node);
				}
			}
		}

		/**
		 * Parse XML file, and append root elements as children.
		 * The XML file is validated using 'recolor.xsd'.
		 * @param aPalette Palette to use for potential PalettePickers.
		 * @param aFileName File to read
		 */
		public void appendFile(Palette aPalette, String aFileName)
		{
			try {
				SchemaFactory schema_factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				factory.setIgnoringComments(true);
				factory.setNamespaceAware(false);
				factory.setCoalescing(true);
				factory.setSchema(schema_factory.newSchema(getClass().getResource("recolor.xsd")));
				DocumentBuilder builder = factory.newDocumentBuilder();
				Document document = builder.parse(getClass().getResource(aFileName).openStream());
				appendChildren(aPalette, document);
			} catch (Exception e) {
				System.out.println(aFileName + " is invalid: " + e.toString());
				System.exit(1);
			}
		}

		/**
		 * Construct a simple branch item
		 * @param aParent Parent item in the tree
		 * @param aName Name to use
		 */
		public BranchItem(TreeItem aParent, String aName)
		{
			super(aParent, aName);
		}

		/**
		 * Construct a simple branch item
		 * @param aParent Parent item in the tree
		 * @param aElement Element to take name and description from
		 */
		public BranchItem(TreeItem aParent, Element aElement)
		{
			super(aParent, aElement);
		}

		public Enumeration children()
		{
			return fSubItems.elements();
		}

		public boolean getAllowsChildren()
		{
			return true;
		}

		public TreeNode getChildAt(int childIndex)
		{
			return (TreeItem)fSubItems.get(childIndex);
		}

		public int getChildCount()
		{
			return fSubItems.size();
		}

		public int getIndex(TreeNode node)
		{
			return fSubItems.indexOf(node);
		}

		public boolean isLeaf()
		{
			return false;
		}
	}

	/** Branch item witch exactly one child item selected at a time */
	static class ChoiceItem extends BranchItem {
		/** ButtonGroup of the child items */
		protected ButtonGroup fButtonGroup = new ButtonGroup();

		public void setClimate(int aClimate)
		{
			super.setClimate(aClimate);
			if (isEnabled()) {
				TreeItem first_enabled = null;
				for (int i = 0; i < fSubItems.size(); i++) {
					TreeItem sub = (TreeItem)fSubItems.get(i);
					if (!sub.isEnabled()) continue;
					if (first_enabled == null) first_enabled = sub;
					if (sub.isSelected()) return;
				}
				if (first_enabled != null) first_enabled.doSelect();
			}
		}

		public Recoloring getRecoloring(boolean[] aSeparated)
		{
			Recoloring recolor = null;
			for (int i = 0; i < fSubItems.size(); i++) {
				TreeItem sub = (TreeItem)fSubItems.get(i);
				if (!sub.isEnabled() || !sub.isSelected()) continue;
				Recoloring sub_recolor = sub.getRecoloring(aSeparated);
				if (sub_recolor != null) {
					if (recolor == null) {
						recolor = sub_recolor;
					} else {
						recolor = new Recoloring(new Recoloring[] {recolor, sub_recolor});
					}
				}
			}
			return recolor;
		}

		public void append(TreeItem aItem)
		{
			super.append(aItem);
			aItem.addToButtonGroup(fButtonGroup);
		}

		/**
		 * Construct branch item using a radiobuttons for the child items
		 * @param aParent Parent item in the tree
		 * @param aName Name to use
		 */
		public ChoiceItem(TreeItem aParent, String aName)
		{
			super(aParent, aName);
		}

		/**
		 * Construct branch item using a radiobuttons for the child items of an Element.
		 * The child items are recursively constructed from the child elements.
		 * @param aParent Parent item in the tree
		 * @param aPalette Palette to use for potential PalettePickers.
		 * @param aElement Element to take name and description from
		 */
		public ChoiceItem(TreeItem aParent, Palette aPalette, Element aElement)
		{
			super(aParent, aElement);
			appendChildren(aPalette, aElement);
		}
	}

	/** Branch item with all child items selected all the time */
	static class SequenceItem extends BranchItem {
		/**
		 * Construct branch item using a radiobuttons for the child items
		 * @param aParent Parent item in the tree
		 * @param aName Name to use
		 */
		public SequenceItem(TreeItem aParent, String aName)
		{
			super(aParent, aName);
		}

		/**
		 * Construct branch item using a the child items of an Element.
		 * The child items are recursively constructed from the child elements.
		 * @param aParent Parent item in the tree
		 * @param aPalette Palette to use for potential PalettePickers.
		 * @param aElement Element to take name and description from
		 */
		public SequenceItem(TreeItem aParent, Palette aPalette, Element aElement)
		{
			super(aParent, aElement);
			appendChildren(aPalette, aElement);
		}

		/**
		 * Construct branch item containing PaletteAnimations from an array.
		 * @param aParent Parent item in the tree
		 * @param aName Name to use
		 * @param aPalette Palette to use for PalettePickers.
		 * @param aCycles PaletteAnimations to add
		 */
		public SequenceItem(TreeItem aParent, String aName, Palette aPalette, PaletteAnimation[] aCycles)
		{
			super(aParent, aName);
			for (int i = 0; i < aCycles.length; i++) {
				append(new CycleItem(this, aPalette, aCycles[i]));
			}
		}
	}

	/** Root node of the tree */
	private TreeItem fRoot;

	/** The palette to operate on */
	protected TTDPalette fPalette;

	/** Separated colors in currently selected recolorings */
	public boolean[] fSeparated = new boolean[256];

	/** Merged recoloring of currently selected items */
	public Recoloring fRecoloring;

	/** Listeners to notify, when selection changes. */
	private DefaultChangeEventTrigger fChangeEventListeres = new DefaultChangeEventTrigger();

	/** The listener will be notified on changes to the selected recolorings. */
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

	/** Merge selected recolorings, construct {@link #fSeparated} and {@link #fRecoloring}, and fire the event */
	protected void rebuildRecoloring()
	{
		for (int i = 0; i < 256; i++) {
			fSeparated[i] = false;
		}
		fRecoloring = fRoot.getRecoloring(fSeparated);
		if (fRecoloring == null) fRecoloring = new Recoloring();
		fireChangeEvent();
	}

	/**
	 * Main constructor.
	 * @param aPalette Palette used in PalettePickers. Only used for updating the display.
	 * @param aRoot Root node to use
	 * @see #createDefaultBrowser
	 */
	public RecolorBrowser(TTDPalette aPalette, TreeItem aRoot)
	{
		super(aRoot);
		fRoot = aRoot;
		fPalette = aPalette;
		fRoot.setClimate(fPalette.getClimate());

		/* Redraw the browser, when the palette changes */
		aPalette.addChangeListener(new ChangeListener() {
			int fLastClimate = fPalette.getClimate();

			public void stateChanged(ChangeEvent e)
			{
				int climate = fPalette.getClimate();
				if (climate != fLastClimate) {
					fLastClimate = climate;
					fRoot.setClimate(climate);
					rebuildRecoloring();
				}
				repaint();
			}
		});
		rebuildRecoloring();

		/* Register item painter */
		setCellRenderer(new TreeCellRenderer() {
			public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus)
			{
				TreeItem item = (TreeItem)value;
				Component display = item.getDisplay();
				display.setPreferredSize(new Dimension(280, 16)); // FIXME what a hack
				display.setSize(new Dimension(280, 16));
				return display;
			}
		});

		/* Register mouse handler */
		addMouseListener(new MouseInputAdapter() {
			public void mouseClicked(MouseEvent e)
			{
				TreePath path = getSelectionPath();
				if (path != null) {
					Rectangle bounds = getPathBounds(path);
					e.translatePoint(-bounds.x, -bounds.y);

					TreeItem item = (TreeItem)(path.getLastPathComponent());
					Component display = item.getDisplay();
					display.setSize(new Dimension(280, 16));

					/* dispatchEvent just does not work; only the mouselisteneres in the directly targeted class work */
					while (display != null) {
						Component next = display.getComponentAt(e.getPoint());
						if (next == display) {
							display.dispatchEvent(e);
							break;
						}
						display = next;
						if (display != null) e.translatePoint(-next.getX(), -next.getY());
					}

					rebuildRecoloring();
				}
			}
		});

		// Note: We assume keyboard support would be unusable anyway :) */

		setRootVisible(false);
		setBackground(Color.WHITE);
		ToolTipManager.sharedInstance().registerComponent(this);
	}

	/**
	 * Creates the main recolor browser containing all palette animations and the recolorings from 'recolor.xml'.
	 * @param aPalette Palette for the PalettePickers and PaletteAnimations.
	 */
	static public RecolorBrowser createDefaultBrowser(TTDPalette aPalette)
	{
		SequenceItem aRoot = new SequenceItem(null, "");
		aRoot.append(new SequenceItem(aRoot, "Palette Animation", aPalette, aPalette.palette_animations));
		aRoot.appendFile(aPalette, "recolor.xml");
		RecolorBrowser browser = new RecolorBrowser(aPalette, aRoot);
		for (int i = browser.getRowCount() - 1; i >= 0; i--) {
			browser.expandRow(i);
		}
		return browser;
	}
}
