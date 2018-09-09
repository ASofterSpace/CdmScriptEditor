package com.asofterspace.cdm;

import com.asofterspace.toolbox.io.File;
import com.asofterspace.toolbox.io.XmlFile;
import com.asofterspace.toolbox.Utils;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * This class abstracts one particular CDM file (or, as one might call it, one particular configuration item.)
 * It could be a ScriptCI, a Script2ActivityMapperCI, an McmCI, ...
 */
public class CdmFile extends XmlFile {

	// this prefix is in the MIDDLE of the version string, a PREFIX to the actual version;
	// but preceded by whatever nonsense the CDM-writing-application wrote into it!
	private final static String CDM_VERSION_PREFIX = "/ConfigurationTracking/";
	
	private String ciType;
	
	private boolean deleted = false;

	/**
	 * You can construct a CdmFile instance by basing it on an existing file object.
	 */
	public CdmFile(File regularFile) {

		super(regularFile);
		
		ciType = getRoot().getNodeName();
	}
	
	public String getCiType() {
		return ciType;
	}

	/**
	 * Get all the scripts defined in this CDM file
	 * (this does not check if this even is a ScriptCI - you should check it first, to not search through others forever ^^)
	 */
	public List<CdmScript> getScripts() {

		List<CdmScript> results = new ArrayList<>();
		
		if (deleted) {
			return results;
		}

		NodeList elements = getRoot().getChildNodes();

		int len = elements.getLength();

		for (int i = 0; i < len; i++) {
			try {
				Node elem = elements.item(i);
				if ("script".equals(elem.getNodeName())) {
					results.add(new CdmScript(this, elem));
				}
			} catch (NullPointerException e) {
				// ignore script nodes that do not contain name or scriptContent attributes
				System.err.println("ERROR: A script in " + getFilename() + " does not have a properly assigned attribute and will be ignored!");
			}
		}

		return results;
	}
	
	/**
	 * Get all the script to activity mapper entries defined in this CDM file
	 * (this does not check if this even is a Script2ActivityMapperCI - you should check it first, to not search through others forever ^^)
	 */
	public List<CdmScript2Activity> getScript2Activities() {

		List<CdmScript2Activity> results = new ArrayList<>();
		
		if (deleted) {
			return results;
		}

		NodeList elements = getRoot().getChildNodes();

		int len = elements.getLength();

		for (int i = 0; i < len; i++) {
			try {
				Node elem = elements.item(i);
				if ("scriptActivityImpl".equals(elem.getNodeName())) {
					results.add(new CdmScript2Activity(this, elem));
				}
			} catch (NullPointerException e) {
				// ignore script nodes that do not contain name or scriptContent attributes
				System.err.println("ERROR: A scriptActivityImpl in " + getFilename() + " does not have a properly assigned attribute and will be ignored!");
			}
		}

		return results;
	}
	
	/**
	 * Get all the activities defined in this CDM file
	 * (this does not check if this even is an McmCI - you should check it first, to not search through others forever ^^)
	 */
	public List<CdmActivity> getActivities() {

		List<CdmActivity> results = new ArrayList<>();

		if (deleted) {
			return results;
		}

		NodeList elements = getRoot().getChildNodes();

		int len = elements.getLength();

		for (int i = 0; i < len; i++) {
			try {
				Node mce = elements.item(i);
				if ("monitoringControlElement".equals(mce.getNodeName())) {
					NodeList mceAspects = mce.getChildNodes();
					int mceAspectLen = mceAspects.getLength();
					for (int j = 0; j < mceAspectLen; j++) {
						Node mceAspect = mceAspects.item(j);
						if ("monitoringControlElementAspects".equals(mceAspect.getNodeName())) {
							if ("monitoringcontrolmodel:Activity".equals(mceAspect.getAttributes().getNamedItem("xsi:type").getNodeValue())) {
								results.add(new CdmActivity(this, mceAspect));
							}
						}
					}
				}
			} catch (NullPointerException e) {
				System.err.println("ERROR: The " + Utils.th(i) + " child node in " + getFilename() + " does not have a properly assigned attribute and will be ignored!");
			}
		}

		return results;
	}
	
	/**
	 * Get the CDM version that this CDM file belongs to, or null if none can be identified.
	 */
	public String getCdmVersion() {

		try {
			Node root = getRoot();
			
			NamedNodeMap scriptAttributes = root.getAttributes();
			String cdmVersion = scriptAttributes.getNamedItem("xmlns:configurationcontrol").getNodeValue();

			if (cdmVersion.contains(CDM_VERSION_PREFIX)) {
				cdmVersion = cdmVersion.substring(cdmVersion.indexOf(CDM_VERSION_PREFIX) + CDM_VERSION_PREFIX.length());
			}
			
			return cdmVersion;
			
		} catch (NullPointerException e) {
		
			return null;
		}
	}
	
	public void delete() {
	
		// remember for later that we have been deleted
		deleted = true;
	}
	
	public void save() {

		if (deleted) {
			// if deleted, then actually delete the file from disk
			super.delete();
		} else {
			// actually save the file for real :)
			super.save();
		}
	}

}
