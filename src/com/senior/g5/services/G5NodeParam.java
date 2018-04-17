package com.senior.g5.services;

import java.util.ArrayList;
import java.util.List;

public class G5NodeParam {
	
	private String name;
	private String value;
	private List<G5NodeParam> nodeChildren;
	
	public G5NodeParam(String name) {
		this.name = name;
		this.value = "";
	}
	
	public G5NodeParam(String name, String value) {
		this.name = name;
		this.value = value;
	}
	
	public void addChild(G5NodeParam child) {
		if (nodeChildren == null) {
			nodeChildren = new ArrayList<G5NodeParam>();			
		}
		nodeChildren.add(child);
	}
	
	public String getName() {
		return name;
	}
	
	public String getValue() {
		return value;
	}
	
	public List<G5NodeParam> getChildren() {
		return nodeChildren;
	}

}
