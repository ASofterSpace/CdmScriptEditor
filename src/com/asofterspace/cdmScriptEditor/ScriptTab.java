/**
 * Unlicensed code created by A Softer Space, 2019
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.cdmScriptEditor;

import com.asofterspace.toolbox.cdm.CdmActivity;
import com.asofterspace.toolbox.cdm.CdmCtrl;
import com.asofterspace.toolbox.cdm.CdmFile;
import com.asofterspace.toolbox.cdm.CdmMonitoringControlElement;
import com.asofterspace.toolbox.cdm.CdmScript;
import com.asofterspace.toolbox.cdm.CdmScript2Activity;
import com.asofterspace.toolbox.cdm.exceptions.AttemptingEmfException;
import com.asofterspace.toolbox.cdm.exceptions.CdmLoadingException;
import com.asofterspace.toolbox.codeeditor.GroovyCode;
import com.asofterspace.toolbox.configuration.ConfigFile;
import com.asofterspace.toolbox.io.Directory;
import com.asofterspace.toolbox.io.File;
import com.asofterspace.toolbox.gui.Arrangement;
import com.asofterspace.toolbox.gui.GuiUtils;
import com.asofterspace.toolbox.utils.Callback;

import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.border.CompoundBorder;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JTree;


public class ScriptTab {

	private CdmCtrl cdmCtrl;

	private JPanel parent;

	private JPanel visualPanel;

	private CdmScript script;

	private GUI gui;

	private Callback onChangeCallback;

	private boolean changed = false;

	private boolean infoShown = false;
	private boolean mappingsShown = false;

	// graphical components
	private JLabel titleLabel;
	private JTextPane sourceCodeEditor;
	private JPanel scriptInfo;
	private JPanel scriptMappings;
	private JTextArea scriptInfoText;
	private JPanel mappingsPanel;
	private JButton mappingsAddBtn;


	public ScriptTab(JPanel parentPanel, CdmScript script, final GUI gui, CdmCtrl cdmCtrl) {

		this.parent = parentPanel;

		this.script = script;

		this.gui = gui;
		
		this.cdmCtrl = cdmCtrl;
		
		this.onChangeCallback = new Callback() {
			public void call() {
				if (!changed) {
					changed = true;
					gui.regenerateScriptList();
				}
			}
		};

		visualPanel = createVisualPanel();
	}

	private JPanel createVisualPanel() {

		JPanel tab = new JPanel();
		tab.setLayout(new GridBagLayout());

		titleLabel = new JLabel(script.getName());
		titleLabel.setPreferredSize(new Dimension(0, titleLabel.getPreferredSize().height));
		tab.add(titleLabel, new Arrangement(0, 0, 1.0, 0.0));

		sourceCodeEditor = new JTextPane() {
			private final static long serialVersionUID = 1L;

			public boolean getScrollableTracksViewportWidth() {
				return getUI().getPreferredSize(this).width <= getParent().getSize().width;
			}
		};
		GroovyCode groovyCode = new GroovyCode(sourceCodeEditor);
		sourceCodeEditor.setText(script.getSourceCode());
		groovyCode.setOnChange(onChangeCallback);
		JScrollPane sourceCodeScroller = new JScrollPane(sourceCodeEditor);
		sourceCodeScroller.setPreferredSize(new Dimension(1, 1));
		tab.add(sourceCodeScroller, new Arrangement(0, 1, 1.0, 0.8));
		gui.configureCodeEditor(groovyCode);

		parent.add(tab);

		tab.setVisible(false);

		// scroll to the top
		sourceCodeEditor.setCaretPosition(0);

	    return tab;
	}

	private void createInfoArea() {

		scriptInfo = new JPanel();
		scriptInfo.setLayout(new GridBagLayout());

		JPanel scriptInfoHeadline = new JPanel();
		scriptInfoHeadline.setLayout(new GridBagLayout());

		JLabel scriptInfoHeadLabel = new JLabel("Info:");
		scriptInfoHeadline.add(scriptInfoHeadLabel, new Arrangement(0, 0, 0.0, 0.0));
		JPanel scriptInfoHeadGapPanel = new JPanel();
		scriptInfoHeadline.add(scriptInfoHeadGapPanel, new Arrangement(1, 0, 1.0, 0.0));
		// TODO :: also add button to "Maximize"
		JButton scriptInfoHide = new JButton("Hide");
		scriptInfoHide.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// hide info only on this particular tab:
				// hideInfo();

				// hide info on all tabs:
				gui.setShowScriptFileInfoSwitch(false);
			}
		});
		scriptInfoHeadline.add(scriptInfoHide, new Arrangement(2, 0, 0.0, 0.0));

		scriptInfo.add(scriptInfoHeadline, new Arrangement(0, 0, 1.0, 0.0));

		scriptInfoText = new JTextArea();
		scriptInfoText.setEditable(false);

		JScrollPane infoScroller = new JScrollPane(scriptInfoText);
		scriptInfo.add(infoScroller, new Arrangement(0, 1, 1.0, 1.0));

		scriptInfo.setPreferredSize(new Dimension(1, 1));
		visualPanel.add(scriptInfo, new Arrangement(0, 2, 1.0, 1.0));

		visualPanel.revalidate();
	}

	private void createMappingArea() {

		scriptMappings = new JPanel();
		scriptMappings.setLayout(new GridBagLayout());

		JPanel mappingsHeadline = new JPanel();
		mappingsHeadline.setLayout(new GridBagLayout());

		JLabel mappingsHeadLabel = new JLabel("Activity Mappings:");
		mappingsHeadline.add(mappingsHeadLabel, new Arrangement(0, 0, 0.0, 0.0));
		JPanel mappingsHeadGapPanel = new JPanel();
		mappingsHeadline.add(mappingsHeadGapPanel, new Arrangement(1, 0, 1.0, 0.0));
		// TODO :: also add button to "Maximize"
		JButton mappingsHide = new JButton("Hide");
		mappingsHide.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// hide mappings only on this particular tab:
				// hideMappings();

				// hide mappings on all tabs:
				gui.setManageActMapsSwitch(false);
			}
		});
		mappingsHeadline.add(mappingsHide, new Arrangement(2, 0, 0.0, 0.0));

		scriptMappings.add(mappingsHeadline, new Arrangement(0, 0, 1.0, 0.0));

		mappingsPanel = new JPanel();
		mappingsPanel.setLayout(new BoxLayout(mappingsPanel, BoxLayout.Y_AXIS));
		mappingsPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

		// adding of new mappings happens here, either mapping to an existing activity or creating a new activity
		// (but not to an existing activity that is already mapped somewhere? or then delete the old mapping first?)

		mappingsAddBtn = new JButton("Add Mapping");
		mappingsAddBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showAddMappingDialog();
			}
		});
		GuiUtils.makeWide(mappingsAddBtn);
		mappingsPanel.add(mappingsAddBtn);

		JScrollPane mappingsScroller = new JScrollPane(mappingsPanel);
		scriptMappings.add(mappingsScroller, new Arrangement(0, 1, 1.0, 1.0));

		scriptMappings.setPreferredSize(new Dimension(1, 1));
		visualPanel.add(scriptMappings, new Arrangement(0, 3, 1.0, 1.0));

		visualPanel.revalidate();
	}
	
	private void showAddMappingDialog() {
	
		// Create the window
		final JDialog addMappingDialog = new JDialog(gui.getMainFrame(), "Add Mapping to " + script.getName(), true);
		addMappingDialog.setLayout(new GridBagLayout());
		addMappingDialog.getRootPane().setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

		// Populate the window
		JLabel explanationLabel = new JLabel();
		explanationLabel.setText("You can create a mapping by selecting one of the existing activities:");
		addMappingDialog.add(explanationLabel, new Arrangement(0, 0, 1.0, 0.0));

		addMappingDialog.add(Box.createRigidArea(new Dimension(8, 8)), new Arrangement(0, 1, 1.0, 0.0));

		JPanel activitiesPanel = new JPanel();
		activitiesPanel.setLayout(new BoxLayout(activitiesPanel, BoxLayout.Y_AXIS));
		activitiesPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

		// show all the existing activities
		// TODO :: add a way to filter, maybe sort, etc.

		Set<CdmActivity> activities = cdmCtrl.getActivities();

		boolean first = true;

		for (final CdmActivity activity : activities) {

			// only show activity instances here
			if (activity.isDefinition()) {
				continue;
			}
			
			if (!first) {
				activitiesPanel.add(Box.createRigidArea(new Dimension(8, 8)));
			}
			first = false;

			JPanel actPanel = new JPanel();
			actPanel.setLayout(new BoxLayout(actPanel, BoxLayout.Y_AXIS));
			actPanel.setBorder(new CompoundBorder(
				BorderFactory.createLineBorder(Color.gray),
				BorderFactory.createEmptyBorder(8, 8, 8, 8)
			));

			JLabel nameLabel = new JLabel("Name: " + activity.getName());
			GuiUtils.makeWide(nameLabel);
			actPanel.add(nameLabel);
			actPanel.add(Box.createRigidArea(new Dimension(8, 8)));

			String alias = activity.getAlias();
			if (alias == null) {
				alias = "(none)";
			}
			JLabel aliasLabel = new JLabel("Alias: " + alias);
			GuiUtils.makeWide(aliasLabel);
			actPanel.add(aliasLabel);
			actPanel.add(Box.createRigidArea(new Dimension(8, 8)));

			JPanel buttonRow = new JPanel();
			GridLayout buttonRowLayout = new GridLayout(1, 1);
			buttonRowLayout.setHgap(8);
			buttonRow.setLayout(buttonRowLayout);
			actPanel.add(buttonRow);

			JButton mapBtn = new JButton("Map to Current Script");
			mapBtn.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (cdmCtrl.addScriptToActivityMapping(script, activity) != null) {
						// set the current script to changed, display this in the GUI, and update the info and mappings tabs
						mappingsOfThisScriptChanged();
						
						addMappingDialog.dispose();
					} else {
						JOptionPane.showMessageDialog(gui.getMainFrame(), "Oops - while trying to create the a new script to activity mapping CI, after creating it temporarily, it could not be found!", "Sorry", JOptionPane.ERROR_MESSAGE);
					}
				}
			});
			buttonRow.add(mapBtn);

			GuiUtils.makeWide(actPanel);

			activitiesPanel.add(actPanel);
		}

		JScrollPane activitiesScroller = new JScrollPane(activitiesPanel);
		addMappingDialog.add(activitiesScroller, new Arrangement(0, 2, 1.0, 1.0));

		addMappingDialog.add(Box.createRigidArea(new Dimension(8, 8)), new Arrangement(0, 3, 1.0, 0.0));

		JPanel buttonRow = new JPanel();
		GridLayout buttonRowLayout = new GridLayout(1, 3);
		buttonRowLayout.setHgap(8);
		buttonRow.setLayout(buttonRowLayout);
		addMappingDialog.add(buttonRow, new Arrangement(0, 4, 1.0, 0.0));

		JButton createNewActBtn = new JButton("Create New Activity");
		createNewActBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showAddActivityDialog(addMappingDialog);
			}
		});
		buttonRow.add(createNewActBtn);

		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addMappingDialog.dispose();
			}
		});
		buttonRow.add(cancelButton);

		// Set the preferred size of the dialog
		int width = 500;
		int height = 600;
		addMappingDialog.setSize(width, height);
		addMappingDialog.setPreferredSize(new Dimension(width, height));

		GuiUtils.centerAndShowWindow(addMappingDialog);
	}
	
	private void showAddActivityDialog(final JDialog parentDialog) {
	
		// open a new dialog to ask about the properties of the new activity (name, location in MCM tree, ...)

		// Create the window
		final JDialog createActivityDialog = new JDialog(gui.getMainFrame(), "Create Activity", true);
		createActivityDialog.setLayout(new GridBagLayout());
		createActivityDialog.getRootPane().setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

		// Populate the window
		JLabel explanationLabel = new JLabel();
		explanationLabel.setText("Enter the name of the new activity:");
		createActivityDialog.add(explanationLabel, new Arrangement(0, 0, 1.0, 0.0));

		createActivityDialog.add(Box.createRigidArea(new Dimension(8, 8)), new Arrangement(0, 1, 1.0, 0.0));

		final JTextField newActivityName = new JTextField();
		newActivityName.setText(script.getName());
		createActivityDialog.add(newActivityName, new Arrangement(0, 2, 1.0, 0.0));

		createActivityDialog.add(Box.createRigidArea(new Dimension(8, 8)), new Arrangement(0, 3, 1.0, 0.0));

		JLabel explanationLabelAlias = new JLabel();
		explanationLabelAlias.setText("Enter the alias of the new activity - or leave this empty:");
		createActivityDialog.add(explanationLabelAlias, new Arrangement(0, 4, 1.0, 0.0));

		createActivityDialog.add(Box.createRigidArea(new Dimension(8, 8)), new Arrangement(0, 5, 1.0, 0.0));

		final JTextField newActivityAlias = new JTextField();
		newActivityAlias.setText("");
		createActivityDialog.add(newActivityAlias, new Arrangement(0, 6, 1.0, 0.0));

		createActivityDialog.add(Box.createRigidArea(new Dimension(8, 8)), new Arrangement(0, 7, 1.0, 0.0));

		// TODO :: also ask for parameters of the activity (right now, we just always create activities without parameters)
		
		// show all the existing mces
		// TODO :: add a way to filter, maybe sort, etc.

		
		final JTree mcmTree;

		CdmMonitoringControlElement treeRoot = cdmCtrl.getMcmTreeRoot();
		
		if (treeRoot == null) {
			mcmTree = new JTree();
		} else {
			mcmTree = new JTree(treeRoot);
		}
		
		JScrollPane mcmTreeScroller = new JScrollPane(mcmTree);
		createActivityDialog.add(mcmTreeScroller, new Arrangement(0, 8, 1.0, 1.0));

		createActivityDialog.add(Box.createRigidArea(new Dimension(8, 8)), new Arrangement(0, 9, 1.0, 0.0));

		JPanel buttonRow = new JPanel();
		GridLayout buttonRowLayout = new GridLayout(1, 2);
		buttonRowLayout.setHgap(8);
		buttonRow.setLayout(buttonRowLayout);
		createActivityDialog.add(buttonRow, new Arrangement(0, 10, 1.0, 0.0));

		// TODO :: enable adding new MCEs, just like we are adding new activities

		/*
		JButton createNewActBtn = new JButton("Create New Element");
		createNewActBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showAddActivityDialog(createActivityDialog);
			}
		});
		createNewActBtn.setEnabled(false);
		buttonRow.add(createNewActBtn);
		*/

		// the deletion of mappings happens here, optionally including the deletion of the mapped activity
		JButton createActivityBtn = new JButton("Create Activity");
		createActivityBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			
				Object selectedElement = mcmTree.getLastSelectedPathComponent();
				
				if ((selectedElement == null) || !(selectedElement instanceof CdmMonitoringControlElement)) {
					JOptionPane.showMessageDialog(gui.getMainFrame(), "Please select an M&C element from the MCM tree to create the activity as child of that element!", "Select Element", JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				CdmMonitoringControlElement mce = (CdmMonitoringControlElement) selectedElement;
		
				// create new Activity
				CdmActivity activity = cdmCtrl.addActivity(newActivityName.getText().trim(), newActivityAlias.getText().trim(), mce);
				
				// TODO - also add a definition for the new activity (if possible - that is, if the mce in which
				// the activity lies has an associated mce definition in which the activity's definition can be hosted)
				
				// map the new activity to the script
				if (cdmCtrl.addScriptToActivityMapping(script, activity) != null) {
					// set the current script to changed, display this in the GUI, and update the info and mappings tabs
					mappingsOfThisScriptChanged();
					
					createActivityDialog.dispose();
					parentDialog.dispose();
				} else {
					JOptionPane.showMessageDialog(gui.getMainFrame(), "Oops - while trying to create the a new script to activity mapping CI, after creating it temporarily, it could not be found!", "Sorry", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		buttonRow.add(createActivityBtn);

		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				createActivityDialog.dispose();
			}
		});
		buttonRow.add(cancelButton);

		// Set the preferred size of the dialog
		int width = 500;
		int height = 600;
		createActivityDialog.setSize(width, height);
		createActivityDialog.setPreferredSize(new Dimension(width, height));

		GuiUtils.centerAndShowWindow(createActivityDialog);
	}

	private void reloadInfoData() {

		String format = "(unknown)";
		switch (script.getParentFile().getMode()) {
			case XML_LOADED:
				format = "XML";
				break;
			case EMF_LOADED:
				format = "EMF binary";
				break;
		}

		StringBuilder activityMappings = new StringBuilder();

		Set<CdmScript2Activity> mappings = script.getAssociatedScript2Activities();

		int i = 1;

		for (CdmScript2Activity mapping : mappings) {
			activityMappings.append(i + ": " + mapping.getName() + "\n");
			i++;
		}

		String activityMappingsStr = activityMappings.toString();

		if ("".equals(activityMappingsStr)) {
			activityMappingsStr = "(none)\n";
		}
		
		String cdmVersion = script.getParentFile().getCdmVersion();
		String cdmPrefix = script.getParentFile().getCdmVersionPrefix();

		scriptInfoText.setText(
			"Script Name: " + script.getName() + "\n" +
			"Script Namespace: " + script.getNamespace() + "\n" +
			"Script ID: " + script.getId() + "\n\n" +
			"CI File Format: " + format + "\n" +
			"CI Path: " + script.getParentFile().getFilename() + "\n\n" +
			"Associated Activity Mappings:\n" +
			activityMappingsStr + "\n" +
			"CDM Version: " + cdmVersion + "\n" +
			"CDM Version Prefix: " + cdmPrefix + "\n" +
			"CDM compatible with EGS-CC release: " + CdmCtrl.getCompatWithEGSCCstr(cdmVersion, cdmPrefix) + "\n" +
			"CDM compatible with RTF Framework CDM editor version: " + CdmCtrl.getCompatWithMCDEstr(cdmVersion, cdmPrefix)
		);
	}

	private void reloadMappingData() {

		mappingsPanel.removeAll();

		Set<CdmScript2Activity> mappings = script.getAssociatedScript2Activities();

		// show the actual mappings
		for (final CdmScript2Activity mapping : mappings) {

			JPanel mapPanel = new JPanel();
			mapPanel.setLayout(new BoxLayout(mapPanel, BoxLayout.Y_AXIS));
			mapPanel.setBorder(new CompoundBorder(
				BorderFactory.createLineBorder(Color.gray),
				BorderFactory.createEmptyBorder(8, 8, 8, 8)
			));

			JLabel nameLabel = new JLabel("Name: " + mapping.getName());
			GuiUtils.makeWide(nameLabel);
			mapPanel.add(nameLabel);
			mapPanel.add(Box.createRigidArea(new Dimension(8, 8)));

			String mapsTo = "(nothing)";

			if (mapping.getMappedActivityId() != null) {

				String activityId = mapping.getMappedActivityId();

				mapsTo = "Unknown activity (id: " + activityId + ") in " + mapping.getMappedActivityFilename();

				CdmActivity mappedActivity = mapping.getMappedActivity();

				if (mappedActivity != null) {
					String alias = "without alias";
					if (mappedActivity.getAlias() != null) {
						alias = "with alias " + mappedActivity.getAlias();
					}
					mapsTo = "Activity " + mappedActivity.getName() + " (id: " + activityId + ") " + alias + " in " + mapping.getMappedActivityFilename();
				}
			}

			JLabel mapsToLabel = new JLabel("Maps to: " + mapsTo);
			GuiUtils.makeWide(mapsToLabel);
			mapPanel.add(mapsToLabel);
			mapPanel.add(Box.createRigidArea(new Dimension(8, 8)));

			JPanel buttonRow = new JPanel();
			GridLayout buttonRowLayout = new GridLayout(1, 3);
			buttonRowLayout.setHgap(8);
			buttonRow.setLayout(buttonRowLayout);
			mapPanel.add(buttonRow);

			// the deletion of mappings happens here, optionally including the deletion of the mapped activity
			JButton deleteMappingBtn = new JButton("Delete Mapping");
			deleteMappingBtn.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					mapping.delete();

					// set the current script to changed, display this in the GUI, and update the info and mappings tabs
					mappingsOfThisScriptChanged();
				}
			});
			buttonRow.add(deleteMappingBtn);

			JButton deleteMappingAndActivityBtn = new JButton("Delete Mapping and Activity");
			deleteMappingAndActivityBtn.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					CdmActivity mappedActivity = mapping.getMappedActivity();

					mapping.delete();

					if (mappedActivity != null) {
						mappedActivity.delete();
					}

					// set the current script to changed, display this in the GUI, and update the info and mappings tabs
					mappingsOfThisScriptChanged();

					// TODO :: potentially invalidate mappings on other script tabs too, as the activity
					// may have been associated with them as well and may have deleted the other mappings
					// while going down? aargs!
				}
			});
			buttonRow.add(deleteMappingAndActivityBtn);

			JButton changeAliasBtn = new JButton("Change Alias");
			changeAliasBtn.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					// TODO - open dialog in which we ask for the new Alias (preset with the old one)
				}
			});
			changeAliasBtn.setEnabled(false);
			buttonRow.add(changeAliasBtn);

			GuiUtils.makeWide(mapPanel);

			mappingsPanel.add(mapPanel);

			mappingsPanel.add(Box.createRigidArea(new Dimension(8, 8)));
		}

		mappingsPanel.add(mappingsAddBtn);

		visualPanel.revalidate();
	}

	private void mappingsOfThisScriptChanged() {

		// refresh the script info pane in case it is open, as the mappings shown in there might have changed now
		invalidateInfo();

		// refresh the mappings pane, as that definitely changed
		invalidateMappings();

		// if our tab does not have a change indicator in the GUI yet...
		if (changed == false) {
			changed = true;
			// ... then add one!
			gui.regenerateScriptList();
		}
	}

	/**
	 * Call this to invalidate the info area such that it is repopulated before being shown to the user the next time
	 */
	public void invalidateInfo() {

		// if the info is not currently shown, do nothing, as the info data will be reloaded when it is shown again anyway
		if (infoShown) {
			reloadInfoData();
		}
	}

	/**
	 * Call this to invalidate the mapping area such that it is repopulated before being shown to the user the next time
	 */
	public void invalidateMappings() {

		// if the mappings are not currently shown, do nothing, as the mapping data will be reloaded when it is shown again anyway
		if (mappingsShown) {
			reloadMappingData();
		}
	}

	public void showInfo() {

		if (scriptInfo == null) {
			createInfoArea();
		}

		reloadInfoData();

		scriptInfo.setVisible(true);

		infoShown = true;
	}

	public void hideInfo() {

		if (scriptInfo != null) {
			scriptInfo.setVisible(false);
		}

		infoShown = false;
	}

	public void toggleInfo() {

		if (infoShown) {
			hideInfo();
		} else {
			showInfo();
		}
	}

	public void showMappings() {

		if (scriptMappings == null) {
			createMappingArea();
		}

		reloadMappingData();

		scriptMappings.setVisible(true);

		mappingsShown = true;
	}

	public void hideMappings() {

		if (scriptMappings != null) {
			scriptMappings.setVisible(false);
		}

		mappingsShown = false;
	}

	public void toggleMappings() {

		if (mappingsShown) {
			hideInfo();
		} else {
			showInfo();
		}
	}

	public CdmScript getScript() {
		return script;
	}

	public boolean isItem(String item) {

		if (item == null) {
			return false;
		}

		if (script == null) {
			return false;
		}

		return item.equals(script.getName());
	}

	public boolean hasBeenChanged() {

		return changed;
	}

	public String getName() {

		return script.getName();
	}

	public void setName(String newName) {

		titleLabel.setText(newName);

		changed = true;

		script.setName(newName);
	}

	public void setChanged(boolean changed) {

		this.changed = changed;
	}

	public void show() {

		visualPanel.setVisible(true);
	}

	public void hide() {

		visualPanel.setVisible(false);
	}

	public void setScriptEditorContent(String newScriptContent) {

		// set the new script content (without saving it anywhere)
		sourceCodeEditor.setText(newScriptContent);

		// scroll to the top
		sourceCodeEditor.setCaretPosition(0);
	}

	public void applyChanges() {

		script.setSourceCode(sourceCodeEditor.getText());

		changed = false;
	}

	public void remove() {

		parent.remove(visualPanel);
	}

	public void delete() {

		// even after calling delete, we do not set the script to null, as we want to be able
		// to call save() later - and THEN actually delete the file on disk!
		script.delete();

		remove();
	}

}
