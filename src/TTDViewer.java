/*
 * This file is part of TTDViewer.
 * TTDViewer is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, version 2.
 * TTDViewer is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received a copy of the GNU General Public License along with TTDViewer. If not, see <http://www.gnu.org/licenses/>.
 */

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

/** Main class containing the GUI stuff and wiring the events between the important parts. */
public class TTDViewer extends JFrame {

	/** Version string. */
	public static final String fVersion;

	static {
		String version = "unknown version";
		try {
			version = (new BufferedReader(new InputStreamReader(TTDViewer.class.getResourceAsStream("rev.txt")))).readLine();
		} catch (Exception e) {}
		fVersion = version;
	}

	public static void main(String[] Args)
	{
		new TTDViewer();
	}

	public TTDPalette fPalette = new TTDPalette();

	protected TTDDisplay fImage;
	protected RecolorBrowser fRecolorBrowser;
	protected PalettePicker fMainPalette;

	/** Reloads the image if it is changed on disk. */
	protected class AutoReloader implements ChangeListener {
		File fFile = null;

		/** Switch the file to monitor. */
		public void changeFile(File aFile)
		{
			FileMonitor.removeChangeListener(this);
			fFile = aFile;
			FileMonitor.addChangeListener(fFile, this);
		}

		public void stateChanged(ChangeEvent e)
		{
			/* Try to reload the file.
			 * Do nothing if the file is invalid or got removed, but wait until it is valid again. */
			try {
				fImage.loadFrom(fFile);
			} catch (Exception error) {};
		}
	};

	protected AutoReloader fAutoReloader = new AutoReloader();

	private JCheckBoxMenuItem fSepAnimation;
	private JCheckBoxMenuItem fSepPink;
	private JCheckBoxMenuItem fSepRecolored;
	private JCheckBoxMenuItem fSepPureWhite;
	private JRadioButtonMenuItem fToyland;

	private JFileChooser fFileChooser;
	private JLabel fZoomLevel;
	private JLabel fFileName;

	/** Transfer settings from buttons to the backend */
	protected void rebuildMainPalette()
	{
		fPalette.setClimate(fToyland.isSelected() ? TTDPalette.TOYLAND : TTDPalette.TEMPERATE);
		fPalette.setGlobalRecoloring(fRecolorBrowser.fRecoloring);

		boolean sep = fSepRecolored.isSelected();
		for (int i = 0; i < 256; i++) {
			fMainPalette.hide_color[i] = sep && fRecolorBrowser.fSeparated[i];
		}

		if (fSepPink.isSelected()) {
			for (int i = 0; i < TTDPalette.MAGIC_PINK.length; i++) {
				fMainPalette.hide_color[TTDPalette.MAGIC_PINK[i]] = true;
			}
		}

		if (fSepPureWhite.isSelected()) {
			fMainPalette.hide_color[TTDPalette.PURE_WHITE] = true;
		}

		if (fSepAnimation.isSelected()) {
			for (int j = 0; j < fPalette.palette_animations.length; j++) {
				int[] separate = fPalette.palette_animations[j].colors;
				for (int i = 0; i < separate.length; i++) {
					fMainPalette.hide_color[separate[i]] = true;
				}
			}
		}

		fMainPalette.repaint();
	}

