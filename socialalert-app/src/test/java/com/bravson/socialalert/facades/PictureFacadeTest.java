package com.bravson.socialalert.facades;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Resource;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.context.SecurityContextHolder;

import com.bravson.socialalert.app.entities.AlertAlbum;
import com.bravson.socialalert.app.entities.AlertComment;
import com.bravson.socialalert.app.entities.AlertInteraction;
import com.bravson.socialalert.app.entities.ApplicationUser;
import com.bravson.socialalert.app.entities.PictureAlert;
import com.bravson.socialalert.app.entities.TagStatistic;
import com.bravson.socialalert.app.entities.UserProfile;
import com.bravson.socialalert.app.exceptions.DataMissingException;
import com.bravson.socialalert.app.services.MediaStorageService;
import com.bravson.socialalert.app.services.UserSessionService;
import com.bravson.socialalert.common.domain.ActivityInfo;
import com.bravson.socialalert.common.domain.ActivityType;
import com.bravson.socialalert.common.domain.AlbumInfo;
import com.bravson.socialalert.common.domain.ApprovalModifier;
import com.bravson.socialalert.common.domain.CommentInfo;
import com.bravson.socialalert.common.domain.GeoAddress;
import com.bravson.socialalert.common.domain.GeoStatistic;
import com.bravson.socialalert.common.domain.MediaCategory;
import com.bravson.socialalert.common.domain.PictureInfo;
import com.bravson.socialalert.common.domain.QueryResult;
import com.bravson.socialalert.common.domain.TagInfo;
import com.bravson.socialalert.common.facade.PictureFacade;
import com.bravson.socialalert.common.facade.UserFacade;
import com.bravson.socialalert.infrastructure.DataServiceTest;

public class PictureFacadeTest extends DataServiceTest {
	
	@Resource
	private PictureFacade facade;
	
	@Resource
	private UserFacade userFacade;
	
	@Resource
	private MediaStorageService storageService;
	
	@Resource
	private UserSessionService sessionService;
	
	@Value("${media.temp.dir}")
	private File tempDir;
	
	@Value("${media.base.dir}")
	private File baseDir;
	
	@Before
	public void setUp() throws Exception {
		FileUtils.cleanDirectory(tempDir);
		FileUtils.cleanDirectory(baseDir);
		fullImport(ApplicationUser.class);
		fullImport(UserProfile.class);
		fullImport(PictureAlert.class);
		fullImport(AlertInteraction.class);
		fullImport(AlertComment.class);
		fullImport(AlertAlbum.class);
		fullImport(TagStatistic.class);
		sessionService.clearAll();
		SecurityContextHolder.clearContext();
	}

	@Test
	public void claimValidPicture() throws IOException {
		userFacade.login("lucien@test.com", "123");
		File file = new File("src/test/resources/media/IMG_0397.JPG");
		URI uri = storageService.storePicture(FileUtils.openInputStream(file), (int) file.length());
		PictureInfo info = facade.claimPicture(uri, "Test", null, Collections.<MediaCategory>emptyList(), Collections.<String>emptyList());
		assertNotNull(info);
		assertEquals("sg33g5", info.getCreator());
		assertEquals("Test", info.getTitle());
		assertEquals(UUID.fromString("a95472c0-e0e6-11e2-a28f-0800200c9a77"), info.getProfileId());
		assertNotNull(info.getTags());
		assertTrue(info.getTags().isEmpty());
		assertEquals(Integer.valueOf(2448), info.getPictureWidth());
		assertEquals(Integer.valueOf(3264), info.getPictureHeight());
		assertEquals(new DateTime(2013, 4, 14, 16, 28, 26), info.getPictureTimestamp());
		assertEquals("Apple", info.getCameraMaker());
		assertEquals("iPhone 5", info.getCameraModel());
		assertEquals(46.68666666666667, info.getPictureLatitude(), 0.0);
		assertEquals(7.858833333333333, info.getPictureLongitude(), 0.0);
		assertEquals(0, info.getHitCount());
		assertEquals(0, info.getLikeCount());
		assertEquals(0, info.getDislikeCount());
	}
	
