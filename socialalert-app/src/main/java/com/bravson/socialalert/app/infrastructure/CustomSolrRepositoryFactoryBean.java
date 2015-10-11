package com.bravson.socialalert.app.infrastructure;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.WeakHashMap;

import javax.annotation.Resource;

import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.core.NamedQueries;
import org.springframework.data.repository.core.RepositoryInformation;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import org.springframework.data.repository.core.support.TransactionalRepositoryFactoryBeanSupport;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.data.repository.query.QueryLookupStrategy.Key;
import org.springframework.data.repository.query.RepositoryQuery;
import org.springframework.data.solr.core.SolrOperations;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.mapping.SimpleSolrMappingContext;
import org.springframework.data.solr.core.mapping.SolrDocument;
import org.springframework.data.solr.repository.query.PartTreeSolrQuery;
import org.springframework.data.solr.repository.query.SolrEntityInformation;
import org.springframework.data.solr.repository.query.SolrEntityInformationCreator;
import org.springframework.data.solr.repository.query.SolrQueryMethod;
import org.springframework.data.solr.repository.query.StringBasedSolrQuery;
import org.springframework.data.solr.repository.support.SolrEntityInformationCreatorImpl;
import org.springframework.data.solr.server.SolrServerFactory;

@NoRepositoryBean
public class CustomSolrRepositoryFactoryBean<T, ID extends Serializable> extends TransactionalRepositoryFactoryBeanSupport<CustomBaseRepository<T, ID>, T, ID> {

	@Resource
	private SolrServerFactory solrServerFactory;
	
	private SimpleSolrMappingContext solrMappingContext;
	
    @Override
    protected RepositoryFactorySupport doCreateRepositoryFactory() {
        return new CustomSolrRepositoryFactory<T, ID>(solrServerFactory, solrMappingContext);
    }
    
    /**
	 * Configures the {@link SolrOperations} to be used to create Solr repositories.
	 * 
	 * @param operations the operations to set
	 */
	public void setSolrOperations(SolrOperations operations) {
	}
	
	public void setSchemaCreationSupport(boolean schemaCreationSupport) {
	}
	
	public void setSolrMappingContext(SimpleSolrMappingContext solrMappingContext) {
		super.setMappingContext(solrMappingContext);
		this.solrMappingContext = solrMappingContext;
	}

    private static class CustomSolrRepositoryFactory<T, ID extends Serializable> extends RepositoryFactorySupport {

    	private final Map<Class<?>, SolrTemplate> operationsMap = new WeakHashMap<Class<?>, SolrTemplate>();
    	private final SolrServerFactory solrServerFactory;
    	private final SolrEntityInformationCreator entityInformationCreator;
    	private final SimpleSolrMappingContext solrMappingContext;

        public CustomSolrRepositoryFactory(SolrServerFactory solrServerFactory, SimpleSolrMappingContext solrMappingContext) {
        	
        	if (solrMappingContext == null) {
        		solrMappingContext = new SimpleSolrMappingContext();
        	}
        	
            this.solrServerFactory = solrServerFactory;
            this.solrMappingContext = solrMappingContext;
            this.entityInformationCreator = new SolrEntityInformationCreatorImpl(solrMappingContext);
        }
        
        private SolrOperations determineSolrOperation(RepositoryMetadata metadata) {
        	Class<?> entityClass = metadata.getDomainType();
        	SolrTemplate template = operationsMap.get(entityClass);
        	if (template == null) {
	        	SolrDocument doc = entityClass.getAnnotation(SolrDocument.class);
	        	String coreName = doc != null ? doc.solrCoreName() : entityClass.getSimpleName();
	        	template = new SolrTemplate(solrServerFactory);
	        	template.setMappingContext(solrMappingContext);
	        	template.setSolrCore(coreName);
	        	template.afterPropertiesSet();
	        	operationsMap.put(entityClass, template);
        	}
        	return template;
        }
        
        @SuppressWarnings("unchecked")
		@Override
        protected Object getTargetRepository(RepositoryInformation metadata) {
            return new CustomBaseRepositoryImpl<T, ID>((SolrEntityInformation<T, ID>) getEntityInformation(metadata.getDomainType()), determineSolrOperation(metadata));
        }

        @Override
        protected Class<?> getRepositoryBaseClass(RepositoryMetadata metadata) {
            return CustomBaseRepository.class;
        }
        
        @Override
    	public <T2, ID2 extends Serializable> SolrEntityInformation<T2, ID2> getEntityInformation(Class<T2> domainClass) {
    		return entityInformationCreator.getEntityInformation(domainClass);
    	}

    	@Override
    	protected QueryLookupStrategy getQueryLookupStrategy(Key key) {
    		return new SolrQueryLookupStrategy();
    	}

    	private class SolrQueryLookupStrategy implements QueryLookupStrategy {

			@Override
    		public RepositoryQuery resolveQuery(Method method, RepositoryMetadata metadata, NamedQueries namedQueries) {

    			SolrQueryMethod queryMethod = new SolrQueryMethod(method, metadata, entityInformationCreator);
    			String namedQueryName = queryMethod.getNamedQueryName();

    			if (namedQueries.hasQuery(namedQueryName)) {
    				String namedQuery = namedQueries.getQuery(namedQueryName);
    				return new StringBasedSolrQuery(namedQuery, queryMethod, determineSolrOperation(metadata));
    			} else if (queryMethod.hasAnnotatedQuery()) {
    				return new StringBasedSolrQuery(queryMethod, determineSolrOperation(metadata));
    			} else {
    				return new PartTreeSolrQuery(queryMethod, determineSolrOperation(metadata));
    			}
    		}

    	}
    }
}
