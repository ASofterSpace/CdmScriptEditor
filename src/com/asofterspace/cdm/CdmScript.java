package com.asofterspace.cdm;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class CdmScript {

	private CdmFile parent;
	
	private String name;
	
	private String namespace;
	
	private String id;
	
	private String content;
	

	public CdmScript(CdmFile parent, Node scriptNode) {
	
		NamedNodeMap scriptAttributes = scriptNode.getAttributes();

		this.parent = parent;
		
		this.name = scriptAttributes.getNamedItem("name").getNodeValue();
		
		this.namespace = scriptAttributes.getNamedItem("namespace").getNodeValue();
		
		this.id = scriptAttributes.getNamedItem("xmi:id").getNodeValue();
		
		this.content = scriptAttributes.getNamedItem("scriptContent").getNodeValue();
	}
	
	public CdmFile getParent() {
		return parent;
	}
	
	public String getName() {
		return name;
	}
	
	public String getNamespace() {
		return namespace;
	}
	
	public String getId() {
		return id;
	}
	
	public String getSourceCode() {
		return content;
	}
	
	private Node getScriptNode() {
	
		NodeList elements = parent.getRoot().getChildNodes();

		int len = elements.getLength();

		for (int i = 0; i < len; i++) {
			try {
				Node elem = elements.item(i);
				if ("script".equals(elem.getNodeName())) {
					NamedNodeMap scriptAttributes = elem.getAttributes();
					String scriptIdFound = scriptAttributes.getNamedItem("xmi:id").getNodeValue();
					if (scriptIdFound.equals(id)) {
						return elem;
					}
				}
			} catch (NullPointerException e) {
				// ignore script nodes that do not contain name or scriptContent attributes
				System.err.println("ERROR: A script in " + parent.getFilename() + " does not have a properly assigned attribute and will be ignored!");
			}
		}
		
		return null;
	}
	
	private NamedNodeMap getScriptNodeAttributes() {
	
		Node scriptNode = getScriptNode();
		
		if (scriptNode == null) {
			return null;
		}
		
		return scriptNode.getAttributes();
	}
	
	public void setSourceCode(String scriptContent) {
	
		NamedNodeMap scriptNodeAttributes = getScriptNodeAttributes();
		
		Node scriptContentNode = scriptNodeAttributes.getNamedItem("scriptContent");
		
		if (scriptContentNode == null) {
			return;
		}
		
		scriptContentNode.setNodeValue(scriptContent);
		
		content = scriptContent;
	}
	
	public void setName(String newName) {
	
		NamedNodeMap scriptNodeAttributes = getScriptNodeAttributes();
		
		Node scriptNameNode = scriptNodeAttributes.getNamedItem("name");
		
		if (scriptNameNode == null) {
			return;
		}
		
		scriptNameNode.setNodeValue(newName);
		
		name = newName;
	}
	
	/**
	 * Get all script2Activity mappings associated with this particular script - there could be several
	 * mappings mapping to this script!
	 */
	public List<CdmScript2Activity> getAssociatedScript2Activities() {
	
		List<CdmScript2Activity> results = new ArrayList<>();
		
		List<CdmFile> scriptToActivityMapperCis = CdmCtrl.getScriptToActivityMappers();
	
		String parentFilename = parent.getLocalFilename();
	
		for (CdmFile scriptToActivityMapperCi : scriptToActivityMapperCis) {
			
			List<CdmScript2Activity> script2Activities = scriptToActivityMapperCi.getScript2Activities();
			
			for (CdmScript2Activity script2Activity : script2Activities) {
				// check if a script to activity mapper maps the script id of this particular script!
				if (script2Activity.mapsScript(parentFilename, id)) {
					results.add(script2Activity);
				}
			}
		}
		
		return results;
	}
	
	public void delete() {

		// delete entries from the script to activity mapper - as there could be several
		// activities mapped to this script...
		List<CdmScript2Activity> script2Activities = getAssociatedScript2Activities();

		// ... we iterate...
		for (CdmScript2Activity script2Activity : script2Activities) {

			// ... we delete the associated activities, if the user wants us to ...
			// TODO
			
			// ... and we delete the mappings themselves!
			script2Activity.delete();
		}
		
		// delete the script itself from the parent file
		Node scriptNode = getScriptNode();
		
		if (scriptNode == null) {
			return;
		}
		
		parent.getRoot().removeChild(scriptNode);

		// check if there are still elements left now, and if not, delete the entire parent file
		NodeList elements = parent.getRoot().getChildNodes();
		
		// assume there are no elements left...
		boolean noElementsLeft = true;
		
		// ... iterate over all nodes...
		int len = elements.getLength();
		
		for (int i = 0; i < len; i++) {
			Node elem = elements.item(i);
		
			// ... ignoring text nodes...
			if (!"#text".equals(elem.getNodeName())) {
				// ... but if there are any others, then elements are actually left!
				noElementsLeft = false;
				break;
			}
		}
		
		// delete the entire parent file
		// (or actually set a deleted flag, to delete it when save() is called ^^)
		if (noElementsLeft) {
			parent.delete();
		}
	}

}