	@Test(expected=DuplicateKeyException.class)
	public void claimValidPictureTwice() throws IOException {
		userFacade.login("lucien@test.com", "123");
		File file = new File("src/test/resources/media/IMG_0397.JPG");
		URI uri = storageService.storePicture(FileUtils.openInputStream(file), (int) file.length());
		facade.claimPicture(uri, "Test", null, Collections.<MediaCategory>emptyList(), Collections.<String>emptyList());
		URI finalUri = storageService.buildFinalMediaUri(uri, DateTime.now());
		storageService.archiveMedia(uri, finalUri);
		storageService.storePicture(FileUtils.openInputStream(file), (int) file.length());
		facade.claimPicture(uri, "Test", null, Collections.<MediaCategory>emptyList(), Collections.<String>emptyList());
	}
	
	@Test(expected=AccessDeniedException.class)
	public void claimPictureWithGuest() throws IOException {
		userFacade.login("unverified@test.com", "123");
		File file = new File("src/test/resources/media/IMG_0397.JPG");
		URI uri = storageService.storePicture(FileUtils.openInputStream(file), (int) file.length());
		facade.claimPicture(uri, "Test", new GeoAddress(), Collections.<MediaCategory>emptyList(), Collections.<String>emptyList());
	}
	
	@Test(expected=DataMissingException.class)
	public void claimUndefinedPicture() throws IOException {
		userFacade.login("lucien@test.com", "123");
		URI uri = URI.create("xxx.jpg");
		facade.claimPicture(uri, "Test", null, Collections.<MediaCategory>emptyList(), Collections.<String>emptyList());
	}
	
	@Test(expected=AuthenticationCredentialsNotFoundException.class)
	public void claimPictureWithoutLogin() throws IOException {
		File file = new File("src/test/resources/media/IMG_0397.JPG");
		URI uri = storageService.storePicture(FileUtils.openInputStream(file), (int) file.length());
		facade.claimPicture(uri, "Test", null, Collections.<MediaCategory>emptyList(), Collections.<String>emptyList());
	}
	
	@Test
	public void updateExistingPicture() throws IOException {
		userFacade.login("lucien@test.com", "123");
		URI pictureUri = URI.create("20130712/7e9a5a5bd5e64171c176ac6c7b32d685.jpg");
		PictureInfo info = facade.updatePictureInfo(pictureUri, "New title", "A description", null, "Sony", "RX100II", null, Collections.singletonList(MediaCategory.NEWS), Collections.singletonList("New tag"));
		assertNotNull(info);
		assertEquals(pictureUri, info.getPictureUri());
		assertEquals("New title", info.getTitle());
		assertEquals("A description", info.getDescription());
		assertEquals("Sony", info.getCameraMaker());
		assertEquals("RX100II", info.getCameraModel());
		assertEquals(Collections.singletonList("NEWS"), info.getCategories());
		assertEquals(Collections.singletonList("New tag"), info.getTags());
		assertTrue(DateTime.parse("2013-07-12T12:04:01Z").compareTo(info.getCreation()) == 0);
	}
	
	@Test
	public void viewValidURI() throws IOException {
		userFacade.login("lucien@test.com", "123");
		PictureInfo info = facade.viewPictureDetail(URI.create("20130712/7e9a5a5bd5e64171c176ac6c7b32d685.jpg"));
		assertNotNull(info);
		assertEquals(2, info.getHitCount());
	}
	
	@Test(expected=DataMissingException.class)
	public void viewInvalidURI() throws IOException {
		userFacade.login("lucien@test.com", "123");
		facade.viewPictureDetail(URI.create("xxx.jpg"));
	}

	@Test(expected=DataMissingException.class)
	public void deleteNonExistingPicture() throws IOException {
		userFacade.login("lucien@test.com", "123");
		URI pictureUri = URI.create("7e9a5a5bd5e64171c176ac6c7b32d685.jpg");
		facade.deletePicture(pictureUri);
	}
	
	@Test
	public void deleteExistingPicture() throws IOException {
		userFacade.login("lucien@test.com", "123");
		URI pictureUri = URI.create("20130712/7e9a5a5bd5e64171c176ac6c7b32d685.jpg");
		facade.deletePicture(pictureUri);
	}
	
	@Test
	public void dislikeLikedPicture() throws IOException {
		userFacade.login("lucien@test.com", "123");
		URI pictureUri = URI.create("20130712/7e9a5a5bd5e64171c176ac6c7b32d685.jpg");
		PictureInfo info = facade.setPictureApproval(pictureUri, ApprovalModifier.DISLIKE);
		assertNotNull(info);
		assertEquals(pictureUri, info.getPictureUri());
		assertEquals(0, info.getLikeCount());
		assertEquals(1, info.getDislikeCount());
	}
	
