package com.senior.g5.services;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Enumeration;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;

public class G5Utils {	
	
	public static SOAPMessage doG5PostRest(HttpServletRequest request, String user, String pass, String token, Integer encType, G5ServiceConfig g5ServiceParams, boolean devMode) throws G5ServiceException {
		
		int encryptionType = encType != null ? encType : 2; // default 2 login integrado com criptografia
				
		if (pass == null) {
			pass = token;			
			encryptionType = 3;
			if(user == null){				
				user = ObterMeusDados.getUserFromToken(token, devMode);
			}
		}
		HashMap<String, G5NodeParam> parameters = getParameters(request);

		G5Service g5Service = new G5Service(g5ServiceParams.server, g5ServiceParams.module, g5ServiceParams.service, encryptionType, g5ServiceParams.separator);
		return g5Service.callPort(g5ServiceParams.port, user, pass, parameters);
	}

	private static HashMap<String, G5NodeParam> getParameters(HttpServletRequest request) throws G5ServiceException {
		HashMap<String, G5NodeParam> parameters = getPOSTParameters(request);		
		try {
			final HashMap<String, G5NodeParam> jsonParameters = getJSONParameters(request);
			if(jsonParameters.size()>0){
				parameters.putAll(jsonParameters);
			}
		} catch (IOException e) {			
			e.printStackTrace();
			throw new G5ServiceException(e.getMessage());
		}
		return parameters;
	}

	public static void allowAnyOrigin(HttpServletResponse response) {
		response.setHeader("Access-Control-Allow-Origin", "*");
		response.setHeader("Access-Control-Allow-Methods", "*");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Methods","GET,HEAD,OPTIONS,POST,PUT");
		response.setHeader(
				"Access-Control-Allow-Headers",
				"Access-Control-Allow-Headers, Origin,Accept, X-Requested-With, Content-Type, Access-Control-Request-Method, Access-Control-Request-Headers, Authorization , encryptiontype, pass, user");
	}

	/*public static SOAPMessage doG7PostRest(HttpServletRequest request, HashMap<String, String> configParams, HashMap<String, String> g5Params) throws G5ServiceException {
		String user = request.getHeader(G5Service.PARAMETER_USER);
		String pass = request.getHeader(G5Service.G7PARAMETER_AUTORIZATION);
		if (pass != null) {
			pass = pass.replace("Bearer ", "");
		} else {
			pass = "";
		}
		int encryptionType = 3; // default 3 token g7

		String server = configParams.get(G5Service.PARAMETER_SERVER);
		String module = configParams.get(G5Service.PARAMETER_MODULE);
		String service = configParams.get(G5Service.PARAMETER_SERVICE).replaceAll("\\.", "_");
		String port = configParams.get(G5Service.PARAMETER_PORT);
		String separator = configParams.get(G5Service.PARAMETER_SEPARATOR);
		try {
			encryptionType = Integer.parseInt(configParams.get(G5Service.PARAMETER_ENCRYPTIONTYPE));
		} catch (Exception e) {
		}
		if (user == null || user.isEmpty()) {
			user = g5Params.get(G5Service.PARAMETER_USER);
		}

		G5Service g5Service = new G5Service(server, module, service,
				encryptionType, separator);
		return g5Service.callPort(port, user, pass, g5Params);
	}

	private static boolean isConfigParameter(String paramName) {
		if (paramName.equalsIgnoreCase(G5Service.PARAMETER_SERVER)
				|| paramName.equalsIgnoreCase(G5Service.PARAMETER_MODULE)
				|| paramName.equalsIgnoreCase(G5Service.PARAMETER_SERVER)
				|| paramName.equalsIgnoreCase(G5Service.PARAMETER_SERVICE)
				|| paramName.equalsIgnoreCase(G5Service.PARAMETER_PORT)
				|| paramName.equalsIgnoreCase(G5Service.PARAMETER_SEPARATOR)) {
			return true;
		}
		return false;
	}*/

	/**
	 * @return um HashMap com todos os parametros da requisição
	 */
	private static HashMap<String, G5NodeParam> getPOSTParameters(HttpServletRequest request) {
		Enumeration<String> requestParams = request.getParameterNames();
		HashMap<String, G5NodeParam> soapParams = new HashMap<String, G5NodeParam>();
		while (requestParams.hasMoreElements()) {
			String param = requestParams.nextElement();
			String valor = request.getParameter(param);
			if (param != "serviceURL" && !valor.isEmpty()) {
				soapParams.put(param, new G5NodeParam(param, valor));
			}
		}
		return soapParams;
	}

