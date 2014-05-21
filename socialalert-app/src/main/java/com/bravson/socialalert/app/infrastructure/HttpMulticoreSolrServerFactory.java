package com.bravson.socialalert.app.infrastructure;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.springframework.util.Assert;
import org.xml.sax.SAXException;

public class HttpMulticoreSolrServerFactory extends BaseMulticoreSolrServerFactory {

	private String serverUrl;

	protected HttpMulticoreSolrServerFactory() {

	}

	public HttpMulticoreSolrServerFactory(String serverUrl) throws ParserConfigurationException, IOException, SAXException {
		Assert.hasText(serverUrl);
		this.serverUrl = serverUrl;
	}

	public void setServerUrl(String serverUrl) {
		Assert.hasText(serverUrl);
		this.serverUrl = serverUrl;
	}
	
	@Override
	protected SolrServer createSolrServer(String coreName) {
		return new HttpSolrServer(serverUrl + "/" + coreName);
	}
}
