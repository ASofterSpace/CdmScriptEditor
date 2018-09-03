package com.asofterspace.cdm;

public class CdmScript {

	private CdmFile parent;
	
	private String name;
	
	private String content;
	

	public CdmScript(CdmFile parent, String name, String content) {
	
		this.parent = parent;
		
		this.name = name;
		
		this.content = content;
	}
	
	public String getName() {
	
		return name;
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
