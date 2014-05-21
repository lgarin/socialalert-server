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
import com.bravson.socialalert.app.entities.PictureAlert;
import com.bravson.socialalert.app.entities.ProfileStatistic;
import com.bravson.socialalert.app.exceptions.DataMissingException;
import com.bravson.socialalert.app.services.PictureAlertService;
import com.bravson.socialalert.common.domain.ApprovalModifier;
import com.bravson.socialalert.common.domain.GeoAddress;
import com.bravson.socialalert.common.domain.GeoArea;
import com.bravson.socialalert.common.domain.GeoStatistic;
import com.bravson.socialalert.common.domain.MediaCategory;
import com.bravson.socialalert.common.domain.PictureInfo;
import com.bravson.socialalert.common.domain.QueryResult;
import com.bravson.socialalert.infrastructure.DataServiceTest;

public class PictureAlertServiceTest extends DataServiceTest {

	@Resource
	private PictureAlertService service;
	
	@Before
	public void setUp() throws Exception {
		fullImport(PictureAlert.class);
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
		PictureInfo info = service.createAlert(pictureUri, profileId, "Test", address, metadata, Collections.singletonList(MediaCategory.ART), Collections.<String>emptyList());
		assertNotNull(info);
		assertEquals(profileId, info.getProfileId());
		assertEquals(pictureUri, info.getPictureUri());
		assertEquals("Test", info.getTitle());
		assertEquals(Arrays.asList("Ostermundigen", "Switzerland"), info.getTags());
		assertEquals(Collections.singletonList("ART"), info.getCategories());
		assertEquals(metadata.getHeight(), info.getPictureHeight());
		assertEquals(metadata.getWidth(), info.getPictureWidth());
		assertNull(info.getPictureLongitude());
		assertNull(info.getPictureLatitude());
		assertEquals(metadata.getTimestamp(), info.getPictureTimestamp());
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
		PictureInfo info = service.createAlert(pictureUri, profileId, "Test", address, metadata, Collections.<MediaCategory>emptyList(), Collections.singletonList("Tag"));
		assertNotNull(info);
		assertEquals(profileId, info.getProfileId());
		assertEquals(pictureUri, info.getPictureUri());
		assertEquals("Test", info.getTitle());
		assertNotNull(info.getTags());
		assertEquals(3, info.getTags().size());
		assertEquals("Tag", info.getTags().get(0));
		assertTrue(info.getCategories().isEmpty());
		assertNull(info.getPictureHeight());
		assertNull(info.getPictureWidth());
		assertEquals(46.9, info.getPictureLongitude(), 0.01);
		assertEquals(7.5, info.getPictureLatitude(), 0.01);
		assertNull(info.getPictureTimestamp());
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
		service.createAlert(pictureUri, profileId, "Test", null, metadata, Collections.singletonList(MediaCategory.ART), Collections.singletonList("Tag"));
	}
	
	@Test
	public void updateExistingPicture() {
		URI pictureUri = URI.create("20130716/f317f3c7918c83ff6ec24aabb6c017fd.jpg");
		UUID profileId = UUID.fromString("e7d166ae-9b3f-4405-be0d-fa1567728593");
		DateTime beforeUpdate = DateTime.now();
		GeoAddress address = new GeoAddress(7.5, 46.9, "Terrassenrain 6, 3072 Ostermundigen, Suisse", "Ostermundigen", "Switzerland");
		PictureInfo info = service.updateAlert(pictureUri, profileId, "New title", "A description", address, "Sony", "RX100II", null, Collections.singletonList(MediaCategory.NEWS), Collections.singletonList("New tag"));
		assertNotNull(info);
		assertEquals(profileId, info.getProfileId());
		assertEquals(pictureUri, info.getPictureUri());
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
		PictureInfo info = service.viewPictureDetail(URI.create("20130712/7e9a5a5bd5e64171c176ac6c7b32d685.jpg"));
		assertNotNull(info);
		assertEquals(2, info.getHitCount());
	}
	
