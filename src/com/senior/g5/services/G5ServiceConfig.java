package com.senior.g5.services;

public class G5ServiceConfig {
	
	String server;
	String module;
	String service;
	String port;
	String separator;
	
	public G5ServiceConfig(String server, String module, String service, String port, String separator) {
		super();
		this.server = server;
		this.module = module;
		this.service = service.replaceAll("\\.", "_");
		this.port = port;
		this.separator = separator;
	}
	

}
