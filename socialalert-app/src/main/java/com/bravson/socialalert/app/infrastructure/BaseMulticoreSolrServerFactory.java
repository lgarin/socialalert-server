package com.bravson.socialalert.app.infrastructure;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.solr.client.solrj.SolrServer;
import org.springframework.data.solr.server.SolrServerFactory;

public abstract class BaseMulticoreSolrServerFactory implements SolrServerFactory {

	private ConcurrentHashMap<String, SolrServer> serverMap = new ConcurrentHashMap<String, SolrServer>();

	@Override
	public List<String> getCores() {
		return new ArrayList<String>(serverMap.keySet());
	}

	@Override
	public SolrServer getSolrServer() {
		return getSolrServer("");
	}
	
	@Override
	public SolrServer getSolrServer(String coreName) {
		SolrServer server = serverMap.get(coreName);
		if (server == null) {
			server = createSolrServer(coreName);
			serverMap.put(coreName, server);
		}
		return server;
	}

	protected abstract SolrServer createSolrServer(String coreName);
}