	@Test
	public void likeLikedPicture() throws IOException {
		userFacade.login("lucien@test.com", "123");
		URI pictureUri = URI.create("20130712/7e9a5a5bd5e64171c176ac6c7b32d685.jpg");
		PictureInfo info = facade.setPictureApproval(pictureUri, ApprovalModifier.LIKE);
		assertNotNull(info);
		assertEquals(pictureUri, info.getPictureUri());
		assertEquals(1, info.getLikeCount());
		assertEquals(0, info.getDislikeCount());
	}
	
	@Test(expected=DataMissingException.class)
	public void likeUnknownPicture() throws IOException {
		userFacade.login("lucien@test.com", "123");
		URI pictureUri = URI.create("abc.jpg");
		facade.setPictureApproval(pictureUri, ApprovalModifier.DISLIKE);
	}
	
	@Test(expected=AuthenticationCredentialsNotFoundException.class)
	public void likePictureWithoutLogin() throws IOException {
		URI pictureUri = URI.create("20130712/7e9a5a5bd5e64171c176ac6c7b32d685.jpg");
		facade.setPictureApproval(pictureUri, ApprovalModifier.DISLIKE);
	}
	
	@Test
	public void commentPicture() throws IOException {
		userFacade.login("lucien@test.com", "123");
		URI pictureUri = URI.create("20130712/7e9a5a5bd5e64171c176ac6c7b32d685.jpg");
		CommentInfo comment = facade.addComment(pictureUri, "My valuable comment");
		assertNotNull(comment);
		assertEquals("sg33g5", comment.getCreator());
		assertEquals("My valuable comment", comment.getComment());
		assertEquals(pictureUri, comment.getMediaUri());
		assertTrue(comment.isOnline());
	}
	
	@Test(expected=DataMissingException.class)
	public void commentUnknownPicture() throws IOException {
		userFacade.login("lucien@test.com", "123");
		URI pictureUri = URI.create("abc.jpg");
		facade.addComment(pictureUri, "Useless comment");
	}
	
	@Test(expected=AuthenticationCredentialsNotFoundException.class)
	public void commentPictureWithoutLogin() throws IOException {
		URI pictureUri = URI.create("20130712/7e9a5a5bd5e64171c176ac6c7b32d685.jpg");
		facade.addComment(pictureUri, "Unauthorized comment");
	}
	
	@Test
	public void listComments() throws IOException {
		userFacade.login("lucien@test.com", "123");
		URI mediaUri = URI.create("20130712/7e9a5a5bd5e64171c176ac6c7b32d685.jpg");
		QueryResult<CommentInfo> result = facade.listComments(mediaUri, 0, 10);
		assertNotNull(result);
		assertEquals(1, result.getPageCount());
		assertEquals(0, result.getPageNumber());
		assertNotNull(result.getContent());
		assertEquals(2, result.getContent().size());
		CommentInfo comment1 = result.getContent().get(0);
		assertNotNull(comment1);
		assertEquals("Hello 2", comment1.getComment());
		assertEquals("sg33g5", comment1.getCreator());
		CommentInfo comment2 = result.getContent().get(1);
		assertNotNull(comment2);
		assertEquals("Hello 1", comment2.getComment());
		assertEquals("sg33g5", comment2.getCreator());
	}
	
	@Test
	public void listPicturesByExistingProfileId() throws IOException {
		userFacade.login("lucien@test.com", "123");
		QueryResult<PictureInfo> result = facade.listPicturesByProfile(UUID.fromString("a95472c0-e0e6-11e2-a28f-0800200c9a77"), 0, 10);
		assertNotNull(result);
		assertEquals(0, result.getPageNumber());
		assertEquals(1, result.getPageCount());
		assertNotNull(result.getContent());
		assertEquals(1, result.getContent().size());
	}
	
	@Test
	public void listPicturesByNonExistingProfileId() throws IOException {
		userFacade.login("lucien@test.com", "123");
		QueryResult<PictureInfo> result = facade.listPicturesByProfile(UUID.fromString("ddd166ae-9b3f-4405-be0d-fa1567728593"), 0, 10);
		assertNotNull(result);
		assertEquals(0, result.getPageNumber());
		assertEquals(0, result.getPageCount());
		assertNotNull(result.getContent());
		assertEquals(0, result.getContent().size());
	}
	
