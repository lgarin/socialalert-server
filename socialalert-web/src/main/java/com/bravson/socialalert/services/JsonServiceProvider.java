package com.bravson.socialalert.services;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import org.apache.tapestry5.ioc.AnnotationProvider;
import org.apache.tapestry5.ioc.ObjectLocator;
import org.apache.tapestry5.ioc.ObjectProvider;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.services.Request;

import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.googlecode.jsonrpc4j.JsonRpcHttpClient;
import com.googlecode.jsonrpc4j.JsonRpcService;
import com.googlecode.jsonrpc4j.ProxyUtil;

public class JsonServiceProvider implements ObjectProvider {
	
	private static JodaModule jodaModule = new JodaModule();
	
	private static JsonClientExceptionResolver exceptionResolver = new JsonClientExceptionResolver();
	
	private URL appUrl;
	
	public JsonServiceProvider(@Symbol("app.server.url") URL appUrl) {
		this.appUrl = appUrl;
	}
	
	@Override
	public <T> T provide(Class<T> objectType, AnnotationProvider annotationProvider, ObjectLocator locator) {
		JsonRpcService annotation = objectType.getAnnotation(JsonRpcService.class);
		if (annotation != null) {
			JsonRpcHttpClient client = createClient(objectType, annotation, locator.getService(Request.class));
			client.getObjectMapper().registerModule(jodaModule);
			client.setExceptionResolver(exceptionResolver);
			return ProxyUtil.createClientProxy(locator.getClass().getClassLoader(), objectType, true, client, new HashMap<String, String>());
		}
		return null;
	}

	private <T> JsonRpcHttpClient createClient(Class<T> objectType,
			JsonRpcService annotation, Request request) {
		try {
			return new JsonRpcHttpSession(request, new URL(appUrl.toExternalForm() + "/" + annotation.value()));
		} catch (MalformedURLException e) {
			throw new RuntimeException("Invalid service URL for " + objectType.getName(), e);
		}
	}

}
