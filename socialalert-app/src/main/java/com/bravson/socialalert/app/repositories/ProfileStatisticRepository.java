package com.bravson.socialalert.app.repositories;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.solr.repository.Query;

import com.bravson.socialalert.app.entities.ProfileStatistic;
import com.bravson.socialalert.app.infrastructure.CustomBaseRepository;

public interface ProfileStatisticRepository extends CustomBaseRepository<ProfileStatistic, UUID> {

	@Query(value="{!boost b=product(log(max(hitCount,2)),sqrt(max(sub(likeCount,dislikeCount),2)))} *:*")
	Page<ProfileStatistic> findTopCreators(Pageable pageable);
	
}
