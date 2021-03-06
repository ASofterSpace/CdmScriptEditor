/**
 * Unlicensed code created by A Softer Space, 2019
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.cdmScriptEditor;

import com.asofterspace.toolbox.cdm.CdmCtrl;
import com.asofterspace.toolbox.cdm.CdmFile;
import com.asofterspace.toolbox.cdm.CdmScript;
import com.asofterspace.toolbox.cdm.exceptions.AttemptingEmfException;
import com.asofterspace.toolbox.cdm.exceptions.CdmLoadingException;
import com.asofterspace.toolbox.cdm.exceptions.CdmSavingException;
import com.asofterspace.toolbox.codeeditor.GroovyCode;
import com.asofterspace.toolbox.coders.UuidEncoderDecoder;
import com.asofterspace.toolbox.configuration.ConfigFile;
import com.asofterspace.toolbox.gui.Arrangement;
import com.asofterspace.toolbox.gui.GuiUtils;
import com.asofterspace.toolbox.gui.MainWindow;
import com.asofterspace.toolbox.gui.ProgressDialog;
import com.asofterspace.toolbox.io.Directory;
import com.asofterspace.toolbox.io.File;
import com.asofterspace.toolbox.io.SimpleFile;
import com.asofterspace.toolbox.utils.Callback;
import com.asofterspace.toolbox.Utils;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;


public class GUI extends MainWindow {

	private CdmCtrl cdmCtrl;

	private JPanel mainPanelRight;
	private JRadioButtonMenuItem lightScheme;
	private JRadioButtonMenuItem darkScheme;
	private int currentFontSize = 15;

	private ScriptTab currentlyShownTab;

	private final static String REASONABLE_DEFAULT_CDM_PREFIX = "http://www.esa.int/egscc/";

	private final static String CONFIG_KEY_LAST_DIRECTORY = "lastDirectory";
	private final static String CONFIG_KEY_EDITOR_SCHEME = "editorScheme";
	private final static String CONFIG_KEY_EDITOR_FONT_SIZE = "editorFontSize";
	private final static String CONFIG_VAL_SCHEME_LIGHT = "groovyLight";
	private final static String CONFIG_VAL_SCHEME_DARK = "groovyDark";

	private final static String SCRIPT_TEMPLATE_NONE = "(none)";
	private final static String SCRIPT_TEMPLATE_DEFAULT = "Default Template";

	// on the left hand side, we add this string to indicate that the script has changed
	private final static String CHANGE_INDICATOR = " *";

	private JMenuItem newCdm;
	private JMenuItem openCdm;
	private JMenuItem validateCdm;
	private JMenuItem convertCdm;
	private JMenuItem saveCdm;
	private JMenuItem saveCdmAs;
	private JMenuItem addScriptFile;
	private JMenuItem renameCurScriptFile;
	private JMenuItem deleteCurScriptFile;
	private JMenuItem addScriptFilePopup;
	private JMenuItem renameCurScriptFilePopup;
	private JMenuItem deleteCurScriptFilePopup;
	private JCheckBoxMenuItem showScriptFileInfo;
	private JCheckBoxMenuItem manageActMaps;
	private JMenuItem close;

	private boolean showScriptFileInfoSwitch;
	private boolean manageActMapsSwitch;

	private List<ScriptTab> scriptTabs;

	private ConfigFile configuration;
	private JList<String> scriptListComponent;
	private JPopupMenu scriptListPopup;
	private String[] strScripts;


	public GUI(ConfigFile config) {

		configuration = config;

		strScripts = new String[0];

		scriptTabs = new ArrayList<>();

		cdmCtrl = new CdmCtrl();
	}

	@Override
	public void run() {

		super.create();

		GuiUtils.maximizeWindow(mainFrame);

		// Add content to the window
		createMenu(mainFrame);

		createPopupMenu(mainFrame);

		createMainPanel(mainFrame);

		// TODO :: show an extra panel in the middle that lets a user either create a new empty CDM, or open a CDM directory - instead of having to wobble through the menu in search of it all ^^

		configureGUI();

		refreshTitleBar();

		reEnableDisableMenuItems();

		super.show();
	}

	private JMenuBar createMenu(JFrame parent) {

		JMenuBar menu = new JMenuBar();

		// TODO :: add undo / redo (for basically any action, but first of all of course for the editor)

		JMenu file = new JMenu("File");
		menu.add(file);
		newCdm = new JMenuItem("Create Empty CDM");
		newCdm.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));
		newCdm.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				createNewCdm();
			}
		});
		file.add(newCdm);
		openCdm = new JMenuItem("Open CDM");
		openCdm.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
		openCdm.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				openCdmDirectory();
			}
		});
		file.add(openCdm);
		validateCdm = new JMenuItem("Validate CDM");
		validateCdm.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				validateCdm();
			}
		});
		file.add(validateCdm);
		convertCdm = new JMenuItem("Convert CDM");
		convertCdm.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				convertCdm();
			}
		});
		file.add(convertCdm);
		saveCdm = new JMenuItem("Save CDM");
		saveCdm.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
		saveCdm.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				saveCdm();
			}
		});
		file.add(saveCdm);
		saveCdmAs = new JMenuItem("Save CDM As...");
		saveCdmAs.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.ALT_MASK | ActionEvent.CTRL_MASK));
		saveCdmAs.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				saveCdmAs();
			}
		});
		file.add(saveCdmAs);
		file.addSeparator();
		addScriptFile = new JMenuItem("Add New Script File");
		addScriptFile.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				openAddNewScriptDialog();
			}
		});
		file.add(addScriptFile);
		renameCurScriptFile = new JMenuItem("Rename Current Script File");
		renameCurScriptFile.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				openRenameCurrentScriptDialog();
			}
		});
		file.add(renameCurScriptFile);
		deleteCurScriptFile = new JMenuItem("Delete Current Script File");
		deleteCurScriptFile.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				openDeleteCurrentScriptDialog();
			}
		});
		file.add(deleteCurScriptFile);
		file.addSeparator();
		close = new JMenuItem("Close");
		close.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, ActionEvent.ALT_MASK));
		close.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ifAllowedToLeaveCurrentCDM(new Callback() {
					public void call() {
						System.exit(0);
					}
				});
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
				GroovyCode.setLightSchemeForAllEditors();
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
				GroovyCode.setDarkSchemeForAllEditors();
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
				GroovyCode.setFontSizeForAllEditors(currentFontSize);
				configuration.set(CONFIG_KEY_EDITOR_FONT_SIZE, currentFontSize);
			}
		});
		editor.add(fontLarger);
		JMenuItem fontSmaller = new JMenuItem("Font Smaller");
		fontSmaller.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				currentFontSize--;
				GroovyCode.setFontSizeForAllEditors(currentFontSize);
				configuration.set(CONFIG_KEY_EDITOR_FONT_SIZE, currentFontSize);
			}
		});
		editor.add(fontSmaller);

		JMenu scriptBlocks = new JMenu("Script Blocks");
		JMenuItem insertScriptBlock = new JMenuItem("Insert Script Block:");
		insertScriptBlock.setEnabled(false);
		scriptBlocks.add(insertScriptBlock);
		// TODO :: read script blocks from the configuration (maybe come pre-equipped with a few good ones, like getting a (local) service)
		scriptBlocks.addSeparator();
		JMenuItem defineNewScriptBlock = new JMenuItem("Define New Script Block");
		defineNewScriptBlock.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// open a dialog window with a source code editor in which the script block can be defined
				// (and a second editor in which the necessary imports can be listed!)
				// TODO

				// upon OK, store the script block in the configuration and add it to the menu
				// TODO

				JOptionPane.showMessageDialog(mainFrame, "Sorry, I am not yet working...", "Sorry", JOptionPane.ERROR_MESSAGE);
			}
		});
		scriptBlocks.add(defineNewScriptBlock);
		defineNewScriptBlock.setEnabled(false);
		menu.add(scriptBlocks);

		JMenu views = new JMenu("Views");
		showScriptFileInfo = new JCheckBoxMenuItem("Show Script File Info");
		showScriptFileInfo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

				setShowScriptFileInfoSwitch(!showScriptFileInfoSwitch);
			}
		});
		// TODO :: initialize from (and upon change store in) config file!
		showScriptFileInfoSwitch = false;
		reShowScriptFileInfo();
		views.add(showScriptFileInfo);
		manageActMaps = new JCheckBoxMenuItem("Manage Activity Mappings");
		manageActMaps.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

				setManageActMapsSwitch(!manageActMapsSwitch);
			}
		});
		// TODO :: initialize from (and upon change store in) config file!
		manageActMapsSwitch = false;
		reManageActMaps();
		views.add(manageActMaps);
		menu.add(views);

		JMenu huh = new JMenu("?");
		JMenuItem about = new JMenuItem("About");
		about.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String aboutMessage = "This is the " + Main.PROGRAM_TITLE + ".\n" +
					"Version: " + Main.VERSION_NUMBER + " (" + Main.VERSION_DATE + ")\n" +
					"Brought to you by: A Softer Space";
				JOptionPane.showMessageDialog(mainFrame, aboutMessage, "About", JOptionPane.INFORMATION_MESSAGE);
			}
		});
		huh.add(about);
		menu.add(huh);

		parent.setJMenuBar(menu);

		return menu;
	}

	private JPopupMenu createPopupMenu(JFrame parent) {

		scriptListPopup = new JPopupMenu();

		addScriptFilePopup = new JMenuItem("Add New Script File");
		addScriptFilePopup.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				openAddNewScriptDialog();
			}
		});
		scriptListPopup.add(addScriptFilePopup);
		renameCurScriptFilePopup = new JMenuItem("Rename Current Script File");
		renameCurScriptFilePopup.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				openRenameCurrentScriptDialog();
			}
		});
		scriptListPopup.add(renameCurScriptFilePopup);
		deleteCurScriptFilePopup = new JMenuItem("Delete Current Script File");
		deleteCurScriptFilePopup.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				openDeleteCurrentScriptDialog();
			}
		});
		scriptListPopup.add(deleteCurScriptFilePopup);

		// don't do the following:
		//   scriptListComponent.setComponentPopupMenu(popupMenu);
		// instead manually show the popup when the right mouse key is pressed in the mouselistener
		// for the script list, because that means that we can right click on an entry, select it immediately,
		// and open the popup for exactly that entry

		return scriptListPopup;
	}

	private JPanel createMainPanel(JFrame parent) {

		JPanel mainPanel = new JPanel();
		mainPanel.setPreferredSize(new Dimension(800, 500));
		GridBagLayout mainPanelLayout = new GridBagLayout();
		mainPanel.setLayout(mainPanelLayout);

		mainPanelRight = new JPanel();
		mainPanelRight.setLayout(new CardLayout());
		mainPanelRight.setPreferredSize(new Dimension(8, 8));

		JPanel gapPanel = new JPanel();
		gapPanel.setPreferredSize(new Dimension(8, 8));

		String[] scriptList = new String[0];
		scriptListComponent = new JList<String>(scriptList);
		scriptTabs = new ArrayList<>();

		scriptListComponent.addMouseListener(new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent e) {
				showSelectedTab();
			}

			@Override
			public void mousePressed(MouseEvent e) {
				showPopupAndSelectedTab(e);
			}

			@Override
			public void mouseEntered(MouseEvent e) {
			}

			@Override
			public void mouseExited(MouseEvent e) {
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				showPopupAndSelectedTab(e);
			}

			private void showPopupAndSelectedTab(MouseEvent e) {
				if (e.isPopupTrigger()) {
					scriptListComponent.setSelectedIndex(scriptListComponent.locationToIndex(e.getPoint()));
					scriptListPopup.show(scriptListComponent, e.getX(), e.getY());
				}

				showSelectedTab();
			}
		});

		scriptListComponent.addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent e) {
			}

			@Override
			public void keyPressed(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent e) {
				switch (e.getKeyCode()) {
					case KeyEvent.VK_UP:
					case KeyEvent.VK_DOWN:
						showSelectedTab();
						break;
				}
			}
		});

		JScrollPane scriptListScroller = new JScrollPane(scriptListComponent);
		scriptListScroller.setPreferredSize(new Dimension(8, 8));
		scriptListScroller.setBorder(BorderFactory.createEmptyBorder());

		mainPanel.add(scriptListScroller, new Arrangement(0, 0, 0.2, 1.0));

		mainPanel.add(gapPanel, new Arrangement(1, 0, 0.0, 0.0));

		mainPanel.add(mainPanelRight, new Arrangement(2, 0, 1.0, 1.0));

		parent.add(mainPanel, BorderLayout.CENTER);

		return mainPanel;
	}

	private void showSelectedTab() {

		String selectedItem = scriptListComponent.getSelectedValue();

		if (selectedItem == null) {
			return;
		}

		if (selectedItem.endsWith(CHANGE_INDICATOR)) {
			selectedItem = selectedItem.substring(0, selectedItem.length() - CHANGE_INDICATOR.length());
		}

		showTab(selectedItem);
	}

	private void showTab(String name) {

		for (ScriptTab tab : scriptTabs) {
			if (tab.isItem(name)) {
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
					GroovyCode.setLightSchemeForAllEditors();
					lightScheme.setSelected(true);
					darkScheme.setSelected(false);
					break;

				case CONFIG_VAL_SCHEME_DARK:
					GroovyCode.setDarkSchemeForAllEditors();
					lightScheme.setSelected(false);
					darkScheme.setSelected(true);
					break;
			}
		}

		Integer configFontSize = configuration.getInteger(CONFIG_KEY_EDITOR_FONT_SIZE);

		if ((configFontSize != null) && (configFontSize > 0)) {
			currentFontSize = configFontSize;
		}

		GroovyCode.setFontSizeForAllEditors(currentFontSize);
	}

	void configureCodeEditor(GroovyCode code) {

		String editorScheme = configuration.getValue(CONFIG_KEY_EDITOR_SCHEME);

		if (editorScheme != null) {
			switch (editorScheme) {
				case CONFIG_VAL_SCHEME_LIGHT:
					code.setLightScheme();
					break;

				case CONFIG_VAL_SCHEME_DARK:
					code.setDarkScheme();
					break;
			}
		}

		code.setFontSize(currentFontSize);
	}

	private void createNewCdm() {
		ifAllowedToLeaveCurrentCDM(new Callback() {
			public void call() {
				// show dialog in which the user can select the path to the new CDM, the CDM format (XML / EMF) and the CDM version
				// (offer several presets or also a free-text-field, in each case going for CdmCtrl.ASS_CDM_NAMESPACE + version)

				// Create the window
				final JDialog newCdmDialog = new JDialog(mainFrame, "Create New CDM", true);
				GridLayout newCdmDialogLayout = new GridLayout(10, 1);
				newCdmDialogLayout.setVgap(8);
				newCdmDialog.setLayout(newCdmDialogLayout);
				newCdmDialog.getRootPane().setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

				// Populate the window
				JLabel explanationLabel = new JLabel();
				explanationLabel.setText("Please enter the working directory in which the new CDM shall be stored:");
				newCdmDialog.add(explanationLabel);

				// next to the path edit field, there is a small button with three dots, clicking upon which opens a directory picker dialog
				// TODO

				final JTextField newCdmPath = new JTextField();
				String lastDirectory = configuration.getValue(CONFIG_KEY_LAST_DIRECTORY);
				if (lastDirectory != null) {
					newCdmPath.setText(lastDirectory);
				}
				newCdmDialog.add(newCdmPath);

				JLabel explanationLabelTemplate = new JLabel();
				explanationLabelTemplate.setText("Please select the template to be used for the new CDM:");
				newCdmDialog.add(explanationLabelTemplate);

				final String[] templatesArr = CdmCtrl.getTemplates().toArray(new String[0]);

				final JComboBox<String> newCdmTemplate = new JComboBox<>(templatesArr);
				newCdmTemplate.setSelectedIndex(0);
				newCdmTemplate.setEditable(false);
				newCdmDialog.add(newCdmTemplate);

				// enable the user to choose between creating an XML and an EMF binary CDM
				// TODO

				JLabel explanationLabelCdmVersion = new JLabel();
				explanationLabelCdmVersion.setText("Please enter the CDM version that should be used:");
				newCdmDialog.add(explanationLabelCdmVersion);

				// TODO :: store the various versions that have been entered before in the configuration,
				// and offer them in the dropdown

				// make copies of the arrays such that we can insert elements without confusing the backend ;)
				List<String> versions = new ArrayList<>(CdmCtrl.getKnownCdmVersions());
				List<String> versionPrefixes = new ArrayList<>(CdmCtrl.getKnownCdmPrefixes());

				// if the currently used CDM version is none of the default ones, also offer that one
				String curVersion = cdmCtrl.getCdmVersion();
				if ((curVersion != null) && !"".equals(curVersion)) {
					boolean versionFound = false;
					for (String version : versions) {
						if (curVersion.equals(version)) {
							versionFound = true;
							break;
						}
					}
					if (!versionFound) {
						versions.add(0, curVersion);
						versionPrefixes.add(0, cdmCtrl.getCdmVersionPrefix());
					}
				}

				final String[] versionsArr = versions.toArray(new String[0]);
				final String[] versionPrefixesArr = versionPrefixes.toArray(new String[0]);

				final JComboBox<String> newCdmVersion = new JComboBox<>(versionsArr);
				newCdmVersion.setSelectedIndex(0);
				newCdmVersion.setEditable(true);
				newCdmDialog.add(newCdmVersion);

				JLabel explanationLabelCdmVersionPrefix = new JLabel();
				explanationLabelCdmVersionPrefix.setText("If needed, you can manually override the CDM version prefix:");
				newCdmDialog.add(explanationLabelCdmVersionPrefix);

				final JTextField newCdmVersionPrefix = new JTextField();
				newCdmVersionPrefix.setText(versionPrefixesArr[0]);
				newCdmDialog.add(newCdmVersionPrefix);

				JLabel explanationLabelAfterCdmVersionPrefix = new JLabel();
				explanationLabelAfterCdmVersionPrefix.setText("(As this is based on the CDM version, usually just leave the default.)");
				newCdmDialog.add(explanationLabelAfterCdmVersionPrefix);

				// on select in newCdmVersion, adjust newCdmVersionPrefix
				newCdmVersion.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						String version = newCdmVersion.getSelectedItem().toString().trim();
						int i = newCdmVersion.getSelectedIndex();
						String versionPrefix;
						if (i < 0) {
							// this seems to be a reasonable default in case of a user-provided CDM version...
							// TODO :: actually let the user also specify the prefix!
							versionPrefix = REASONABLE_DEFAULT_CDM_PREFIX;
						} else {
							versionPrefix = versionPrefixesArr[i];
						}
						newCdmVersionPrefix.setText(versionPrefix);
					}
				});

				JPanel buttonRow = new JPanel();
				GridLayout buttonRowLayout = new GridLayout(1, 2);
				buttonRowLayout.setHgap(8);
				buttonRow.setLayout(buttonRowLayout);
				newCdmDialog.add(buttonRow);

				JButton okButton = new JButton("OK");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						String cdmPath = newCdmPath.getText().trim();
						String version = newCdmVersion.getSelectedItem().toString().trim();
						String versionPrefix = newCdmVersionPrefix.getText().trim();
						String template = newCdmTemplate.getSelectedItem().toString().trim();

						configuration.set(CONFIG_KEY_LAST_DIRECTORY, cdmPath);

						try {
							if (cdmCtrl.createNewCdm(cdmPath, version, versionPrefix, template)) {
								clearAllScriptTabs();
								newCdmDialog.dispose();
								reloadAllScriptTabs();
							}
						} catch (AttemptingEmfException | CdmLoadingException | CdmSavingException e2) {
							JOptionPane.showMessageDialog(mainFrame, e2.getMessage(), "CDM Loading Failed", JOptionPane.ERROR_MESSAGE);
						}
					}
				});
				buttonRow.add(okButton);

				JButton cancelButton = new JButton("Cancel");
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						newCdmDialog.dispose();
					}
				});
				buttonRow.add(cancelButton);

				// Set the preferred size of the dialog
				int width = 550;
				int height = 370;
				newCdmDialog.setSize(width, height);
				newCdmDialog.setPreferredSize(new Dimension(width, height));

				GuiUtils.centerAndShowWindow(newCdmDialog);
			}
		});
	}

	private void openCdmDirectory() {

		ifAllowedToLeaveCurrentCDM(new Callback() {
			public void call() {
				// TODO :: de-localize the JFileChooser (by default it seems localized, which is inconsistent when the rest of the program is in English...)
				// (while you're at it, make Öffnen into Save for the save dialog, but keep it as Open for the open dialog... ^^)
				JFileChooser activeCdmPicker;

				String lastDirectory = configuration.getValue(CONFIG_KEY_LAST_DIRECTORY);

				if ((lastDirectory != null) && !"".equals(lastDirectory)) {
					activeCdmPicker = new JFileChooser(new java.io.File(lastDirectory));
				} else {
					activeCdmPicker = new JFileChooser();
				}

				// TODO :: also allow opening a CDM zipfile

				activeCdmPicker.setDialogTitle("Open a CDM working directory");
				activeCdmPicker.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

				int result = activeCdmPicker.showOpenDialog(mainFrame);

				switch (result) {

					case JFileChooser.APPROVE_OPTION:

						clearAllScriptTabs();

						// load the CDM files
						configuration.set(CONFIG_KEY_LAST_DIRECTORY, activeCdmPicker.getCurrentDirectory().getAbsolutePath());
						final Directory cdmDir = new Directory(activeCdmPicker.getSelectedFile());

						new Thread(new Runnable() {
							@Override
							public void run() {
								try {
									// add a progress bar (which is especially helpful when the CDM contains no scripts
									// so the main view stays empty after loading a CDM!)
									ProgressDialog progress = new ProgressDialog("Loading the CDM directory...");
									cdmCtrl.loadCdmDirectory(cdmDir, progress);
								} catch (AttemptingEmfException | CdmLoadingException e) {
									JOptionPane.showMessageDialog(mainFrame, e.getMessage(), "CDM Loading Failed", JOptionPane.ERROR_MESSAGE);
								}

								SwingUtilities.invokeLater(new Runnable() {
									public void run() {
										reloadAllScriptTabs();
									}
								});
							}
						}).start();

						break;

					case JFileChooser.CANCEL_OPTION:
						// cancel was pressed... do nothing for now
						break;
				}
			}
		});
	}

	private void validateCdm() {

		List<String> result = new ArrayList<>();

		int validity = cdmCtrl.checkValidity(result);

		if (validity == 0) {
			JOptionPane.showMessageDialog(mainFrame, "The CDM looks valid! :)", "Valid", JOptionPane.INFORMATION_MESSAGE);
		} else {
			StringBuilder dialogContent = new StringBuilder();
			if (validity == 1) {
				dialogContent.append("One problem has been found:\n\n");
			} else {
				dialogContent.append(validity + " problems have been found:\n\n");
			}
			for (String resultLine : result) {
				dialogContent.append(resultLine + "\n");
			}
			JOptionPane.showMessageDialog(mainFrame, dialogContent.toString(), "Invalid", JOptionPane.WARNING_MESSAGE);
		}
	}

	public void convertCdm() {

		// Create the window
		final JDialog convertCdmDialog = new JDialog(mainFrame, "Convert CDM", true);
		GridLayout convertCdmDialogLayout = new GridLayout(6, 1);
		convertCdmDialogLayout.setVgap(8);
		convertCdmDialog.setLayout(convertCdmDialogLayout);
		convertCdmDialog.getRootPane().setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

		// Populate the window
		JLabel explanationLabel = new JLabel();
		explanationLabel.setText("Please enter the new CDM version to which you want to convert:");
		convertCdmDialog.add(explanationLabel);

		// TODO :: store the various versions that have been entered before in the configuration,
		// and offer them in the dropdown (coordinate also with createNewCdm())

		final String[] versionsArr = CdmCtrl.getKnownCdmVersions().toArray(new String[0]);
		final String[] versionPrefixesArr = CdmCtrl.getKnownCdmPrefixes().toArray(new String[0]);

		final JComboBox<String> newCdmVersion = new JComboBox<>(versionsArr);
		newCdmVersion.setSelectedIndex(0);
		newCdmVersion.setEditable(true);
		convertCdmDialog.add(newCdmVersion);

		JLabel explanationLabelCdmVersionPrefix = new JLabel();
		explanationLabelCdmVersionPrefix.setText("If needed, you can manually override the CDM version prefix:");
		convertCdmDialog.add(explanationLabelCdmVersionPrefix);

		final JTextField newCdmVersionPrefix = new JTextField();
		newCdmVersionPrefix.setText(versionPrefixesArr[0]);
		convertCdmDialog.add(newCdmVersionPrefix);

		JLabel explanationLabelAfterCdmVersionPrefix = new JLabel();
		explanationLabelAfterCdmVersionPrefix.setText("(As this is based on the CDM version, usually just leave the default.)");
		convertCdmDialog.add(explanationLabelAfterCdmVersionPrefix);

		// on select in newCdmVersion, adjust newCdmVersionPrefix
		newCdmVersion.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String version = newCdmVersion.getSelectedItem().toString().trim();
				int i = newCdmVersion.getSelectedIndex();
				String versionPrefix;
				if (i < 0) {
					// this seems to be a reasonable default in case of a user-provided CDM version...
					// TODO :: actually let the user also specify the prefix!
					versionPrefix = REASONABLE_DEFAULT_CDM_PREFIX;
				} else {
					versionPrefix = versionPrefixesArr[i];
				}
				newCdmVersionPrefix.setText(versionPrefix);
			}
		});

		JPanel buttonRow = new JPanel();
		GridLayout buttonRowLayout = new GridLayout(1, 2);
		buttonRowLayout.setHgap(8);
		buttonRow.setLayout(buttonRowLayout);
		convertCdmDialog.add(buttonRow);

		JButton okButton = new JButton("OK");
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				String version = newCdmVersion.getSelectedItem().toString().trim();
				String versionPrefix = newCdmVersionPrefix.getText().trim();

				// TODO :: actually check if the version and versionPrefix fields have been filled
				// maybe throw an exception otherwise in the convertTo function? or just check here?)

				cdmCtrl.convertTo(version, versionPrefix);

				convertCdmDialog.dispose();

				// apply changed marker to all scripts (a conversion changes everything, alright? ^^)
				// TODO :: we could, in theory, keep track of which scripts actually did change and only
				// apply changed markers to those... but seriously, frakk it xD
				for (ScriptTab tab : scriptTabs) {
					tab.setChanged(true);
				}

				regenerateScriptList();
			}
		});
		buttonRow.add(okButton);

		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				convertCdmDialog.dispose();
			}
		});
		buttonRow.add(cancelButton);

		// Set the preferred size of the dialog
		int width = 550;
		int height = 270;
		convertCdmDialog.setSize(width, height);
		convertCdmDialog.setPreferredSize(new Dimension(width, height));

		GuiUtils.centerAndShowWindow(convertCdmDialog);
	}

	private void prepareToSave() {

		if (!cdmCtrl.hasCdmBeenLoaded()) {
			JOptionPane.showMessageDialog(mainFrame, "The CDM cannot be saved as no CDM has been opened.", "Sorry", JOptionPane.ERROR_MESSAGE);
			return;
		}

		// TODO :: add validation step here, in which we validate that all scripts are assigned to activities, and if they are not,
		// then we ask the user explicitly whether we should really save the scripts in the current state or not
		// (for this, we can call CdmCtrl.checkValidity())

		// apply all changes, such that the current source code editor contents are actually stored in the CDM file objects
		for (ScriptTab scriptTab : scriptTabs) {
			scriptTab.applyChanges();
		}

		// remove all change indicators on the left-hand side
		regenerateScriptList();
	}

	private void saveCdm() {

		prepareToSave();

		// save all opened CDM files
		cdmCtrl.save();

		JOptionPane.showMessageDialog(mainFrame, "The currently opened CDM files have been saved!", "CDM Saved", JOptionPane.INFORMATION_MESSAGE);
	}

	private void saveCdmAs() {

		// open a save dialog in which a directory can be picked
		JFileChooser saveCdmPicker;

		String lastDirectory = configuration.getValue(CONFIG_KEY_LAST_DIRECTORY);

		if ((lastDirectory != null) && !"".equals(lastDirectory)) {
			saveCdmPicker = new JFileChooser(new java.io.File(lastDirectory));
		} else {
			saveCdmPicker = new JFileChooser();
		}

		saveCdmPicker.setDialogTitle("Select the new CDM working directory");
		saveCdmPicker.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

		int result = saveCdmPicker.showOpenDialog(mainFrame);

		switch (result) {

			case JFileChooser.APPROVE_OPTION:

				configuration.set(CONFIG_KEY_LAST_DIRECTORY, saveCdmPicker.getCurrentDirectory().getAbsolutePath());
				Directory cdmDir = new Directory(saveCdmPicker.getSelectedFile());

				// if the new directory does not yet exist, then we have to create it...
				if (!cdmDir.exists()) {
					cdmDir.create();
				}

				// complain if the directory is not empty
				Boolean isEmpty = cdmDir.isEmpty();
				if ((isEmpty == null) || !isEmpty) {
					JOptionPane.showMessageDialog(mainFrame, "The specified directory is not empty - please save into an empty directory!", "Directory Not Empty", JOptionPane.ERROR_MESSAGE);
					saveCdmAs();
					return;
				}

				prepareToSave();

				// for all currently opened CDM files, save them relative to the new directory as they were in the previous one
				cdmCtrl.saveTo(cdmDir);

				// also copy over the Manifest file
				// TODO

				for (ScriptTab scriptTab : scriptTabs) {
					scriptTab.invalidateInfo();
				}

				refreshTitleBar();

				JOptionPane.showMessageDialog(mainFrame, "The currently opened CDM files have been saved!", "CDM Saved", JOptionPane.INFORMATION_MESSAGE);

				break;

			case JFileChooser.CANCEL_OPTION:
				// cancel was pressed... do nothing for now
				break;
		}
	}

	private void openAddNewScriptDialog() {

		// open a dialog in which the name of the new script can be entered

		// Create the window
		final JDialog addDialog = new JDialog(mainFrame, "Add Script", true);
		GridLayout addDialogLayout = new GridLayout(9, 1);
		addDialogLayout.setVgap(8);
		addDialog.setLayout(addDialogLayout);
		addDialog.getRootPane().setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

		// Populate the window
		JLabel explanationLabel = new JLabel();
		explanationLabel.setText("Please enter the name of the new script file:");
		addDialog.add(explanationLabel);

		final JTextField newScriptName = new JTextField();
		newScriptName.setText("DoSomething");
		addDialog.add(newScriptName);

		JLabel explanationLabelNamespace = new JLabel();
		explanationLabelNamespace.setText("Please enter the namespace of the new script:");
		addDialog.add(explanationLabelNamespace);

		// TODO :: remember the last namespace choice and read it from the configuration
		final JTextField newScriptNamespace = new JTextField();
		newScriptNamespace.setText(CdmCtrl.DEFAULT_NAMESPACE);
		addDialog.add(newScriptNamespace);

		JLabel explanationLabelCI = new JLabel();
		explanationLabelCI.setText("Please enter the name of the new script CI containing the new script:");
		addDialog.add(explanationLabelCI);

		final JTextField newCiName = new JTextField();
		newCiName.setText("DoSomethingScript");
		addDialog.add(newCiName);

		// automatically write newScriptName + Script in the newCiName field whenever newScriptName is changed
		newScriptName.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent e) {
				onchange();
			}
			public void removeUpdate(DocumentEvent e) {
				onchange();
			}
			public void insertUpdate(DocumentEvent e) {
				onchange();
			}
			public void onchange() {
				newCiName.setText(newScriptName.getText() + "Script");
			}
		});

		JLabel explanationLabelTemplate = new JLabel();
		explanationLabelTemplate.setText("Please select a template for the new script:");
		addDialog.add(explanationLabelTemplate);

		// TODO :: add more templates, e.g. one for scripts using parameters
		String[] templates = { SCRIPT_TEMPLATE_NONE, SCRIPT_TEMPLATE_DEFAULT };
		final JComboBox<String> newScriptTemplate = new JComboBox<>(templates);
		newScriptTemplate.setSelectedIndex(1);
		addDialog.add(newScriptTemplate);

		// also let the user immediately associate an activity with this script?
		// TODO

		JPanel buttonRow = new JPanel();
		GridLayout buttonRowLayout = new GridLayout(1, 2);
		buttonRowLayout.setHgap(8);
		buttonRow.setLayout(buttonRowLayout);
		addDialog.add(buttonRow);

		JButton okButton = new JButton("OK");
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (addScript(
					newScriptName.getText().trim(),
					newCiName.getText().trim(),
					newScriptNamespace.getText().trim(),
					newScriptTemplate.getSelectedItem().toString()
				)) {
					addDialog.dispose();
				}
			}
		});
		buttonRow.add(okButton);

		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addDialog.dispose();
			}
		});
		buttonRow.add(cancelButton);

		// Set the preferred size of the dialog
		int width = 450;
		int height = 340;
		addDialog.setSize(width, height);
		addDialog.setPreferredSize(new Dimension(width, height));

		GuiUtils.centerAndShowWindow(addDialog);
	}

	// TODO :: move main part of this to CdmCtrl!
	private boolean addScript(String newScriptName, String newCiName, String newNamespace, String newTemplate) {

		File newFileLocation = new File(cdmCtrl.getLastLoadedDirectory(), newCiName + ".cdm");

		// check that the newCiName (+ .cdm) is not already the file name of some other CDM file!
		if (newFileLocation.exists()) {
			JOptionPane.showMessageDialog(mainFrame, newCiName + ".cdm already exists - please choose a different CI name!", "CI Name Already Taken", JOptionPane.ERROR_MESSAGE);
			return false;
		}

		// add a script CI with one script with exactly this name - but do not save it on the hard disk just yet
		String scriptCiContent =
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
			"<configurationcontrol:ScriptCI xmi:version=\"2.0\" xmlns:xmi=\"http://www.omg.org/XMI\" " + cdmCtrl.getXMLNS() + " xmi:id=\"" + UuidEncoderDecoder.generateEcoreUUID() + "\" externalVersionLabel=\"Created by the " + Utils.getFullProgramIdentifier() + "\" isModified=\"false\" name=\"" + newCiName + "\" onlineRevisionIdentifier=\"0\">\n" +
			"  <script name=\"" + newScriptName + "\" namespace=\"" + newNamespace + "\" scriptContent=\"\" xmi:id=\"" + UuidEncoderDecoder.generateEcoreUUID() + "\"/>\n" +
			"</configurationcontrol:ScriptCI>";

		SimpleFile tmpCi = new SimpleFile("tmpfile.tmp");
		tmpCi.setContent(scriptCiContent);
		tmpCi.save();

		// keep track of which scripts there were before loading somesuch... (making a shallow copy!)
		Set<CdmScript> scriptsBefore = new HashSet<>(cdmCtrl.getScripts());

		try {
			CdmFile newCdmFile = cdmCtrl.loadAnotherCdmFile(tmpCi);

			Set<CdmScript> scriptsAfter = new HashSet<>(cdmCtrl.getScripts());

			scriptsAfter.removeAll(scriptsBefore);

			if (scriptsAfter.size() != 1) {
				JOptionPane.showMessageDialog(mainFrame, "Oops - while trying to create the new script, after creating it temporarily, it could not be found!", "Sorry", JOptionPane.ERROR_MESSAGE);
				return true;
			}

			newCdmFile.setFilelocation(newFileLocation);

			tmpCi.delete();

			// if the user wants to associate an activity, also add a script2activity mapper, and if a new CI has to be created for it, adjust the manifest file
			// TODO

			// add the new script CI to the Manifest file
			// TODO

			// add a script tab for the new CDM script as currentlyShownTab
			currentlyShownTab = new ScriptTab(mainPanelRight, scriptsAfter.iterator().next(), this, cdmCtrl);

			currentlyShownTab.setChanged(true);

			String newScriptTemplate = "";

			switch (newTemplate) {
				case SCRIPT_TEMPLATE_DEFAULT:
					newScriptTemplate = "package scripts;\n" +
						"\n" +
						"import org.osgi.framework.BundleContext;\n" +
						"import org.osgi.framework.Filter;\n" +
						"import org.osgi.framework.FrameworkUtil;\n" +
						"import org.osgi.framework.ServiceReference;\n" +
						"\n" +
						"import esa.egscc.kernel.api.commonDataTypes.exceptions.EgsccException;\n" +
						"import esa.egscc.kernel.infrastructure.componentframework.core.EgsccBundleContext;\n" +
						"import esa.egscc.kernel.infrastructure.componentframework.core.EgsccFrameworkUtil;\n" +
						"\n" +
						"\n" +
						"/**\n" +
						" * " + newScriptName + " script\n" +
						" *\n" +
						" * Created with the A Softer Space CDM Script Editor\n" +
						" *\n" +
						" * @author \n" +
						" */\n" +
						"public class " + newScriptName + " {\n" +
						"\n" +
						"	private static EgsccBundleContext context;\n" +
						"\n" +
						"\n" +
						"	public static main(args) {\n" +
						"		println \"The " + newScriptName + " script has been called!\"\n" +
						"	}\n" +
						"\n" +
						"	private static <T> T getService(Class<T> serviceToGet, String filter) {\n" +
						"\n" +
						"		if (context == null) {\n" +
						"			context = EgsccFrameworkUtil.getEgsccBundleContext();\n" +
						"		}\n" +
						"\n" +
						"		if (filterString == null) {\n" +
						"			return context.getService(serviceToGet);\n" +
						"		} else {\n" +
						"			Filter serviceFilter = FrameworkUtil.createFilter(filter);\n" +
						"			Collection<ServiceReference<T>> refs = context.getServiceReferences(serviceToGet, serviceFilter);\n" +
						"			if (refs.size() < 1) {\n" +
						"				return null;\n" +
						"			}\n" +
						"			return context.getService(refs.getAt(0));\n" +
						"		}\n" +
						"	}\n" +
						"}";
					break;
			}

			// by default, load up a nice script template
			currentlyShownTab.setScriptEditorContent(newScriptTemplate);

			// add the new script to the GUI
			scriptTabs.add(currentlyShownTab);

			reScriptTabViews();

		} catch (AttemptingEmfException | CdmLoadingException e) {
			JOptionPane.showMessageDialog(mainFrame, "Oops - while trying to create the new script, after creating it temporarily, it could not be loaded!", "Sorry", JOptionPane.ERROR_MESSAGE);
		}

		// this also automagically switches to the newly added tab, as it is the currentlyShownTab
		regenerateScriptList();

		reEnableDisableMenuItems();

		return true;
	}

	private void openRenameCurrentScriptDialog() {

		// figure out which script tab is currently open (show error if none is open)
		if (currentlyShownTab == null) {
			JOptionPane.showMessageDialog(mainFrame, "No script has been selected, so no script can be renamed - sorry!", "Sorry", JOptionPane.ERROR_MESSAGE);
			return;
		}

		// open a dialog in which the new name is to be entered (pre-filled with the current name)

		// Create the window
		final JDialog renameDialog = new JDialog(mainFrame, "Rename Script", true);
		GridLayout renameDialogLayout = new GridLayout(3, 1);
		renameDialogLayout.setVgap(8);
		renameDialog.setLayout(renameDialogLayout);
		renameDialog.getRootPane().setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

		// Populate the window
		JLabel explanationLabel = new JLabel();
		explanationLabel.setText("Please enter the new name of the script file:");
		renameDialog.add(explanationLabel);

		final JTextField newScriptName = new JTextField();
		newScriptName.setText(currentlyShownTab.getName());
		renameDialog.add(newScriptName);

		JPanel buttonRow = new JPanel();
		GridLayout buttonRowLayout = new GridLayout(1, 2);
		buttonRowLayout.setHgap(8);
		buttonRow.setLayout(buttonRowLayout);
		renameDialog.add(buttonRow);

		JButton okButton = new JButton("OK");
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (renameCurrentScript(newScriptName.getText().trim())) {
					renameDialog.dispose();
				}
			}
		});
		buttonRow.add(okButton);

		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				renameDialog.dispose();
			}
		});
		buttonRow.add(cancelButton);

		// Set the preferred size of the dialog
		int width = 350;
		int height = 160;
		renameDialog.setSize(width, height);
		renameDialog.setPreferredSize(new Dimension(width, height));

		GuiUtils.centerAndShowWindow(renameDialog);
	}

	/**
	 * Rename the currently opened script to the name newScriptStr
	 * @return true if something happened and the dialog should be closed, false if it should stay open
	 */
	private boolean renameCurrentScript(String newScriptStr) {

		// TODO :: also add a way to rename the associated activity, if an activity is associated, or even the alias

		if ("".equals(newScriptStr)) {
			JOptionPane.showMessageDialog(mainFrame, "Please enter a new name for the script.", "Enter Name", JOptionPane.ERROR_MESSAGE);
			return false;
		}

		if (currentlyShownTab == null) {
			JOptionPane.showMessageDialog(mainFrame, "The script cannot be renamed as currently no script has been opened.", "Sorry", JOptionPane.ERROR_MESSAGE);
			return true;
		}

		// if the name does not change - do nothing... ;)
		String oldScriptStr = currentlyShownTab.getName();
		if (oldScriptStr.equals(newScriptStr)) {
			return true;
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
		regenerateScriptList();

		return true;
	}

	private void openDeleteCurrentScriptDialog() {

		// figure out which script tab is currently open (show error if none is open)
		if (currentlyShownTab == null) {
			JOptionPane.showMessageDialog(mainFrame, "No script has been selected, so no script can be deleted - sorry!", "Sorry", JOptionPane.ERROR_MESSAGE);
			return;
		}

		// open a dialog to confirm that the script should be deleted

		// Create the window
		String deleteScript = currentlyShownTab.getName();
		final JDialog deleteDialog = new JDialog(mainFrame, "Delete " + deleteScript, true);
		GridLayout deleteDialogLayout = new GridLayout(3, 1);
		deleteDialogLayout.setVgap(8);
		deleteDialog.setLayout(deleteDialogLayout);
		deleteDialog.getRootPane().setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

		// Populate the window
		JLabel explanationLabel = new JLabel();
		explanationLabel.setText("Do you really want to delete the script:");
		deleteDialog.add(explanationLabel);

		JLabel scriptNameLabel = new JLabel();
		scriptNameLabel.setText(deleteScript);
		deleteDialog.add(scriptNameLabel);

		JPanel buttonRow = new JPanel();
		GridLayout buttonRowLayout = new GridLayout(1, 2);
		buttonRowLayout.setHgap(8);
		buttonRow.setLayout(buttonRowLayout);
		deleteDialog.add(buttonRow);

		JButton deleteButton = new JButton("Delete");
		deleteButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (deleteCurrentScript()) {
					deleteDialog.dispose();
				}
			}
		});
		buttonRow.add(deleteButton);

		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				deleteDialog.dispose();
			}
		});
		buttonRow.add(cancelButton);

		// Set the preferred size of the dialog
		int width = 300;
		int height = 160;
		deleteDialog.setSize(width, height);
		deleteDialog.setPreferredSize(new Dimension(width, height));

		GuiUtils.centerAndShowWindow(deleteDialog);
	}

	/**
	 * Delete the currently opened script
	 * @return true if something happened and the dialog should be closed, false if it should stay open
	 */
	private boolean deleteCurrentScript() {

		if (currentlyShownTab == null) {
			JOptionPane.showMessageDialog(mainFrame, "The script cannot be deleted as currently no script has been opened.", "Sorry", JOptionPane.ERROR_MESSAGE);
			return true;
		}

		// TODO :: make it configurable whether to also delete the related activity!

		// tell the currently opened script tab to tell the cdmscript to tell the cdmfile to delete the script
		// (actually, most likely the whole file has to be deleted, together with potentially the activity mapper
		// entry that attaches the script to an activity, and possibly even the entire activity... hooray!)
		currentlyShownTab.delete();

		// remove the currently shown tab from the list of existing tabs
		List<ScriptTab> oldScriptTabs = scriptTabs;

		scriptTabs = new ArrayList<>();
		for (ScriptTab sT : oldScriptTabs) {
			if (sT != currentlyShownTab) {
				scriptTabs.add(sT);
			}
		}

		currentlyShownTab = null;

		// remove script from the left hand side
		regenerateScriptList();

		reEnableDisableMenuItems();

		return true;
	}

	/**
	 * Regenerate the script list on the left hand side based on the scriptTabs list,
	 * and (if at least one script exists), select and open the current tab or, if it
	 * is null, the first one
	 */
	public void regenerateScriptList() {

		strScripts = new String[scriptTabs.size()];

		int i = 0;

		for (ScriptTab scriptTab : scriptTabs) {
			strScripts[i] = scriptTab.getName();
			if (scriptTab.hasBeenChanged()) {
				strScripts[i] += CHANGE_INDICATOR;
			}
			i++;
		}

		scriptListComponent.setListData(strScripts);

		// if there is no last shown tab...
		if (currentlyShownTab == null) {
			// ... show the first tab explicitly - this is fun, and the tabbed layout otherwise shows it anyway, so may as well...
			if (scriptTabs.size() > 0) {
				currentlyShownTab = scriptTabs.get(0);
			}
		}

		// if there still is no last shown tab (e.g. we just deleted the very last one)...
		if (currentlyShownTab == null) {
			// ... then we do not need to show or highlight any ;)
			return;
		}

		// show the last shown tab
		showTab(currentlyShownTab.getName());

		highlightTabInLeftList(currentlyShownTab.getName());
	}

	private void highlightTabInLeftList(String name) {

		int i = 0;

		for (ScriptTab scriptTab : scriptTabs) {
			if (name.equals(scriptTab.getName())) {
				scriptListComponent.setSelectedIndex(i);
				break;
			}
			i++;
		}
	}

	/**
	 * Enable and disable menu items related to the current state of the application,
	 * e.g. if no CDM is loaded at all, do not enable the user to add scripts to the
	 * current CDM, etc.
	 */
	private void reEnableDisableMenuItems() {

		boolean cdmLoaded = cdmCtrl.hasCdmBeenLoaded();

		boolean scriptsExist = scriptTabs.size() > 0;

		boolean scriptIsSelected = currentlyShownTab != null;

		// enabled and disable menu items according to the state of the application
		validateCdm.setEnabled(cdmLoaded);
		convertCdm.setEnabled(cdmLoaded);
		saveCdm.setEnabled(cdmLoaded);
		saveCdmAs.setEnabled(cdmLoaded);
		addScriptFile.setEnabled(cdmLoaded);
		addScriptFilePopup.setEnabled(cdmLoaded);
		renameCurScriptFile.setEnabled(scriptIsSelected);
		renameCurScriptFilePopup.setEnabled(scriptIsSelected);
		deleteCurScriptFile.setEnabled(scriptIsSelected);
		deleteCurScriptFilePopup.setEnabled(scriptIsSelected);
	}

	private void refreshTitleBar() {

		Directory lastLoadedDir = cdmCtrl.getLastLoadedDirectory();

		if (lastLoadedDir == null) {
			mainFrame.setTitle(Main.PROGRAM_TITLE);
		} else {
			mainFrame.setTitle(Main.PROGRAM_TITLE + " - " + lastLoadedDir.getDirname());
		}
	}

	private void clearAllScriptTabs() {

		// remove old script tabs
		for (ScriptTab scriptTab : scriptTabs) {
			scriptTab.remove();
		}
		strScripts = new String[0];
		scriptTabs = new ArrayList<>();
		scriptListComponent.setListData(strScripts);
		currentlyShownTab = null;

		mainPanelRight.repaint();
	}

	private void reloadAllScriptTabs() {

		// update the script list on the left and load the new script tabs
		scriptTabs = new ArrayList<>();

		Set<CdmScript> scripts = cdmCtrl.getScripts();
		for (CdmScript script : scripts) {
			scriptTabs.add(new ScriptTab(mainPanelRight, script, this, cdmCtrl));
		}

		reScriptTabViews();

		regenerateScriptList();

		reEnableDisableMenuItems();

		refreshTitleBar();
	}

	/**
	 * Check if currently a CDM is loaded, and if so then if files have been changed,
	 * and if yes ask the user if we want to save first, proceed, or cancel
	 * return true if we saved or proceed anyway, and false if we cancel
	 */
	private void ifAllowedToLeaveCurrentCDM(final Callback proceedWithThisIfAllowed) {

		// check all scripts; if any have been changed, ask first before closing!
		boolean noneHaveBeenChanged = true;

		for (ScriptTab scriptTab : scriptTabs) {
			if (scriptTab.hasBeenChanged()) {
				noneHaveBeenChanged = false;
				break;
			}
		}

		// if none have been changed, then we are allowed to proceed in any case :)
		if (noneHaveBeenChanged) {
			proceedWithThisIfAllowed.call();
			return;
		}

		// okay, something has been changed, so we now want to ask the user about what to do...

		// Create the window
		final JDialog whatToDoDialog = new JDialog(mainFrame, "What to do?", true);
		GridLayout whatToDoDialogLayout = new GridLayout(2, 1);
		whatToDoDialogLayout.setVgap(8);
		whatToDoDialog.setLayout(whatToDoDialogLayout);
		whatToDoDialog.getRootPane().setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

		// Populate the window
		JLabel explanationLabel = new JLabel();
		explanationLabel.setText("The currently loaded CDM has been modified - what do you want to do?");
		whatToDoDialog.add(explanationLabel);

		JPanel buttonRow = new JPanel();
		GridLayout buttonRowLayout = new GridLayout(1, 3);
		buttonRowLayout.setHgap(8);
		buttonRow.setLayout(buttonRowLayout);
		whatToDoDialog.add(buttonRow);

		JButton saveButton = new JButton("Save, then Proceed");
		saveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveCdm();
				whatToDoDialog.dispose();
				proceedWithThisIfAllowed.call();
			}
		});
		buttonRow.add(saveButton);

		JButton proceedButton = new JButton("Proceed without Saving");
		proceedButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				whatToDoDialog.dispose();
				proceedWithThisIfAllowed.call();
			}
		});
		buttonRow.add(proceedButton);

		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				whatToDoDialog.dispose();
			}
		});
		buttonRow.add(cancelButton);

		// Set the preferred size of the dialog
		int width = 600;
		int height = 120;
		whatToDoDialog.setSize(width, height);
		whatToDoDialog.setPreferredSize(new Dimension(width, height));

		GuiUtils.centerAndShowWindow(whatToDoDialog);
	}

	private void reScriptTabViews() {

		reShowScriptFileInfo();

		reManageActMaps();
	}

	public void setShowScriptFileInfoSwitch(boolean value) {

		showScriptFileInfoSwitch = value;

		// TODO :: store value in the configuration

		reShowScriptFileInfo();
	}

	private void reShowScriptFileInfo() {

		showScriptFileInfo.setSelected(showScriptFileInfoSwitch);

		for (ScriptTab scriptTab : scriptTabs) {
			if (showScriptFileInfoSwitch) {
				scriptTab.showInfo();
			} else {
				scriptTab.hideInfo();
			}
		}
	}

	public void setManageActMapsSwitch(boolean value) {

		manageActMapsSwitch = value;

		// TODO :: store value in the configuration

		reManageActMaps();
	}

	private void reManageActMaps() {

		manageActMaps.setSelected(manageActMapsSwitch);

		for (ScriptTab scriptTab : scriptTabs) {
			if (manageActMapsSwitch) {
				scriptTab.showMappings();
			} else {
				scriptTab.hideMappings();
			}
		}
	}

}
