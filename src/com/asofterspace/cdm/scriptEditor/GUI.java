package com.asofterspace.cdm.scriptEditor;

import com.asofterspace.cdm.CdmCtrl;
import com.asofterspace.cdm.CdmScript;
import com.asofterspace.cdm.exceptions.AttemptingEmfException;
import com.asofterspace.cdm.exceptions.CdmLoadingException;
import com.asofterspace.toolbox.codeeditor.GroovyCode;
import com.asofterspace.toolbox.configuration.ConfigFile;
import com.asofterspace.toolbox.io.Directory;
import com.asofterspace.toolbox.io.XmlMode;
import com.asofterspace.toolbox.utils.Callback;
import com.asofterspace.toolbox.web.JSON;

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
import java.awt.GridLayout;
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
import javax.swing.JTextField;
import javax.swing.KeyStroke;


public class GUI implements Runnable {

	private JFrame mainWindow;

	private JPanel mainPanelRight;
	private JRadioButtonMenuItem lightScheme;
	private JRadioButtonMenuItem darkScheme;
	private int currentFontSize = 15;

	private ScriptTab currentlyShownTab;

	private final static String CONFIG_KEY_LAST_DIRECTORY = "lastDirectory";
	private final static String CONFIG_KEY_EDITOR_SCHEME = "editorScheme";
	private final static String CONFIG_KEY_EDITOR_FONT_SIZE = "editorFontSize";
	private final static String CONFIG_VAL_SCHEME_LIGHT = "groovyLight";
	private final static String CONFIG_VAL_SCHEME_DARK = "groovyDark";

	// on the left hand side, we add this string to indicate that the script has changed
	private final static String CHANGE_INDICATOR = " *";

	private List<ScriptTab> scriptTabs;

	private ConfigFile configuration;

	private JList<String> scriptListComponent;
	private String[] strScripts;


	public GUI(ConfigFile config) {
		
		configuration = config;
		
		strScripts = new String[0];
		
		scriptTabs = new ArrayList<>();
	}

