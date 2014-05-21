package com.bravson.socialalert.app.infrastructure;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.solr.core.query.Criteria;
import org.springframework.data.solr.core.query.FacetOptions;
import org.springframework.data.solr.core.query.UpdateField;
import org.springframework.data.solr.core.query.result.FacetPage;
import org.springframework.data.solr.repository.SolrCrudRepository;

@NoRepositoryBean
public interface CustomBaseRepository<T, ID extends Serializable> extends SolrCrudRepository<T, ID> {
	
	public T findById(ID id);

	public int batchUpdateProperties(List<T> entities, String... properties);
	
	public T lockById(ID id);
	
	public List<T> lockAll(List<T> entities);
	
	public T lock(T entity);
	
	public Page<T> query(Criteria criteria, PageRequest pageRequest);
	
	public <P> Page<P> querySingleField(Criteria criteria, String propertyName, Class<P> propertyType, PageRequest pageRequest);
	
	public List<String> findSuggestion(String partial);

	public int reindexAll(int pageSize);

	public void partialUpdate(ID id, List<UpdateField> updateFields);
	
	public FacetPage<T> queryForFacetPage(Criteria criteria, Collection<Criteria> filters, FacetOptions facetOptions, PageRequest pageRequest);
}
