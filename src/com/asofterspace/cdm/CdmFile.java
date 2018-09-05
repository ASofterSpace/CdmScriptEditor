package com.asofterspace.cdm;

import com.asofterspace.toolbox.io.File;
import com.asofterspace.toolbox.io.XmlFile;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class CdmFile extends XmlFile {

	// this prefix is in the MIDDLE of the version string, a PREFIX to the actual version;
	// but preceded by whatever nonsense the CDM-writing-application wrote into it!
	private final static String CDM_VERSION_PREFIX = "/ConfigurationTracking/";

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

		List<CdmScript> results = new ArrayList<>();

		NodeList elements = getRoot().getChildNodes();

		int len = elements.getLength();

		for (int i = 0; i < len; i++) {
			try {
				Node elem = elements.item(i);
				if ("script".equals(elem.getNodeName())) {
					NamedNodeMap scriptAttributes = elem.getAttributes();
					String scriptContent = scriptAttributes.getNamedItem("scriptContent").getNodeValue();
					String scriptName = scriptAttributes.getNamedItem("name").getNodeValue();
					String scriptNamespace = scriptAttributes.getNamedItem("namespace").getNodeValue();
					String scriptId = scriptAttributes.getNamedItem("xmi:id").getNodeValue();
					CdmScript script = new CdmScript(this, scriptName, scriptNamespace, scriptId, scriptContent);
					results.add(script);
				}
			} catch (NullPointerException e) {
				// ignore script nodes that do not contain name or scriptContent attributes
				System.err.println("ERROR: A script in " + getFilename() + " does not have a properly assigned name or scriptContent attribute and will be ignored!");
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

	public void setScriptSourceCode(String scriptName, String scriptContent) {

		NodeList elements = getRoot().getChildNodes();

		int len = elements.getLength();

		for (int i = 0; i < len; i++) {
			try {
				Node elem = elements.item(i);
				if ("script".equals(elem.getNodeName())) {
					NamedNodeMap scriptAttributes = elem.getAttributes();
					String scriptNameFound = scriptAttributes.getNamedItem("name").getNodeValue();
					if (scriptNameFound.equals(scriptName)) {
						Node scriptContentNode = scriptAttributes.getNamedItem("scriptContent");
						scriptContentNode.setNodeValue(scriptContent);
					}
				}
			} catch (NullPointerException e) {
				// ignore script nodes that do not contain name or scriptContent attributes
				System.err.println("ERROR: A script in " + getFilename() + " does not have a properly assigned name or scriptContent attribute and will be ignored!");
			}
		}
	}

	public void setScriptName(String scriptName, String newScriptName) {
	
		NodeList elements = getRoot().getChildNodes();

		int len = elements.getLength();

		for (int i = 0; i < len; i++) {
			try {
				Node elem = elements.item(i);
				if ("script".equals(elem.getNodeName())) {
					NamedNodeMap scriptAttributes = elem.getAttributes();
					String scriptNameFound = scriptAttributes.getNamedItem("name").getNodeValue();
					if (scriptNameFound.equals(scriptName)) {
						Node scriptNameNode = scriptAttributes.getNamedItem("name");
						scriptNameNode.setNodeValue(newScriptName);
					}
				}
			} catch (NullPointerException e) {
				// ignore script nodes that do not contain name or scriptContent attributes
				System.err.println("ERROR: A script in " + getFilename() + " does not have a properly assigned name attribute and will be ignored!");
			}
		}
	}

}
