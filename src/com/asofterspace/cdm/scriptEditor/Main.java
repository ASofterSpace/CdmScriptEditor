package com.asofterspace.cdm.scriptEditor;

import javax.swing.SwingUtilities;

import com.asofterspace.toolbox.configuration.ConfigFile;
import com.asofterspace.toolbox.Utils;
import com.asofterspace.toolbox.web.JSON;

public class Main {

	public final static String PROGRAM_TITLE = "CDM Script Editor";
	public final static String VERSION_NUMBER = "0.0.0.5(" + Utils.TOOLBOX_VERSION_NUMBER + ")";
	public final static String VERSION_DATE = "26. August 2018 - 6. September 2018";
	
	public static void main(String[] args) {

		ConfigFile config = new ConfigFile("settings");
		
		// create a default config file, if necessary
		if (config.getAllContents().isEmpty()) {
			config.setAllContents(new JSON("{\"lastDirectory\": \"\"}"));
		}

		SwingUtilities.invokeLater(new GUI(config));
	}
	
}
