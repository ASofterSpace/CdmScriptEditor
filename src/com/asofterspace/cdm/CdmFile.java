package com.asofterspace.cdm;

import com.asofterspace.toolbox.io.File;
import com.asofterspace.toolbox.io.XmlFile;

import java.util.ArrayList;
import java.util.List;


public class CdmFile extends XmlFile {

	/**
	 * You can construct a CdmFile instance by basing it on an existing file object.
	 */
	public CdmFile(File regularFile) {
	
		super(regularFile);
	}
	
	/**
	 * Get all the scripts defined in this CDM file
	 */
	public List<CdmScript> getScripts() {
	
		return new ArrayList<CdmScript>();
	}

}
