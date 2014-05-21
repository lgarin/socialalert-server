package com.bravson.socialalert.app.repositories;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.solr.repository.Query;

import com.bravson.socialalert.app.entities.AlertComment;
import com.bravson.socialalert.app.infrastructure.CustomBaseRepository;

public interface AlertCommentRepository extends CustomBaseRepository<AlertComment, UUID> {

	@Query(value="*:*", filters={"mediaUri: ?0"})
	Page<AlertComment> findByMediaUri(URI mediaUri, Pageable pageable);
	
	@Query(value="*:*", filters={"commentId:(?0)"})
	List<AlertComment> findByCommentIds(Collection<UUID> commentIds, Pageable page);
}