	@Test(expected=DataMissingException.class)
	public void viewInvalidURI() {
		service.viewPictureDetail(URI.create("xxx.jpg"));
	}
	
	@Test
	public void getValidURI() {
		PictureInfo info = service.getPictureInfo(URI.create("20130712/7e9a5a5bd5e64171c176ac6c7b32d685.jpg"));
		assertNotNull(info);
		assertEquals(1, info.getHitCount());
		assertEquals(UUID.fromString("a95472c0-e0e6-11e2-a28f-0800200c9a77"), info.getProfileId());
	}
	
	@Test(expected=DataMissingException.class)
	public void getInvalidURI() {
		service.getPictureInfo(URI.create("xxx.jpg"));
	}
	
	@Test
	public void listAllRecentPictures() {
		QueryResult<PictureInfo> result = service.searchPictures(null, null, 360 * DateUtils.MILLIS_PER_DAY, 0, 10);
		assertNotNull(result);
		assertEquals(0, result.getPageNumber());
		assertEquals(1, result.getPageCount());
		assertNotNull(result.getContent());
		assertEquals(2, result.getContent().size());
	}
	
	@Test
	public void listAllRecentPicturesByCategory() {
		QueryResult<PictureInfo> result = service.searchPicturesInCategory(null, null, 360 * DateUtils.MILLIS_PER_DAY, "PLACES", 0, 10);
		assertNotNull(result);
		assertEquals(0, result.getPageNumber());
		assertEquals(1, result.getPageCount());
		assertNotNull(result.getContent());
		assertEquals(2, result.getContent().size());
	}
	
	@Test
	public void pageThroughRecentPictures() {
		QueryResult<PictureInfo> result1 = service.searchPictures(null, null, 360 * DateUtils.MILLIS_PER_DAY, 0, 1);
		assertNotNull(result1);
		assertEquals(0, result1.getPageNumber());
		assertEquals(2, result1.getPageCount());
		assertNotNull(result1.getContent());
		assertEquals(1, result1.getContent().size());
		PictureInfo first = result1.getContent().get(0);
		QueryResult<PictureInfo> result2 = service.searchPictures(null, null, 360 * DateUtils.MILLIS_PER_DAY, 1, 1);
		assertNotNull(result2);
		assertEquals(1, result2.getPageNumber());
		assertEquals(2, result2.getPageCount());
		assertNotNull(result2.getContent());
		assertEquals(1, result2.getContent().size());
		PictureInfo second = result2.getContent().get(0);
		assertTrue(second.getCreation().isBefore(first.getCreation()));
	}
	
	@Test
	public void listNoRecentPictures() {
		QueryResult<PictureInfo> result = service.searchPictures(null, null, 360 * DateUtils.MILLIS_PER_DAY, 3, 10);
		assertNotNull(result);
		assertEquals(3, result.getPageNumber());
		assertEquals(1, result.getPageCount());
		assertNotNull(result.getContent());
		assertEquals(0, result.getContent().size());
	}
	
