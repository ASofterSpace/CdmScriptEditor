package com.asofterspace.cdm.scriptEditor;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;

import com.asofterspace.cdm.CdmCtrl;
import com.asofterspace.toolbox.configuration.ConfigFile;
import com.asofterspace.toolbox.io.Directory;
import com.asofterspace.toolbox.web.JSON;

public class GUI implements Runnable {

	private JFrame mainWindow;
	
	private String[] pageList;
	
	private List<ScriptTab> scriptTabs;
	
	private ConfigFile configuration;
	
	private JList<String> scriptListComponent;
	
	
	public GUI(ConfigFile config) {
		configuration = config;
	}
	
	@Override
	public void run() {
		
		createGUI();
		
		showGUI();
		
		openCdmFile();
	}

	private void createGUI() {

		// Create the window
		mainWindow = new JFrame(Main.PROGRAM_TITLE);

		// Add content to the window
		createTopPanel(mainWindow);
		createMainPanel(mainWindow);
		createBottomPanel(mainWindow);
		
		// Stage everything to be shown
		mainWindow.pack();
		
		// Center the window
        mainWindow.setLocationRelativeTo(null);
        
        mainWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Actually display the whole jazz
        mainWindow.setVisible(true);
	}
	
	private JPanel createTopPanel(JFrame parent) {

	    JPanel topPanel = new JPanel();
	    topPanel.setLayout(new FlowLayout(FlowLayout.CENTER));

		JLabel versionLabel = new JLabel(Main.PROGRAM_TITLE + " version " + Main.VERSION_NUMBER + " from " + Main.VERSION_DATE);
		topPanel.add(versionLabel);

		parent.add(topPanel, BorderLayout.PAGE_START);
		
		return topPanel;
	}
	
	private JPanel createMainPanel(JFrame parent) {
		
	    JPanel mainPanel = new JPanel();
	    mainPanel.setPreferredSize(new Dimension(800, 500));
	    mainPanel.setLayout(new GridLayout(1, 2));

	    JPanel mainPanelRight = new JPanel();
		String[] cdmList = new String[0];
	    
		scriptListComponent = new JList<String>(cdmList);
		
		MouseListener pageListClickListener = new MouseListener() {
			
			@Override
		    public void mouseClicked(MouseEvent e) {

		         String selectedItem = (String) scriptListComponent.getSelectedValue();

		         for (ScriptTab tab : scriptTabs) {
		        	 if (tab.isItem(selectedItem)) {
		        		 tab.show();
		        	 } else {
		        		 tab.hide();
		        	 }
		         }
		    }

			@Override
			public void mouseEntered(MouseEvent e) {
			}

			@Override
			public void mouseExited(MouseEvent e) {
			}

			@Override
			public void mousePressed(MouseEvent e) {
			}

			@Override
			public void mouseReleased(MouseEvent e) {
			}
		};
		scriptListComponent.addMouseListener(pageListClickListener);
		
		mainPanel.add(scriptListComponent);
	    mainPanel.add(mainPanelRight);

		parent.add(mainPanel, BorderLayout.CENTER);
		
	    return mainPanel;
	}
	
	private JPanel createBottomPanel(JFrame parent) {
		
	    JPanel bottomPanel = new JPanel();
	    bottomPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
	    
	    JButton closeButton = new JButton("Close");
	    closeButton.addActionListener(new ActionListener()
	    {
	      public void actionPerformed(ActionEvent e)
	      {
	        System.exit(0);
	      }
	    });
	    bottomPanel.add(closeButton);

		parent.add(bottomPanel, BorderLayout.PAGE_END);
		
	    return bottomPanel;
	}
	
	private void showGUI() {
		
		mainWindow.pack();
		mainWindow.setVisible(true);
	}
	
	private void openCdmFile() {

		JFileChooser activeCdmPicker;
		
		String lastDirectory = configuration.getValue("lastDirectory");
		
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
				configuration.set("lastDirectory", activeCdmPicker.getCurrentDirectory().getAbsolutePath());
				Directory cdmDir = new Directory(activeCdmPicker.getSelectedFile());
				CdmCtrl.loadCdmDirectory(cdmDir);
				List<String> scripts = CdmCtrl.getScripts();
				scriptListComponent.setListData(scripts.toArray(new String[0]));
				break;

			case JFileChooser.CANCEL_OPTION:
				// cancel was pressed... do nothing for now
				break;
		}
	}

}
