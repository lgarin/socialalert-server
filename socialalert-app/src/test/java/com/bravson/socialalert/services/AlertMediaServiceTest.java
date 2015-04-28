package com.bravson.socialalert.services;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import javax.annotation.Resource;
import javax.validation.ValidationException;

import org.apache.commons.lang3.time.DateUtils;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.springframework.dao.DuplicateKeyException;

import com.bravson.socialalert.app.domain.PictureMetadata;
import com.bravson.socialalert.app.entities.AlertMedia;
import com.bravson.socialalert.app.entities.ProfileStatistic;
import com.bravson.socialalert.app.exceptions.DataMissingException;
import com.bravson.socialalert.app.services.AlertMediaService;
import com.bravson.socialalert.common.domain.ApprovalModifier;
import com.bravson.socialalert.common.domain.GeoAddress;
import com.bravson.socialalert.common.domain.GeoArea;
import com.bravson.socialalert.common.domain.GeoStatistic;
import com.bravson.socialalert.common.domain.MediaCategory;
import com.bravson.socialalert.common.domain.MediaInfo;
import com.bravson.socialalert.common.domain.MediaType;
import com.bravson.socialalert.common.domain.QueryResult;
import com.bravson.socialalert.infrastructure.DataServiceTest;

public class AlertMediaServiceTest extends DataServiceTest {

	@Resource
	private AlertMediaService service;
	
	@Before
	public void setUp() throws Exception {
		fullImport(AlertMedia.class);
		fullImport(ProfileStatistic.class);
	}
	
	@Test
	public void createPictureWithAllMetadata() {
		PictureMetadata metadata = new PictureMetadata();
		metadata.setCameraMaker("abc");
		metadata.setCameraModel("123");
		metadata.setHeight(345);
		metadata.setWidth(567);
		metadata.setTimestamp(new DateTime(2013, 7, 14, 12, 23, 43));
		UUID profileId = UUID.fromString("e7d166ae-9b3f-4405-be0d-fa1567728593");
		URI pictureUri = URI.create("abc.jpg");
		GeoAddress address = new GeoAddress(null, null, "Terrassenrain 6, 3072 Ostermundigen, Suisse", "Ostermundigen", "Switzerland");
		MediaInfo info = service.createPictureAlert(pictureUri, profileId, "Test", address, metadata, Collections.singletonList(MediaCategory.ART), Collections.<String>emptyList());
		assertNotNull(info);
		assertEquals(profileId, info.getProfileId());
		assertEquals(pictureUri, info.getMediaUri());
		assertEquals("Test", info.getTitle());
		assertEquals(Arrays.asList("Ostermundigen", "Switzerland"), info.getTags());
		assertEquals(Collections.singletonList("ART"), info.getCategories());
		assertEquals(metadata.getHeight(), info.getHeight());
		assertEquals(metadata.getWidth(), info.getWidth());
		assertNull(info.getLongitude());
		assertNull(info.getLatitude());
		assertEquals(metadata.getTimestamp(), info.getTimestamp());
		assertEquals(metadata.getCameraMaker(), info.getCameraMaker());
		assertEquals(metadata.getCameraModel(), info.getCameraModel());
		assertEquals(0, info.getHitCount());
		assertEquals(0, info.getLikeCount());
		assertEquals(0, info.getDislikeCount());
	}
	
	@Test
	public void createPictureWithEmptyMetadata() {
		PictureMetadata metadata = new PictureMetadata();
		UUID profileId = UUID.fromString("e7d166ae-9b3f-4405-be0d-fa1567728593");
		URI pictureUri = URI.create("abc.jpg");
		GeoAddress address = new GeoAddress(7.5, 46.9, "Terrassenrain 6, 3072 Ostermundigen, Suisse", "Ostermundigen", "Switzerland");
		MediaInfo info = service.createPictureAlert(pictureUri, profileId, "Test", address, metadata, Collections.<MediaCategory>emptyList(), Collections.singletonList("Tag"));
		assertNotNull(info);
		assertEquals(profileId, info.getProfileId());
		assertEquals(pictureUri, info.getMediaUri());
		assertEquals("Test", info.getTitle());
		assertNotNull(info.getTags());
		assertEquals(3, info.getTags().size());
		assertEquals("Tag", info.getTags().get(0));
		assertTrue(info.getCategories().isEmpty());
		assertNull(info.getHeight());
		assertNull(info.getWidth());
		assertEquals(46.9, info.getLongitude(), 0.01);
		assertEquals(7.5, info.getLatitude(), 0.01);
		assertNull(info.getTimestamp());
		assertNull(info.getCameraMaker());
		assertNull(info.getCameraModel());
		assertEquals(0, info.getHitCount());
		assertEquals(0, info.getLikeCount());
		assertEquals(0, info.getDislikeCount());
	}
	