	@Test
	public void findSuggestion() throws IOException {
		userFacade.login("lucien@test.com", "123");
		List<String> suggestions = facade.findKeywordSuggestions("para");
		assertEquals(Collections.singletonList("paragliding"), suggestions);
	}
	
	@Test
	public void createEmptyAlbum() throws IOException {
		userFacade.login("lucien@test.com", "123");
		DateTime now = DateTime.now(DateTimeZone.UTC);
		AlbumInfo info = facade.createEmptyAlbum("Test", "My description");
		assertNotNull(info);
		assertEquals("Test", info.getTitle());
		assertEquals("My description", info.getDescription());
		assertTrue(now.isBefore(info.getCreation()));
		assertFalse(info.getCreation().isAfter(info.getLastUpdate()));
		assertNotNull(info.getMediaList());
		assertTrue(info.getMediaList().isEmpty());
	}
	
	@Test
	public void updateExistingAlbum() throws IOException {
		userFacade.login("lucien@test.com", "123");
		UUID albumId = UUID.fromString("a95472c0-aae6-11e2-a28f-0800200c9a22");
		DateTime now = DateTime.now(DateTimeZone.UTC);
		List<URI> mediaList = Collections.singletonList(URI.create("20130716/f317f3c7918c83ff6ec24aabb6c017fd.jpg"));
		AlbumInfo info = facade.updateAlbum(albumId, "New Title", "a description", mediaList);
		assertNotNull(info);
		assertEquals("New Title", info.getTitle());
		assertEquals("a description", info.getDescription());
		assertTrue(now.isAfter(info.getCreation()));
		assertFalse(info.getCreation().isAfter(info.getLastUpdate()));
		assertEquals(mediaList, info.getMediaList());
	}

	@Test
	public void getAllAlbums() throws IOException {
		userFacade.login("unverified@test.com", "123");
		UUID profileId = UUID.fromString("a95472c0-e0e6-11e2-a28f-0800200c9a77");
		QueryResult<AlbumInfo> result = facade.getAlbums(profileId, 0, 100);
		assertNotNull(result);
		assertEquals(0, result.getPageNumber());
		assertEquals(1, result.getPageCount());
		assertEquals(1, result.getContent().size());
	}
	
	@Test
	public void deleteExistingAlbum() throws IOException {
		userFacade.login("lucien@test.com", "123");
		UUID albumId = UUID.fromString("a95472c0-aae6-11e2-a28f-0800200c9a22");
		facade.deleteAlbum(albumId);
	}
	
	@Test
	public void readTopSearchedTag() throws IOException {
		userFacade.login("lucien@test.com", "123");
		List<TagInfo> result = facade.getTopSearchedTags(1);
		assertEquals(1, result.size());
		assertEquals("sport", result.get(0).getTag());
	}
	
	@Test
	public void readTopUsedTag() throws IOException {
		userFacade.login("lucien@test.com", "123");
		List<TagInfo> result = facade.getTopUsedTags(1);
		assertEquals(1, result.size());
		assertEquals("mountain", result.get(0).getTag());
	}
	
	@Test
	public void mapPictureMatchCountWithKeyword() throws IOException {
		userFacade.login("lucien@test.com", "123");
		List<GeoStatistic> result = facade.mapPictureMatchCount(46.95, 7.5, 100.0, "paragliding", 3650 * DateUtils.MILLIS_PER_DAY, null);
		assertNotNull(result);
		assertEquals(1, result.size());
		GeoStatistic stat = result.iterator().next();
		assertEquals(1, stat.getCount());
		assertEquals(46.7, stat.getLatitude(), 0.2);
		assertEquals(7.8, stat.getLongitude(), 0.2);
		assertEquals(2.4, stat.getRadius(), 0.1);
	}
	
	@Test
	public void mapPictureMatchCountWithoutKeyword() throws IOException {
		userFacade.login("lucien@test.com", "123");
		List<GeoStatistic> result = facade.mapPictureMatchCount(46.95, 7.5, 100.0, null, 3650 * DateUtils.MILLIS_PER_DAY, null);
		assertNotNull(result);
		assertEquals(1, result.size());
		GeoStatistic stat = result.iterator().next();
		assertEquals(1, stat.getCount());
		assertEquals(46.7, stat.getLatitude(), 0.2);
		assertEquals(7.8, stat.getLongitude(), 0.2);
		assertEquals(2.4, stat.getRadius(), 0.1);
	}
	
