package com.asofterspace.cdm.scriptEditor;

import com.asofterspace.cdm.CdmCtrl;
import com.asofterspace.cdm.CdmFile;
import com.asofterspace.cdm.CdmScript;
import com.asofterspace.cdm.exceptions.AttemptingEmfException;
import com.asofterspace.cdm.exceptions.CdmLoadingException;
import com.asofterspace.toolbox.codeeditor.GroovyCode;
import com.asofterspace.toolbox.configuration.ConfigFile;
import com.asofterspace.toolbox.io.Directory;
import com.asofterspace.toolbox.io.File;
import com.asofterspace.toolbox.io.XmlMode;
import com.asofterspace.toolbox.gui.Arrangement;
import com.asofterspace.toolbox.gui.GuiUtils;
import com.asofterspace.toolbox.Utils;
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
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
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
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;


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

	private JMenuItem newCdm;
	private JMenuItem openCdm;
	private JMenuItem validateCdm;
	private JMenuItem saveCdm;
	private JMenuItem saveCdmAs;
	private JMenuItem addScriptFile;
	private JMenuItem renameCurScriptFile;
	private JMenuItem deleteCurScriptFile;
	private JCheckBoxMenuItem showScriptFileInfo;
	private JCheckBoxMenuItem manageActMaps;
	private JMenuItem close;

	private boolean showScriptFileInfoSwitch;
	private boolean manageActMapsSwitch;

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

		refreshTitleBar();

		reEnableDisableMenuItems();
	}

	private void createGUI() {

		JFrame.setDefaultLookAndFeelDecorated(false);

		// Create the window
		mainWindow = new JFrame();

		// Add content to the window
		createMenu(mainWindow);
		createMainPanel(mainWindow);

		// TODO :: show an extra panel in the middle that lets a user either create a new empty CDM, or open a CDM directory - instead of having to wobble through the menu in search of it all ^^

        mainWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Maximize the window
		GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
		Rectangle bounds = env.getMaximumWindowBounds();
		mainWindow.setMaximizedBounds(bounds);
		mainWindow.setSize((int) bounds.getWidth(), (int) bounds.getHeight());
		mainWindow.setPreferredSize(new Dimension((int) bounds.getWidth(), (int) bounds.getHeight()));
		// This should actually maximize the window, but for some reason does not work (reliably),
		// so instead we do it manually in the lines above...
		// mainWindow.setExtendedState(mainWindow.getExtendedState() | JFrame.MAXIMIZED_BOTH);

		GuiUtils.centerAndShowWindow(mainWindow);
	}

	private JMenuBar createMenu(JFrame parent) {

		JMenuBar menu = new JMenuBar();

		// TODO :: add undo / redo (for basically any action, but first of all of course for the editor)

		// TODO :: add conversion tool between zip and xml, later on xml and emf binary, and finally even different cdm versions!

		JMenu file = new JMenu("File");
		menu.add(file);
		newCdm = new JMenuItem("Create Empty CDM");
		newCdm.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));
		newCdm.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ifAllowedToLeaveCurrentCDM(new Callback() {
					public void call() {
						// show dialog in which the user can select the path to the new CDM, the CDM format (XML / EMF) and the CDM version
						// (offer several presets or also a free-text-field, in each case going for CdmCtrl.ASS_CDM_NAMESPACE + version)

						// Create the window
						JDialog newCdmDialog = new JDialog(mainWindow, "Create New CDM", true);
						GridLayout newCdmDialogLayout = new GridLayout(5, 1);
						newCdmDialogLayout.setVgap(8);
						newCdmDialog.setLayout(newCdmDialogLayout);
						newCdmDialog.getRootPane().setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

						// Populate the window
						JLabel explanationLabel = new JLabel();
						explanationLabel.setText("Please enter the working directory in which the new CDM shall be stored:");
						newCdmDialog.add(explanationLabel);

						// next to the path edit field, there is a small button with three dots, clicking upon which opens a directory picker dialog
						// TODO

						JTextField newCdmPath = new JTextField();
						String lastDirectory = configuration.getValue(CONFIG_KEY_LAST_DIRECTORY);
						if (lastDirectory != null) {
							newCdmPath.setText(lastDirectory);
						}
						newCdmDialog.add(newCdmPath);

						// enable the user to choose between creating an XML and an EMF binary CDM
						// TODO

						JLabel explanationLabelCdmVersion = new JLabel();
						explanationLabelCdmVersion.setText("Please enter the CDM version that should be used, e.g. 1.13.0bd1 or 1.14.0b:");
						newCdmDialog.add(explanationLabelCdmVersion);

						// store the various versions that have been entered before in the configuration,
						// and offer a dropdown of them all
						// TODO

						JTextField newCdmVersion = new JTextField();
						newCdmVersion.setText(CdmCtrl.getCdmVersion());
						newCdmDialog.add(newCdmVersion);

						JPanel buttonRow = new JPanel();
						GridLayout buttonRowLayout = new GridLayout(1, 2);
						buttonRowLayout.setHgap(8);
						buttonRow.setLayout(buttonRowLayout);
						newCdmDialog.add(buttonRow);

						JButton okButton = new JButton("OK");
						okButton.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								if (createNewCdm(newCdmPath.getText().trim(), newCdmVersion.getText().trim())) {
									newCdmDialog.dispose();
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
						int height = 220;
						newCdmDialog.setSize(width, height);
						newCdmDialog.setPreferredSize(new Dimension(width, height));

						GuiUtils.centerAndShowWindow(newCdmDialog);
					}
				});
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

				// open a dialog in which the name of the new script can be entered

				// Create the window
				JDialog addDialog = new JDialog(mainWindow, "Add Script", true);
				GridLayout addDialogLayout = new GridLayout(7, 1);
				addDialogLayout.setVgap(8);
				addDialog.setLayout(addDialogLayout);
				addDialog.getRootPane().setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

				// Populate the window
				JLabel explanationLabel = new JLabel();
				explanationLabel.setText("Please enter the name of the new script file:");
				addDialog.add(explanationLabel);

				JTextField newScriptName = new JTextField();
				newScriptName.setText("DoSomething");
				addDialog.add(newScriptName);

				JLabel explanationLabelCI = new JLabel();
				explanationLabelCI.setText("Please enter the name of the new script CI containing it:");
				addDialog.add(explanationLabelCI);

				JTextField newCiName = new JTextField();
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

				JLabel explanationLabelNamespace = new JLabel();
				explanationLabelNamespace.setText("Please enter the namespace of the new script:");
				addDialog.add(explanationLabelNamespace);

				// TODO :: remember the last namespace choice and read it from the configuration
				JTextField newScriptNamespace = new JTextField();
				newScriptNamespace.setText(CdmCtrl.DEFAULT_NAMESPACE);
				addDialog.add(newScriptNamespace);

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
						if (addScript(newScriptName.getText().trim(), newCiName.getText().trim(), newScriptNamespace.getText().trim())) {
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
				int height = 280;
				addDialog.setSize(width, height);
				addDialog.setPreferredSize(new Dimension(width, height));

				GuiUtils.centerAndShowWindow(addDialog);
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

				JOptionPane.showMessageDialog(mainWindow, "Sorry, I am not yet working...", "Sorry", JOptionPane.ERROR_MESSAGE);
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
				JOptionPane.showMessageDialog(mainWindow, aboutMessage, "About", JOptionPane.INFORMATION_MESSAGE);
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

	    mainPanelRight = new JPanel();
		mainPanelRight.setLayout(new CardLayout());
		mainPanelRight.setPreferredSize(new Dimension(8, 8));

	    JPanel gapPanel = new JPanel();
	    gapPanel.setPreferredSize(new Dimension(8, 8));

		String[] scriptList = new String[0];
		scriptListComponent = new JList<String>(scriptList);
		scriptListComponent.setPreferredSize(new Dimension(8, 8));
		scriptTabs = new ArrayList<>();

		MouseListener scriptListClickListener = new MouseListener() {

			@Override
		    public void mouseClicked(MouseEvent e) {
				showSelectedTab();
		    }

			@Override
			public void mousePressed(MouseEvent e) {
				showSelectedTab();
			}

			@Override
			public void mouseEntered(MouseEvent e) {
			}

			@Override
			public void mouseExited(MouseEvent e) {
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				showSelectedTab();
			}
		};
		scriptListComponent.addMouseListener(scriptListClickListener);

		mainPanel.add(scriptListComponent, new Arrangement(0, 0, 0.2, 1.0));

		mainPanel.add(gapPanel, new Arrangement(1, 0, 0.0, 0.0));

	    mainPanel.add(mainPanelRight, new Arrangement(2, 0, 1.0, 1.0));

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

	private boolean createNewCdm(String newCdmPath, String newCdmVersion) {

		if ("".equals(newCdmPath)) {
			JOptionPane.showMessageDialog(mainWindow, "Please enter a CDM path to create the new CDM files!", "CDM Path Missing", JOptionPane.ERROR_MESSAGE);
			return false;
		}

		if ("".equals(newCdmVersion)) {
			JOptionPane.showMessageDialog(mainWindow, "Please enter a CDM version to create the new CDM files!", "CDM Version Missing", JOptionPane.ERROR_MESSAGE);
			return false;
		}

		configuration.set(CONFIG_KEY_LAST_DIRECTORY, newCdmPath);
		Directory cdmDir = new Directory(newCdmPath);

		// if the new directory does not yet exist, then we have to create it...
		if (!cdmDir.exists()) {
			cdmDir.create();
		}

		// complain if the directory is not empty
		Boolean isEmpty = cdmDir.isEmpty();
		if ((isEmpty == null) || !isEmpty) {
			JOptionPane.showMessageDialog(mainWindow, "The specified directory is not empty - please create the new CDM in an empty directory!", "Directory Not Empty", JOptionPane.ERROR_MESSAGE);
			return false;
		}

		// now create just the ResourceMcm.cdm file in XML format with one root node (mcmRoot)
		String newCiName = "Mcm";
		String routeUuid = Utils.generateEcoreUUID();
		String routeTypeUuid = Utils.generateEcoreUUID();
		String sapUuid = Utils.generateEcoreUUID();
		String mcmRootDefinitionUuid = Utils.generateEcoreUUID();
		String routeDefinitionUuid = Utils.generateEcoreUUID();
		String routeTypeDefinitionUuid = Utils.generateEcoreUUID();
		String sapDefinitionUuid = Utils.generateEcoreUUID();
		String resourceMcmContent =
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
			"<configurationcontrol:McmCI xmi:version=\"2.0\" xmlns:xmi=\"http://www.omg.org/XMI\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:checkandcondition=\"" + CdmCtrl.ASS_CDM_NAMESPACE_ROOT + "MonitoringControl/MonitoringControlCommon/CheckAndCondition/" + newCdmVersion + "\" xmlns:configurationcontrol=\"" + CdmCtrl.ASS_CDM_NAMESPACE + newCdmVersion + "\" xmlns:mcmchecks=\"" + CdmCtrl.ASS_CDM_NAMESPACE_ROOT + "MonitoringControl/MonitoringControlModel/MCMChecks/1.13.0bd1\" xmlns:mcmimplementationitems=\"" + CdmCtrl.ASS_CDM_NAMESPACE_ROOT + "MonitoringControl/MCMImplementationItems/1.13.0bd1\" xmlns:monitoringcontrolcommon=\"" + CdmCtrl.ASS_CDM_NAMESPACE_ROOT + "MonitoringControl/MonitoringControlCommon/1.13.0bd1\" xmlns:monitoringcontrolmodel=\"" + CdmCtrl.ASS_CDM_NAMESPACE_ROOT + "MonitoringControl/MonitoringControlModel/1.13.0bd1\" xmlns:qudv.conceptualmodel_extModel=\"" + CdmCtrl.ASS_CDM_NAMESPACE_ROOT + "core/qudv/conceptualmodel/1.5\" xmi:id=\"" + Utils.generateEcoreUUID() + "\" externalVersionLabel=\"Created by the " + Utils.getFullProgramIdentifier() + "\" onlineRevisionIdentifier=\"0\" name=\"" + newCiName + "CI\">\n" +
			"  <monitoringControlElement xmi:id=\"" + Utils.generateEcoreUUID() + "\" name=\"mcmRoot\" subElements=\"\" defaultRoute=\"" + routeUuid + "\" definition=\"" + mcmRootDefinitionUuid + "\" defaultServiceAccessPoint=\"" + sapUuid + "\">\n" +
			"    <monitoringControlElementAspects xsi:type=\"monitoringcontrolmodel:Route\" xmi:id=\"" + routeUuid + "\" name=\"DefaultRoute\" baseElement=\"" + routeDefinitionUuid + "\" hasPredictedValue=\"false\" routeName=\"DefaultRoute\" routeID=\"1\" routeType=\"" + routeTypeUuid + "\"/>\n" +
			"    <monitoringControlElementAspects xsi:type=\"monitoringcontrolmodel:RouteType\" xmi:id=\"" + routeTypeUuid + "\" name=\"DefaultRouteType\" baseElement=\"" + routeTypeDefinitionUuid + "\" hasPredictedValue=\"false\" routeIDType=\"1\"/>\n" +
			"    <monitoringControlElementAspects xsi:type=\"mcmimplementationitems:ServiceAccessPoint\" xmi:id=\"" + sapUuid + "\" name=\"0\" baseElement=\"" + sapDefinitionUuid + "\" hasPredictedValue=\"false\" validRoutes=\"" + routeUuid + "\"/>\n" +
			"  </monitoringControlElement>\n" +
			"  <monitoringControlElementDefinition xmi:id=\"" + mcmRootDefinitionUuid + "\" name=\"mcmRoot_Definition\" subElements=\"\">\n" +
			"    <monitoringControlElementAspects xsi:type=\"monitoringcontrolmodel:Route\" xmi:id=\"" + routeDefinitionUuid + "\" name=\"DefaultRoute\" hasPredictedValue=\"false\" routeName=\"DefaultRoute\" routeID=\"1\" routeType=\"" + routeTypeDefinitionUuid + "\"/>\n" +
			"    <monitoringControlElementAspects xsi:type=\"monitoringcontrolmodel:RouteType\" xmi:id=\"" + routeTypeDefinitionUuid + "\" name=\"DefaultRouteType\" hasPredictedValue=\"false\" routeIDType=\"1\"/>\n" +
			"    <monitoringControlElementAspects xsi:type=\"mcmimplementationitems:ServiceAccessPoint\" xmi:id=\"" + sapDefinitionUuid + "\" name=\"0\" hasPredictedValue=\"false\" validRoutes=\"" + routeDefinitionUuid + "\" />\n" +
			"  </monitoringControlElementDefinition>\n" +
			"</configurationcontrol:McmCI>";

		File mcmCi = new File(cdmDir, "Resource_" + newCiName + ".cdm");
		mcmCi.setContent(resourceMcmContent);
		mcmCi.save();

		// also create the Manifest file
		// TODO

		// immediately open the newly created CDM using the CdmCtrl, just as if the open dialog had been called
		clearAllScriptTabs();

		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					CdmCtrl.loadCdmDirectory(cdmDir);
				} catch (AttemptingEmfException | CdmLoadingException e) {
					JOptionPane.showMessageDialog(mainWindow, e.getMessage(), "CDM Loading Failed", JOptionPane.ERROR_MESSAGE);
				}

				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						reloadAllScriptTabs();
					}
				});
			}
		}).start();

		return true;
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

				int result = activeCdmPicker.showOpenDialog(mainWindow);

				switch (result) {

					case JFileChooser.APPROVE_OPTION:

						clearAllScriptTabs();

						// load the CDM files
						configuration.set(CONFIG_KEY_LAST_DIRECTORY, activeCdmPicker.getCurrentDirectory().getAbsolutePath());
						Directory cdmDir = new Directory(activeCdmPicker.getSelectedFile());

						new Thread(new Runnable() {
							@Override
							public void run() {
								try {
									CdmCtrl.loadCdmDirectory(cdmDir);
								} catch (AttemptingEmfException | CdmLoadingException e) {
									JOptionPane.showMessageDialog(mainWindow, e.getMessage(), "CDM Loading Failed", JOptionPane.ERROR_MESSAGE);
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

		StringBuilder result = new StringBuilder();

		if (CdmCtrl.isCdmValid(result)) {
			JOptionPane.showMessageDialog(mainWindow, "As far as scripts are concerned, the CDM seems valid! :)", "Valid", JOptionPane.INFORMATION_MESSAGE);
		} else {
			JOptionPane.showMessageDialog(mainWindow, "Problems with the CDM have been found:\n\n" + result.toString(), "Invalid", JOptionPane.WARNING_MESSAGE);
		}
	}

	private void prepareToSave() {

		if (!CdmCtrl.hasCdmBeenLoaded()) {
			JOptionPane.showMessageDialog(mainWindow, "The CDM cannot be saved as no CDM has been opened.", "Sorry", JOptionPane.ERROR_MESSAGE);
			return;
		}

		// TODO :: add validation step here, in which we validate that all scripts are assigned to activities, and if they are not,
		// then we ask the user explicitly whether we should really save the scripts in the current state or not
		// (for this, we can call CdmCtrl.isCdmValid())

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
		CdmCtrl.save();

		JOptionPane.showMessageDialog(mainWindow, "The currently opened CDM files have been saved!", "CDM Saved", JOptionPane.INFORMATION_MESSAGE);
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

		int result = saveCdmPicker.showOpenDialog(mainWindow);

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
					JOptionPane.showMessageDialog(mainWindow, "The specified directory is not empty - please save into an empty directory!", "Directory Not Empty", JOptionPane.ERROR_MESSAGE);
					saveCdmAs();
					return;
				}

				prepareToSave();

				// for all currently opened CDM files, save them relative to the new directory as they were in the previous one
				CdmCtrl.saveTo(cdmDir);

				// also copy over the Manifest file
				// TODO

				for (ScriptTab scriptTab : scriptTabs) {
					scriptTab.invalidateInfo();
				}

				refreshTitleBar();

				JOptionPane.showMessageDialog(mainWindow, "The currently opened CDM files have been saved!", "CDM Saved", JOptionPane.INFORMATION_MESSAGE);

				break;

			case JFileChooser.CANCEL_OPTION:
				// cancel was pressed... do nothing for now
				break;
		}
	}

	private boolean addScript(String newScriptName, String newCiName, String newNamespace) {

		File newFileLocation = new File(CdmCtrl.getLastLoadedDirectory(), newCiName + ".cdm");

		// check that the newCiName (+ .cdm) is not already the file name of some other CDM file!
		if (newFileLocation.exists()) {
			JOptionPane.showMessageDialog(mainWindow, newCiName + ".cdm already exists - please choose a different CI name!", "CI Name Already Taken", JOptionPane.ERROR_MESSAGE);
			return false;
		}

		// add a script CI with one script with exactly this name - but do not save it on the hard disk just yet
		String scriptCiContent =
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
			"<configurationcontrol:ScriptCI xmi:version=\"2.0\" xmlns:xmi=\"http://www.omg.org/XMI\" xmlns:configurationcontrol=\"" + CdmCtrl.ASS_CDM_NAMESPACE + CdmCtrl.getCdmVersion() + "\" xmi:id=\"" + Utils.generateEcoreUUID() + "\" externalVersionLabel=\"Created by the " + Utils.getFullProgramIdentifier() + "\" name=\"" + newCiName + "\" onlineRevisionIdentifier=\"0\">\n" +
			"  <script name=\"" + newScriptName + "\" namespace=\"" + newNamespace + "\" scriptContent=\"\" xmi:id=\"" + Utils.generateEcoreUUID() + "\"/>\n" +
			"</configurationcontrol:ScriptCI>";

		File tmpCi = new File("tmpfile.tmp");
		tmpCi.setContent(scriptCiContent);
		tmpCi.save();

		try {
			CdmFile newCdmFile = CdmCtrl.loadCdmFile(tmpCi);

			List<CdmScript> scripts = newCdmFile.getScripts();

			if (scripts.size() != 1) {
				JOptionPane.showMessageDialog(mainWindow, "Oops - while trying to create the new script, after creating it temporarily, it could not be found!", "Sorry", JOptionPane.ERROR_MESSAGE);
				return true;
			}

			newCdmFile.setFilelocation(newFileLocation);

			tmpCi.delete();

			// if the user wants to associate an activity, also add a script2activity mapper, and if a new CI has to be created for it, adjust the manifest file
			// TODO

			// add the new script CI to the Manifest file
			// TODO

			// add a script tab for the new CDM script as currentlyShownTab
			currentlyShownTab = new ScriptTab(mainPanelRight, scripts.get(0), this);

			currentlyShownTab.setChanged(true);

			String newScriptTemplate = "package scripts;\n" +
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

			// by default, load up a nice script template
			// TODO :: create several templates and let the user select one (or none) when adding a script!
			// (e.g. templates could be: none, regular script, script with arguments - to see how arguments work, check the IR4 scripts! THEY ARE WONKY!)
			currentlyShownTab.setScriptEditorContent(newScriptTemplate);

			// add the new script to the GUI
			scriptTabs.add(currentlyShownTab);

			reScriptTabViews();

		} catch (AttemptingEmfException | CdmLoadingException e) {
			JOptionPane.showMessageDialog(mainWindow, "Oops - while trying to create the new script, after creating it temporarily, it could not be loaded!", "Sorry", JOptionPane.ERROR_MESSAGE);
		}

		// this also automagically switches to the newly added tab, as it is the currentlyShownTab
		regenerateScriptList();

		reEnableDisableMenuItems();

		return true;
	}

	private void openRenameCurrentScriptDialog() {

		// figure out which script tab is currently open (show error if none is open)
		if (currentlyShownTab == null) {
			JOptionPane.showMessageDialog(mainWindow, "No script has been selected, so no script can be renamed - sorry!", "Sorry", JOptionPane.ERROR_MESSAGE);
			return;
		}

		// open a dialog in which the new name is to be entered (pre-filled with the current name)

		// Create the window
		JDialog renameDialog = new JDialog(mainWindow, "Rename Script", true);
		GridLayout renameDialogLayout = new GridLayout(3, 1);
		renameDialogLayout.setVgap(8);
		renameDialog.setLayout(renameDialogLayout);
		renameDialog.getRootPane().setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

		// Populate the window
		JLabel explanationLabel = new JLabel();
		explanationLabel.setText("Please enter the new name of the script file:");
		renameDialog.add(explanationLabel);

		JTextField newScriptName = new JTextField();
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
			JOptionPane.showMessageDialog(mainWindow, "Please enter a new name for the script.", "Enter Name", JOptionPane.ERROR_MESSAGE);
			return false;
		}

		if (currentlyShownTab == null) {
			JOptionPane.showMessageDialog(mainWindow, "The script cannot be renamed as currently no script has been opened.", "Sorry", JOptionPane.ERROR_MESSAGE);
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
			JOptionPane.showMessageDialog(mainWindow, "No script has been selected, so no script can be deleted - sorry!", "Sorry", JOptionPane.ERROR_MESSAGE);
			return;
		}

		// open a dialog to confirm that the script should be deleted

		// Create the window
		String deleteScript = currentlyShownTab.getName();
		JDialog deleteDialog = new JDialog(mainWindow, "Delete " + deleteScript, true);
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
			JOptionPane.showMessageDialog(mainWindow, "The script cannot be deleted as currently no script has been opened.", "Sorry", JOptionPane.ERROR_MESSAGE);
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

		boolean cdmLoaded = CdmCtrl.hasCdmBeenLoaded();

		boolean scriptsExist = scriptTabs.size() > 0;

		boolean scriptIsSelected = currentlyShownTab != null;

		// enabled and disable menu items according to the state of the application
		validateCdm.setEnabled(cdmLoaded);
		saveCdm.setEnabled(cdmLoaded);
		saveCdmAs.setEnabled(cdmLoaded);
		addScriptFile.setEnabled(cdmLoaded);
		renameCurScriptFile.setEnabled(scriptIsSelected);
		deleteCurScriptFile.setEnabled(scriptIsSelected);
	}

	private void refreshTitleBar() {

		Directory lastLoadedDir = CdmCtrl.getLastLoadedDirectory();

		if (lastLoadedDir == null) {
			mainWindow.setTitle(Main.PROGRAM_TITLE);
		} else {
			mainWindow.setTitle(Main.PROGRAM_TITLE + " - " + lastLoadedDir.getDirname());
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
		List<CdmScript> scripts = CdmCtrl.getScripts();
		scriptTabs = new ArrayList<>();
		for (int i = 0; i < scripts.size(); i++) {
			CdmScript script = scripts.get(i);
			scriptTabs.add(new ScriptTab(mainPanelRight, script, this));
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
	private void ifAllowedToLeaveCurrentCDM(Callback proceedWithThisIfAllowed) {

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
		JDialog whatToDoDialog = new JDialog(mainWindow, "What to do?", true);
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

	public JFrame getMainWindow() {
		return mainWindow;
	}

}