	@Test(expected=DuplicateKeyException.class)
	public void createPictureWithClaimedUri() {
		PictureMetadata metadata = new PictureMetadata();
		UUID profileId = UUID.randomUUID();
		URI pictureUri = URI.create("20130712/7e9a5a5bd5e64171c176ac6c7b32d685.jpg");
		service.createPictureAlert(pictureUri, profileId, "Test", null, metadata, Collections.singletonList(MediaCategory.ART), Collections.singletonList("Tag"));
	}
	
	@Test
	public void updateExistingPicture() {
		URI pictureUri = URI.create("20130716/f317f3c7918c83ff6ec24aabb6c017fd.jpg");
		UUID profileId = UUID.fromString("e7d166ae-9b3f-4405-be0d-fa1567728593");
		DateTime beforeUpdate = DateTime.now();
		GeoAddress address = new GeoAddress(7.5, 46.9, "Terrassenrain 6, 3072 Ostermundigen, Suisse", "Ostermundigen", "Switzerland");
		MediaInfo info = service.updateAlert(pictureUri, profileId, "New title", "A description", address, "Sony", "RX100II", null, Collections.singletonList(MediaCategory.NEWS), Collections.singletonList("New tag"));
		assertNotNull(info);
		assertEquals(profileId, info.getProfileId());
		assertEquals(pictureUri, info.getMediaUri());
		assertEquals("New title", info.getTitle());
		assertEquals("A description", info.getDescription());
		assertEquals("Sony", info.getCameraMaker());
		assertEquals("RX100II", info.getCameraModel());
		assertEquals(Collections.singletonList("NEWS"), info.getCategories());
		assertEquals(Arrays.asList("New tag", "Ostermundigen", "Switzerland"), info.getTags());
		assertTrue(DateTime.parse("2013-07-16T16:50:40.492Z").compareTo(info.getCreation()) == 0);
		assertFalse(beforeUpdate.isAfter(info.getLastUpdate()));
	}
	