	@Test
	public void repostExistingComment() throws IOException {
		userFacade.login("lucien@test.com", "123");
		ActivityInfo info = facade.repostComment(UUID.fromString("c95472c0-e0e6-11e2-a28f-0800200c9a33"));
		assertNotNull(info);
		assertEquals(ActivityType.REPOST_COMMENT, info.getActivityType());
		assertEquals("sg33g5", info.getCreator());
		assertEquals("Hello 2", info.getMessage());
	}
	
	@Test
	public void repostExistingCommentTwice() throws IOException {
		userFacade.login("lucien@test.com", "123");
		ActivityInfo info = facade.repostComment(UUID.fromString("b95472c0-e0e6-11e2-a28f-0800200c9a22"));
		assertNotNull(info);
		assertEquals(ActivityType.REPOST_COMMENT, info.getActivityType());
		assertEquals("sg33g5", info.getCreator());
		assertEquals("Hello 1", info.getMessage());
		ActivityInfo info2 = facade.repostComment(UUID.fromString("b95472c0-e0e6-11e2-a28f-0800200c9a22"));
		assertNull(info2);
	}
	
	@Test(expected=DataMissingException.class)
	public void repostNonExistingComment() throws IOException {
		userFacade.login("lucien@test.com", "123");
		facade.repostComment(UUID.fromString("095472c0-e0e6-11e2-a28f-0800200c9a33"));
	}
	
	@Test
	public void repostExistingPicture() throws IOException {
		userFacade.login("lucien@test.com", "123");
		ActivityInfo info = facade.repostPicture(URI.create("20130712/7e9a5a5bd5e64171c176ac6c7b32d685.jpg"));
		assertNotNull(info);
		assertEquals(ActivityType.REPOST_PICTURE, info.getActivityType());
		assertEquals("sg33g5", info.getCreator());
		assertNull(info.getMessage());
	}
	
	@Test
	public void repostExistingPictureTwice() throws IOException {
		userFacade.login("lucien@test.com", "123");
		ActivityInfo info = facade.repostPicture(URI.create("20130716/f317f3c7918c83ff6ec24aabb6c017fd.jpg"));
		assertNotNull(info);
		assertEquals(ActivityType.REPOST_PICTURE, info.getActivityType());
		assertEquals("sg33g5", info.getCreator());
		assertNull(info.getMessage());
		ActivityInfo info2 = facade.repostPicture(URI.create("20130716/f317f3c7918c83ff6ec24aabb6c017fd.jpg"));
		assertNull(info2);
	}
	
	@Test(expected=DataMissingException.class)
	public void repostNonExistingPicture() throws IOException {
		userFacade.login("lucien@test.com", "123");
		facade.repostPicture(URI.create("20130712/99.jpg"));
	}
	
	@Test
	public void searchPictures() throws IOException {
		userFacade.login("lucien@test.com", "123");
		QueryResult<PictureInfo> result = facade.searchPictures(null, null, null, "sport", 3650 * DateUtils.MILLIS_PER_DAY, 0, 10);
		assertNotNull(result);
		assertEquals(0, result.getPageNumber());
		assertEquals(1, result.getPageCount());
		assertNotNull(result.getContent());
		assertEquals(1, result.getContent().size());
	}
	
	@Test
	public void searchPicturesInCategory() throws IOException {
		userFacade.login("lucien@test.com", "123");
		QueryResult<PictureInfo> result = facade.searchPicturesInCategory(null, null, null, "sport", 3650 * DateUtils.MILLIS_PER_DAY, "PLACES", 0, 10);
		assertNotNull(result);
		assertEquals(0, result.getPageNumber());
		assertEquals(1, result.getPageCount());
		assertNotNull(result.getContent());
		assertEquals(1, result.getContent().size());
	}
	
	@Test
	public void searchTopPicturesByCategories() throws IOException {
		userFacade.login("lucien@test.com", "123");
		Map<String, List<PictureInfo>> result = facade.searchTopPicturesByCategories(null, null, null, null, 3650 * DateUtils.MILLIS_PER_DAY, Arrays.asList("ART", "PLACES"), 10);
		assertNotNull(result);
		assertEquals(2, result.get("PLACES").size());
		assertEquals(0, result.get("ART").size());
	}
}
