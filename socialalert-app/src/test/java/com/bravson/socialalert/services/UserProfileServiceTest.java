package com.bravson.socialalert.services;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Resource;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.HttpClientErrorException;

import com.bravson.socialalert.app.domain.ExternalProfileInfo;
import com.bravson.socialalert.app.domain.PictureMetadata;
import com.bravson.socialalert.app.entities.UserProfile;
import com.bravson.socialalert.app.exceptions.DataMissingException;
import com.bravson.socialalert.app.services.MediaStorageService;
import com.bravson.socialalert.app.services.UserProfileService;
import com.bravson.socialalert.common.domain.Gender;
import com.bravson.socialalert.common.domain.ProfileInfo;
import com.bravson.socialalert.common.domain.PublicProfileInfo;
import com.bravson.socialalert.common.domain.QueryResult;
import com.bravson.socialalert.infrastructure.DataServiceTest;

public class UserProfileServiceTest extends DataServiceTest {

	@Resource
	private UserProfileService service;
	
	@Resource
	private MediaStorageService storage;
	
	@Value("${media.temp.dir}")
	private File tempDir;
	
	@Value("${media.base.dir}")
	private File baseDir;

	@Before
	public void setUp() throws Exception {
		FileUtils.cleanDirectory(tempDir);
		FileUtils.cleanDirectory(baseDir);
		fullImport(UserProfile.class);
		authenticate("test", "USER");
	}
	
	@Test
	public void createEmptyProfile() throws URISyntaxException {
		UserProfile profile = service.createEmptyProfile("testNickname");
		assertNotNull(profile);
		assertEquals("testNickname", profile.getNickname());
		assertNotNull(profile.getId());
		assertNull(profile.getFirstname());
		assertNull(profile.getLastname());
		assertNull(profile.getImage());
		assertNull(profile.getCountry());
		assertNull(profile.getLanguage());
		assertNull(profile.getBirthdate());
	}
	
	@Test
	public void getExistingProfile() {
		UUID uuid = UUID.fromString("a95472c0-e0e6-11e2-a28f-0800200c9a77");
		UserProfile profile = service.getProfileById(uuid);
		assertNotNull(profile);
		assertEquals(uuid, profile.getId());
		assertEquals("Lucien", profile.getFirstname());
		assertEquals("Garin", profile.getLastname());
		assertNull(profile.getLanguage());
		assertNull(profile.getCountry());
		assertNull(profile.getGender());
		assertNull(profile.getBirthdate());
		assertNull(profile.getImage());
	}
	
	@Test(expected=DataMissingException.class)
	public void getNonExistingProfile() {
		UUID uuid = UUID.fromString("12345678-e0e6-11e2-a28f-0800200c9a77");
		service.getProfileById(uuid);
	}
	
	@Test
	public void getProfileMap() {
		UUID existingUuid = UUID.fromString("a95472c0-e0e6-11e2-a28f-0800200c9a77");
		UUID randomUuid = UUID.fromString("12345678-e0e6-11e2-a28f-0800200c9a77");
		Map<UUID, UserProfile> result = service.getProfileMap(Arrays.asList(existingUuid, randomUuid));
		assertNotNull(result);
		assertEquals(1, result.size());
		assertTrue(result.containsKey(existingUuid));
		UserProfile profile = result.get(existingUuid);
		assertEquals("Lucien", profile.getFirstname());
		assertEquals("Garin", profile.getLastname());
	}
	
	@Test
	public void completeExistingProfile() {
		UUID uuid = UUID.fromString("a95472c0-e0e6-11e2-a28f-0800200c9a77");
		ExternalProfileInfo info = new ExternalProfileInfo();
		info.setBirthdate(new LocalDate(1988, 2, 3));
		info.setGender(Gender.MALE);
		info.setLanguage("EN");
		info.setCountry("Switzerland");
		info.setFirstname("Mister");
		info.setLastname("Mike");
		UserProfile profile = service.completeProfile(uuid, info);
		assertNotNull(profile);
		assertEquals("Lucien", profile.getFirstname());
		assertEquals("Garin", profile.getLastname());
		assertEquals("Switzerland", profile.getCountry());
		assertEquals(Gender.MALE, profile.getGender());
		assertEquals("EN", profile.getLanguage());
		assertNull(profile.getImage());
	}
	
