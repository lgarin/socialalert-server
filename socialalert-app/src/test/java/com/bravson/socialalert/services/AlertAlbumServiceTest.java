package com.bravson.socialalert.services;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import javax.annotation.Resource;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Before;
import org.junit.Test;

import com.bravson.socialalert.app.entities.AlertAlbum;
import com.bravson.socialalert.app.services.AlertAlbumService;
import com.bravson.socialalert.common.domain.AlbumInfo;
import com.bravson.socialalert.common.domain.QueryResult;
import com.bravson.socialalert.infrastructure.DataServiceTest;

public class AlertAlbumServiceTest extends DataServiceTest {

	@Resource
	private AlertAlbumService service;

	@Before
	public void setUp() throws Exception {
		fullImport(AlertAlbum.class);
	}

	@Test
	public void createEmptyAlbum() {
		UUID profileId = UUID.fromString("a95472c0-e0e6-11e2-a28f-0800200c9a77");
		DateTime now = DateTime.now(DateTimeZone.UTC);
		AlbumInfo info = service.createEmptyAlbum(profileId, "Test", null);
		assertNotNull(info);
		assertEquals("Test", info.getTitle());
		assertNull(info.getDescription());
		assertFalse(now.isAfter(info.getCreation()));
		assertFalse(info.getCreation().isAfter(info.getLastUpdate()));
		assertNotNull(info.getMediaList());
		assertTrue(info.getMediaList().isEmpty());
	}
	
	@Test
	public void updateExistingAlbum() {
		UUID profileId = UUID.fromString("a95472c0-e0e6-11e2-a28f-0800200c9a77");
		UUID albumId = UUID.fromString("a95472c0-aae6-11e2-a28f-0800200c9a22");
		DateTime now = DateTime.now(DateTimeZone.UTC);
		List<URI> mediaList = Collections.singletonList(URI.create("20130716/f317f3c7918c83ff6ec24aabb6c017fd.jpg"));
		AlbumInfo info = service.updateAlbum(albumId, profileId, "New Title", "a description", mediaList);
		assertNotNull(info);
		assertEquals("New Title", info.getTitle());
		assertEquals("a description", info.getDescription());
		assertTrue(now.isAfter(info.getCreation()));
		assertFalse(info.getCreation().isAfter(info.getLastUpdate()));
		assertEquals(mediaList, info.getMediaList());
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void updateExistingAlbumWithInvalidProfile() {
		UUID profileId = UUID.fromString("b95472c0-e0e6-11e2-a28f-0800200c9a77");
		UUID albumId = UUID.fromString("a95472c0-aae6-11e2-a28f-0800200c9a22");
		List<URI> mediaList = Collections.singletonList(URI.create("20130716/f317f3c7918c83ff6ec24aabb6c017fd.jpg"));
		service.updateAlbum(albumId, profileId, "New Title", "a description", mediaList);
	}
	
	@Test
	public void getAllAlbums() {
		UUID profileId = UUID.fromString("a95472c0-e0e6-11e2-a28f-0800200c9a77");
		QueryResult<AlbumInfo> result = service.getAlbums(profileId, 0, 100);
		assertNotNull(result);
		assertEquals(0, result.getPageNumber());
		assertEquals(1, result.getPageCount());
		assertEquals(1, result.getContent().size());
	}
	
	@Test
	public void deleteExistingAlbum() {
		UUID profileId = UUID.fromString("a95472c0-e0e6-11e2-a28f-0800200c9a77");
		UUID albumId = UUID.fromString("a95472c0-aae6-11e2-a28f-0800200c9a22");
		service.deleteAlbum(albumId, profileId);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void deleteExistingAlbumWithInvalidProfile() {
		UUID profileId = UUID.fromString("b95472c0-e0e6-11e2-a28f-0800200c9a77");
		UUID albumId = UUID.fromString("a95472c0-aae6-11e2-a28f-0800200c9a22");
		service.deleteAlbum(albumId, profileId);
	}
}
