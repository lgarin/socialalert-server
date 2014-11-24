package com.bravson.socialalert.services;

import java.io.File;
import java.io.IOException;

import javax.annotation.Resource;

import org.apache.commons.io.FileUtils;
import org.joda.time.Duration;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Value;

import com.bravson.socialalert.app.domain.VideoMetadata;
import com.bravson.socialalert.app.services.VideoFileService;
import com.bravson.socialalert.infrastructure.SimpleServiceTest;

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
	public void testInit() {
		
	}
	
	@Test
	public void testThumbnail() throws IOException {
		service.createJpegImage(new File("src/test/resources/media/sample_mpeg4.mp4"));
	}
	
	@Test(expected=IOException.class)
	public void testInvalidVideo() throws IOException {
		service.createJpegImage(new File("src/test/resources/media/invalid.jpg"));
	}
	
	@Test
	public void parseMetaData() throws IOException {
		VideoMetadata metadata = service.parseMp4Metadata(new File("src/test/resources/media/sample_mpeg4.mp4"));
		assertNotNull(metadata);
		assertEquals(Integer.valueOf(240), metadata.getHeight());
		assertEquals(Integer.valueOf(190), metadata.getWidth());
		assertEquals(Duration.standardSeconds(4), metadata.getDuration());
		
	}
}