	@Test(expected=DataMissingException.class)
	public void updateNonExistingPicture() {
		URI pictureUri = URI.create("xyz.jpg");
		UUID profileId = UUID.fromString("e7d166ae-9b3f-4405-be0d-fa1567728593");
		service.updateAlert(pictureUri, profileId, "New title", "A description", null, "Sony", "RX100II", null, Collections.singletonList(MediaCategory.NEWS), Collections.singletonList("New tag"));
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void updateExistingPictureWithWrongProfile() {
		URI pictureUri = URI.create("20130716/f317f3c7918c83ff6ec24aabb6c017fd.jpg");
		UUID profileId = UUID.fromString("a7d166ae-9b3f-4405-be0d-fa156772859b");
		service.updateAlert(pictureUri, profileId, "New title", "A description", null, "Sony", "RX100II", null, Collections.singletonList(MediaCategory.NEWS), Collections.singletonList("New tag"));
	}

	@Test
	public void viewValidURI() {
		MediaInfo info = service.viewMediaDetail(URI.create("20130712/7e9a5a5bd5e64171c176ac6c7b32d685.jpg"));
		assertNotNull(info);
		assertEquals(2, info.getHitCount());
	}
	
	@Test(expected=DataMissingException.class)
	public void viewInvalidURI() {
		service.viewMediaDetail(URI.create("xxx.jpg"));
	}
	
	@Test
	public void getValidURI() {
		MediaInfo info = service.getMediaInfo(URI.create("20130712/7e9a5a5bd5e64171c176ac6c7b32d685.jpg"));
		assertNotNull(info);
		assertEquals(1, info.getHitCount());
		assertEquals(UUID.fromString("a95472c0-e0e6-11e2-a28f-0800200c9a77"), info.getProfileId());
	}
	
	@Test(expected=DataMissingException.class)
	public void getInvalidURI() {
		service.getMediaInfo(URI.create("xxx.jpg"));
	}
	
	@Test
	public void listAllRecentPictures() {
		QueryResult<MediaInfo> result = service.searchMedia(MediaType.PICTURE, null, null, 3650 * DateUtils.MILLIS_PER_DAY, 0, 10);
		assertNotNull(result);
		assertEquals(0, result.getPageNumber());
		assertEquals(1, result.getPageCount());
		assertNotNull(result.getContent());
		assertEquals(2, result.getContent().size());
	}
	
	@Test
	public void listAllRecentPicturesByCategory() {
		QueryResult<MediaInfo> result = service.searchMediaInCategory(MediaType.PICTURE, null, null, 3650 * DateUtils.MILLIS_PER_DAY, "PLACES", 0, 10);
		assertNotNull(result);
		assertEquals(0, result.getPageNumber());
		assertEquals(1, result.getPageCount());
		assertNotNull(result.getContent());
		assertEquals(2, result.getContent().size());
	}
	
	@Test
	public void pageThroughRecentPictures() {
		QueryResult<MediaInfo> result1 = service.searchMedia(MediaType.PICTURE, null, null, 3650 * DateUtils.MILLIS_PER_DAY, 0, 1);
		assertNotNull(result1);
		assertEquals(0, result1.getPageNumber());
		assertEquals(2, result1.getPageCount());
		assertNotNull(result1.getContent());
		assertEquals(1, result1.getContent().size());
		MediaInfo first = result1.getContent().get(0);
		QueryResult<MediaInfo> result2 = service.searchMedia(MediaType.PICTURE, null, null, 3650 * DateUtils.MILLIS_PER_DAY, 1, 1);
		assertNotNull(result2);
		assertEquals(1, result2.getPageNumber());
		assertEquals(2, result2.getPageCount());
		assertNotNull(result2.getContent());
		assertEquals(1, result2.getContent().size());
		MediaInfo second = result2.getContent().get(0);
		assertTrue(second.getCreation().isBefore(first.getCreation()));
	}
	
	@Test
	public void listNoRecentPictures() {
		QueryResult<MediaInfo> result = service.searchMedia(MediaType.PICTURE, null, null, 3650 * DateUtils.MILLIS_PER_DAY, 3, 10);
		assertNotNull(result);
		assertEquals(3, result.getPageNumber());
		assertEquals(1, result.getPageCount());
		assertNotNull(result.getContent());
		assertEquals(0, result.getContent().size());
	}
	
	@Test
	public void listNoRecentPicturesInCategory() {
		QueryResult<MediaInfo> result = service.searchMediaInCategory(MediaType.PICTURE, null, null, 3650 * DateUtils.MILLIS_PER_DAY, "PLACES", 3, 10);
		assertNotNull(result);
		assertEquals(3, result.getPageNumber());
		assertEquals(1, result.getPageCount());
		assertNotNull(result.getContent());
		assertEquals(0, result.getContent().size());
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void listRecentPicturesWithLargePageSize() {
		service.searchMedia(MediaType.PICTURE, null, null, 3650 * DateUtils.MILLIS_PER_DAY, 0, 100000);
	}
	
	@Test(expected=ValidationException.class)
	public void listRecentPicturesWithInvalidParameter() {
		service.searchMedia(MediaType.PICTURE, null, null, 3650 * DateUtils.MILLIS_PER_DAY, 0, -1);
	}
	
	@Test(expected=DataMissingException.class)
	public void deleteNonExistingPicture() {
		URI pictureUri = URI.create("7e9a5a5bd5e64171c176ac6c7b32d685.jpg");
		service.deleteMedia(pictureUri, UUID.fromString("a95472c0-e0e6-11e2-a28f-0800200c9a77"));
	}
	
	@Test
	public void deleteExistingPicture() {
		URI pictureUri = URI.create("20130712/7e9a5a5bd5e64171c176ac6c7b32d685.jpg");
		service.deleteMedia(pictureUri, UUID.fromString("a95472c0-e0e6-11e2-a28f-0800200c9a77"));
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void deletePictureWithOtherProfile() {
		URI pictureUri = URI.create("20130712/7e9a5a5bd5e64171c176ac6c7b32d685.jpg");
		service.deleteMedia(pictureUri, UUID.fromString("e7d166ae-9b3f-4405-be0d-fa1567728593"));
	}
	
	@Test
	public void likePicture() {
		DateTime beforeUpdate = DateTime.now();
		MediaInfo info = service.updateLikeDislike(URI.create("20130716/f317f3c7918c83ff6ec24aabb6c017fd.jpg"), null, ApprovalModifier.LIKE);
		assertNotNull(info);
		assertEquals(2, info.getLikeCount());
		assertEquals(1, info.getDislikeCount());
		assertTrue(info.getLastUpdate().compareTo(beforeUpdate) >= 0);
	}
	
	@Test
	public void revertLikedPicture() {
		DateTime beforeUpdate = DateTime.now();
		MediaInfo info = service.updateLikeDislike(URI.create("20130716/f317f3c7918c83ff6ec24aabb6c017fd.jpg"), ApprovalModifier.LIKE, null);
		assertNotNull(info);
		assertEquals(0, info.getLikeCount());
		assertEquals(1, info.getDislikeCount());
		assertTrue(info.getLastUpdate().compareTo(beforeUpdate) >= 0);
	}
	
	@Test
	public void confirmLikedPicture() {
		DateTime beforeUpdate = DateTime.now();
		MediaInfo info = service.updateLikeDislike(URI.create("20130716/f317f3c7918c83ff6ec24aabb6c017fd.jpg"), ApprovalModifier.LIKE, ApprovalModifier.LIKE);
		assertNotNull(info);
		assertEquals(1, info.getLikeCount());
		assertEquals(1, info.getDislikeCount());
		assertTrue(info.getLastUpdate().compareTo(beforeUpdate) >= 0);
	}
	
	@Test
	public void dislikePicture() {
		DateTime beforeUpdate = DateTime.now();
		MediaInfo info = service.updateLikeDislike(URI.create("20130716/f317f3c7918c83ff6ec24aabb6c017fd.jpg"), null, ApprovalModifier.DISLIKE);
		assertNotNull(info);
		assertEquals(1, info.getLikeCount());
		assertEquals(2, info.getDislikeCount());
		assertTrue(info.getLastUpdate().compareTo(beforeUpdate) >= 0);
	}
	
	@Test
	public void revertDislikedPicture() {
		DateTime beforeUpdate = DateTime.now();
		MediaInfo info = service.updateLikeDislike(URI.create("20130716/f317f3c7918c83ff6ec24aabb6c017fd.jpg"), ApprovalModifier.DISLIKE, null);
		assertNotNull(info);
		assertEquals(1, info.getLikeCount());
		assertEquals(0, info.getDislikeCount());
		assertTrue(info.getLastUpdate().compareTo(beforeUpdate) >= 0);
	}
	
	@Test
	public void confirmDislikedPicture() {
		DateTime beforeUpdate = DateTime.now();
		MediaInfo info = service.updateLikeDislike(URI.create("20130716/f317f3c7918c83ff6ec24aabb6c017fd.jpg"), ApprovalModifier.DISLIKE, ApprovalModifier.DISLIKE);
		assertNotNull(info);
		assertEquals(1, info.getLikeCount());
		assertEquals(1, info.getDislikeCount());
		assertTrue(info.getLastUpdate().compareTo(beforeUpdate) >= 0);
	}
	
	@Test
	public void dislikeLikedPicture() {
		DateTime beforeUpdate = DateTime.now();
		MediaInfo info = service.updateLikeDislike(URI.create("20130716/f317f3c7918c83ff6ec24aabb6c017fd.jpg"), ApprovalModifier.LIKE, ApprovalModifier.DISLIKE);
		assertNotNull(info);
		assertEquals(0, info.getLikeCount());
		assertEquals(2, info.getDislikeCount());
		assertTrue(info.getLastUpdate().compareTo(beforeUpdate) >= 0);
	}
	
	@Test
	public void likeDislikedPicture() {
		DateTime beforeUpdate = DateTime.now();
		MediaInfo info = service.updateLikeDislike(URI.create("20130716/f317f3c7918c83ff6ec24aabb6c017fd.jpg"), ApprovalModifier.DISLIKE, ApprovalModifier.LIKE);
		assertNotNull(info);
		assertEquals(2, info.getLikeCount());
		assertEquals(0, info.getDislikeCount());
		assertTrue(info.getLastUpdate().compareTo(beforeUpdate) >= 0);
	}
	
	@Test(expected=DataMissingException.class)
	public void likeInvalidURI() {
		service.updateLikeDislike(URI.create("xxx.jpg"), null, ApprovalModifier.LIKE);
	}
	
	@Test
	public void listPicturesInArea() {
		QueryResult<MediaInfo> result = service.searchMedia(MediaType.PICTURE, new GeoArea(46.7, 7.8, 5.0), null, 3650 * DateUtils.MILLIS_PER_DAY, 0, 10);
		assertNotNull(result);
		assertEquals(0, result.getPageNumber());
		assertEquals(1, result.getPageCount());
		assertNotNull(result.getContent());
		assertEquals(1, result.getContent().size());
	}
	
	@Test
	public void searchPicturesInAreaWithKeyword() {
		QueryResult<MediaInfo> result = service.searchMedia(MediaType.PICTURE, new GeoArea(46.7, 7.8, 5.0), "sport", 3650 * DateUtils.MILLIS_PER_DAY, 0, 10);
		assertNotNull(result);
		assertEquals(0, result.getPageNumber());
		assertEquals(1, result.getPageCount());
		assertNotNull(result.getContent());
		assertEquals(1, result.getContent().size());
	}
	
	@Test
	public void searchPicturesInAreaWithBadKeyword() {
		QueryResult<MediaInfo> result = service.searchMedia(MediaType.PICTURE, new GeoArea(46.7, 7.8, 5.0), "abc", 3650 * DateUtils.MILLIS_PER_DAY, 0, 10);
		assertNotNull(result);
		assertEquals(0, result.getPageNumber());
		assertEquals(0, result.getPageCount());
		assertNotNull(result.getContent());
		assertEquals(0, result.getContent().size());
	}
	
	@Test
	public void listPicturesOutOfArea() {
		QueryResult<MediaInfo> result = service.searchMedia(MediaType.PICTURE, new GeoArea(46.7, 7.8, 1.0), null, 3650 * DateUtils.MILLIS_PER_DAY, 0, 10);
		assertNotNull(result);
		assertEquals(0, result.getPageNumber());
		assertEquals(0, result.getPageCount());
		assertNotNull(result.getContent());
		assertEquals(0, result.getContent().size());
	}

	@Test(expected=ValidationException.class)
	public void listPicturesInAreaWithInvalidAge() {
		service.searchMedia(MediaType.PICTURE, new GeoArea(46.7, 7.8, 1.0), null, -2, 0, 10);
	}
	
	@Test
	public void searchPicturesWithTitlePart() {
		QueryResult<MediaInfo> result = service.searchMedia(MediaType.PICTURE, null, "interlaken", 3650 * DateUtils.MILLIS_PER_DAY, 0, 10);
		assertNotNull(result);
		assertEquals(0, result.getPageNumber());
		assertEquals(1, result.getPageCount());
		assertNotNull(result.getContent());
		assertEquals(1, result.getContent().size());
	}
	
	@Test
	public void searchPicturesWithTags() {
		QueryResult<MediaInfo> result = service.searchMedia(MediaType.PICTURE, null, "sport", 3650 * DateUtils.MILLIS_PER_DAY, 0, 10);
		assertNotNull(result);
		assertEquals(0, result.getPageNumber());
		assertEquals(1, result.getPageCount());
		assertNotNull(result.getContent());
		assertEquals(1, result.getContent().size());
	}
	
	@Test
	public void searchPicturesInCategoryWithBadCategory() {
		QueryResult<MediaInfo> result = service.searchMediaInCategory(MediaType.PICTURE, null, "sport", 3650 * DateUtils.MILLIS_PER_DAY, "ART", 0, 10);
		assertNotNull(result);
		assertEquals(0, result.getPageNumber());
		assertEquals(0, result.getPageCount());
		assertNotNull(result.getContent());
		assertEquals(0, result.getContent().size());
	}
	
	@Test
	public void searchPicturesInCategoryWithTags() {
		QueryResult<MediaInfo> result = service.searchMediaInCategory(MediaType.PICTURE, null, "sport", 3650 * DateUtils.MILLIS_PER_DAY, "PLACES", 0, 10);
		assertNotNull(result);
		assertEquals(0, result.getPageNumber());
		assertEquals(1, result.getPageCount());
		assertNotNull(result.getContent());
		assertEquals(1, result.getContent().size());
	}
	
	@Test
	public void searchPicturesWithTitlePartAndTags() {
		QueryResult<MediaInfo> result = service.searchMedia(MediaType.PICTURE, null, "sport interlaken test", 3650 * DateUtils.MILLIS_PER_DAY, 0, 10);
		assertNotNull(result);
		assertEquals(0, result.getPageNumber());
		assertEquals(1, result.getPageCount());
		assertNotNull(result.getContent());
		assertEquals(1, result.getContent().size());
	}
	
	@Test
	public void searchPicturesInCategoryWithTitlePartAndTags() {
		QueryResult<MediaInfo> result = service.searchMediaInCategory(MediaType.PICTURE, null, "sport interlaken test", 3650 * DateUtils.MILLIS_PER_DAY, "PLACES", 0, 10);
		assertNotNull(result);
		assertEquals(0, result.getPageNumber());
		assertEquals(1, result.getPageCount());
		assertNotNull(result.getContent());
		assertEquals(1, result.getContent().size());
	}
	
	@Test
	public void searchPicturesWithBadTag() {
		QueryResult<MediaInfo> result = service.searchMedia(MediaType.PICTURE, null, "land", 3650 * DateUtils.MILLIS_PER_DAY, 0, 10);
		assertNotNull(result);
		assertEquals(0, result.getPageNumber());
		assertEquals(0, result.getPageCount());
		assertNotNull(result.getContent());
		assertEquals(0, result.getContent().size());
	}
	
	@Test
	public void searchPicturesInCategoryWithBadTag() {
		QueryResult<MediaInfo> result = service.searchMediaInCategory(MediaType.PICTURE, null, "land", 3650 * DateUtils.MILLIS_PER_DAY, "PLACES", 0, 10);
		assertNotNull(result);
		assertEquals(0, result.getPageNumber());
		assertEquals(0, result.getPageCount());
		assertNotNull(result.getContent());
		assertEquals(0, result.getContent().size());
	}
	
	@Test
	public void listPicturesByExistingProfileId() {
		QueryResult<MediaInfo> result = service.listMediaByProfile(MediaType.PICTURE, UUID.fromString("e7d166ae-9b3f-4405-be0d-fa1567728593"), 0, 10);
		assertNotNull(result);
		assertEquals(0, result.getPageNumber());
		assertEquals(1, result.getPageCount());
		assertNotNull(result.getContent());
		assertEquals(1, result.getContent().size());
	}
	
	@Test
	public void listPicturesByNonExistingProfileId() {
		QueryResult<MediaInfo> result = service.listMediaByProfile(MediaType.PICTURE, UUID.fromString("ddd166ae-9b3f-4405-be0d-fa1567728593"), 0, 10);
		assertNotNull(result);
		assertEquals(0, result.getPageNumber());
		assertEquals(0, result.getPageCount());
		assertNotNull(result.getContent());
		assertEquals(0, result.getContent().size());
	}
	
	@Test
	public void findPictureKeywordSuggestion() {
		List<String> suggestions = service.findKeywordSuggestions(MediaType.PICTURE, "para");
		assertEquals(Collections.singletonList("paragliding"), suggestions);
	}

	@Test
	public void testNoPictureKeywordSuggestion() {
		List<String> suggestions = service.findKeywordSuggestions(MediaType.PICTURE, "xxx");
		assertEquals(Collections.emptyList(), suggestions);
	}
	
	@Test
	public void findMisspeledPictureKeywordSuggestion() {
		List<String> suggestions = service.findKeywordSuggestions(MediaType.PICTURE, "parg");
		assertEquals(Collections.singletonList("paragliding"), suggestions);
	}
	
	@Test
	public void findPictureKeywordSuggestionFromTitle() {
		List<String> suggestions = service.findKeywordSuggestions(MediaType.PICTURE, "Interl");
		assertEquals(Collections.singletonList("interlaken"), suggestions);
	}
	
	@Test
	public void findSentencePictureKeywordSuggestion() {
		List<String> suggestions = service.findKeywordSuggestions(MediaType.PICTURE, "Interlaken spo");
		assertEquals(Collections.singletonList("interlaken sport"), suggestions);
	}
	
	@Test
	public void increaseCommentCount() {
		DateTime beforeUpdate = DateTime.now();
		MediaInfo info = service.increaseCommentCount(URI.create("20130712/7e9a5a5bd5e64171c176ac6c7b32d685.jpg"));
		assertNotNull(info);
		assertEquals(1, info.getCommentCount());
		assertTrue(info.getLastUpdate().compareTo(beforeUpdate) >= 0);
	}
	
	@Test
	public void mapPictureMatchCountWithKeyword() {
		GeoArea area = new GeoArea(46.95, 7.5, 100.0);
		List<GeoStatistic> result = service.mapMediaMatchCount(MediaType.PICTURE, area, "paragliding", 3650 * DateUtils.MILLIS_PER_DAY, null);
		assertNotNull(result);
		assertEquals(1, result.size());
		GeoStatistic stat = result.iterator().next();
		assertEquals(1, stat.getCount());
		assertEquals(46.7, stat.getLatitude(), 0.1);
		assertEquals(7.9, stat.getLongitude(), 0.1);
		assertEquals(2.4, stat.getRadius(), 0.1);
	}
	
	@Test
	public void mapPictureMatchCountWithoutKeyword() {
		GeoArea area = new GeoArea(46.95, 7.5, 100.0);
		List<GeoStatistic> result = service.mapMediaMatchCount(MediaType.PICTURE, area, null, 3650 * DateUtils.MILLIS_PER_DAY, null);
		assertNotNull(result);
		assertEquals(1, result.size());
		GeoStatistic stat = result.iterator().next();
		assertEquals(1, stat.getCount());
		assertEquals(46.7, stat.getLatitude(), 0.1);
		assertEquals(7.9, stat.getLongitude(), 0.1);
		assertEquals(2.4, stat.getRadius(), 0.1);
	}
	
	@Test
	public void mapPictureMatchCountWithProfiles() {
		GeoArea area = new GeoArea(46.95, 7.5, 100.0);
		List<GeoStatistic> result = service.mapMediaMatchCount(MediaType.PICTURE, area, "paragliding", 3650 * DateUtils.MILLIS_PER_DAY, Arrays.asList(UUID.fromString("a95472c0-e0e6-11e2-a28f-0800200c9a77"), UUID.fromString("e7d166ae-9b3f-4405-be0d-fa1567728593")));
		assertNotNull(result);
		assertEquals(1, result.size());
		GeoStatistic stat = result.iterator().next();
		assertEquals(1, stat.getCount());
		assertEquals(46.7, stat.getLatitude(), 0.1);
		assertEquals(7.9, stat.getLongitude(), 0.1);
		assertEquals(2.4, stat.getRadius(), 0.1);
	}
	
	@Test
	public void mapPictureMatchCountForWorld() {
		GeoArea area = new GeoArea(0.0, 0.0, 26000.0);
		List<GeoStatistic> result = service.mapMediaMatchCount(MediaType.PICTURE, area, null, 3650 * DateUtils.MILLIS_PER_DAY, null);
		assertNotNull(result);
		assertEquals(1, result.size());
		GeoStatistic stat = result.iterator().next();
		assertEquals(1, stat.getCount());
		assertEquals(47.8, stat.getLatitude(), 0.1);
		assertEquals(5.6, stat.getLongitude(), 0.1);
		assertEquals(469.1, stat.getRadius(), 0.1);
	}
	
	@Test
	public void mapPictureMatchCountWithUnknownProfile() {
		GeoArea area = new GeoArea(46.95, 7.5, 100.0);
		List<GeoStatistic> result = service.mapMediaMatchCount(MediaType.PICTURE, area, "paragliding", 3650 * DateUtils.MILLIS_PER_DAY, Collections.singletonList(UUID.fromString("d95472c0-e0e6-11e2-a28f-0800200c9a77")));
		assertNotNull(result);
		assertEquals(0, result.size());
	}
}


