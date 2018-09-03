package com.asofterspace.cdm.scriptEditor;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.FlowLayout;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.KeyStroke;

import com.asofterspace.cdm.CdmCtrl;
import com.asofterspace.cdm.CdmScript;
import com.asofterspace.toolbox.codeeditor.GroovyCode;
import com.asofterspace.toolbox.configuration.ConfigFile;
import com.asofterspace.toolbox.io.Directory;
import com.asofterspace.toolbox.utils.Callback;
import com.asofterspace.toolbox.web.JSON;


public class GUI implements Runnable {

	private JFrame mainWindow;
	
	private JPanel mainPanelRight;
	private JRadioButtonMenuItem lightScheme;
	private JRadioButtonMenuItem darkScheme;
	private int currentFontSize = 15;
	
	private final static String CONFIG_KEY_LAST_DIRECTORY = "lastDirectory";
	private final static String CONFIG_KEY_EDITOR_SCHEME = "editorScheme";
	private final static String CONFIG_KEY_EDITOR_FONT_SIZE = "editorFontSize";
	private final static String CONFIG_VAL_SCHEME_LIGHT = "groovyLight";
	private final static String CONFIG_VAL_SCHEME_DARK = "groovyDark";
	
	private List<ScriptTab> scriptTabs;
	
	private ConfigFile configuration;
	
	private JList<String> scriptListComponent;
	private String[] strScripts;
	
	
	public GUI(ConfigFile config) {
		configuration = config;
	}
	
	@Override
	public void run() {
		
		createGUI();
		
		configureGUI();
		
		showGUI();
		
		openCdmDirectory();
	}

	private void createGUI() {

		JFrame.setDefaultLookAndFeelDecorated(false);
		
		// Create the window
		mainWindow = new JFrame(Main.PROGRAM_TITLE);

		// Add content to the window
		createMenu(mainWindow);
		createMainPanel(mainWindow);
		
		// Stage everything to be shown
		mainWindow.pack();
		
        mainWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Actually display the whole jazz
        mainWindow.setVisible(true);
		
		// Maximize the window
		GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
		Rectangle bounds = env.getMaximumWindowBounds();
		mainWindow.setMaximizedBounds(bounds);
		mainWindow.setSize((int) bounds.getWidth(), (int) bounds.getHeight());
		mainWindow.setPreferredSize(new Dimension((int) bounds.getWidth(), (int) bounds.getHeight()));
		// This should actually maximize the window, but for some reason does not work (reliably),
		// so instead we do it manually in the lines above...
		// mainWindow.setExtendedState(mainWindow.getExtendedState() | JFrame.MAXIMIZED_BOTH);
		
		// Center the window
        mainWindow.setLocationRelativeTo(null);
	}
	
