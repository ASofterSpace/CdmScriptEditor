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

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

import com.asofterspace.cdm.CdmScript;
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
	
	private boolean changed;

	// graphical components
	private JLabel titleLabel;
	private JTextPane sourceCodeEditor;


	public ScriptTab(JPanel parentPanel, CdmScript script, GUI gui) {

		changed = false;
		
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

		sourceCodeEditor.setCaretPosition(0);

	    return tab;
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

	public void show() {

		visualPanel.setVisible(true);
	}

	public void hide() {

		visualPanel.setVisible(false);
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
