package com.bravson.socialalert.services;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.annotation.Resource;

import net.coobird.thumbnailator.tasks.UnsupportedFormatException;

import org.apache.commons.io.FileUtils;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Value;

import com.bravson.socialalert.app.domain.PictureMetadata;
import com.bravson.socialalert.app.services.PictureFileService;
import com.bravson.socialalert.infrastructure.SimpleServiceTest;
import com.drew.imaging.jpeg.JpegProcessingException;

public class PictureFileServiceTest extends SimpleServiceTest {

	@Resource
	private PictureFileService service;
	
	@Value("${media.temp.dir}")
	private File tempDir;
	
	@Before
	public void cleanTempDir() throws IOException {
		FileUtils.cleanDirectory(tempDir);
	}
	
	@Test
	public void createThumbnailWithJpegFile() throws IOException {
		service.createJpegThumbnail(new File("src/test/resources/media/IMG_0397.JPG"));
	}
	
	@Test(expected=UnsupportedFormatException.class)
	public void createThumbnailWithInvalidFile() throws IOException {
		service.createJpegThumbnail(new File("src/test/resources/media/invalid.jpg"));
	}
	
	@Test(expected=FileNotFoundException.class)
	public void createThumbnailWithNonExistingFile() throws IOException {
		service.createJpegThumbnail(new File("src/test/resources/media/xxx.jpg"));
	}
	
	@Test
	public void createPreviewWithJpegFile() throws IOException {
		service.createJpegPreview(new File("src/test/resources/media/IMG_0397.JPG"));
	}
	
	@Test(expected=UnsupportedFormatException.class)
	public void createPreviewWithInvalidFile() throws IOException {
		service.createJpegPreview(new File("src/test/resources/media/invalid.jpg"));
	}
	
	@Test(expected=FileNotFoundException.class)
	public void createPreviewWithNonExistingFile() throws IOException {
		service.createJpegPreview(new File("src/test/resources/media/xxx.jpg"));
	}
	
	@Test
	public void parseMetadataWithAppleFile() throws IOException, JpegProcessingException {
		PictureMetadata metadata = service.parseJpegMetadata(new File("src/test/resources/media/IMG_0397.JPG"));
		assertNotNull(metadata);
		assertEquals(Integer.valueOf(2448), metadata.getWidth());
		assertEquals(Integer.valueOf(3264), metadata.getHeight());
		assertEquals(new DateTime(2013, 4, 14, 16, 28, 26), metadata.getTimestamp());
		assertEquals("Apple", metadata.getCameraMaker());
		assertEquals("iPhone 5", metadata.getCameraModel());
		assertEquals(46.68666666666667, metadata.getLatitude(), 0.0);
		assertEquals(7.858833333333333, metadata.getLongitude(), 0.0);
	}
	
	@Test
	public void parseMetadataWithThumbnailFile() throws IOException, JpegProcessingException {
		service.createJpegThumbnail(new File("src/test/resources/media/IMG_0397.JPG"));
		PictureMetadata metadata = service.parseJpegMetadata(new File("src/test/resources/media/thumb-IMG_0397.JPG"));
		assertNotNull(metadata);
		assertEquals(Integer.valueOf(320), metadata.getWidth());
		assertEquals(Integer.valueOf(240), metadata.getHeight());
		assertNull(metadata.getTimestamp());
		assertNull(metadata.getCameraMaker());
		assertNull(metadata.getCameraModel());
		assertNull(metadata.getLatitude());
		assertNull(metadata.getLongitude());
	}
	
	@Test(expected=JpegProcessingException.class)
	public void parseMetadataWithInvalidFile() throws IOException, JpegProcessingException {
		service.parseJpegMetadata(new File("src/test/resources/media/invalid.jpg"));
	}
	
	@Test(expected=FileNotFoundException.class)
	public void parseMetadataWithNonExistingFile() throws IOException, JpegProcessingException {
		service.parseJpegMetadata(new File("src/test/resources/media/xxx.jpg"));
	}
}
