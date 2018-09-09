package com.asofterspace.cdm;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class CdmActivity {

	private CdmFile parent;

	private Node thisNode;

	private String name;

	private String baseElementId;

	private String hasPredictedValue;

	private String permittedRouteId;

	private String defaultRouteId;

	private String defaultServiceAccessPointId;

	private String alias;

	private String id;


	// the activityNode is a monitoringControlElementAspects node which is a child of a monitoringControlElement node,
	// and NOT a direct child of a CI root node!
	public CdmActivity(CdmFile parent, Node activityNode) {

		NamedNodeMap attributes = activityNode.getAttributes();

		this.parent = parent;

		this.thisNode = activityNode;

		this.name = attributes.getNamedItem("name").getNodeValue();

		this.baseElementId = attributes.getNamedItem("baseElement").getNodeValue();

		this.hasPredictedValue = attributes.getNamedItem("hasPredictedValue").getNodeValue();

		this.permittedRouteId = attributes.getNamedItem("permittedRoute").getNodeValue();

		this.defaultRouteId = attributes.getNamedItem("defaultRoute").getNodeValue();

		this.defaultServiceAccessPointId = attributes.getNamedItem("defaultServiceAccessPoint").getNodeValue();

		NodeList aliassesAndArgs = activityNode.getChildNodes();

		int len = aliassesAndArgs.getLength();

		for (int i = 0; i < len; i++) {
			Node aliasOrArg = aliassesAndArgs.item(i);
			if ("aliases".equals(aliasOrArg.getNodeName())) {
				this.alias = aliasOrArg.getAttributes().getNamedItem("alias").getNodeValue();
			}
		}

		// TODO :: also take care of arguments, e.g.
		// <arguments xsi:type="monitoringcontrolmodel:EngineeringArgument" xmi:id="_AAAAACqUzEIAAAAAAAABoA" engineeringArgumentDefinition="______91W8zUAAAAAAAAB8w">
		//   <engineeringDefaultValue xsi:type="..." xmi:id="_2Fdl8qboEeiEK5o2bemhxQ" parameter="_2Fdl8KboEeiEK5o2bemhxQ"/>
		// </arguments>

		this.id = attributes.getNamedItem("xmi:id").getNodeValue();
	}

	public CdmFile getParent() {
		return parent;
	}

	public String getName() {
		return name;
	}

	public boolean isDefinition() {
		// if it has a baseElement attribute then it is a "real" activity, if it does not have it, then it is a definition
		return baseElementId == null;
	}

	public String getAlias() {
		return alias;
	}

	public String getId() {
		return id;
	}

	public void setName(String newName) {

		NamedNodeMap scriptNodeAttributes = thisNode.getAttributes();

		Node scriptNameNode = scriptNodeAttributes.getNamedItem("name");

		if (scriptNameNode == null) {
			return;
		}

		scriptNameNode.setNodeValue(newName);

		name = newName;
	}

	public void delete() {

		// delete the activity itself from the parent file
		parent.getRoot().removeChild(thisNode);

		// TODO - delete the definition as well if this is a regular activity, or delete the activity/ies as well if this is a definition
	}

}