	@Test(expected=DataMissingException.class)
	public void completeNonExistingProfile() {
		UUID uuid = UUID.fromString("12345678-e0e6-11e2-a28f-0800200c9a77");
		ExternalProfileInfo info = new ExternalProfileInfo();
		info.setBirthdate(new LocalDate(1988, 2, 3));
		info.setGender(Gender.MALE);
		info.setLanguage("EN");
		info.setCountry("Switzerland");
		info.setFirstname("Mister");
		info.setLastname("Mike");
		service.completeProfile(uuid, info);
	}
	
	@Test
	public void updateExistingProfile() {
		UUID uuid = UUID.fromString("a95472c0-e0e6-11e2-a28f-0800200c9a77");
		ProfileInfo info = new ProfileInfo();
		info.setBirthdate(null);
		info.setGender(Gender.MALE);
		info.setLanguage("EN");
		info.setCountry("Switzerland");
		info.setFirstname("Mister");
		info.setLastname("Mike");
		UserProfile profile = service.updateProfile(uuid, info);
		assertNotNull(profile);
		assertEquals("Mister", profile.getFirstname());
		assertEquals("Mike", profile.getLastname());
		assertEquals("Switzerland", profile.getCountry());
		assertEquals(Gender.MALE, profile.getGender());
		assertEquals("EN", profile.getLanguage());
		assertNull(profile.getBirthdate());
		assertNull(profile.getImage());
	}
	
	@Test(expected=DataMissingException.class)
	public void updateNonExistingProfile() {
		UUID uuid = UUID.fromString("12345678-e0e6-11e2-a28f-0800200c9a77");
		ProfileInfo info = new ProfileInfo();
		info.setBirthdate(new LocalDate(1988, 2, 3));
		info.setGender(Gender.MALE);
		info.setLanguage("EN");
		info.setCountry("Switzerland");
		info.setFirstname("Mister");
		info.setLastname("Mike");
		service.updateProfile(uuid, info);
	}
	
	@Test
	public void downloadValidProfilePicture() throws IOException, URISyntaxException {
		UUID uuid = UUID.fromString("a95472c0-e0e6-11e2-a28f-0800200c9a77");
		URL pictureUrl = new URL("http://www.w3.org/MarkUp/Test/xhtml-print/20050519/tests/jpeg420exif.jpg");
		UserProfile profile = service.downloadProfilePicture(uuid, pictureUrl);
		assertNotNull(profile);
		assertEquals(uuid, profile.getId());
		assertEquals("Lucien", profile.getFirstname());
		assertEquals("Garin", profile.getLastname());
		assertNull(profile.getLanguage());
		assertNull(profile.getCountry());
		assertNull(profile.getGender());
		assertNull(profile.getBirthdate());
		URI finalUri = URI.create("profiles/a95472c0-e0e6-11e2-a28f-0800200c9a77.jpg");
		assertEquals(finalUri, profile.getImage());
	}
	
	@Test(expected=HttpClientErrorException.class)
	public void downloadInvalidProfilePicture() throws IOException {
		UUID uuid = UUID.fromString("a95472c0-e0e6-11e2-a28f-0800200c9a77");
		URL pictureUrl = new URL("http://www.eclipse.org/eclipse.org-common/themes/Nova/images/eclipse.png");
		service.downloadProfilePicture(uuid, pictureUrl);
	}

	@Test(expected=DataMissingException.class)
	public void downloadProfilePictureWithNonExistingProfile() throws IOException {
		UUID uuid = UUID.fromString("12345678-e0e6-11e2-a28f-0800200c9a77");
		URL pictureUrl = new URL("http://www.eclipse.org/eclipse.org-common/themes/Nova/images/eclipse.png");
		service.downloadProfilePicture(uuid, pictureUrl);
	}
	
