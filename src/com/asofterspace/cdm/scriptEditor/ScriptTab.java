package com.asofterspace.cdm.scriptEditor;

import java.awt.Desktop;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.asofterspace.cdm.CdmScript;
import com.asofterspace.toolbox.configuration.ConfigFile;
import com.asofterspace.toolbox.io.Directory;
import com.asofterspace.toolbox.io.File;
import com.asofterspace.toolbox.web.JSON;

public class ScriptTab {

	JPanel parent;

	String title;
	
	JPanel visualPanel;
	

	public ScriptTab(JPanel parentPanel, CdmScript script) {

		parent = parentPanel;
		
		title = script.getName();
		
		visualPanel = createVisualPanel();
		
		parent.add(visualPanel);
	}
	
	private JPanel createVisualPanel() {
		
		JPanel tab = new JPanel();
		tab.setLayout(new GridLayout(4, 1));

		JLabel titleLabel = new JLabel(title);
		tab.add(titleLabel);

		JPanel buttonRow = new JPanel();
		GridLayout buttonRowLayout = new GridLayout(1, 2);
		buttonRowLayout.setHgap(10);
		buttonRow.setLayout(buttonRowLayout);
		tab.add(buttonRow);

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

		tab.setVisible(false);

	    return tab;
	}

	public boolean isItem(String item) {

		if (title == null) {
			return false;
		}
		
		return title.equals(item);
	}

	public void show() {

		visualPanel.setVisible(true);
	}

	public void hide() {

		visualPanel.setVisible(false);
	}
	
}
