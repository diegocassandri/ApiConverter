package com.senior.g5.services.genericConsult;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Scanner;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.soap.SOAPMessage;

import org.json.JSONArray;
import org.json.JSONObject;

import com.senior.g5.services.G5Service;
import com.senior.g5.services.G5Utils;
import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;

@WebServlet("/GenericConsult")
public class GenericConsult extends HttpServlet {
	
	private static final String SERVICE = "com.senior.g7.wf";
	private static final String PORT = "genericConsult";
	private HashMap<String, String> columsDef;
	
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)	throws ServletException, IOException {
		response.setContentType("application/json");
		G5Utils.allowAnyOrigin(response);				
		
		final String consultName = request.getParameter("consult");
		
		JSONObject consultDefinition = readConsult(consultName);
				
		String dataSourceFilter = request.getParameter("$filter");
		String dataSourceRow = request.getParameter("$top");

		
		try {			
			final String server = request.getParameter(G5Service.PARAMETER_SERVER);
			final String module = request.getParameter(G5Service.PARAMETER_MODULE);
			String user = request.getHeader(G5Service.PARAMETER_USER);
			String pass = request.getHeader(G5Service.G7PARAMETER_AUTORIZATION);
			Integer encType = getEncType(request, G5Service.PARAMETER_ENCRYPTIONTYPE);
			if(encType==G5Service.ENCRIPTION_TYPE_PLAIN_FROM_URL){
				user = request.getParameter(G5Service.PARAMETER_USER);
				pass = request.getParameter(G5Service.PARAMETER_PASS);
				encType = G5Service.ENCRIPTION_TYPE_TEXT_PLAIN;
			}
			if(encType == null){
				encType = G5Service.ENCRIPTION_TYPE_TOKEN_G7;
			}
			G5Service g5Service = new G5Service(server, module, SERVICE, encType);
			
			String envelope = buildEnvelope(PORT,user,pass,encType, consultDefinition, dataSourceFilter, request);
			SOAPMessage g5Resp = g5Service.callPort(user, pass, envelope);			
			JSONObject json = G5Utils.parseResponseToJSON(g5Resp, PORT, null);
			if(json.has("jsonresponse")){
				String jsonresponse = json.getString("jsonresponse");
				jsonresponse = new String(Base64.decode(jsonresponse));
				response.getWriter().write(jsonresponse);
			}
		} catch (Exception e) {			
			e.printStackTrace();
			throw new RuntimeException(e);
		}			
	}

	private String buildEnvelope(String port, String user, String pass, Integer encriptionType, JSONObject parameters, String dataSourceFilter, HttpServletRequest request) {
		
		String colums = buildColums(parameters);
		String tables = buildTables(parameters);		
		String join = buildJoin(parameters);		
		String where = buildWhere(parameters, dataSourceFilter, request);		
		String order = buildOrder(parameters);
		
		String envelope = "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ser=\"http://services.senior.com.br\">"
				+ "<SOAP-ENV:Header/><SOAP-ENV:Body>"
				+ "<ser:%s><user>%s</user><password>%s</password><encryption>%d</encryption><encrypted>%d</encrypted><pass>senior</pass>"
				+ "<parameters>"
				+ "%s" //colums
				+ "%s" //tables
				+ "%s" //join
				+ "%s" //where
				+ "%s" //order
				+ "</parameters>"
				+ "</ser:%s></SOAP-ENV:Body></SOAP-ENV:Envelope>";
		final String format = String.format(envelope, PORT, user, pass, encriptionType, encriptionType, colums, tables, join, where, order, PORT);
		System.out.println("GENERICCONSULT: "+format);
		return format;
	}

	private String buildOrder(JSONObject parameters) {
		JSONArray json;
		String order = "";
		json = parameters.getJSONArray("order");
		final String orderTpl = "<order><columname>%s</columname></order>";
		for (int i = 0; i < json.length(); i++) {
			final JSONObject obj = json.getJSONObject(i);
			order+=String.format(orderTpl, obj.getString("order"));
		}
		return order;
	}

	private String buildWhere(JSONObject parameters, String dataSourceFilter, HttpServletRequest request) {
		JSONArray json = parameters.getJSONArray("where");
		String where = "";
		final String whereTpl = "<where><columtype>%s</columtype><columname>%s</columname><operator>%s</operator><columvalue>%s</columvalue></where>";
		for (int i = 0; i < json.length(); i++) {
			final JSONObject obj = json.getJSONObject(i);
			final String type = obj.getString("columtype");
			final String columName = obj.getString("columname");
			final String operator = obj.has("operator")?obj.getString("operator"):"";
			String value = "";
			String columValue = obj.get("columvalue").toString();
			if(columValue.startsWith(":")){
				columValue = columValue.replace(":", "");
				value = request.getParameter(columValue);
				if(value== null){
					throw new RuntimeException(String.format("Não recebido o parametro %s do where", columValue));
				}
			} else {
				value = obj.get("columvalue").toString();
			}
			where+=String.format(whereTpl, type, columName, operator, value);
		}
		where+=buildWhereWithDataSourceFilter(dataSourceFilter);
		return where;
	}
	
	private String buildWhereWithDataSourceFilter(String dataSourceFilter) {				
		//filter sample numemp eq '1' and datadm gt '01/01/1980' and valsal eq '1457,93'
		//TODO
		String where = "";
		final String whereTpl = "<where><columtype>%s</columtype><columname>%s</columname><operator>%s</operator><columvalue>%s</columvalue></where>";
		if(dataSourceFilter!=null){
			String[] filters = dataSourceFilter.split(" and ");
			for (String filter : filters) {
				String[] elements = filter.split(" ");
				String columName = elements[0];
				String operator = parseDataSourceOperator(elements[1]);
				String value = elements[2].substring(1, elements[2].length()-1); //remove ''
				String type = columsDef.get(columName);
				where+=String.format(whereTpl, type, columName, operator, value);
			}			
		}		
		return "";
	}

	private String buildJoin(JSONObject parameters) {
		JSONArray json;
		String join = "";
		json = parameters.getJSONArray("join");
		final String joinTpl = "<join><colum1>%s</colum1><colum2>%s</colum2></join>";
		for (int i = 0; i < json.length(); i++) {
			final JSONObject obj = json.getJSONObject(i);
			join+=String.format(joinTpl, obj.getString("colum1"), obj.getString("colum2"));
		}
		return join;
	}

	private String buildTables(JSONObject parameters) {
		JSONArray json;
		String tables = "";
		json = parameters.getJSONArray("tables");
		final String tableTpl = "<tables><tablename>%s</tablename></tables>";
		for (int i = 0; i < json.length(); i++) {
			final JSONObject obj = json.getJSONObject(i);
			tables+=String.format(tableTpl, obj.getString("tablename"));
		}
		return tables;
	}

	private String buildColums(JSONObject parameters) {
		String colums = "";
		columsDef = new HashMap<String, String>();
		JSONArray json = parameters.getJSONArray("colums");
		final String columTpl = "<colums><columtype>%s</columtype><columname>%s</columname><enumname>%s</enumname></colums>";
		for (int i = 0; i < json.length(); i++) {
			final JSONObject obj = json.getJSONObject(i);
			final String columName = obj.getString("columname");
			final String columType = obj.getString("type");
			final String enumName = obj.has("enumname") ? obj.getString("enumname") : "";
			columsDef.put(columName, columType);
			colums+=String.format(columTpl, columType, columName, enumName);
		}
		return colums;
	}

	private JSONObject readConsult(String consultName) {
		String result = "";
		InputStream resourceAsStream = this.getServletContext().getResourceAsStream("/consults.json");
		Scanner scan = new Scanner(resourceAsStream);
		while(scan.hasNext()){
			result+= scan.next();
		}
		JSONArray consults = new JSONArray(result);
		for (int i = 0; i < consults.length(); i++) {				
			final JSONObject consultDefinition = consults.getJSONObject(i);
			if(consultDefinition.has(consultName)){
				return consultDefinition.getJSONObject(consultName);
			}
		}
		throw new RuntimeException(String.format("Consulta não definida: %s",consultName));
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("application/json");
		doPost(request, response);
	}	
	
	private String parseDataSourceOperator(String operator){
		/*
		   equals: 'eq',
	       notEqualTo: 'ne',
	       greaterThan: 'gt',
	       lessThan: 'lt',
	       greaterThanOrEqualTo: 'ge',
	       lessThanOrEqualTo: 'le',
	    */
		if(operator.equalsIgnoreCase("eq")){
			return "=";
		} else if(operator.equalsIgnoreCase("ne")){
			return "<>";
		} else if(operator.equalsIgnoreCase("gt")){
			return ">";
		} else if(operator.equalsIgnoreCase("lt")){
			return "<";
		} else if(operator.equalsIgnoreCase("ge")){
			return ">=";
		} else if(operator.equalsIgnoreCase("le")){
			return "<=";
		}
		return null;
	}
	
	private Integer getEncType(HttpServletRequest request, String paramEncType) {
		String type = request.getHeader(paramEncType);
		try {
			return Integer.parseInt(type);				
		} catch (NumberFormatException e) {	
			type = request.getParameter(paramEncType);				
			try {
				return Integer.parseInt(type);	
			} catch (NumberFormatException e2) {}
		}
		return null;
	}
}
