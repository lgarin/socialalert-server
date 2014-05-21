package com.bravson.socialalert.app.repositories;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.solr.repository.Query;

import com.bravson.socialalert.app.entities.UserProfile;
import com.bravson.socialalert.app.infrastructure.CustomBaseRepository;

public interface UserProfileRepository extends CustomBaseRepository<UserProfile, UUID> {

	@Query(value="nickname: (?0*)")
	Page<UserProfile> findWithKeyword(String keyword, Pageable pageable);
}
