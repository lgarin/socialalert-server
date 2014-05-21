package com.bravson.socialalert.app.repositories;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.solr.repository.Query;

import com.bravson.socialalert.app.entities.ProfileLink;
import com.bravson.socialalert.app.infrastructure.CustomBaseRepository;

public interface ProfileLinkRepository extends CustomBaseRepository<ProfileLink, String> {

	@Query(value="*:*", filters={"sourceProfileId: ?0"})
	Page<ProfileLink> findBySourceProfileId(UUID sourceProfileId, Pageable pageable);
	
	@Query(value="*:*", filters={"targetProfileId: ?0"})
	Page<ProfileLink> findByTargetProfileId(UUID targetProfileId, Pageable pageable);
}