	@Test
	public void listNoRecentPicturesInCategory() {
		QueryResult<PictureInfo> result = service.searchPicturesInCategory(null, null, 360 * DateUtils.MILLIS_PER_DAY, "PLACES", 3, 10);
		assertNotNull(result);
		assertEquals(3, result.getPageNumber());
		assertEquals(1, result.getPageCount());
		assertNotNull(result.getContent());
		assertEquals(0, result.getContent().size());
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void listRecentPicturesWithLargePageSize() {
		service.searchPictures(null, null, 360 * DateUtils.MILLIS_PER_DAY, 0, 100000);
	}
	
	@Test(expected=ValidationException.class)
	public void listRecentPicturesWithInvalidParameter() {
		service.searchPictures(null, null, 360 * DateUtils.MILLIS_PER_DAY, 0, -1);
	}
	
	@Test(expected=DataMissingException.class)
	public void deleteNonExistingPicture() {
		URI pictureUri = URI.create("7e9a5a5bd5e64171c176ac6c7b32d685.jpg");
		service.deletePicture(pictureUri, UUID.fromString("a95472c0-e0e6-11e2-a28f-0800200c9a77"));
	}
	
	@Test
	public void deleteExistingPicture() {
		URI pictureUri = URI.create("20130712/7e9a5a5bd5e64171c176ac6c7b32d685.jpg");
		service.deletePicture(pictureUri, UUID.fromString("a95472c0-e0e6-11e2-a28f-0800200c9a77"));
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void deletePictureWithOtherProfile() {
		URI pictureUri = URI.create("20130712/7e9a5a5bd5e64171c176ac6c7b32d685.jpg");
		service.deletePicture(pictureUri, UUID.fromString("e7d166ae-9b3f-4405-be0d-fa1567728593"));
	}
	
	@Test
	public void likePicture() {
		DateTime beforeUpdate = DateTime.now();
		PictureInfo info = service.updateLikeDislike(URI.create("20130716/f317f3c7918c83ff6ec24aabb6c017fd.jpg"), null, ApprovalModifier.LIKE);
		assertNotNull(info);
		assertEquals(2, info.getLikeCount());
		assertEquals(1, info.getDislikeCount());
		assertTrue(info.getLastUpdate().compareTo(beforeUpdate) >= 0);
	}
	
	@Test
	public void revertLikedPicture() {
		DateTime beforeUpdate = DateTime.now();
		PictureInfo info = service.updateLikeDislike(URI.create("20130716/f317f3c7918c83ff6ec24aabb6c017fd.jpg"), ApprovalModifier.LIKE, null);
		assertNotNull(info);
		assertEquals(0, info.getLikeCount());
		assertEquals(1, info.getDislikeCount());
		assertTrue(info.getLastUpdate().compareTo(beforeUpdate) >= 0);
	}
	
	@Test
	public void confirmLikedPicture() {
		DateTime beforeUpdate = DateTime.now();
		PictureInfo info = service.updateLikeDislike(URI.create("20130716/f317f3c7918c83ff6ec24aabb6c017fd.jpg"), ApprovalModifier.LIKE, ApprovalModifier.LIKE);
		assertNotNull(info);
		assertEquals(1, info.getLikeCount());
		assertEquals(1, info.getDislikeCount());
		assertTrue(info.getLastUpdate().compareTo(beforeUpdate) >= 0);
	}
	
	@Test
	public void dislikePicture() {
		DateTime beforeUpdate = DateTime.now();
		PictureInfo info = service.updateLikeDislike(URI.create("20130716/f317f3c7918c83ff6ec24aabb6c017fd.jpg"), null, ApprovalModifier.DISLIKE);
		assertNotNull(info);
		assertEquals(1, info.getLikeCount());
		assertEquals(2, info.getDislikeCount());
		assertTrue(info.getLastUpdate().compareTo(beforeUpdate) >= 0);
	}
	
	@Test
	public void revertDislikedPicture() {
		DateTime beforeUpdate = DateTime.now();
		PictureInfo info = service.updateLikeDislike(URI.create("20130716/f317f3c7918c83ff6ec24aabb6c017fd.jpg"), ApprovalModifier.DISLIKE, null);
		assertNotNull(info);
		assertEquals(1, info.getLikeCount());
		assertEquals(0, info.getDislikeCount());
		assertTrue(info.getLastUpdate().compareTo(beforeUpdate) >= 0);
	}
	
	@Test
	public void confirmDislikedPicture() {
		DateTime beforeUpdate = DateTime.now();
		PictureInfo info = service.updateLikeDislike(URI.create("20130716/f317f3c7918c83ff6ec24aabb6c017fd.jpg"), ApprovalModifier.DISLIKE, ApprovalModifier.DISLIKE);
		assertNotNull(info);
		assertEquals(1, info.getLikeCount());
		assertEquals(1, info.getDislikeCount());
		assertTrue(info.getLastUpdate().compareTo(beforeUpdate) >= 0);
	}
	
	@Test
	public void dislikeLikedPicture() {
		DateTime beforeUpdate = DateTime.now();
		PictureInfo info = service.updateLikeDislike(URI.create("20130716/f317f3c7918c83ff6ec24aabb6c017fd.jpg"), ApprovalModifier.LIKE, ApprovalModifier.DISLIKE);
		assertNotNull(info);
		assertEquals(0, info.getLikeCount());
		assertEquals(2, info.getDislikeCount());
		assertTrue(info.getLastUpdate().compareTo(beforeUpdate) >= 0);
	}
	
	@Test
	public void likeDislikedPicture() {
		DateTime beforeUpdate = DateTime.now();
		PictureInfo info = service.updateLikeDislike(URI.create("20130716/f317f3c7918c83ff6ec24aabb6c017fd.jpg"), ApprovalModifier.DISLIKE, ApprovalModifier.LIKE);
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
		QueryResult<PictureInfo> result = service.searchPictures(new GeoArea(46.7, 7.8, 5.0), null, 360 * DateUtils.MILLIS_PER_DAY, 0, 10);
		assertNotNull(result);
		assertEquals(0, result.getPageNumber());
		assertEquals(1, result.getPageCount());
		assertNotNull(result.getContent());
		assertEquals(1, result.getContent().size());
	}
	
	@Test
	public void searchPicturesInAreaWithKeyword() {
		QueryResult<PictureInfo> result = service.searchPictures(new GeoArea(46.7, 7.8, 5.0), "sport", 360 * DateUtils.MILLIS_PER_DAY, 0, 10);
		assertNotNull(result);
		assertEquals(0, result.getPageNumber());
		assertEquals(1, result.getPageCount());
		assertNotNull(result.getContent());
		assertEquals(1, result.getContent().size());
	}
	
	@Test
	public void searchPicturesInAreaWithBadKeyword() {
		QueryResult<PictureInfo> result = service.searchPictures(new GeoArea(46.7, 7.8, 5.0), "abc", 360 * DateUtils.MILLIS_PER_DAY, 0, 10);
		assertNotNull(result);
		assertEquals(0, result.getPageNumber());
		assertEquals(0, result.getPageCount());
		assertNotNull(result.getContent());
		assertEquals(0, result.getContent().size());
	}
	
	@Test
	public void listPicturesOutOfArea() {
		QueryResult<PictureInfo> result = service.searchPictures(new GeoArea(46.7, 7.8, 1.0), null, 360 * DateUtils.MILLIS_PER_DAY, 0, 10);
		assertNotNull(result);
		assertEquals(0, result.getPageNumber());
		assertEquals(0, result.getPageCount());
		assertNotNull(result.getContent());
		assertEquals(0, result.getContent().size());
	}

	@Test(expected=ValidationException.class)
	public void listPicturesInAreaWithInvalidAge() {
		service.searchPictures(new GeoArea(46.7, 7.8, 1.0), null, -2, 0, 10);
	}
	
	@Test
	public void searchPicturesWithTitlePart() {
		QueryResult<PictureInfo> result = service.searchPictures(null, "interlaken", 360 * DateUtils.MILLIS_PER_DAY, 0, 10);
		assertNotNull(result);
		assertEquals(0, result.getPageNumber());
		assertEquals(1, result.getPageCount());
		assertNotNull(result.getContent());
		assertEquals(1, result.getContent().size());
	}
	
	@Test
	public void searchPicturesWithTags() {
		QueryResult<PictureInfo> result = service.searchPictures(null, "sport", 360 * DateUtils.MILLIS_PER_DAY, 0, 10);
		assertNotNull(result);
		assertEquals(0, result.getPageNumber());
		assertEquals(1, result.getPageCount());
		assertNotNull(result.getContent());
		assertEquals(1, result.getContent().size());
	}
	
	@Test
	public void searchPicturesInCategoryWithBadCategory() {
		QueryResult<PictureInfo> result = service.searchPicturesInCategory(null, "sport", 360 * DateUtils.MILLIS_PER_DAY, "ART", 0, 10);
		assertNotNull(result);
		assertEquals(0, result.getPageNumber());
		assertEquals(0, result.getPageCount());
		assertNotNull(result.getContent());
		assertEquals(0, result.getContent().size());
	}
	
	@Test
	public void searchPicturesInCategoryWithTags() {
		QueryResult<PictureInfo> result = service.searchPicturesInCategory(null, "sport", 360 * DateUtils.MILLIS_PER_DAY, "PLACES", 0, 10);
		assertNotNull(result);
		assertEquals(0, result.getPageNumber());
		assertEquals(1, result.getPageCount());
		assertNotNull(result.getContent());
		assertEquals(1, result.getContent().size());
	}
	
	@Test
	public void searchPicturesWithTitlePartAndTags() {
		QueryResult<PictureInfo> result = service.searchPictures(null, "sport interlaken test", 360 * DateUtils.MILLIS_PER_DAY, 0, 10);
		assertNotNull(result);
		assertEquals(0, result.getPageNumber());
		assertEquals(1, result.getPageCount());
		assertNotNull(result.getContent());
		assertEquals(1, result.getContent().size());
	}
	
	@Test
	public void searchPicturesInCategoryWithTitlePartAndTags() {
		QueryResult<PictureInfo> result = service.searchPicturesInCategory(null, "sport interlaken test", 360 * DateUtils.MILLIS_PER_DAY, "PLACES", 0, 10);
		assertNotNull(result);
		assertEquals(0, result.getPageNumber());
		assertEquals(1, result.getPageCount());
		assertNotNull(result.getContent());
		assertEquals(1, result.getContent().size());
	}
	
	@Test
	public void searchPicturesWithBadTag() {
		QueryResult<PictureInfo> result = service.searchPictures(null, "land", 360 * DateUtils.MILLIS_PER_DAY, 0, 10);
		assertNotNull(result);
		assertEquals(0, result.getPageNumber());
		assertEquals(0, result.getPageCount());
		assertNotNull(result.getContent());
		assertEquals(0, result.getContent().size());
	}
	
	@Test
	public void searchPicturesInCategoryWithBadTag() {
		QueryResult<PictureInfo> result = service.searchPicturesInCategory(null, "land", 360 * DateUtils.MILLIS_PER_DAY, "PLACES", 0, 10);
		assertNotNull(result);
		assertEquals(0, result.getPageNumber());
		assertEquals(0, result.getPageCount());
		assertNotNull(result.getContent());
		assertEquals(0, result.getContent().size());
	}
	
	@Test
	public void listPicturesByExistingProfileId() {
		QueryResult<PictureInfo> result = service.listPicturesByProfile(UUID.fromString("e7d166ae-9b3f-4405-be0d-fa1567728593"), 0, 10);
		assertNotNull(result);
		assertEquals(0, result.getPageNumber());
		assertEquals(1, result.getPageCount());
		assertNotNull(result.getContent());
		assertEquals(1, result.getContent().size());
	}
	
	@Test
	public void listPicturesByNonExistingProfileId() {
		QueryResult<PictureInfo> result = service.listPicturesByProfile(UUID.fromString("ddd166ae-9b3f-4405-be0d-fa1567728593"), 0, 10);
		assertNotNull(result);
		assertEquals(0, result.getPageNumber());
		assertEquals(0, result.getPageCount());
		assertNotNull(result.getContent());
		assertEquals(0, result.getContent().size());
	}
	
	@Test
	public void findSuggestion() {
		List<String> suggestions = service.findKeywordSuggestions("para");
		assertEquals(Collections.singletonList("paragliding"), suggestions);
	}

	@Test
	public void testNoSuggestion() {
		List<String> suggestions = service.findKeywordSuggestions("xxx");
		assertEquals(Collections.emptyList(), suggestions);
	}
	
	@Test
	public void findMisspeledSuggestion() {
		List<String> suggestions = service.findKeywordSuggestions("parg");
		assertEquals(Collections.singletonList("paragliding"), suggestions);
	}
	
	@Test
	public void findSuggestionFromTitle() {
		List<String> suggestions = service.findKeywordSuggestions("Interl");
		assertEquals(Collections.singletonList("interlaken"), suggestions);
	}
	
	@Test
	public void findSentenceSuggestion() {
		List<String> suggestions = service.findKeywordSuggestions("Interlaken spo");
		assertEquals(Collections.singletonList("interlaken sport"), suggestions);
	}
	
	@Test
	public void increaseCommentCount() {
		DateTime beforeUpdate = DateTime.now();
		PictureInfo info = service.increaseCommentCount(URI.create("20130712/7e9a5a5bd5e64171c176ac6c7b32d685.jpg"));
		assertNotNull(info);
		assertEquals(1, info.getCommentCount());
		assertTrue(info.getLastUpdate().compareTo(beforeUpdate) >= 0);
	}
	
	@Test
	public void mapPictureMatchCountWithKeyword() {
		GeoArea area = new GeoArea(46.95, 7.5, 100.0);
		List<GeoStatistic> result = service.mapPictureMatchCount(area, "paragliding", 360 * DateUtils.MILLIS_PER_DAY, null);
		assertNotNull(result);
		assertEquals(1, result.size());
		GeoStatistic stat = result.iterator().next();
		assertEquals(1, stat.getCount());
		assertEquals(46.7, stat.getLatitude(), 0.2);
		assertEquals(7.8, stat.getLongitude(), 0.2);
		assertEquals(stat.getRadius(), 20.0, 1.0);
	}
	
	@Test
	public void mapPictureMatchCountWithoutKeyword() {
		GeoArea area = new GeoArea(46.95, 7.5, 100.0);
		List<GeoStatistic> result = service.mapPictureMatchCount(area, null, 360 * DateUtils.MILLIS_PER_DAY, null);
		assertNotNull(result);
		assertEquals(1, result.size());
		GeoStatistic stat = result.iterator().next();
		assertEquals(1, stat.getCount());
		assertEquals(46.7, stat.getLatitude(), 0.2);
		assertEquals(7.8, stat.getLongitude(), 0.2);
		assertEquals(stat.getRadius(), 20.0, 1.0);
	}
	
	@Test
	public void mapPictureMatchCountWithProfiles() {
		GeoArea area = new GeoArea(46.95, 7.5, 100.0);
		List<GeoStatistic> result = service.mapPictureMatchCount(area, "paragliding", 360 * DateUtils.MILLIS_PER_DAY, Arrays.asList(UUID.fromString("a95472c0-e0e6-11e2-a28f-0800200c9a77"), UUID.fromString("e7d166ae-9b3f-4405-be0d-fa1567728593")));
		assertNotNull(result);
		assertEquals(1, result.size());
		GeoStatistic stat = result.iterator().next();
		assertEquals(1, stat.getCount());
		assertEquals(46.7, stat.getLatitude(), 0.2);
		assertEquals(7.8, stat.getLongitude(), 0.2);
		assertEquals(stat.getRadius(), 20.0, 1.0);
	}
	
	@Test
	public void mapPictureMatchCountWithUnknownProfile() {
		GeoArea area = new GeoArea(46.95, 7.5, 100.0);
		List<GeoStatistic> result = service.mapPictureMatchCount(area, "paragliding", 360 * DateUtils.MILLIS_PER_DAY, Collections.singletonList(UUID.fromString("d95472c0-e0e6-11e2-a28f-0800200c9a77")));
		assertNotNull(result);
		assertEquals(0, result.size());
	}
}


