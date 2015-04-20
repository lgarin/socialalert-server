package com.bravson.socialalert.app.services;

import io.humble.video.Decoder;
import io.humble.video.Demuxer;
import io.humble.video.DemuxerStream;
import io.humble.video.Global;
import io.humble.video.KeyValueBag;
import io.humble.video.MediaDescriptor;
import io.humble.video.MediaPacket;
import io.humble.video.MediaPicture;
import io.humble.video.awt.MediaPictureConverter;
import io.humble.video.awt.MediaPictureConverterFactory;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.bravson.socialalert.app.domain.VideoMetadata;

@Service
public class VideoFileServiceImpl implements VideoFileService {

	private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
	private static final Pattern LOCATION_PATTERN = Pattern.compile("([+-]\\d+.\\d+)([+-]\\d+.\\d+)([+-]\\d+.\\d+)/");
	
	@Value("${video.snapshot.delay}")
	private long snapshotDelay;
	
	@Autowired
	private PictureFileService pictureService;
	
	private MediaPicture buildPicture(Demuxer demuxer, DemuxerStream stream) throws InterruptedException, IOException {
		Decoder decoder = stream.getDecoder();
		decoder.open(null, null);
		MediaPicture picture = MediaPicture.make(decoder.getWidth(), decoder.getHeight(), decoder.getPixelFormat());
		MediaPacket packet = MediaPacket.make();
		while (demuxer.read(packet) >= 0) {
			if (packet.getStreamIndex() == stream.getIndex()) {
				int offset = 0;
				int bytesRead = 0;
				do {
					bytesRead += decoder.decodeVideo(picture, packet, offset);
					if (picture.isComplete()) {
						return picture;
					}
					offset += bytesRead;
				} while (offset < packet.getSize());
			}
		}

		do {
			decoder.decodeVideo(picture, null, 0);
			if (picture.isComplete()) {
				return picture;
			}
		} while (picture.isComplete());

		return null;
	}

	private File createSnapshot(File sourceFile) throws IOException {
		if (!sourceFile.canRead()) {
			throw new IOException("Cannot read file " + sourceFile);
		}
		final File outputFile = new File(sourceFile.getParent(), FilenameUtils.getBaseName(sourceFile.getName()) + ".jpg");
		if (outputFile.canRead()) {
			return outputFile;
		}

		Demuxer demuxer = Demuxer.make();
		try {
			try {
				demuxer.open(sourceFile.toString(), null, false, true, null, null);
			} catch (InterruptedException e) {
				throw new IOException("Cannot open file " + sourceFile, e);
			}

			DemuxerStream stream = findVideoStream(demuxer);
			MediaPicture picture = buildPicture(demuxer, stream);
			MediaPictureConverter converter = MediaPictureConverterFactory.createConverter(
					MediaPictureConverterFactory.HUMBLE_BGR_24, picture);
			BufferedImage image = converter.toImage(null, picture);
			ImageIO.write(image, "jpg", outputFile);
			return outputFile;
		} catch (InterruptedException e) {
			throw new IOException(e);
		} finally {
			try {
				demuxer.close();
			} catch (InterruptedException e) {

			}
		}
	}
	
	@Override
	public File createPreview(File sourceFile) throws IOException {
		File snapshot = createSnapshot(sourceFile);
		return pictureService.createJpegPreview(snapshot);
	}
	
	@Override
	public File createThumbnail(File sourceFile) throws IOException {
		File snapshot = createSnapshot(sourceFile);
		return pictureService.createJpegThumbnail(snapshot);
	}
	
	private DemuxerStream findVideoStream(Demuxer demuxer) throws IOException {
		try {
			int ns = demuxer.getNumStreams();
			for (int i = 0; i < ns; i++) {
				DemuxerStream stream = demuxer.getStream(i);
				Decoder decoder = stream.getDecoder();
				if (decoder != null && decoder.getCodecType() == MediaDescriptor.Type.MEDIA_VIDEO) {
					return stream;
				}
			}
			return null;
		} catch (InterruptedException e) {
			throw new IOException("Cannot find video stream in " + demuxer.getURL(), e);
		}
	}
	
	@Override
	public VideoMetadata parseMetadata(File sourceFile) throws IOException {
		VideoMetadata result = new VideoMetadata();
		Demuxer demuxer = Demuxer.make();
		try {
			try {
				demuxer.open(sourceFile.toString(), null, false, true, null, null);
			} catch (InterruptedException e) {
				throw new IOException("Cannot open file " + sourceFile, e);
			}
			
		    KeyValueBag metadata = demuxer.getMetaData();
		    result.setCameraModel(metadata.getValue("model"));
		    result.setCameraMaker(metadata.getValue("make"));
		    if (metadata.getValue("creation_time") != null) {
		    	result.setTimestamp(DateTime.parse(metadata.getValue("creation_time"), TIMESTAMP_FORMAT));
		    } else if (metadata.getValue("date") != null) {
		    	result.setTimestamp(DateTime.parse(metadata.getValue("date"), TIMESTAMP_FORMAT));
		    }
		    Matcher locationMatcher = LOCATION_PATTERN.matcher(StringUtils.defaultString(metadata.getValue("location")));
		    if (locationMatcher.matches()) {
		    	result.setDefaultLatitude(Double.parseDouble((locationMatcher.group(1))));
		    	result.setDefaultLongitude(Double.parseDouble((locationMatcher.group(2))));
		    }
		    
		    result.setDuration(Duration.standardSeconds(demuxer.getDuration() / Global.DEFAULT_PTS_PER_SECOND));;
		    
		    DemuxerStream stream = findVideoStream(demuxer);
		    if (stream != null) {
		    	Decoder decoder = stream.getDecoder();
		    	result.setHeight(decoder.getHeight());
	        	result.setWidth(decoder.getWidth());
		    }
		} finally {
			try {
				demuxer.close();
			} catch (InterruptedException e) {
				
			}
		}
		
		return result;
	}

}
