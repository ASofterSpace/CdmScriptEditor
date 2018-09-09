package com.asofterspace.cdm;

import java.util.List;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class CdmScript2Activity {

	private CdmFile parent;
	
	private Node thisNode;
	
	private String name;
	
	private String namespace;
	
	private String id;
	
	private String mappedActivityFilename;
	private String mappedActivityId;
	private String mappedScriptFilename;
	private String mappedScriptId;


	public CdmScript2Activity(CdmFile parent, Node script2ActivityNode) {
	
		NamedNodeMap attributes = script2ActivityNode.getAttributes();

		this.parent = parent;
		
		this.thisNode = script2ActivityNode;
		
		this.name = attributes.getNamedItem("name").getNodeValue();
		
		this.namespace = attributes.getNamedItem("namespace").getNodeValue();
		
		this.id = attributes.getNamedItem("xmi:id").getNodeValue();
		
		NodeList elements = script2ActivityNode.getChildNodes();

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
	
	public void setName(String newName) {
	
		NamedNodeMap script2ActivityNodeAttributes = thisNode.getAttributes();
		
		Node script2ActivityNameNode = script2ActivityNodeAttributes.getNamedItem("name");
		
		if (script2ActivityNameNode == null) {
			return;
		}
		
		script2ActivityNameNode.setNodeValue(newName);
		
		name = newName;
	}
	
	public String getMappedActivityFilename() {
		return mappedActivityFilename;
	}
	
	public String getMappedActivityId() {
		return mappedActivityId;
	}
	
	public CdmActivity getMappedActivity() {
	
		if (mappedActivityId == null) {
			return null;
		}
		
		List<CdmActivity> activities = CdmCtrl.getActivities();
		
		if (activities == null) {
			return null;
		}
		
		for (CdmActivity activity : activities) {
			if (mappedActivityId.equals(activity.getId())) {
				return activity;
			}
		}
		
		return null;
	}
	
	public String getMappedScriptFilename() {
		return mappedScriptFilename;
	}
	
	public String getMappedScriptId() {
		return mappedScriptId;
	}
	
	public void delete() {
	
		// delete the script itself from the parent file
		thisNode.getParentNode().removeChild(thisNode);
	}

}
