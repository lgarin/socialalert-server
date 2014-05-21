package com.bravson.socialalert.infrastructure;

import javax.annotation.Resource;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.request.ContentStreamUpdateRequest;
import org.apache.solr.common.util.ContentStreamBase;
import org.springframework.data.solr.server.SolrServerFactory;
import org.springframework.test.context.transaction.TransactionConfiguration;


@TransactionConfiguration(defaultRollback=true)
public abstract class DataServiceTest extends SimpleServiceTest {

    @Resource
    private SolrServerFactory solrServerFactory;
    
	public void fullImport(Class<?> entityClass) throws Exception {
		String coreName = entityClass.getSimpleName();
		SolrServer solrServer = solrServerFactory.getSolrServer(coreName);
		ContentStreamUpdateRequest request = new ContentStreamUpdateRequest("/update/json");
		request.addContentStream(new ContentStreamBase.URLStream(getClass().getResource(coreName + ".json")));
		solrServer.deleteByQuery("*:*");
		solrServer.request(request);
		solrServer.commit();
	}
}
