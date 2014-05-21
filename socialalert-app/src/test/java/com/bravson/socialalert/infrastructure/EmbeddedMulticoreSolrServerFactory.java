package com.bravson.socialalert.infrastructure;

import java.io.File;
import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.core.CoreContainer;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.util.Assert;
import org.springframework.util.ResourceUtils;
import org.xml.sax.SAXException;

import com.bravson.socialalert.app.infrastructure.BaseMulticoreSolrServerFactory;

public class EmbeddedMulticoreSolrServerFactory extends BaseMulticoreSolrServerFactory implements DisposableBean {

	private String solrHome;
	private CoreContainer solrContainer;
	
	{
		System.setProperty("solr.allow.unsafe.resourceloading", "true");
	}
	
	protected EmbeddedMulticoreSolrServerFactory() {

	}

	public EmbeddedMulticoreSolrServerFactory(String solrHome) throws ParserConfigurationException, IOException, SAXException {
		Assert.hasText(solrHome);
		this.solrHome = solrHome;
	}

	@PostConstruct
	public void afterPropertiesSet() throws Exception {
		solrContainer = createSolrContainer(solrHome);
	}

	private CoreContainer createSolrContainer(String path) throws ParserConfigurationException,
			IOException, SAXException {
		String solrHomeDirectory = ResourceUtils.getURL(path).getPath();
		return CoreContainer.createAndLoad(solrHomeDirectory, new File(solrHomeDirectory + "/solr.xml"));
	}

	public void shutdownSolrContainer() {
		if (this.solrContainer != null) {
			solrContainer.shutdown();
		}
	}
	
	public void setSolrHome(String solrHome) {
		Assert.hasText(solrHome);
		this.solrHome = solrHome;
	}

	@Override
	public void destroy() throws Exception {
		shutdownSolrContainer();
	}
	
	@Override
	protected SolrServer createSolrServer(String coreName) {
		return new EmbeddedSolrServer(solrContainer, coreName);
	}
}
