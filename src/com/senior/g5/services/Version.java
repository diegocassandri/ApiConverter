package com.senior.g5.services;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/Version")
public class Version extends HttpServlet {

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		Properties prop = new Properties();
		final InputStream resourceAsStream = this.getServletContext().getResourceAsStream("/build.properties");		
		prop.load(resourceAsStream);
		String version = prop.getProperty("version.number")+"."+prop.getProperty("build.number"); 
		resp.getWriter().write("Version: " +version);
	}
}
