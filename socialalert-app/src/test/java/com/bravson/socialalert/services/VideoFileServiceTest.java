package com.bravson.socialalert.services;

import java.io.File;
import java.io.IOException;

import javax.annotation.Resource;

import org.apache.commons.io.FileUtils;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Value;

import com.bravson.socialalert.app.domain.VideoMetadata;
import com.bravson.socialalert.app.services.VideoFileService;
import com.bravson.socialalert.infrastructure.SimpleServiceTest;

@Ignore
public class VideoFileServiceTest extends SimpleServiceTest {

	@Resource
	private VideoFileService service;
	
	@Value("${media.temp.dir}")
	private File tempDir;
	
	@Before
	public void cleanTempDir() throws IOException {
		FileUtils.cleanDirectory(tempDir);
	}
	
	@Test
	public void testThumbnail() throws IOException {
		File file = service.createThumbnail(new File("src/test/resources/media/IMG_0236.MOV"));
		assertNotNull(file);
		assertEquals(new File("src/test/resources/media/thumb-IMG_0236.jpg"), file);
	}
	
	@Test(expected=IOException.class)
	public void testInvalidVideo() throws IOException {
		service.createPreview(new File("src/test/resources/media/invalid.jpg"));
	}
	
	@Test
	public void parseMetaData() throws IOException, InterruptedException {
		VideoMetadata metadata = service.parseMetadata(new File("src/test/resources/media/IMG_0236.MOV"));
		assertNotNull(metadata);
		assertEquals(Integer.valueOf(320), metadata.getHeight());
		assertEquals(Integer.valueOf(568), metadata.getWidth());
		assertEquals(Duration.standardSeconds(23), metadata.getDuration());
		assertEquals("Apple", metadata.getCameraMaker());
		assertEquals("iPhone 6", metadata.getCameraModel());
		assertEquals(-1.9949, metadata.getLongitude(), 0.0001);
		assertEquals(43.3222, metadata.getLatitude(), 0.0001);
		assertEquals(new DateTime(2015, 1, 7, 21, 13, 32), metadata.getTimestamp());
	}
	
	@Test
	public void testPreview() throws IOException {
		File file = service.createPreview(new File("src/test/resources/media/IMG_0236.MOV"));
		assertEquals(new File("src/test/resources/media/preview-IMG_0236.avi"), file);
	}
}

