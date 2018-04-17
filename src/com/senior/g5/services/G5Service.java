package com.senior.g5.services;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;

public class G5Service {

	/** Usuário para autenticação */
	public static final String PARAMETER_USER = "user";
	/** Senha para autenticação (para enctyptiontype 0 a 2 */
	public static final String PARAMETER_PASS = "pass";
	/** Tipo de autenticação 0 texto plano, 2 login integrado, 3 token g7 */
	public static final String PARAMETER_ENCRYPTIONTYPE = "encryptionType";
	/** Servidor dos webservices g5 (Ex: https://meuserver.com.br:8181) */
	public static final String PARAMETER_SERVER = "server";
	/** Módulo (Ex: rubi, sm, tr...) */
	public static final String PARAMETER_MODULE = "module";
	/** Módulo (Ex: com.senior.g5.rh.fp.ferias) */
	public static final String PARAMETER_SERVICE = "service";
	/** Módulo (Ex: Programacao) */
	public static final String PARAMETER_PORT = "port";
	/**
	 * Separador da url do serviço. Padrão _ (underline) mas pode ser / em
	 * alguns casos
	 */
	public static final String PARAMETER_SEPARATOR = "separator";
	public static final String PARAMETER_SEPARATOR_DEFAULT = "_";
	/**
	 * Token G7, necessário quando ENCRYPTION_TYPE = ENCRIPTION_TYPE_TOKEN_G7
	 * (3)
	 */
	public static final String G7PARAMETER_AUTORIZATION = "authorization";

	public static final String PARAMETER_ENVELOPE = "envelope";
	public static final Integer ENCRIPTION_TYPE_TEXT_PLAIN = 0;
	public static final Integer ENCRIPTION_TYPE_LOGIN_INTEGRATED = 2;
	public static final Integer ENCRIPTION_TYPE_TOKEN_G7 = 3;
	public static final Integer ENCRIPTION_TYPE_PLAIN_FROM_URL = 9;
	public static final String PARAMETER_DATASOURCEATTRNAME = "DATASOURCEATTRNAME";
	public static final String PARAMETER_DEVMODE = "DEV";

	private String serviceURL;
	private Integer encryptionType;

	public G5Service(String server, String modulo, String service, Integer encryptionType) throws G5ServiceException {
		init(server, modulo, service, encryptionType, null);
	}

	public G5Service(String server, String modulo, String service, Integer encryptionType, String separator) throws G5ServiceException {
		init(server, modulo, service, encryptionType, separator);
	}

	public G5Service(String server, String modulo, String service, String separator) throws G5ServiceException {
		init(server, modulo, service, ENCRIPTION_TYPE_LOGIN_INTEGRATED,	separator);
	}

	private static void validParameters(String server, String module, String service) throws G5ServiceException {
		if (server == null || server.isEmpty()) {
			throw new G5ServiceException(
					"Parâmetro ["
							+ PARAMETER_SERVER
							+ "] não recebido. Informe o nome do servidor. Ex: http://myserver:8080");
		}
		if (module == null || module.isEmpty()) {
			throw new G5ServiceException(
					"Parâmetro ["
							+ PARAMETER_MODULE
							+ "] não recebido. Informe o nome do servidor. Ex: rubi/sapiens/tr/cs/bs/...");
		}
		if (service == null || service.isEmpty()) {
			throw new G5ServiceException(
					"Parâmetro ["
							+ PARAMETER_SERVICE
							+ "] não recebido. Informe o nome do serviço. Ex: com.senior.g5.folhapagamento");
		}
	}

	private static void validParameters(String port) throws G5ServiceException {
		if (port == null || port.isEmpty()) {
			throw new G5ServiceException(
					"Parâmetro ["
							+ PARAMETER_PORT
							+ "] não recebido. Informe o nome da porta. Ex: consultarFolhaPagamento");
		}
	}

