package com.bravson.socialalert.app.repositories;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.solr.repository.Query;

import com.bravson.socialalert.app.entities.AlertAlbum;
import com.bravson.socialalert.app.infrastructure.CustomBaseRepository;

public interface AlertAlbumRepository extends CustomBaseRepository<AlertAlbum, UUID> {

	@Query(value="*:*", filters={"profileId: ?0"})
	Page<AlertAlbum> findByProfileId(UUID profileId, Pageable pageable);
}
