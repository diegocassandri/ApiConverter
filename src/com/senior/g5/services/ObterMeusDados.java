package com.senior.g5.services;


import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ParseException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

public class ObterMeusDados {
	private static final String OBTERMEUSDADOSURL = "https://platform.senior.com.br/t/senior.com.br/bridge/1.0/rest/usuarios/userManager/queries/obterMeusDados";
	private static final String OBTERMEUSDADOSURLDEV = "https://pcbnu002050.interno.senior.com.br:8243/t/senior.com.br/bridge/1.0/rest/usuarios/userManager/queries/obterMeusDados";

	public static String getUserFromToken(String token, boolean dev) throws G5ServiceException{
		
		HttpResponse resp;
		try {
			resp = executePOSTRequest(token, dev);
			if(resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
				final String content = extractStringResponse(resp);				
				JSONObject response = new JSONObject(content);
				return response.getString("nome");				
			}else{
				throw new G5ServiceException(String.format("%d", resp.getStatusLine().getStatusCode()));
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new G5ServiceException(String.format("Não foi possível obter o usuário do token %s. Erro: %s", token, e.getMessage()));
		} 
	}
	
	private static CloseableHttpClient createClient(String authorization) throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
        SSLContext sslContext = SSLContextBuilder.create().loadTrustMaterial(new TrustSelfSignedStrategy()).build();
        HostnameVerifier allowAllHosts = new NoopHostnameVerifier();
        SSLConnectionSocketFactory connectionFactory = new SSLConnectionSocketFactory(sslContext, allowAllHosts);        
        List<BasicHeader> defaultHeaders = Arrays.asList(
        		new BasicHeader("Authorization", authorization),
        		new BasicHeader("Content-Type", "application/json"));
		return HttpClients.custom().setDefaultHeaders(defaultHeaders).setSSLSocketFactory(connectionFactory).build();
    }
	
	
	private static HttpResponse executePOSTRequest(String authorization, boolean dev) throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, IOException, URISyntaxException {
		CloseableHttpClient client = createClient(authorization);
		String url = dev ? OBTERMEUSDADOSURLDEV : OBTERMEUSDADOSURL;
		final HttpUriRequest request = new HttpPost(url);		
		return client.execute(request);
	}
		
	public static String extractStringResponse(HttpResponse response) throws ParseException, IOException{
		HttpEntity entity = response.getEntity();
		return EntityUtils.toString(entity, "UTF-8");
	}	
}
