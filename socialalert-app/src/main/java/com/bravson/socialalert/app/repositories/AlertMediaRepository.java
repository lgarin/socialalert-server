package com.bravson.socialalert.app.repositories;

import java.net.URI;
import java.util.Collection;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Point;
import org.springframework.data.solr.repository.Query;

import com.bravson.socialalert.app.entities.AlertMedia;
import com.bravson.socialalert.app.infrastructure.CustomBaseRepository;
import com.bravson.socialalert.common.domain.MediaType;

public interface AlertMediaRepository extends CustomBaseRepository<AlertMedia, URI> {

	public static final int MAX_FACET_RESULTS = 1000;
	public static final String BOOST = "{!boost b=product(log(max(hitCount,2)),sqrt(max(sub(likeCount,dislikeCount),2)),recip(ms(NOW/HOUR,creation),3.16e-11,2,1))}";

	@Query(value=BOOST + "*:*", filters={"type:(?0)", "{!bbox sfield=location pt=?1 d=?2}", "creation:[NOW/MINUTES-?3MINUTES TO *]"})
	Page<AlertMedia> findWithinArea(Collection<MediaType> types, Point location, Distance distance, long maxAgeInMinutes, Pageable pageable);
	
	@Query(value=BOOST + "(title:(?1) OR tags:(?1))", filters={"type:(?0)", "creation:[NOW/MINUTES-?2MINUTES TO *]"})
	Page<AlertMedia> findWithKeywords(Collection<MediaType> types, String keywords, long maxAgeInMinutes, Pageable pageable);
	
	@Query(value=BOOST + "(title:(?3) OR tags:(?3))", filters={"type:(?0)", "{!bbox sfield=location pt=?1 d=?2}", "creation:[NOW/MINUTES-?4MINUTES TO *]"})
	Page<AlertMedia> findWithinAreaWithKeywords(Collection<MediaType> types, Point location, Distance distance, String keywords, long maxAgeInMinutes, Pageable pageable);
	
	@Query(value=BOOST + "*:*", filters={"type:(?0)", "creation:[NOW/MINUTES-?1MINUTES TO *]"})
	Page<AlertMedia> findRecent(Collection<MediaType> types, long maxAge, Pageable pageable);
	
	@Query(value="*:*", filters={"type:(?0)", "profileId:?1"})
	Page<AlertMedia> findByProfileId(Collection<MediaType> types, UUID profileId, Pageable pageable);
	
	@Query(value=BOOST + "*:*", filters={"type:(?0)", "{!bbox sfield=location pt=?1 d=?2}", "creation:[NOW/MINUTES-?3MINUTES TO *]", "categories:?4"})
	Page<AlertMedia> findWithinAreaByCategory(Collection<MediaType> types, Point location, Distance distance, long maxAgeInMinutes, String category, Pageable pageable);
	
	@Query(value=BOOST + "(title:(?3) OR tags:(?3))", filters={"type:(?0)", "{!bbox sfield=location pt=?1 d=?2}", "creation:[NOW/MINUTES-?4MINUTES TO *]", "categories:?5"})
	Page<AlertMedia> findWithinAreaWithKeywordsByCategory(Collection<MediaType> types, Point location, Distance distance, String keywords, long maxAgeInMinutes, String category, Pageable pageable);
	
	@Query(value=BOOST + "*:*", filters={"type:(?0)", "creation:[NOW/MINUTES-?1MINUTES TO *]", "categories:?2"})
	Page<AlertMedia> findRecentByCategory(Collection<MediaType> types, long maxAge, String category, Pageable pageable);
	
	@Query(value=BOOST + "(title:(?1) OR tags:(?1))", filters={"type:(?0)", "creation:[NOW/MINUTES-?2MINUTES TO *]", "categories:?3"})
	Page<AlertMedia> findWithKeywordsByCategory(Collection<MediaType> types, String keywords, long maxAgeInMinutes, String category, Pageable pageable);
}
