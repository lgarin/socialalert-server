package com.bravson.socialalert.app.services;

import io.humble.video.Decoder;
import io.humble.video.Demuxer;
import io.humble.video.DemuxerFormat;
import io.humble.video.DemuxerStream;
import io.humble.video.KeyValueBag;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.coobird.thumbnailator.Thumbnails;

import org.apache.commons.io.FilenameUtils;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.bravson.socialalert.app.domain.VideoMetadata;

@Service
public class VideoFileServiceImpl implements VideoFileService {

	private static final Pattern LOCATION_PATTERN = Pattern.compile("([+-]\\d+.\\d+)([+-]\\d+.\\d+)([+-]\\d+.\\d+)/");
	
	@Value("${picture.thumbnail.prefix}")
	private String thumbnailPrefix;
	
	@Value("${picture.thumbnail.height}")
	private int thumbnailHeight;
	
	@Value("${picture.thumbnail.width}")
	private int thumbnailWidth;
	
	@Value("${picture.preview.prefix}")
	private String previewPrefix;
	
	@Value("${picture.preview.height}")
	private int previewHeight;
	
	@Value("${picture.preview.width}")
	private int previewWidth;
	
	@Value("${video.snapshot.delay}")
	private long snapshotDelay;

	private File createSnapshot(File sourceFile, String prefix, final int height, final int width) throws IOException {
		if (!sourceFile.canRead()) {
			throw new IOException("Cannot read file " + sourceFile);
		}
		 final File outputFile = new File(sourceFile.getParent(), prefix + FilenameUtils.getBaseName(sourceFile.getName()) + ".jpg");
         
		 return null;
	}
	
	@Override
	public File createPreview(File sourceFile) throws IOException {
		return createSnapshot(sourceFile, previewPrefix, previewHeight, previewWidth);
	}
	
	@Override
	public File createThumbnail(File sourceFile) throws IOException {
		return createSnapshot(sourceFile, thumbnailPrefix, thumbnailHeight, thumbnailWidth);
	}
	
	@Override
	public VideoMetadata parseMetadata(File sourceFile) throws IOException, InterruptedException {
		VideoMetadata result = new VideoMetadata();
		Demuxer demuxer = Demuxer.make();
		try {
			demuxer.open(sourceFile.toString(), null, false, true, null, null);
			
			DemuxerFormat format = demuxer.getFormat();
		    
		    KeyValueBag metadata = demuxer.getMetaData();
		    result.setCameraModel(metadata.getValue("model"));
		    result.setCameraMaker(metadata.getValue("make"));
		    if (metadata.getValue("creation_time") != null) {
		    	result.setTimestamp(DateTime.parse(metadata.getValue("creation_time")));
		    } else if (metadata.getValue("date") != null) {
		    	result.setTimestamp(DateTime.parse(metadata.getValue("date")));
		    }
		    Matcher locationMatcher = LOCATION_PATTERN.matcher(metadata.getValue("location"));
		    if (locationMatcher.matches()) {
		    	result.setDefaultLatitude(Double.parseDouble((locationMatcher.group(1))));
		    	result.setDefaultLongitude(Double.parseDouble((locationMatcher.group(2))));
		    }
		    
		    int ns = demuxer.getNumStreams();
		    for (int i = 0; i < ns; i++) {
		        DemuxerStream stream = demuxer.getStream(i);

		        metadata = stream.getMetaData();
		        // Language is usually embedded as metadata in a stream.
		        final String language = metadata.getValue("language");
		        
		        // We will only be able to make a decoder for streams we can actually
		        // decode, so the caller should check for null.
		        Decoder d = stream.getDecoder();

		        System.out.printf(" Stream #0.%1$d (%2$s): %3$s\n", i, language, d != null ? d.toString() : "unknown coder");
		        System.out.println("  Metadata:");
		        for(String key: metadata.getKeys())
		          System.out.printf("    %s: %s\n", key, metadata.getValue(key));
		      }
			
		} finally {
			demuxer.close();
		}
		
		return null;
	}

}
