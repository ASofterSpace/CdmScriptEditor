package com.asofterspace.cdm.scriptEditor;

import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;

import com.asofterspace.cdm.CdmScript;
import com.asofterspace.cdm.CdmScript2Activity;
import com.asofterspace.toolbox.codeeditor.GroovyCode;
import com.asofterspace.toolbox.configuration.ConfigFile;
import com.asofterspace.toolbox.io.Directory;
import com.asofterspace.toolbox.io.File;
import com.asofterspace.toolbox.gui.Arrangement;
import com.asofterspace.toolbox.utils.Callback;
import com.asofterspace.toolbox.web.JSON;


public class ScriptTab {

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


	public ScriptTab(JPanel parentPanel, CdmScript script, GUI gui) {

		this.parent = parentPanel;

		this.script = script;
		
		this.gui = gui;

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
	
		// enable the adding of new mappings in the dialog, either mapping to an existing activity or creating a new activity
		// (but not to an existing activity that is already mapped somewhere? or then delete the old mapping first?)
		// TODO

		// enable the deletion of mappings in the dialog, with checkbox about deleting the activity too
		// TODO
		
		// tell the script tab to refresh the script info in case it is open as the mappings shown in there might have changed now
		// TODO

		scriptMappings = new JPanel();
		scriptMappings.setLayout(new GridBagLayout());
		
		JPanel mappingsHeadline = new JPanel();
		mappingsHeadline.setLayout(new GridBagLayout());
		
		JLabel mappingsHeadLabel = new JLabel("Activity Mappings:");
		mappingsHeadline.add(mappingsHeadLabel, new Arrangement(0, 0, 0.0, 0.0));
		JPanel mappingsHeadGapPanel = new JPanel();
		mappingsHeadline.add(mappingsHeadGapPanel, new Arrangement(1, 0, 1.0, 0.0));
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

		JPanel mappingsPanel = new JPanel();
		mappingsPanel.setLayout(new GridBagLayout());
		
		// TODO - also show mappings
		
		JButton addBtn = new JButton("Add Mapping");
		addBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// TODO
			}
		});
		mappingsPanel.add(addBtn, new Arrangement(0, 4, 1.0, 0.0));
		
		JScrollPane mappingsScroller = new JScrollPane(mappingsPanel);
		scriptMappings.add(mappingsScroller, new Arrangement(0, 1, 1.0, 1.0));
		
		scriptMappings.setPreferredSize(new Dimension(1, 1));
		visualPanel.add(scriptMappings, new Arrangement(0, 3, 1.0, 1.0));
		
		visualPanel.revalidate();
	}
	
	private void reloadInfoData() {
	
		String format = "(unknown)";
		switch (script.getParent().getMode()) {
			case XML_LOADED:
				format = "XML";
				break;
			case EMF_LOADED:
				format = "EMF binary";
				break;
		}
		
		StringBuilder activityMappings = new StringBuilder();
		
		List<CdmScript2Activity> mappings = script.getAssociatedScript2Activities();
		
		int i = 1;
		
		for (CdmScript2Activity mapping : mappings) {
			activityMappings.append(i + ": " + mapping.getName() + "\n");
			i++;
		}
	
		String activityMappingsStr = activityMappings.toString();
		
		if ("".equals(activityMappingsStr)) {
			activityMappingsStr = "(none)\n";
		}
	
		scriptInfoText.setText(
			"Script Name: " + script.getName() + "\n" +
			"Script Namespace: " + script.getNamespace() + "\n" +
			"Script ID: " + script.getId() + "\n\n" +
			"CI File Format: " + format + "\n" +
			"CI Path: " + script.getParent().getFilename() + "\n\n" +
			"Associated Activity Mappings:\n" +
			activityMappingsStr + "\n" +
			"CDM Version: " + script.getParent().getCdmVersion()
		);
	}
	
	private void reloadMappingData() {
	
		// TODO
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
