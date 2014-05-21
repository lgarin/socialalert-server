package com.bravson.socialalert.app.infrastructure;

import java.io.IOException;

import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.util.NamedList;

public class DummySolrServerFactory extends BaseMulticoreSolrServerFactory {

	@Override
	protected SolrServer createSolrServer(String coreName) {
		return new SolrServer() {
			private static final long serialVersionUID = 1L;
			
			@Override
			public void shutdown() {
				
			}
			@Override
			public NamedList<Object> request(SolrRequest request) throws SolrServerException, IOException {
				return null;
			}
		};
	}
}