	/** Construct GUI. Naturally a mess. */
	public TTDViewer()
	{
		super("TTDViewer [" + fVersion + "]");
		setDefaultCloseOperation(EXIT_ON_CLOSE);

		int[][] main_pal = new int[16][16];
		for (int i = 0; i < 16; i++) {
			for (int j = 0; j < 16; j++) {
				main_pal[i][j] = i * 16 + j;
			}
		}

		fImage = new TTDDisplay(fPalette);
		fMainPalette = new PalettePicker(fPalette, fPalette.global_recoloring, main_pal);

		/* TODO Make this Play/Pause/Stop ? */
		JCheckBox palette_anim = new JCheckBox("Palette Animation", true);
		palette_anim.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e)
			{
				JCheckBox source = (JCheckBox)(e.getSource());
				if (source.isSelected()) {
					fPalette.startPaletteAnimation();
				} else {
					fPalette.pausePaletteAnimation();
				}
			}
		});

		ChangeListener rebuild_main_palette = new ChangeListener() {
			public void stateChanged(ChangeEvent e)
			{
				rebuildMainPalette();
			}
		};

		JMenuBar color_menubar = new JMenuBar();

		JMenu climate_menu = new JMenu("Climate");
		color_menubar.add(climate_menu);

		ButtonGroup climate_group = new ButtonGroup();

		JRadioButtonMenuItem normal = new JRadioButtonMenuItem("Normal", true);
		normal.addChangeListener(rebuild_main_palette);
		climate_group.add(normal);
		climate_menu.add(normal);

		fToyland = new JRadioButtonMenuItem("Toyland");
		fToyland.addChangeListener(rebuild_main_palette);
		climate_group.add(fToyland);
		climate_menu.add(fToyland);

		JMenu sep_menu = new JMenu("Filter Palette");
		color_menubar.add(sep_menu);

		fSepAnimation = new JCheckBoxMenuItem("Hide Animation Colors", true);
		fSepAnimation.addChangeListener(rebuild_main_palette);
		sep_menu.add(fSepAnimation);

		fSepPink = new JCheckBoxMenuItem("Hide Stupid Pink", true);
		fSepPink.addChangeListener(rebuild_main_palette);
		sep_menu.add(fSepPink);

		fSepPureWhite = new JCheckBoxMenuItem("Hide Pure White", true);
		fSepPureWhite.addChangeListener(rebuild_main_palette);
		sep_menu.add(fSepPureWhite);

		fSepRecolored = new JCheckBoxMenuItem("Hide Recolored Colors", true);
		fSepRecolored.addChangeListener(rebuild_main_palette);
		sep_menu.add(fSepRecolored);

		fRecolorBrowser = RecolorBrowser.createDefaultBrowser(fPalette);
		fRecolorBrowser.addChangeListener(rebuild_main_palette);

		JScrollPane browser_scroll_pane = new JScrollPane(fRecolorBrowser, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		browser_scroll_pane.setPreferredSize(new Dimension(360, 500));

		JPanel main_tools = new JPanel();
		main_tools.setLayout(new GridLayout(1, 1));
		main_tools.add(palette_anim);

		JPanel top_panel = new JPanel();
		top_panel.setBackground(Color.WHITE);
		top_panel.setLayout(new BorderLayout());
		top_panel.add(color_menubar, BorderLayout.NORTH);
		top_panel.add(fMainPalette, BorderLayout.CENTER);
		top_panel.add(main_tools, BorderLayout.SOUTH);

		final JScrollPane image_scroll_pane = new JScrollPane(fImage, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);

		JPanel tool_panel = new JPanel();
		tool_panel.setBackground(Color.WHITE);
		tool_panel.setLayout(new BorderLayout());
		tool_panel.add(browser_scroll_pane, BorderLayout.CENTER);
		tool_panel.add(top_panel, BorderLayout.NORTH);

		fFileChooser = new JFileChooser();
		fFileChooser.setCurrentDirectory(new File("."));
		fFileName = new JLabel("");
		/* TODO FileFilter, Preview? */

		JButton load_button = new JButton("open file");
		load_button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				int returnVal = fFileChooser.showOpenDialog(TTDViewer.this);

				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File file = fFileChooser.getSelectedFile();
					try {
						fImage.loadFrom(file);
						fAutoReloader.changeFile(file);
						fFileName.setText(file.getName());
					} catch (Exception error) {
						JOptionPane.showMessageDialog(TTDViewer.this, error.getMessage(), "Opening image failed", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		});

		fZoomLevel = new JLabel("1x");

		final JButton zoom_in_button = new JButton("zoom in");
		final JButton zoom_out_button = new JButton("zoom out");

		ActionListener zoom_listener = new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				JViewport viewport = image_scroll_pane.getViewport();
				Dimension extent = viewport.getExtentSize();

				Point pos = viewport.getViewPosition();
				pos.x = (pos.x + extent.width / 2) / fImage.getZoom();
				pos.y = (pos.y + extent.height / 2) / fImage.getZoom();

				fImage.setZoom(fImage.getZoom() + (e.getSource() == zoom_in_button ? 1 : -1));
				fZoomLevel.setText(fImage.getZoom() + "x");

				pos.x = pos.x * fImage.getZoom() - extent.width / 2;
				pos.y = pos.y * fImage.getZoom() - extent.height / 2;
				viewport.setViewPosition(pos);
			}
		};
		zoom_in_button.addActionListener(zoom_listener);
		zoom_out_button.addActionListener(zoom_listener);

		JPanel menu_panel = new JPanel();
		menu_panel.setLayout(new BoxLayout(menu_panel, BoxLayout.X_AXIS));
		menu_panel.add(load_button);
		menu_panel.add(fFileName);
		menu_panel.add(Box.createHorizontalGlue());
		menu_panel.add(fZoomLevel);
		menu_panel.add(zoom_in_button);
		menu_panel.add(zoom_out_button);

		JPanel main_panel = new JPanel();
		main_panel.setBackground(Color.WHITE);
		main_panel.setLayout(new BorderLayout());
		main_panel.add(image_scroll_pane, BorderLayout.CENTER);
		main_panel.add(menu_panel, BorderLayout.NORTH);

		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(main_panel, BorderLayout.CENTER);
		getContentPane().add(tool_panel, BorderLayout.EAST);

		setSize(1000, 700);

		fPalette.startPaletteAnimation();
		rebuildMainPalette();

		setVisible(true);
	}
}