	private JMenuBar createMenu(JFrame parent) {
	
		JMenuBar menu = new JMenuBar();
		
		JMenu file = new JMenu("File");
		menu.add(file);
		JMenuItem openCdm = new JMenuItem("Open CDM Folder");
		openCdm.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
		openCdm.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				openCdmDirectory();
			}
		});
		file.add(openCdm);
		JMenuItem saveCdm = new JMenuItem("Save CDM");
		saveCdm.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
		saveCdm.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				for (ScriptTab scriptTab : scriptTabs) {
					scriptTab.save();
				}
				for (int i = 0; i < strScripts.length; i++) {
					if (strScripts[i].endsWith(" *")) {
						strScripts[i] = strScripts[i].substring(0, strScripts[i].length() - 2);
					}
					scriptListComponent.setListData(strScripts);
				}
				JOptionPane.showMessageDialog(new JFrame(), "The currently opened CDM files have been saved!", "CDM Saved", JOptionPane.INFORMATION_MESSAGE);
			}
		});
		file.add(saveCdm);
		JMenuItem saveCdmAs = new JMenuItem("Save CDM As...");
		saveCdmAs.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.ALT_MASK | ActionEvent.CTRL_MASK));
		saveCdmAs.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(new JFrame(), "Sorry, I am not yet working...", "Sorry", JOptionPane.ERROR_MESSAGE);
			}
		});
		file.add(saveCdmAs);
		file.addSeparator();
		JMenuItem addScriptFile = new JMenuItem("Add New Script File");
		addScriptFile.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(new JFrame(), "Sorry, I am not yet working...", "Sorry", JOptionPane.ERROR_MESSAGE);
			}
		});
		file.add(addScriptFile);
		JMenuItem renameCurScriptFile = new JMenuItem("Rename Current Script File");
		renameCurScriptFile.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
			
				// figure out which script tab is currently open (show error if none is open)
				// TODO

				// open a dialog in which the new name is to be entered (pre-filled with the current name)
				// TODO
				
				String newFilename = "test";
				
				// tell the currently opened script tab to tell the cdmscript to tell the cdmfile to change the script name
				// TODO
				
				// apply changed marker on the left hand side
				// TODO
				
				JOptionPane.showMessageDialog(new JFrame(), "Sorry, I am not yet working...", "Sorry", JOptionPane.ERROR_MESSAGE);
			}
		});
		file.add(renameCurScriptFile);
		JMenuItem showCurScriptFileInfo = new JMenuItem("Show Current Script File Info");
		showCurScriptFileInfo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
			
				// figure out which script tab is currently open (show error if none is open)
				// TODO

				// show some information about the currently opened script
				// TODO
				
				JOptionPane.showMessageDialog(new JFrame(), "Sorry, I am not yet working...", "Sorry", JOptionPane.ERROR_MESSAGE);
			}
		});
		file.add(showCurScriptFileInfo);
		JMenuItem deleteCurScriptFile = new JMenuItem("Delete Current Script File");
		deleteCurScriptFile.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
			
				// figure out which script tab is currently open (show error if none is open)
				// TODO

				// open a dialog to confirm that the script should be deleted
				// TODO
				
				// tell the currently opened script tab to tell the cdmscript to tell the cdmfile to delete the script
				// (actually, also that file has to tell its parent file, as most likely the whole file has to be deleted)
				// TODO
				
				// remove script from the left hand side
				// TODO
				
				JOptionPane.showMessageDialog(new JFrame(), "Sorry, I am not yet working...", "Sorry", JOptionPane.ERROR_MESSAGE);
			}
		});
		file.add(deleteCurScriptFile);
		file.addSeparator();
		JMenuItem close = new JMenuItem("Close");
		close.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, ActionEvent.ALT_MASK));
		close.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		file.add(close);

		JMenu editor = new JMenu("Editor");
		menu.add(editor);
		JMenu style = new JMenu("Style");
		lightScheme = new JRadioButtonMenuItem("Light Scheme");
		darkScheme = new JRadioButtonMenuItem("Dark Scheme");
		lightScheme.setSelected(true);
		lightScheme.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				GroovyCode.setLightScheme();
				lightScheme.setSelected(true);
				darkScheme.setSelected(false);
				configuration.set(CONFIG_KEY_EDITOR_SCHEME, CONFIG_VAL_SCHEME_LIGHT);
			}
		});
		style.add(lightScheme);
		darkScheme.setSelected(false);
		darkScheme.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				GroovyCode.setDarkScheme();
				lightScheme.setSelected(false);
				darkScheme.setSelected(true);
				configuration.set(CONFIG_KEY_EDITOR_SCHEME, CONFIG_VAL_SCHEME_DARK);
			}
		});
		style.add(darkScheme);
		editor.add(style);
		JMenuItem fontLarger = new JMenuItem("Font Larger");
		fontLarger.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				currentFontSize++;
				GroovyCode.setFontSize(currentFontSize);
				configuration.set(CONFIG_KEY_EDITOR_FONT_SIZE, currentFontSize);
			}
		});
		editor.add(fontLarger);
		JMenuItem fontSmaller = new JMenuItem("Font Smaller");
		fontSmaller.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				currentFontSize--;
				GroovyCode.setFontSize(currentFontSize);
				configuration.set(CONFIG_KEY_EDITOR_FONT_SIZE, currentFontSize);
			}
		});
		editor.add(fontSmaller);
		
		JMenu huh = new JMenu("?");
		JMenuItem about = new JMenuItem("About");
		about.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String aboutMessage = "This is the " + Main.PROGRAM_TITLE + ".\n" +
					"Version: " + Main.VERSION_NUMBER + " (" + Main.VERSION_DATE + ")\n" +
					"Brought to you by: A Softer Space";
				JOptionPane.showMessageDialog(new JFrame(), aboutMessage, "About", JOptionPane.INFORMATION_MESSAGE);
			}
		});
		huh.add(about);
		menu.add(huh);
		
		parent.setJMenuBar(menu);
		
		return menu;
	}
	
	private JPanel createMainPanel(JFrame parent) {

	    JPanel mainPanel = new JPanel();
	    mainPanel.setPreferredSize(new Dimension(800, 500));
		GridBagLayout mainPanelLayout = new GridBagLayout();
		mainPanel.setLayout(mainPanelLayout);

		GridBagConstraints cLeft = new GridBagConstraints();
		cLeft.fill = GridBagConstraints.BOTH;
		cLeft.weightx = 0.2;
		cLeft.weighty = 1.0;
		cLeft.gridx = 0;
		cLeft.gridy = 0;
		
		GridBagConstraints cRight = new GridBagConstraints();
		cRight.fill = GridBagConstraints.BOTH;
		cRight.weightx = 1.0;
		cRight.weighty = 1.0;
		cRight.gridx = 1;
		cRight.gridy = 0;
		
	    mainPanelRight = new JPanel();
		mainPanelRight.setLayout(new CardLayout());
		String[] scriptList = new String[0];
		scriptListComponent = new JList<String>(scriptList);
		scriptTabs = new ArrayList<>();
		
		MouseListener scriptListClickListener = new MouseListener() {
			
			@Override
		    public void mouseClicked(MouseEvent e) {
				showSelectedTab();
		    }

			@Override
			public void mouseEntered(MouseEvent e) {
			}

			@Override
			public void mouseExited(MouseEvent e) {
			}

			@Override
			public void mousePressed(MouseEvent e) {
				showSelectedTab();
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				showSelectedTab();
			}
		};
		scriptListComponent.addMouseListener(scriptListClickListener);
		
		mainPanel.add(scriptListComponent, cLeft);
		
	    mainPanel.add(mainPanelRight, cRight);

		parent.add(mainPanel, BorderLayout.CENTER);
		
	    return mainPanel;
	}
	
	private void showSelectedTab() {

		String selectedItem = (String) scriptListComponent.getSelectedValue();

		for (ScriptTab tab : scriptTabs) {
			if (tab.isItem(selectedItem)) {
				tab.show();
			} else {
				tab.hide();
			}
		}
	}

	private void configureGUI() {
	
		String editorScheme = configuration.getValue(CONFIG_KEY_EDITOR_SCHEME);

		if (editorScheme != null) {
			switch (editorScheme) {
				case CONFIG_VAL_SCHEME_LIGHT:
					GroovyCode.setLightScheme();
					lightScheme.setSelected(true);
					darkScheme.setSelected(false);
					break;
			
				case CONFIG_VAL_SCHEME_DARK:
					GroovyCode.setDarkScheme();
					lightScheme.setSelected(false);
					darkScheme.setSelected(true);
					break;
			}
		}
		
		Integer configFontSize = configuration.getInteger(CONFIG_KEY_EDITOR_FONT_SIZE);

		if ((configFontSize != null) && (configFontSize > 0)) {
			currentFontSize = configFontSize;
		}
		
		GroovyCode.setFontSize(currentFontSize);
	}
		
	private void showGUI() {
		
		mainWindow.pack();
		mainWindow.setVisible(true);
	}
	
	private void openCdmDirectory() {

		JFileChooser activeCdmPicker;
		
		String lastDirectory = configuration.getValue(CONFIG_KEY_LAST_DIRECTORY);
		
		if ((lastDirectory != null) && !"".equals(lastDirectory)) {
			activeCdmPicker = new JFileChooser(new File(lastDirectory));
		} else {
			activeCdmPicker = new JFileChooser();
		}
		
		activeCdmPicker.setDialogTitle("Open a CDM working directory");
		activeCdmPicker.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

		int result = activeCdmPicker.showOpenDialog(mainWindow);
		
		switch (result) {

			case JFileChooser.APPROVE_OPTION:
			
				// remove old script tabs
				for (ScriptTab scriptTab : scriptTabs) {
					scriptTab.remove();
				}
			
				// load the CDM files
				configuration.set(CONFIG_KEY_LAST_DIRECTORY, activeCdmPicker.getCurrentDirectory().getAbsolutePath());
				Directory cdmDir = new Directory(activeCdmPicker.getSelectedFile());
				CdmCtrl.loadCdmDirectory(cdmDir);
				
				// update the script list on the left and load the new script tabs
				List<CdmScript> scripts = CdmCtrl.getScripts();
				strScripts = new String[scripts.size()];
				scriptTabs = new ArrayList<>();
				for (int i = 0; i < scripts.size(); i++) {
					CdmScript script = scripts.get(i);
					strScripts[i] = script.getName();
					final int scriptNumber = i;
					scriptTabs.add(new ScriptTab(mainPanelRight, script, new Callback() {
						public void call() {
							if (!strScripts[scriptNumber].endsWith(" *")) {
								strScripts[scriptNumber] += " *";
								scriptListComponent.setListData(strScripts);
							}
						}
					}));
				}
				scriptListComponent.setListData(strScripts);
				
				break;

			case JFileChooser.CANCEL_OPTION:
				// cancel was pressed... do nothing for now
				break;
		}
	}

}
