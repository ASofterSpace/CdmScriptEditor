package com.asofterspace.cdm;

import com.asofterspace.toolbox.io.File;
import com.asofterspace.toolbox.io.XmlFile;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


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