	@Override
	public void run() {

		createGUI();

		configureGUI();

		showGUI();
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
		JMenuItem newCdm = new JMenuItem("Create Empty CDM");
		newCdm.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));
		newCdm.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO :: sort out the creation of a completely new CDM from scratch; ideally, let the user select between CDM 1.13 and 1.14
				// (or let them supply an own version, THAT will confuse people royally! ^-^)
				JOptionPane.showMessageDialog(new JFrame(), "Sorry, I am not yet working...", "Sorry", JOptionPane.ERROR_MESSAGE);
			}
		});
		file.add(newCdm);
		JMenuItem openCdm = new JMenuItem("Open CDM");
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
					if (strScripts[i].endsWith(CHANGE_INDICATOR)) {
						strScripts[i] = strScripts[i].substring(0, strScripts[i].length() - CHANGE_INDICATOR.length());
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
				// TODO :: sort out the saving-as of CDMs!
				JOptionPane.showMessageDialog(new JFrame(), "Sorry, I am not yet working...", "Sorry", JOptionPane.ERROR_MESSAGE);
			}
		});
		file.add(saveCdmAs);
		file.addSeparator();
		JMenuItem addScriptFile = new JMenuItem("Add New Script File");
		addScriptFile.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO :: sort out the adding of script files!
				// (btw., if no CDM has been loaded at all, instead of "just" adding a script file, actually create a new CDM first - like a click on New before this!)
				JOptionPane.showMessageDialog(new JFrame(), "Sorry, I am not yet working...", "Sorry", JOptionPane.ERROR_MESSAGE);
			}
		});
		file.add(addScriptFile);
		JMenuItem renameCurScriptFile = new JMenuItem("Rename Current Script File");
		renameCurScriptFile.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

				// figure out which script tab is currently open (show error if none is open)
				if (currentlyShownTab == null) {
					JOptionPane.showMessageDialog(new JFrame(), "Please select the script that you want to rename first!", "No Script Selected", JOptionPane.ERROR_MESSAGE);
					return;
				}

				// open a dialog in which the new name is to be entered (pre-filled with the current name)

				// Create the window
				JFrame renameDialog = new JFrame("New Script Name");
				GridLayout renameDialogLayout = new GridLayout(3, 1);
				renameDialogLayout.setVgap(8);
				renameDialog.setLayout(renameDialogLayout);

				// Populate the window
				JLabel explanationLabel = new JLabel();
				explanationLabel.setText("Please enter the new name of the script file:");
				renameDialog.add(explanationLabel);
				
				JTextField newScriptName = new JTextField();
				newScriptName.setText(currentlyShownTab.getScript().getName());
				renameDialog.add(newScriptName);
				
				JPanel buttonRow = new JPanel();
				GridLayout buttonRowLayout = new GridLayout(1, 2);
				buttonRowLayout.setHgap(8);
				buttonRow.setLayout(buttonRowLayout);
				renameDialog.add(buttonRow);

				JButton okButton = new JButton("OK");
				okButton.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{
						String newScriptStr = newScriptName.getText().trim();
						
						if ("".equals(newScriptStr)) {
							JOptionPane.showMessageDialog(new JFrame(), "Please enter a new name for the script.", "Enter Name", JOptionPane.ERROR_MESSAGE);
							return;
						}
						
						if (currentlyShownTab == null) {
							JOptionPane.showMessageDialog(new JFrame(), "The script cannot be renamed as currently no script has been opened.", "Sorry", JOptionPane.ERROR_MESSAGE);
							// TODO :: is there some kind of removal that is even better than just setting the visibility to false?
							renameDialog.setVisible(false);
							return;
						}
						
						// if the name does not change - do nothing... ;)
						String oldScriptStr = currentlyShownTab.getScript().getName();
						if (oldScriptStr.equals(newScriptStr)) {
							// TODO :: is there some kind of removal that is even better than just setting the visibility to false?
							renameDialog.setVisible(false);
							return;
						}

						// tell the currently opened script tab to tell the cdmscript to tell the cdmfile to change the script name
						// (oh and the script tab should change its name, and and and...)
						for (ScriptTab tab : scriptTabs) {
							if (tab.isItem(oldScriptStr)) {
								tab.setName(newScriptStr);
								tab.show();
								currentlyShownTab = tab;
							} else {
								tab.hide();
							}
						}

						// apply changed marker on the left hand side
						String oldScriptStrStar = oldScriptStr + CHANGE_INDICATOR;
						for (int i = 0; i < strScripts.length; i++) {
							if (oldScriptStr.equals(strScripts[i]) || oldScriptStrStar.equals(strScripts[i])) {
								strScripts[i] = newScriptStr + CHANGE_INDICATOR;
								break;
							}
						}
						scriptListComponent.setListData(strScripts);

						// TODO :: is there some kind of removal that is even better than just setting the visibility to false?
						renameDialog.setVisible(false);
					}
				});
				buttonRow.add(okButton);

				JButton cancelButton = new JButton("Cancel");
				cancelButton.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{
						// TODO :: is there some kind of removal that is even better than just setting the visibility to false?
						renameDialog.setVisible(false);
					}
				});
				buttonRow.add(cancelButton);

				// Stage everything to be shown
				renameDialog.pack();

				// Actually display the whole jazz
				renameDialog.setVisible(true);

				// Set the preferred size of the dialog
				int width = 300;
				int height = 160;
				renameDialog.setSize(width, height);
				renameDialog.setPreferredSize(new Dimension(width, height));
				
				// Center the dialog
				renameDialog.setLocationRelativeTo(null);
			}
		});
		file.add(renameCurScriptFile);
		JMenuItem showCurScriptFileInfo = new JMenuItem("Show Current Script File Info");
		showCurScriptFileInfo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

				// figure out which script tab is currently open (show error if none is open)
				if (currentlyShownTab == null) {
					JOptionPane.showMessageDialog(new JFrame(), "No script has been selected, so no information can be shown - sorry!", "Sorry", JOptionPane.ERROR_MESSAGE);
					return;
				}

				// show some information about the currently opened script
				// TODO :: make it possible to copy the information to clipboard!
				CdmScript script = currentlyShownTab.getScript();
				String format = "(unknown)";
				switch (script.getParent().getMode()) {
					case XML_LOADED:
						format = "XML";
						break;
					case EMF_LOADED:
						format = "EMF binary";
						break;
				}
				JOptionPane.showMessageDialog(new JFrame(),
					"Script Name: " + script.getName() + "\n" +
					"Script Namespace: " + script.getNamespace() + "\n" +
					"Script ID: " + script.getId() + "\n" +
					"CI File Format: " + format + "\n" +
					"CI Path: " + script.getParent().getFilename() + "\n" +
					"CDM Version: " + script.getParent().getCdmVersion(),
					"Script Information", JOptionPane.INFORMATION_MESSAGE);
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

		if (selectedItem == null) {
			return;
		}

		if (selectedItem.endsWith(CHANGE_INDICATOR)) {
			selectedItem = selectedItem.substring(0, selectedItem.length() - CHANGE_INDICATOR.length());
		}

		for (ScriptTab tab : scriptTabs) {
			if (tab.isItem(selectedItem)) {
				tab.show();
				currentlyShownTab = tab;
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
				strScripts = new String[0];
				scriptTabs = new ArrayList<>();
				scriptListComponent.setListData(strScripts);
				currentlyShownTab = null;

				try {
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
								if (!strScripts[scriptNumber].endsWith(CHANGE_INDICATOR)) {
									strScripts[scriptNumber] += CHANGE_INDICATOR;
									scriptListComponent.setListData(strScripts);
								}
							}
						}));
					}
					scriptListComponent.setListData(strScripts);
					
					// show the first tab explicitly - this is fun, and the tabbed layout otherwise shows it anyway, so may as well...
					if (scriptTabs.size() > 0) {
						currentlyShownTab = scriptTabs.get(0);
						currentlyShownTab.show();
					}
				
				} catch (AttemptingEmfException | CdmLoadingException e) {
					JOptionPane.showMessageDialog(new JFrame(), e.getMessage(), "CDM Loading Failed", JOptionPane.ERROR_MESSAGE);
				}

				break;

			case JFileChooser.CANCEL_OPTION:
				// cancel was pressed... do nothing for now
				break;
		}
	}

}
