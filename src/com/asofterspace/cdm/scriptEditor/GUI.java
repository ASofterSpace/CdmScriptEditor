package com.asofterspace.cdm.scriptEditor;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.FlowLayout;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
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

import com.asofterspace.cdm.CdmCtrl;
import com.asofterspace.cdm.CdmScript;
import com.asofterspace.toolbox.configuration.ConfigFile;
import com.asofterspace.toolbox.io.Directory;
import com.asofterspace.toolbox.web.JSON;


public class GUI implements Runnable {

	private JFrame mainWindow;
	
	private JPanel mainPanelRight;
	
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
		
		openCdmDirectory();
	}

	private void createGUI() {

		JFrame.setDefaultLookAndFeelDecorated(true);
		
		// Create the window
		mainWindow = new JFrame(Main.PROGRAM_TITLE);

		// Add content to the window
		createTopPanel(mainWindow);
		createMainPanel(mainWindow);
		createBottomPanel(mainWindow);
		
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
		GridLayout mainPanelLayout = new GridLayout(1, 2);
		mainPanelLayout.setHgap(10);
		mainPanel.setLayout(mainPanelLayout);

	    mainPanelRight = new JPanel();
		mainPanelRight.setLayout(new CardLayout());
		String[] scriptList = new String[0];
		scriptListComponent = new JList<String>(scriptList);
		scriptTabs = new ArrayList<>();
		
		MouseListener scriptListClickListener = new MouseListener() {
			
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
		scriptListComponent.addMouseListener(scriptListClickListener);
		
		mainPanel.add(scriptListComponent);
		
	    mainPanel.add(mainPanelRight);

		parent.add(mainPanel, BorderLayout.CENTER);
		
	    return mainPanel;
	}
	
	private JPanel createBottomPanel(JFrame parent) {
		
	    JPanel bottomPanel = new JPanel();
	    bottomPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
	    
	    JButton openCdmButton = new JButton("Open CDM Directory");
	    openCdmButton.addActionListener(new ActionListener()
	    {
	      public void actionPerformed(ActionEvent e)
	      {
	        openCdmDirectory();
	      }
	    });
	    bottomPanel.add(openCdmButton);
		
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
	
	private void openCdmDirectory() {

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
				
				List<CdmScript> scripts = CdmCtrl.getScripts();
				scriptTabs = new ArrayList<>();
				for (CdmScript script : scripts) {
					scriptTabs.add(new ScriptTab(mainPanelRight, script));
				}
				
				String[] strScripts = new String[scripts.size()];
				for (int i = 0; i < scripts.size(); i++) {
					strScripts[i] = scripts.get(i).getName();
				}
				scriptListComponent.setListData(strScripts);

				break;

			case JFileChooser.CANCEL_OPTION:
				// cancel was pressed... do nothing for now
				break;
		}
	}

}
