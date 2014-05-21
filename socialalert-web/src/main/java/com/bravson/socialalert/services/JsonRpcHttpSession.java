package com.bravson.socialalert.services;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.Map;

import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.Session;

import com.googlecode.jsonrpc4j.JsonRpcHttpClient;

public class JsonRpcHttpSession extends JsonRpcHttpClient {

	private Request request;
	
	public JsonRpcHttpSession(Request request, URL serviceUrl) {
		super(serviceUrl);
		this.request = request;
	}

	private void storeAppCookie(HttpURLConnection con) {
		Session session = request.getSession(true);
		String cookie = con.getHeaderField("Set-Cookie");
		if (cookie != null && cookie.contains("JSESSIONID")) {
		   int start = cookie.indexOf("JSESSIONID");
		   int end = cookie.indexOf(';', start);
		   if (end < 0)  {
		      end = cookie.length();
		   }
		   session.setAttribute("AppCookie", cookie.substring(start, end));
		}
	}
	
	private Map<String, String> buildAppCookie() {
		Session session = request.getSession(false);
		Object cookie = null;
		if (session != null) {
			cookie = session.getAttribute("AppCookie");
		}
		if (cookie instanceof String) {
			return Collections.singletonMap("Cookie", cookie.toString());
		} else {
			return Collections.emptyMap();
		}
	}
	
	public Object invoke(String methodName, Object argument, Type returnType,
			Map<String, String> extraHeaders) throws Throwable {

		
		// create URLConnection
		HttpURLConnection con = prepareConnection(buildAppCookie());
		con.connect();

		// invoke it
		OutputStream ops = con.getOutputStream();
		try {
			super.invoke(methodName, argument, ops);
		} finally {
			ops.close();
		}

		// store session id
		storeAppCookie(con);

		// read and return value
		InputStream ips = con.getInputStream();
		try {
			return super.readResponse(returnType, ips);
		} finally {
			ips.close();
		}
	}
}