	private void init(String server, String module, String service, Integer encryptionType, String separator) throws G5ServiceException {
		validParameters(server, module, service);
		server += server.endsWith("/") ? "" : "/";
		if (separator == null || separator.isEmpty()) {
			separator = PARAMETER_SEPARATOR_DEFAULT;
		}
		this.serviceURL = server + "g5-senior-services/" + module + separator + "Sync" + service.replace(".", "_");
		System.out.println("G5Service Request URL: " + this.serviceURL);
		this.encryptionType = encryptionType;
	}

	public SOAPMessage callPort(String portName, String user, String pass, HashMap<String, G5NodeParam> params) throws G5ServiceException {
		validParameters(portName);
		try {
			SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
			SOAPConnection soapConnection = soapConnectionFactory.createConnection();
			final SOAPMessage request = createSOAPRequest(portName, user, pass, params);
			SOAPMessage soapResponse = soapConnection.call(request, this.serviceURL);
			if (soapResponse == null) {
				throw new Exception("Response return null to service " + this.serviceURL + " / " + portName);
			}
			soapConnection.close();
			return soapResponse;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(
					"Error occurred while sending SOAP Request to Server "
							+ e.getMessage());
		}
	}

	public SOAPMessage callPort(String user, String pass, String soapMessage)
			throws G5ServiceException {

		try {
			SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory
					.newInstance();
			SOAPConnection soapConnection = soapConnectionFactory
					.createConnection();
			SOAPMessage soapResponse = soapConnection.call(
					createSOAPRequest(soapMessage), this.serviceURL);
			soapConnection.close();
			return soapResponse;
		} catch (Exception e) {
			System.err
					.println("Error occurred while sending SOAP Request to Server");
			e.printStackTrace();
		}
		return null;
	}

	private SOAPMessage createSOAPRequest(String soapEnvelope) throws IOException, SOAPException {
		InputStream is = new ByteArrayInputStream(soapEnvelope.getBytes());
		SOAPMessage request = MessageFactory.newInstance().createMessage(null, is);
		return request;
	}

	private SOAPMessage createSOAPRequest(String portName, String user, String pass, HashMap<String,G5NodeParam> params) throws Exception {
		MessageFactory messageFactory = MessageFactory.newInstance();
		SOAPMessage soapMessage = messageFactory.createMessage();
		SOAPPart soapPart = soapMessage.getSOAPPart();

		// SOAP Envelope
		SOAPEnvelope envelope = soapPart.getEnvelope();

		String serverURI = "http://services.senior.com.br";
		envelope.addNamespaceDeclaration("ser", serverURI);

		SOAPBody soapBody = envelope.getBody();
		SOAPElement port = soapBody.addChildElement(portName, "ser");
		if (user != null) {
			port.addChildElement("user").addTextNode(user);
		}
		// para g5
		if (pass == null) {
			pass = "";
		}
		port.addChildElement("password").addTextNode(pass);
		port.addChildElement("encryption")
				.addTextNode("" + this.encryptionType);

		// para workflow
		port.addChildElement("pass").addTextNode(pass);
		port.addChildElement("encrypted").addTextNode("" + this.encryptionType);
		SOAPElement parameters = port.addChildElement("parameters");

		Iterator<Entry<String, G5NodeParam>> it = params.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, G5NodeParam> pair = it.next();
			if (!pair.getKey().equals("$top") && !pair.getKey().equals("$skip")) {
				if (pair.getValue().getChildren() == null) {
					parameters.addChildElement(pair.getKey()).addTextNode(
							pair.getValue().getValue());
				} else {
					SOAPElement elem = parameters
							.addChildElement(pair.getKey());
					for (G5NodeParam node : pair.getValue().getChildren()) {
						elem.addChildElement(node.getName()).addTextNode(
								node.getValue());
					}
				}
			}
		}
		soapMessage.saveChanges();
		System.out.print("G5RestService Request: ");
		soapMessage.writeTo(System.out);
		return soapMessage;
	}
}
