package com.senior.g5.services;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.soap.SOAPMessage;


@WebServlet("/G5Soap")
public class G5SoapServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("application/xml");
		
		allowOrigin(response);
		String soapResponse = null;
		try {			
			final SOAPMessage message = G5Utils.doPostSoap(request);
			soapResponse = G5Utils.parseResponseToString(message);			
			response.getWriter().write(soapResponse.toString());
		} catch (Exception e) {			
			response.getWriter().write(formatError(e.getMessage()));
			e.printStackTrace();						
		}
	}

	private void allowOrigin(HttpServletResponse response) {
		response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "*");
	}
	
	private String formatError(String msg){
		String err = "<S:Envelope xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
						"<S:Body>" +
							"<ns2:g5Response xmlns:ns2=\"http://services.senior.com.br\">" +
								"<result>" +
									"<erroExecucao>%s</erroExecucao>" +
								"</result>" +
							"</ns2:g5Response>" +
						"</S:Body>" +
					"</S:Envelope>";
		final String formatted = String.format(err, msg);
		System.out.println("Formatted msg: "+formatted);
		return formatted;
	}
}
