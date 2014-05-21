package com.bravson.socialalert.app.repositories;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.solr.repository.Query;

import com.bravson.socialalert.app.entities.ApplicationUser;
import com.bravson.socialalert.app.infrastructure.CustomBaseRepository;
import com.bravson.socialalert.common.domain.UserState;

public interface ApplicationUserRepository extends CustomBaseRepository<ApplicationUser, String> {

	@Query(value="*:*", filters={"state:?0", "lastUpdate:[* TO NOW-?1MILLIS]"})
	public List<ApplicationUser> findByState(UserState state, long delay, Pageable page);
	
	@Query(value="*:*", filters={"profileId:(?0)"})
	public List<ApplicationUser> findByProfileIds(Collection<UUID> profileIds, Pageable page);
}
