package com.asofterspace.cdm.scriptEditor;

import javax.swing.SwingUtilities;

import com.asofterspace.toolbox.configuration.ConfigFile;
import com.asofterspace.toolbox.Utils;
import com.asofterspace.toolbox.web.JSON;

public class Main {

	public final static String PROGRAM_TITLE = "CDM Script Editor";
	public final static String VERSION_NUMBER = "0.1.0.0(" + Utils.TOOLBOX_VERSION_NUMBER + ")";
	public final static String VERSION_DATE = "26. August 2018 - 10. September 2018";

	public static void main(String[] args) {
	
		// let the Utils know in what program it is being used
		Utils.setProgramTitle(PROGRAM_TITLE);
		Utils.setVersionNumber(VERSION_NUMBER);
		Utils.setVersionDate(VERSION_DATE);

		ConfigFile config = new ConfigFile("settings");

		// create a default config file, if necessary
		if (config.getAllContents().isEmpty()) {
			config.setAllContents(new JSON("{\"lastDirectory\": \"\"}"));
		}

		SwingUtilities.invokeLater(new GUI(config));
	}

}