	@Test
	public void claimExistingProfilePicture() throws IOException, URISyntaxException {
		UUID uuid = UUID.fromString("a95472c0-e0e6-11e2-a28f-0800200c9a77");
		File file = new File("src/test/resources/media/IMG_0397.JPG");
		Pair<URI, PictureMetadata> pair = storage.storePicture(FileUtils.openInputStream(file), (int) file.length());
		UserProfile profile = service.claimProfilePicture(uuid, pair.getKey());
		assertNotNull(profile);
		URI finalUri = URI.create("profiles/a95472c0-e0e6-11e2-a28f-0800200c9a77.jpg");
		assertEquals(finalUri, profile.getImage());
		File archivedFile = storage.resolveMediaUri(finalUri);
		assertNotNull(archivedFile);
		assertTrue(archivedFile.exists());
	}
	
	@Test(expected=DataMissingException.class)
	public void claimExistingProfilePictureTwice() throws IOException, URISyntaxException {
		UUID uuid = UUID.fromString("a95472c0-e0e6-11e2-a28f-0800200c9a77");
		File file = new File("src/test/resources/media/IMG_0397.JPG");
		Pair<URI, PictureMetadata> pair = storage.storePicture(FileUtils.openInputStream(file), (int) file.length());
		URI uri = pair.getKey();
		UserProfile profile = service.claimProfilePicture(uuid, uri);
		assertNotNull(profile);
		URI finalUri = URI.create("profiles/a95472c0-e0e6-11e2-a28f-0800200c9a77.jpg");
		assertEquals(finalUri, profile.getImage());
		service.claimProfilePicture(uuid, uri);
	}
	
	@Test
	public void changeProfilePictureTwice() throws IOException, URISyntaxException {
		UUID uuid = UUID.fromString("a95472c0-e0e6-11e2-a28f-0800200c9a77");
		File file = new File("src/test/resources/media/IMG_0397.JPG");
		Pair<URI, PictureMetadata> pair = storage.storePicture(FileUtils.openInputStream(file), (int) file.length());
		URI uri = pair.getKey();
		UserProfile profile = service.claimProfilePicture(uuid, uri);
		assertNotNull(profile);
		URI finalUri = URI.create("profiles/a95472c0-e0e6-11e2-a28f-0800200c9a77.jpg");
		assertEquals(finalUri, profile.getImage());
		
		Pair<URI, PictureMetadata> pair2 = storage.storePicture(FileUtils.openInputStream(file), (int) file.length());
		URI uri2 = pair2.getKey();
		UserProfile profile2 = service.claimProfilePicture(uuid, uri2);
		assertNotNull(profile2);
		assertEquals(finalUri, profile2.getImage());
	}
	
	@Test(expected=DataMissingException.class)
	public void claimNonExistingProfilePicture() throws IOException, URISyntaxException {
		UUID uuid = UUID.fromString("a95472c0-e0e6-11e2-a28f-0800200c9a77");
		URI uri = new URI("7e9a5a5bd5e64171c176ac6c7b32d685.jpg");
		service.claimProfilePicture(uuid, uri);
	}
	
	@Test
	public void searchProfileWithNickname() {
		QueryResult<PublicProfileInfo> result = service.searchProfiles("sg33", 0, 10);
		assertNotNull(result);
		assertEquals(1, result.getPageCount());
		assertEquals(0, result.getPageNumber());
		assertNotNull(result.getContent());
		assertEquals(1, result.getContent().size());
		PublicProfileInfo profile = result.getContent().get(0);
		assertNotNull(profile);
		assertEquals("sg33g5", profile.getNickname());
	}
	
	@Test
	public void findNicknameSuggestion() {
		List<String> result = service.findNicknameSuggestions("sg33");
		assertEquals(Arrays.asList("sg33g5"), result);
	}
}
