package com.bravson.socialalert.app.infrastructure;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Arrays;

import javax.annotation.Resource;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.core.NamedQueries;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import org.springframework.data.repository.core.support.TransactionalRepositoryFactoryBeanSupport;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.data.repository.query.QueryLookupStrategy.Key;
import org.springframework.data.repository.query.RepositoryQuery;
import org.springframework.data.solr.core.SolrOperations;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.convert.CustomConversions;
import org.springframework.data.solr.core.convert.MappingSolrConverter;
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
	
    @Override
    protected RepositoryFactorySupport doCreateRepositoryFactory() {
        return new CustomSolrRepositoryFactory<T, ID>(solrServerFactory);
    }
    
    /**
	 * Configures the {@link SolrOperations} to be used to create Solr repositories.
	 * 
	 * @param operations the operations to set
	 */
	public void setSolrOperations(SolrOperations operations) {
	}

	private enum SolrDocumentToStringConvertor implements Converter<org.apache.solr.common.SolrDocument, String> {
		INSTANCE;
		
		@Override
		public String convert(org.apache.solr.common.SolrDocument source) {
			return source.values().iterator().next().toString();
		}
	}
	
    private static class CustomSolrRepositoryFactory<T, ID extends Serializable> extends RepositoryFactorySupport {

    	private final SolrServerFactory solrServerFactory;
    	private final SolrEntityInformationCreator entityInformationCreator;
    	private final MappingSolrConverter solrConverter;

        public CustomSolrRepositoryFactory(SolrServerFactory solrServerFactory) {
        	this.solrConverter = new MappingSolrConverter(new SimpleSolrMappingContext());
        	this.solrConverter.setCustomConversions(new CustomConversions(Arrays.asList(SolrDocumentToStringConvertor.INSTANCE)));
        	this.solrConverter.afterPropertiesSet();
            this.solrServerFactory = solrServerFactory;
            this.entityInformationCreator = new SolrEntityInformationCreatorImpl(solrConverter.getMappingContext());
        }
        
        private SolrOperations determineSolrOperation(RepositoryMetadata metadata) {
        	// TODO use cache
        	Class<?> entityClass = metadata.getDomainType();
        	SolrDocument doc = entityClass.getAnnotation(SolrDocument.class);
        	String coreName = doc != null ? doc.solrCoreName() : entityClass.getSimpleName();
        	SolrTemplate template = new SolrTemplate(solrServerFactory, solrConverter);
        	template.setSolrCore(coreName);
        	return template;
        }
        
        @SuppressWarnings("unchecked")
		@Override
        protected Object getTargetRepository(RepositoryMetadata metadata) {
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
