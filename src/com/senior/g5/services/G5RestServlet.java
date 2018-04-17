package com.senior.g5.services;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.soap.SOAPMessage;

import org.apache.http.protocol.ResponseServer;
import org.json.JSONObject;


@WebServlet("/G5Rest")
public class G5RestServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("application/json");
		
		G5Utils.allowAnyOrigin(response);		
		JSONObject jsonResponse = null;
		try {			
			String user = request.getHeader(G5Service.PARAMETER_USER);
			String pass = request.getHeader(G5Service.PARAMETER_PASS);
			Integer encType = getEncType(request, G5Service.PARAMETER_ENCRYPTIONTYPE);
			if(encType==G5Service.ENCRIPTION_TYPE_PLAIN_FROM_URL){
				user = request.getParameter(G5Service.PARAMETER_USER);
				pass = request.getParameter(G5Service.PARAMETER_PASS);
				encType = G5Service.ENCRIPTION_TYPE_TEXT_PLAIN;
			}
			String token = request.getHeader(G5Service.G7PARAMETER_AUTORIZATION);			
			boolean devMode = isDevMode(request.getParameter(G5Service.PARAMETER_DEVMODE));			
			String server = getServer(request, request.getParameter(G5Service.PARAMETER_SERVER));			
			String module = request.getParameter(G5Service.PARAMETER_MODULE);
			String service = request.getParameter(G5Service.PARAMETER_SERVICE);
			String port = request.getParameter(G5Service.PARAMETER_PORT);
			String separator = request.getParameter(G5Service.PARAMETER_SEPARATOR);
			
			
			G5ServiceConfig g5Params = new G5ServiceConfig(server, module, service, port, separator);
			
			final SOAPMessage g5Response = G5Utils.doG5PostRest(request, user, pass, token, encType, g5Params, devMode);
			
			final String dataSourceAttr = request.getParameter(G5Service.PARAMETER_DATASOURCEATTRNAME);
			jsonResponse = G5Utils.parseResponseToJSON(g5Response, port, dataSourceAttr);			
			response.getWriter().write(jsonResponse.toString());
		} catch (G5ServiceException e) {		
			e.printStackTrace();
			//throw new ServletException(e.getMessage(),e);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			JSONObject error = new JSONObject();			
			StringWriter stack = new StringWriter();			
			e.printStackTrace(new PrintWriter(stack));
			
			error.put("errorMessage", e.getMessage());
			error.put("errorType", "G5ServiceException");
			error.put("stackTrace", stack.toString());
			response.getWriter().write(error.toString());			
		}
	}

	private String getServer(HttpServletRequest request, String server) {		
		if(server == null){
			server = getBaseUrl(request);
		}
		return server;
	}

	private boolean isDevMode(String devMode) {
		return devMode != null && devMode.equalsIgnoreCase("true");
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
		
	@Override
	protected void doOptions(HttpServletRequest req, HttpServletResponse response) throws ServletException, IOException {
		G5Utils.allowAnyOrigin(response);
		super.doOptions(req, response);
	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)	throws ServletException, IOException {
		doPost(req, resp);
	}
	
	private static String getBaseUrl(HttpServletRequest request) {
		if ((request.getServerPort() == 80) || (request.getServerPort() == 443))
			return request.getScheme() + "://" + request.getServerName() + request.getContextPath();
		else
			return request.getScheme() + "://" + request.getServerName() + ":"+ request.getServerPort();
	}
}
