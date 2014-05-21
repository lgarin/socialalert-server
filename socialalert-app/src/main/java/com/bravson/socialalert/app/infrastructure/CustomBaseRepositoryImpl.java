package com.bravson.socialalert.app.infrastructure;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.SpellCheckResponse.Collation;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.util.NamedList;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.mapping.model.BeanWrapper;
import org.springframework.data.solr.core.SolrCallback;
import org.springframework.data.solr.core.SolrOperations;
import org.springframework.data.solr.core.mapping.SolrPersistentEntity;
import org.springframework.data.solr.core.mapping.SolrPersistentProperty;
import org.springframework.data.solr.core.query.Criteria;
import org.springframework.data.solr.core.query.FacetOptions;
import org.springframework.data.solr.core.query.PartialUpdate;
import org.springframework.data.solr.core.query.Query;
import org.springframework.data.solr.core.query.SimpleFacetQuery;
import org.springframework.data.solr.core.query.SimpleField;
import org.springframework.data.solr.core.query.SimpleFilterQuery;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.data.solr.core.query.UpdateField;
import org.springframework.data.solr.core.query.result.FacetPage;
import org.springframework.data.solr.repository.query.SolrEntityInformation;
import org.springframework.data.solr.repository.support.SimpleSolrRepository;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.transaction.TransactionTimedOutException;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;


public class CustomBaseRepositoryImpl<T, ID extends Serializable> extends SimpleSolrRepository<T, ID> implements CustomBaseRepository<T, ID> {

	private static final int CONCURRENT_ENTITY_UPDATES = 1000;
	
	private SolrPersistentEntity<T> persistentEntity;
	private final ConversionService conversionService;
	private final SolrEntityInformation<T, ID> entityInformation;
	private final HashMap<ID, Thread> lockedEntities = new HashMap<>(CONCURRENT_ENTITY_UPDATES);
	
	@SuppressWarnings("unchecked")
	public CustomBaseRepositoryImpl(SolrEntityInformation<T, ID> metadata, SolrOperations solrOperations) {
		super(metadata, solrOperations);
		entityInformation = metadata;
		conversionService = getSolrOperations().getConverter().getConversionService();
		MappingContext<? extends SolrPersistentEntity<?>, SolrPersistentProperty> mappingContext = getSolrOperations().getConverter().getMappingContext();
		persistentEntity = (SolrPersistentEntity<T>) mappingContext.getPersistentEntity(getEntityClass());
	}
	
	@Override
	public T findById(final ID id) {
		if (id == null) {
			return null;
		}
		
		return getSolrOperations().execute(new SolrCallback<T>() {
			@Override
			public T doInSolr(SolrServer solrServer) throws SolrServerException, IOException {
				return realtimeGet(solrServer, id);
			}
		});
	}
	
	public T lockById(ID id) {
		if (id == null) {
			return null;
		}
		lockId(id);
		return findById(id);
	}
	
	public T lock(T entity) {
		if (entity == null) {
			return null;
		}
		ID id = entityInformation.getId(entity);
		if (id == null) {
			return entity;
		}
		lockId(id);
		return findById(id);
	}

	public List<T> lockAll(List<T> entities) {
		if (entities.isEmpty()) {
			return entities;
		}
		ArrayList<ID> ids = new ArrayList<>(entities.size());
		for (T entity : entities) {
			ID id = entityInformation.getId(entity);
			if (id != null) {
				ids.add(id);
			}
		}
		if (ids.size() == 0) {
			return entities;
		} else if (ids.size() != entities.size()) {
			throw new IllegalStateException("New and existing entities cannot be persisted at the same time");
		}
		lockIds(ids);
		return findAll(ids);
	}
	
	public List<T> findAll(Collection<ID> ids) {
		Query query = new SimpleQuery(new Criteria(getIdFieldName()).in(ids));
		query.setPageRequest(new PageRequest(0, ids.size()));
		return getSolrOperations().queryForPage(query, getEntityClass()).getContent();
	}
	
	@Override
	public Page<T> query(Criteria criteria, PageRequest pageRequest) {
		SimpleQuery query = new SimpleQuery(criteria, pageRequest);
		return getSolrOperations().queryForPage(query, getEntityClass());
	}
	
	@Override
	public <P> Page<P> querySingleField(Criteria criteria, String propertyName, Class<P> propertyType, PageRequest pageRequest) {
		SimpleQuery query = new SimpleQuery(criteria, pageRequest);
		query.addProjectionOnField(new SimpleField(propertyName));
		return getSolrOperations().queryForPage(query, propertyType);
	}
	
