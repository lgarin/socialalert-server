package com.bravson.socialalert.app.repositories;

import java.net.URI;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Point;
import org.springframework.data.solr.repository.Query;

import com.bravson.socialalert.app.entities.PictureAlert;
import com.bravson.socialalert.app.infrastructure.CustomBaseRepository;

@Deprecated
public interface PictureAlertRepository extends CustomBaseRepository<PictureAlert, URI> {

	public static final int MAX_FACET_RESULTS = 1000;
	public static final String BOOST = "{!boost b=product(log(max(hitCount,2)),sqrt(max(sub(likeCount,dislikeCount),2)),recip(ms(NOW/HOUR,creation),3.16e-11,2,1))}";

	@Query(value=BOOST + "*:*", filters={"{!bbox sfield=pictureLocation pt=?0 d=?1}", "creation:[NOW/MINUTES-?2MINUTES TO *]"})
	Page<PictureAlert> findWithinArea(Point location, Distance distance, long maxAgeInMinutes, Pageable pageable);
	
	@Query(value=BOOST + "(title:(?0) OR tags:(?0))", filters={"creation:[NOW/MINUTES-?1MINUTES TO *]"})
	Page<PictureAlert> findWithKeywords(String keywords, long maxAgeInMinutes, Pageable pageable);
	
	@Query(value=BOOST + "(title:(?2) OR tags:(?2))", filters={"{!bbox sfield=pictureLocation pt=?0 d=?1}", "creation:[NOW/MINUTES-?3MINUTES TO *]"})
	Page<PictureAlert> findWithinAreaWithKeywords(Point location, Distance distance, String keywords, long maxAgeInMinutes, Pageable pageable);
	
	@Query(value=BOOST + "*:*", filters={"creation:[NOW/MINUTES-?0MINUTES TO *]"})
	Page<PictureAlert> findRecent(long maxAge, Pageable pageable);
	
	@Query(value="*:*", filters={"profileId:?0"})
	Page<PictureAlert> findByProfileId(UUID profileId, Pageable pageable);
	
	@Query(value=BOOST + "*:*", filters={"{!bbox sfield=pictureLocation pt=?0 d=?1}", "creation:[NOW/MINUTES-?2MINUTES TO *]", "categories:?3"})
	Page<PictureAlert> findWithinAreaByCategory(Point location, Distance distance, long maxAgeInMinutes, String category, Pageable pageable);
	
	@Query(value=BOOST + "(title:(?2) OR tags:(?2))", filters={"{!bbox sfield=pictureLocation pt=?0 d=?1}", "creation:[NOW/MINUTES-?3MINUTES TO *]", "categories:?4"})
	Page<PictureAlert> findWithinAreaWithKeywordsByCategory(Point location, Distance distance, String keywords, long maxAgeInMinutes, String category, Pageable pageable);
	
	@Query(value=BOOST + "*:*", filters={"creation:[NOW/MINUTES-?0MINUTES TO *]", "categories:?1"})
	Page<PictureAlert> findRecentByCategory(long maxAge, String category, Pageable pageable);
	
	@Query(value=BOOST + "(title:(?0) OR tags:(?0))", filters={"creation:[NOW/MINUTES-?1MINUTES TO *]", "categories:?2"})
	Page<PictureAlert> findWithKeywordsByCategory(String keywords, long maxAgeInMinutes, String category, Pageable pageable);
}
