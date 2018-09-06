package com.asofterspace.cdm;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class CdmScript2Activity {

	private CdmFile parent;
	
	private String name;
	
	private String namespace;
	
	private String id;
	
	private String mappedActivityFilename;
	private String mappedActivityId;
	private String mappedScriptFilename;
	private String mappedScriptId;


	public CdmScript2Activity(CdmFile parent, Node scriptNode) {
	
		NamedNodeMap scriptAttributes = scriptNode.getAttributes();

		this.parent = parent;
		
		this.name = scriptAttributes.getNamedItem("name").getNodeValue();
		
		this.namespace = scriptAttributes.getNamedItem("namespace").getNodeValue();
		
		this.id = scriptAttributes.getNamedItem("xmi:id").getNodeValue();
		
		NodeList elements = scriptNode.getChildNodes();

		int len = elements.getLength();

		for (int i = 0; i < len; i++) {
			
			Node elem = elements.item(i);
			NamedNodeMap elemAttributes = elem.getAttributes();
			
			if (elemAttributes == null) {
				continue;
			}
			
			Node hrefNode = elemAttributes.getNamedItem("href");
			
			if (hrefNode == null) {
				continue;
			}
			
			String href = hrefNode.getNodeValue();
			
			if (href == null) {
				continue;
			}
			
			String[] hrefSplit = href.split("#");
			if (hrefSplit.length > 1) {
				if ("activity".equals(elem.getNodeName())) {
					this.mappedActivityFilename = hrefSplit[0];
					this.mappedActivityId = hrefSplit[1];
				}
				if ("script".equals(elem.getNodeName())) {
					this.mappedScriptFilename = hrefSplit[0];
					this.mappedScriptId = hrefSplit[1];
				}
			}
		}
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
	
	public boolean mapsScript(String cdmFilename, String scriptId) {
	
		if (!cdmFilename.equals(mappedScriptFilename)) {
			return false;
		}
		
		if (!scriptId.equals(mappedScriptId)) {
			return false;
		}
		
		return true;
	}
	
	private Node getScript2ActivityNode() {
	
		NodeList elements = parent.getRoot().getChildNodes();

		int len = elements.getLength();

		for (int i = 0; i < len; i++) {
			try {
				Node elem = elements.item(i);
				if ("scriptActivityImpl".equals(elem.getNodeName())) {
					NamedNodeMap scriptAttributes = elem.getAttributes();
					String scriptIdFound = scriptAttributes.getNamedItem("xmi:id").getNodeValue();
					if (scriptIdFound.equals(id)) {
						return elem;
					}
				}
			} catch (NullPointerException e) {
				// ignore script nodes that do not contain name or scriptContent attributes
				System.err.println("ERROR: A scriptActivityImpl in " + parent.getFilename() + " does not have a properly assigned attribute and will be ignored!");
			}
		}
		
		return null;
	}
	
	private NamedNodeMap getScript2ActivityNodeAttributes() {
	
		Node script2ActivityNode = getScript2ActivityNode();
		
		if (script2ActivityNode == null) {
			return null;
		}
		
		return script2ActivityNode.getAttributes();
	}
	
	public void setName(String newName) {
	
		NamedNodeMap script2ActivityNodeAttributes = getScript2ActivityNodeAttributes();
		
		Node script2ActivityNameNode = script2ActivityNodeAttributes.getNamedItem("name");
		
		if (script2ActivityNameNode == null) {
			return;
		}
		
		script2ActivityNameNode.setNodeValue(newName);
	}
	
	public void delete() {
	
		// delete the script itself from the parent file
		Node script2ActivityNode = getScript2ActivityNode();
		
		if (script2ActivityNode == null) {
			return;
		}
		
		parent.getRoot().removeChild(script2ActivityNode);
	}

}
