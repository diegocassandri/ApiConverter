package com.senior.g5.services;
import java.util.HashMap;

import javax.xml.soap.SOAPMessage;

import org.json.JSONObject;


public class Example {
	
	public static void main(String[] args) {		
		
		final int encryptionType = 0;
		JSONObject jsonResponse = null;
		try {
			/*G5Service g5Service = new G5Service("http://nbbnu005017:8080", "rubi", "teste",	encryptionType);
			final String user = "senior";
			final String pass = "senior";
			HashMap<String, String> params = new HashMap<String, String>();
			params.put("valora", "1");
			params.put("valorb", "2");
			final String portName = "testews";
			SOAPMessage response = null;
			response = g5Service.callPort(portName, user, pass, params);
			System.out.println("\nrestrequest");
			jsonResponse = G5Utils.parseResponseToJSON(response, portName);
			*/
			
			/*String soapRequest = "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ser=\"http://services.senior.com.br\"><SOAP-ENV:Header/><SOAP-ENV:Body><ser:getColaborador><user>senior</user><password>senior</password><encryption>0</encryption><pass>senior</pass><encrypted>0</encrypted><parameters><searchTerm>marc</searchTerm></parameters></ser:getColaborador></SOAP-ENV:Body></SOAP-ENV:Envelope>";
			System.out.println("\nsoaprequest");
			response = g5Service.callPort(user, pass, soapRequest);
			String str = G5Utils.parseResponseToString(response);
			System.out.println("Response: "+str);*/
			
			/*G5Service g5Service = new G5Service("http://nbbnu005017:8080", "rubi", "com.senior.g7.wf.erp",	encryptionType);
			final String user = "senior";
			final String pass = "senior";
			HashMap<String, G5NodeParam> params = new HashMap<String, G5NodeParam>();
			params.put("valora", new G5NodeParam("valora", "1"));
			params.put("valorb", new G5NodeParam("valor2", "2"));
			final String portName = "getEmpresas";
			SOAPMessage response = null;
			response = g5Service.callPort(portName, user, pass, params);
			System.out.println("\nrestrequest");
			jsonResponse = G5Utils.parseResponseToJSON(response, portName, null);*/
		} catch (Exception e) {			
			e.printStackTrace();
		}		
		
		System.out.println(jsonResponse);		

	}
}