	public static HashMap<String, G5NodeParam> getJSONParameters(HttpServletRequest request) throws IOException {
		HashMap<String, G5NodeParam> params = new HashMap<String, G5NodeParam>();
		StringBuffer stringParameters = new StringBuffer();
		String line = null;
		BufferedReader reader = request.getReader();
		try {
			reader.mark(0);
			reader.reset();
		} catch (Exception e) {
			//e.printStackTrace();
		}
		while ((line = reader.readLine()) != null) {
			stringParameters.append(line.toString());
		}
		try {
			if (!stringParameters.toString().isEmpty()) {
				JSONObject jsonParameter = new JSONObject(stringParameters .toString());	
				if(request.getParameter("DEBUG")!=null){
					System.out.println("JSON Parameters: "+jsonParameter);
				}
				String[] atributeNames = JSONObject.getNames(jsonParameter);
				for (String param : atributeNames) {
					Object objValue = jsonParameter.get(param);
					if (objValue instanceof JSONObject) {//é um objeto
						JSONObject jsonObj = (JSONObject) objValue;				
						G5NodeParam node = new G5NodeParam(param);
						final String[] subNames = JSONObject.getNames(jsonObj);
						if(subNames.length>0){
							for (String subParam : subNames) {
								Object subObjValue = jsonObj.get(subParam);
								String subStrValue = parseObjectValue(subParam, subObjValue);
								node.addChild(new G5NodeParam(subParam, subStrValue));
							}					
						}
						params.put(param, node);
					} else {
						String strValue = parseObjectValue(param, objValue);
						G5NodeParam node = new G5NodeParam(param, strValue);
						params.put(param, node);
					}
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
			throw new IOException("Error parsing JSON request string: "	+ e.getMessage());
		}
		return params;
	}

	private static String parseObjectValue(String name, Object objValue) {
		String strValue = "";
		if (objValue instanceof Integer || objValue instanceof Long) {
			strValue = objValue.toString();
		} else if (objValue instanceof Boolean) {
			strValue = ((Boolean) objValue).booleanValue() == true ? "1" : "0";
		} else if (objValue instanceof Float || objValue instanceof Double) {
			strValue = ((Number) objValue).toString();
		} else if (JSONObject.NULL.equals(objValue)) {
			strValue = "";
		} else {
			strValue = objValue.toString();
		}
		return strValue;
	}

	public static JSONObject parseResponseToJSON(SOAPMessage soapResponse,String portName, String dataSourceAttrName) throws G5ServiceException {
		final StringWriter sw = new StringWriter();		
		Transformer transformer = null;
		try {
			transformer = TransformerFactory.newInstance().newTransformer();
			transformer.transform(new DOMSource(soapResponse.getSOAPPart()), new StreamResult(sw));

			JSONObject json = XML.toJSONObject(sw.toString());
			json = json.getJSONObject("S:Envelope").getJSONObject("S:Body");
			if (portName != null && !portName.isEmpty()) {
				String portResponse = "ns2:" + portName + "Response";
				if (json.has(portResponse)) {
					json = json.getJSONObject(portResponse);
					if (json.has("result")) {
						json = json.getJSONObject("result");						
						Object erroExecucao = json.get("erroExecucao");
						if (!(erroExecucao instanceof String)) { //não retornou erroExecução
							if(dataSourceAttrName != null){
								String[] attrs = JSONObject.getNames(json);
								for (String attr : attrs) {
									if(dataSourceAttrName.equals(attr)){
										Object obj = json.get(dataSourceAttrName);
										if(obj != null && !(obj instanceof JSONArray)){
											final JSONArray jsonArray = new JSONArray();
											jsonArray.put(obj);
											json.put(dataSourceAttrName, jsonArray);
											return json;
										}
									}
								}
							}
							return json;
						} else {
							throw new G5ServiceException(erroExecucao.toString());
						}
					}
				} else {
					if (json.has("S:Fault")) {
						json = json.getJSONObject("S:Fault");
						if (json.has("faultstring")) {							
							throw new G5ServiceException(json.getString("faultstring"));
						} else {
							throw new G5ServiceException("Ocorreu um erro desconhecido: " + json.toString());
						}
					}
				}
			}
			throw new G5ServiceException("Ocorreu um erro desconhecido: " + json.toString());
		} catch (Exception e) {
			e.printStackTrace();
			try {
				transformer.transform(soapResponse.getSOAPPart().getContent(),	new StreamResult(sw));
				System.err.println(sw.toString());
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			throw new G5ServiceException(e.getMessage());
		}
	}

	public static String parseResponseToString(SOAPMessage soapResponse) throws SOAPException, IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		soapResponse.writeTo(out);
		return new String(out.toByteArray());

	}	
	
	public static SOAPMessage doPostSoap(HttpServletRequest request) throws G5ServiceException {
		String user = request.getHeader(G5Service.PARAMETER_USER);
		String pass = request.getHeader(G5Service.PARAMETER_PASS);
		int encryptionType = 2; // default 2 login integrado com criptografia
		try {
			encryptionType = Integer.parseInt(request.getHeader(G5Service.PARAMETER_ENCRYPTIONTYPE));
		} catch (NumberFormatException e) {
			System.out.println(G5Service.PARAMETER_ENCRYPTIONTYPE + " não definido no header da requisição. Assumido 2 login integrado com criptografia");
		}
		String server = request.getParameter(G5Service.PARAMETER_SERVER);
		String module = request.getParameter(G5Service.PARAMETER_MODULE);
		String service = request.getParameter(G5Service.PARAMETER_SERVICE).replaceAll("\\.", "_");
		String envelope = request.getHeader(G5Service.PARAMETER_ENVELOPE);
		String separator = request.getParameter(G5Service.PARAMETER_SEPARATOR);
		

		G5Service g5Service = new G5Service(server, module, service, encryptionType, separator);
		return g5Service.callPort(user, pass, envelope);
	}
}