	private void lockIds(final Iterable<ID> ids) {
		final Thread currentThread = Thread.currentThread();
		boolean changed = false;
		synchronized (lockedEntities) {
			for (ID id : ids) {
				Thread otherThread = lockedEntities.get(id);
				if (otherThread == null) {
					lockedEntities.put(id, currentThread);
				}
			}
			for (ID id : ids) {
				Thread otherThread = lockedEntities.get(id);
				while (otherThread != null && otherThread != currentThread) {
					try {
						lockedEntities.wait();
					} catch (InterruptedException e) {
						throw new TransactionTimedOutException("Cannot get locked " + getEntityClass().getSimpleName() + " with ID " + id, e);
					}
					otherThread = lockedEntities.get(id);
				}
				if (otherThread == null) {
					lockedEntities.put(id, currentThread);
					changed = true;
				}
			}
		}
		
		if (!changed) {
			return;
		}
		
		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
			@Override
			public void afterCompletion(int status) {
				synchronized (lockedEntities) {
					for (ID id : ids) {
						Thread thread = lockedEntities.remove(id);
						if (thread != null && thread != currentThread) {
							throw new TransactionSystemException("Lock for " + getEntityClass().getSimpleName() + " with ID " + id + " has been stolen by thread  " + thread.getName());
						}
					}
					lockedEntities.notifyAll();
				}
			}
		});
	}

	private void lockId(final ID id) {
		final Thread currentThread = Thread.currentThread();
		boolean changed = false;
		synchronized (lockedEntities) {
			Thread otherThread = lockedEntities.get(id);
			while (otherThread != null && otherThread != currentThread) {
				try {
					lockedEntities.wait();
				} catch (InterruptedException e) {
					throw new TransactionTimedOutException("Cannot get locked " + getEntityClass().getSimpleName() + " with ID " + id, e);
				}
				otherThread = lockedEntities.get(id);
			}
			if (otherThread == null) {
				lockedEntities.put(id, currentThread);
				changed = true;
			}
		}
		
		if (!changed) {
			return;
		}
		
		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
			@Override
			public void afterCompletion(int status) {
				synchronized (lockedEntities) {
					Thread thread = lockedEntities.remove(id);
					if (thread != null && thread != currentThread) {
						throw new TransactionSystemException("Lock for " + getEntityClass().getSimpleName() + " with ID " + id + " has been stolen by thread  " + thread.getName());
					}
					lockedEntities.notifyAll();
				}
			}
		});
	}
	
	@Override
	public boolean exists(ID id) {
		return findById(id) != null;
	}

	protected T realtimeGet(SolrServer solrServer, ID id) throws SolrServerException, IOException {
		String stringId = conversionService.convert(id, String.class);
		SolrRequest solrRequest = new SolrCustomRequest("/get", Collections.singletonMap("id", stringId));
		NamedList<Object> result = solrServer.request(solrRequest);
		SolrDocument doc  = (SolrDocument) result.get("doc");
		if (doc == null) {
			return null;
		}
		return getSolrOperations().getConverter().read(getEntityClass(), doc);
	}
	
	public List<String> findSuggestion(final String partial) {
		if (partial == null) {
			return null;
		}
		
		return getSolrOperations().execute(new SolrCallback<List<String>>() {
			@Override
			public List<String> doInSolr(SolrServer solrServer) throws SolrServerException, IOException {
				return suggest(solrServer, partial);
			}
		});
	}
	
	
	protected List<String> suggest(SolrServer solrServer, String partial) throws SolrServerException, IOException {
		SolrRequest solrRequest = new SolrCustomRequest("/suggest", Collections.singletonMap("q", partial));
		NamedList<Object> result = solrServer.request(solrRequest);
		QueryResponse response = new QueryResponse(result, solrServer);
		if (CollectionUtils.isEmpty(response.getSpellCheckResponse().getCollatedResults())) {
			return Collections.emptyList();
		}
		ArrayList<String> collations = new ArrayList<>(response.getSpellCheckResponse().getCollatedResults().size());
		for (Collation collation : response.getSpellCheckResponse().getCollatedResults()) {
			collations.add(collation.getCollationQueryString());
		}
		return collations;
	}
	
	@Override
	public int batchUpdateProperties(List<T> entities, String... properties) {
		if (entities.isEmpty()) {
			return 0;
		}
		
		List<PartialUpdate> updates = new ArrayList<>(entities.size());
		for (T entity : entities) {
			BeanWrapper<?,T> wrapper = BeanWrapper.create(entity, conversionService);
			SolrPersistentProperty idProperty = persistentEntity.getIdProperty();
			PartialUpdate update = new PartialUpdate(idProperty.getName(), wrapper.getProperty(idProperty));
			for (String property : properties) {
				SolrPersistentProperty persistentProperty = persistentEntity.getPersistentProperty(property);
				update.add(persistentProperty.getName(), wrapper.getProperty(persistentProperty));
			}
			updates.add(update);
		}
		getSolrOperations().saveBeans(updates);
		return updates.size();
	}
	
	@Override
	public int reindexAll(int pageSize) {
		int updateCount = 0;
		int pageNumber = 0;
		Page<T> page = findAll(new PageRequest(pageNumber++, pageSize));
		while (page.hasContent()) {
			save(page.getContent());
			updateCount += page.getNumberOfElements();
			page = findAll(new PageRequest(pageNumber++, pageSize));
		}
		return updateCount;
	}
	
	@Override
	public void partialUpdate(ID id, List<UpdateField> updateFields) {
		PartialUpdate update = new PartialUpdate(getIdFieldName(), id);
		for (UpdateField updateField : updateFields) {
			update.add(updateField);
		}
		getSolrOperations().saveBean(update);
	}
	
	@Override
	public FacetPage<T> queryForFacetPage(Criteria criteria, Collection<Criteria> filters, FacetOptions facetOptions, PageRequest pageRequest) {
		SimpleFacetQuery query =  new SimpleFacetQuery(criteria, pageRequest);
		query.setFacetOptions(facetOptions);
		for (Criteria filter : filters) {
			query.addFilterQuery(new SimpleFilterQuery(filter));
		}
		return getSolrOperations().queryForFacetPage(query, getEntityClass());
	}
}
