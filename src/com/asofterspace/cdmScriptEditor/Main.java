package com.asofterspace.cdmScriptEditor;

import javax.swing.SwingUtilities;

import com.asofterspace.toolbox.configuration.ConfigFile;
import com.asofterspace.toolbox.web.JSON;

public class Main {

	public static String PROGRAM_TITLE = "CDM Script Editor";
	public static String VERSION_NUMBER = "0.0.0.1";
	public static String VERSION_DATE = "26. August 2018";
	
	public static void main(String[] args) {

		ConfigFile config = new ConfigFile("settings");

		// create a default config file, if necessary
		if (config.getAllContents().isEmpty()) {

			config.setAllContents(new JSON("{\"pages\": []}"));
		}

		SwingUtilities.invokeLater(new GUI(config));
	}
	
}
