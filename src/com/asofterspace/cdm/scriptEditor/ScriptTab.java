package com.asofterspace.cdm.scriptEditor;

import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.GridBagConstraints;
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
import com.asofterspace.toolbox.utils.Callback;
import com.asofterspace.toolbox.web.JSON;


public class ScriptTab {

	private JPanel parent;

	private JPanel visualPanel;

	private CdmScript script;

	private Callback callback;
	
	private boolean changed = false;
	
	private boolean infoShown = false;

	// graphical components
	private JLabel titleLabel;
	private JTextPane sourceCodeEditor;
	private JPanel scriptInfo;
	private JTextArea scriptInfoText;


	public ScriptTab(JPanel parentPanel, CdmScript script, GUI gui) {

		this.parent = parentPanel;

		this.script = script;

		this.callback = new Callback() {
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

		GridBagConstraints c1 = new GridBagConstraints();
		c1.fill = GridBagConstraints.BOTH;
		c1.weightx = 1.0;
		c1.weighty = 0.0;
		c1.gridx = 0;
		c1.gridy = 0;

		titleLabel = new JLabel(script.getName());
		titleLabel.setPreferredSize(new Dimension(0, titleLabel.getPreferredSize().height));
		tab.add(titleLabel, c1);

		GridBagConstraints c2 = new GridBagConstraints();
		c2.fill = GridBagConstraints.BOTH;
		c2.weightx = 1.0;
		c2.weighty = 1.0;
		c2.gridx = 0;
		c2.gridy = 1;

		sourceCodeEditor = new JTextPane() {
			public boolean getScrollableTracksViewportWidth() {
				return getUI().getPreferredSize(this).width <= getParent().getSize().width;
			}
		};
		GroovyCode groovyCode = new GroovyCode(sourceCodeEditor);
		sourceCodeEditor.setText(script.getSourceCode());
		groovyCode.setOnChange(callback);
		JScrollPane sourceCodeScroller = new JScrollPane(sourceCodeEditor);
		tab.add(sourceCodeScroller, c2);

		parent.add(tab);

		tab.setVisible(false);

		// scroll to the top
		sourceCodeEditor.setCaretPosition(0);

	    return tab;
	}
	
	private void createInfoArea() {
	
		GridBagConstraints c3 = new GridBagConstraints();
		c3.fill = GridBagConstraints.BOTH;
		c3.weightx = 1.0;
		c3.weighty = 1.0;
		c3.gridx = 0;
		c3.gridy = 2;
		
		scriptInfo = new JPanel();
		scriptInfo.setLayout(new GridBagLayout());
		
		JPanel scriptInfoHeadline = new JPanel();
		scriptInfoHeadline.setLayout(new GridBagLayout());
		
		GridBagConstraints ch1 = new GridBagConstraints();
		ch1.fill = GridBagConstraints.BOTH;
		ch1.weightx = 0.0;
		ch1.weighty = 0.0;
		ch1.gridx = 0;
		ch1.gridy = 0;
		
		GridBagConstraints ch2 = new GridBagConstraints();
		ch2.fill = GridBagConstraints.BOTH;
		ch2.weightx = 1.0;
		ch2.weighty = 0.0;
		ch2.gridx = 1;
		ch2.gridy = 0;
		
		GridBagConstraints ch3 = new GridBagConstraints();
		ch3.fill = GridBagConstraints.BOTH;
		ch3.weightx = 0.0;
		ch3.weighty = 0.0;
		ch3.gridx = 2;
		ch3.gridy = 0;
		
		JLabel scriptInfoHeadLabel = new JLabel("Info:");
		scriptInfoHeadline.add(scriptInfoHeadLabel, ch1);
		JPanel scriptInfoHeadGapPanel = new JPanel();
		scriptInfoHeadline.add(scriptInfoHeadGapPanel, ch2);
		JButton scriptInfoHide = new JButton("Hide");
		scriptInfoHide.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				hideInfo();
			}
		});
		scriptInfoHeadline.add(scriptInfoHide, ch3);
		
		GridBagConstraints c4 = new GridBagConstraints();
		c4.fill = GridBagConstraints.BOTH;
		c4.weightx = 1.0;
		c4.weighty = 0.0;
		c4.gridx = 0;
		c4.gridy = 0;
		
		scriptInfo.add(scriptInfoHeadline, c4);

		scriptInfoText = new JTextArea();
		scriptInfoText.setEditable(false);
		
		GridBagConstraints c5 = new GridBagConstraints();
		c5.fill = GridBagConstraints.BOTH;
		c5.weightx = 1.0;
		c5.weighty = 1.0;
		c5.gridx = 0;
		c5.gridy = 1;
		
		JScrollPane infoScroller = new JScrollPane(scriptInfoText);
		scriptInfo.add(infoScroller, c5);
		
		visualPanel.add(scriptInfo, c3);
		
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
