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
import com.xuggle.xuggler.Global;
import com.xuggle.xuggler.ICodec;
import com.xuggle.xuggler.IContainer;
import com.xuggle.xuggler.IStream;
import com.xuggle.xuggler.IStreamCoder;

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
	public void testSnapshot() throws IOException {
		File file = service.createPreview(new File("src/test/resources/media/msmpeg4.avi"));
		assertNotNull(file);
		assertEquals(new File("src/test/resources/media/preview-msmpeg4.jpg"), file);
	}
	
	@Test
	public void testThumbnail() throws IOException {
		File file = service.createThumbnail(new File("src/test/resources/media/msmpeg4.avi"));
		assertNotNull(file);
		assertEquals(new File("src/test/resources/media/thumb-msmpeg4.jpg"), file);
	}
	
	@Test(expected=RuntimeException.class)
	public void testInvalidVideo() throws IOException {
		service.createPreview(new File("src/test/resources/media/invalid.jpg"));
	}
	
	@Test
	public void parseMetaData() throws IOException {
		VideoMetadata metadata = service.parseMetadata(new File("src/test/resources/media/msmpeg4.avi"));
		assertNotNull(metadata);
		assertEquals(Integer.valueOf(380), metadata.getHeight());
		assertEquals(Integer.valueOf(440), metadata.getWidth());
		assertEquals(Duration.millis(18199), metadata.getDuration());
		
	}
	
	@Test
	public void readMetaData() throws Exception {
		String filename = "C:/Dev/socialalert-server/socialalert-app/src/test/resources/media/msmpeg4.avi";
		//String filename ="C:/Dev/Edge_Scroll.avi";
		IContainer container = IContainer.make();
		// Open up the container
		if (container.open(filename, IContainer.Type.READ, null) < 0)
			throw new IllegalArgumentException("could not open file: " + filename);
		// query how many streams the call to open found
		int numStreams = container.getNumStreams();
		System.out.printf("file \"%s\": %d stream%s; ", filename, numStreams, numStreams == 1 ? "" : "s");
		System.out.printf("duration (ms): %s; ",
				container.getDuration() == Global.NO_PTS ? "unknown" : "" + container.getDuration() / 1000);
		System.out.printf("start time (ms): %s; ", container.getStartTime() == Global.NO_PTS ? "unknown" : ""
				+ container.getStartTime() / 1000);
		System.out.printf("file size (bytes): %d; ", container.getFileSize());
		System.out.printf("bit rate: %d; ", container.getBitRate());
		System.out.printf("\n");
		// and iterate through the streams to print their meta data
		for (int i = 0; i < numStreams; i++) {
			// Find the stream object
			IStream stream = container.getStream(i);
			// Get the pre-configured decoder that can decode this stream;
			IStreamCoder coder = stream.getStreamCoder();
			// and now print out the meta data.
			System.out.printf("stream %d: ", i);
			System.out.printf("type: %s; ", coder.getCodecType());
			System.out.printf("codec: %s; ", coder.getCodecID());
			System.out.printf("duration: %s; ",
					stream.getDuration() == Global.NO_PTS ? "unknown" : "" + stream.getDuration());
			System.out.printf("start time: %s; ",
					container.getStartTime() == Global.NO_PTS ? "unknown" : "" + stream.getStartTime());
			System.out.printf("language: %s; ", stream.getLanguage() == null ? "unknown" : stream.getLanguage());
			System.out.printf("timebase: %d/%d; ", stream.getTimeBase().getNumerator(), stream.getTimeBase()
					.getDenominator());
			System.out.printf("coder tb: %d/%d; ", coder.getTimeBase().getNumerator(), coder.getTimeBase()
					.getDenominator());
			if (coder.getCodecType() == ICodec.Type.CODEC_TYPE_AUDIO) {
				System.out.printf("sample rate: %d; ", coder.getSampleRate());
				System.out.printf("channels: %d; ", coder.getChannels());
				System.out.printf("format: %s", coder.getSampleFormat());
			} else if (coder.getCodecType() == ICodec.Type.CODEC_TYPE_VIDEO) {
				System.out.printf("width: %d; ", coder.getWidth());
				System.out.printf("height: %d; ", coder.getHeight());
				System.out.printf("format: %s; ", coder.getPixelType());
				System.out.printf("frame-rate: %5.2f; ", coder.getFrameRate().getDouble());
			}
			System.out.printf("\n");
		}
	}
}

