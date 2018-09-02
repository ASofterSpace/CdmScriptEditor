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
import com.asofterspace.toolbox.codeeditor.GroovyCodeDark;
import com.asofterspace.toolbox.configuration.ConfigFile;
import com.asofterspace.toolbox.io.Directory;
import com.asofterspace.toolbox.io.File;
import com.asofterspace.toolbox.web.JSON;

public class ScriptTab {

	JPanel parent;

	JPanel visualPanel;
	
	CdmScript script;
	

	public ScriptTab(JPanel parentPanel, CdmScript script) {

		this.parent = parentPanel;
		
		this.script = script;
		
		visualPanel = createVisualPanel();
		
		parent.add(visualPanel);
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
		
		JLabel titleLabel = new JLabel(script.getName());
		titleLabel.setPreferredSize(new Dimension(0, titleLabel.getPreferredSize().height));
		tab.add(titleLabel, c1);
		
		GridBagConstraints c2 = new GridBagConstraints();
		c2.fill = GridBagConstraints.BOTH;
		c2.weightx = 1.0;
		c2.weighty = 1.0;
		c2.gridx = 0;
		c2.gridy = 1;
		
		// TODO :: actually make this into a non-wrapping text pane!
		// see https://stackoverflow.com/questions/23149512/how-to-disable-wordwrap-in-java-jtextpane
		JTextPane sourceCodeEditor = new JTextPane();
		GroovyCode groovyCode = new GroovyCodeDark(sourceCodeEditor);
		sourceCodeEditor.setText(script.getSourceCode());
		JScrollPane sourceCodeScroller = new JScrollPane(sourceCodeEditor);
		tab.add(sourceCodeScroller, c2);

		GridBagConstraints c3 = new GridBagConstraints();
		c3.fill = GridBagConstraints.BOTH;
		c3.weightx = 1.0;
		c3.weighty = 0.0;
		c3.gridx = 0;
		c3.gridy = 2;
		
		/*
		JPanel buttonRow = new JPanel();
		GridLayout buttonRowLayout = new GridLayout(1, 2);
		buttonRowLayout.setHgap(10);
		buttonRow.setLayout(buttonRowLayout);
		tab.add(buttonRow, c3);

	    JButton previewButton = new JButton("Open File");
	    previewButton.addActionListener(new ActionListener()
	    {
			public void actionPerformed(ActionEvent e)
			{
				// TODO
			}
	    });
	    buttonRow.add(previewButton);

	    JButton compileButton = new JButton("Edit Script");
	    compileButton.addActionListener(new ActionListener()
	    {
			public void actionPerformed(ActionEvent e)
			{
				// TODO
			}
	    });
	    buttonRow.add(compileButton);
		*/

		tab.setVisible(false);

	    return tab;
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

	public void show() {

		visualPanel.setVisible(true);
	}

	public void hide() {

		visualPanel.setVisible(false);
	}
	
}
