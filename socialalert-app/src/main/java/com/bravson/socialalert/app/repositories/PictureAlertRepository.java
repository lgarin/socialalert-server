package com.bravson.socialalert.app.repositories;

import java.net.URI;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.solr.core.geo.Distance;
import org.springframework.data.solr.core.geo.GeoLocation;
import org.springframework.data.solr.repository.Query;

import com.bravson.socialalert.app.entities.PictureAlert;
import com.bravson.socialalert.app.infrastructure.CustomBaseRepository;
import com.bravson.socialalert.common.domain.MediaCategory;

public interface PictureAlertRepository extends CustomBaseRepository<PictureAlert, URI> {

	public static final int MAX_FACET_RESULTS = 1000;
	public static final String POPULARITY_FACTOR = "log(max(hitCount,2)),sqrt(max(sub(likeCount,dislikeCount),2)),recip(ms(NOW/HOUR,creation),3.16e-11,2,1)";

	@Query(value="{!boost b=product("+POPULARITY_FACTOR+",recip(geodist(pictureLocation,?0),2,200,20))}*:*", filters={"{!geofilt sfield=pictureLocation pt=?0 d=?1}", "creation:[NOW/MINUTES-?2MINUTES TO *]"})
	Page<PictureAlert> findWithinArea(GeoLocation location, Distance distance, long maxAgeInMinutes, Pageable pageable);
	
	@Query(value="{!boost b=product("+POPULARITY_FACTOR+")}(title:(?0) OR tags:(?0))", filters={"creation:[NOW/MINUTES-?1MINUTES TO *]"})
	Page<PictureAlert> findWithKeywords(String keywords, long maxAgeInMinutes, Pageable pageable);
	
	@Query(value="{!boost b=product("+POPULARITY_FACTOR+",recip(geodist(pictureLocation,?0),2,200,20))}(title:(?2) OR tags:(?2))", filters={"{!geofilt sfield=pictureLocation pt=?0 d=?1}", "creation:[NOW/MINUTES-?3MINUTES TO *]"})
	Page<PictureAlert> findWithinAreaWithKeywords(GeoLocation location, Distance distance, String keywords, long maxAgeInMinutes, Pageable pageable);
	
	@Query(value="{!boost b=product("+POPULARITY_FACTOR+")}*:*", filters={"creation:[NOW/MINUTES-?0MINUTES TO *]"})
	Page<PictureAlert> findRecent(long maxAge, Pageable pageable);
	
	@Query(value="*:*", filters={"profileId:?0"})
	Page<PictureAlert> findByProfileId(UUID profileId, Pageable pageable);
	
	@Query(value="{!boost b=product("+POPULARITY_FACTOR+",recip(geodist(pictureLocation,?0),2,200,20))}*:*", filters={"{!geofilt sfield=pictureLocation pt=?0 d=?1}", "creation:[NOW/MINUTES-?2MINUTES TO *]", "categories:?3"})
	Page<PictureAlert> findWithinAreaByCategory(GeoLocation location, Distance distance, long maxAgeInMinutes, String category, Pageable pageable);
	
	@Query(value="{!boost b=product("+POPULARITY_FACTOR+",recip(geodist(pictureLocation,?0),2,200,20))}(title:(?2) OR tags:(?2))", filters={"{!geofilt sfield=pictureLocation pt=?0 d=?1}", "creation:[NOW/MINUTES-?3MINUTES TO *]", "categories:?4"})
	Page<PictureAlert> findWithinAreaWithKeywordsByCategory(GeoLocation location, Distance distance, String keywords, long maxAgeInMinutes, String category, Pageable pageable);
	
	@Query(value="{!boost b=product("+POPULARITY_FACTOR+")}*:*", filters={"creation:[NOW/MINUTES-?0MINUTES TO *]", "categories:?1"})
	Page<PictureAlert> findRecentByCategory(long maxAge, String category, Pageable pageable);
	
	@Query(value="{!boost b=product("+POPULARITY_FACTOR+")}(title:(?0) OR tags:(?0))", filters={"creation:[NOW/MINUTES-?1MINUTES TO *]", "categories:?2"})
	Page<PictureAlert> findWithKeywordsByCategory(String keywords, long maxAgeInMinutes, String category, Pageable pageable);
}
