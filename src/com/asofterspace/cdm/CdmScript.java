package com.asofterspace.cdm;

public class CdmScript {

	private CdmFile parent;
	
	private String name;
	
	private String namespace;
	
	private String id;
	
	private String content;
	

	public CdmScript(CdmFile parent, String name, String namespace, String id, String content) {
	
		this.parent = parent;
		
		this.name = name;
		
		this.namespace = namespace;
		
		this.id = id;
		
		this.content = content;
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
	
	public void setSourceCode(String content) {
	
		parent.setScriptSourceCode(name, content);
	}
	
	public void save() {
	
		parent.save();
	}

}
