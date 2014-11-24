package com.bravson.socialalert.app.services;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.io.FilenameUtils;
import org.joda.time.Duration;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import uk.co.caprica.vlcj.player.MediaMeta;
import uk.co.caprica.vlcj.player.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.condition.conditions.PausedCondition;
import uk.co.caprica.vlcj.player.headless.HeadlessMediaPlayer;

import com.bravson.socialalert.app.domain.VideoMetadata;
import com.coremedia.iso.IsoFile;
import com.coremedia.iso.boxes.MovieBox;
import com.drew.imaging.jpeg.JpegProcessingException;
import com.sun.jna.NativeLibrary;

@Service
public class VideoFileServiceImpl implements VideoFileService {

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
	
	@Value("${media.vlc.dir}")
	private String vlcDirectory;
	
	private MediaPlayerFactory playerFactory;
	
	@PostConstruct
	protected void init() {
		NativeLibrary.addSearchPath("libvlc", vlcDirectory);
		String[] args = {"--vout", "dummy", "--aout", "dummy"};
		playerFactory = new MediaPlayerFactory(args);
		
	}
	
	@PreDestroy
	protected void destroy() {
		if (playerFactory != null) {
			playerFactory.release();
		}
	}
	
	private File createSnapshot(File sourceFile, String prefix, int height, int width) throws IOException {
		File outputFile = new File(sourceFile.getParent(), prefix + FilenameUtils.getBaseName(sourceFile.getName()) + ".jpg");
		final HeadlessMediaPlayer player = playerFactory.newHeadlessMediaPlayer();
		try {
			if (!player.prepareMedia(sourceFile.getPath())) {
				throw new IOException("Cannot load video file " + sourceFile);
			}
			//player.parseMedia();
			//player.mute(true);
			if (!player.start()) {
				throw new IOException("Cannot load video file " + sourceFile);
			}
			PausedCondition condition = new PausedCondition(player);
			player.setPosition(0.05f);
			player.pause();
			condition.await();
			if (!player.saveSnapshot(outputFile, width, height)) {
				throw new IOException("Cannot save snapshot to file " + outputFile);
			}
		} catch (InterruptedException e) {
			throw new RuntimeException("Processing aborted", e);
		} finally {
			player.release();
		}
		return outputFile;
	}
	
	@Override
	public File createJpegImage(File sourceFile) throws IOException {
		return createSnapshot(sourceFile, "", 0, 0);
	}
	
	@Override
	public File createJpegPreview(File sourceFile) throws IOException {
		return createSnapshot(sourceFile, previewPrefix, previewHeight, previewWidth);
	}
	
	@Override
	public File createJpegThumbnail(File sourceFile) throws IOException {
		return createSnapshot(sourceFile, thumbnailPrefix, thumbnailHeight, thumbnailWidth);
	}
	
	@Override
	public VideoMetadata parseMp4Metadata(File sourceFile) throws IOException {
		final HeadlessMediaPlayer player = playerFactory.newHeadlessMediaPlayer();
		try {
			if (!player.prepareMedia(sourceFile.getPath())) {
				throw new IOException("Cannot load video file " + sourceFile);
			}
			//player.parseMedia();
			if (!player.start()) {
				throw new IOException("Cannot load video file " + sourceFile);
			}
			PausedCondition condition = new PausedCondition(player);
			player.pause();
			condition.await();
			
			
			VideoMetadata result = new VideoMetadata();
			
			Dimension size = player.getVideoDimension();
			result.setHeight(size.height);
			result.setWidth(size.width);
			
			result.setDuration(Duration.millis(player.getLength()));
			MediaMeta metaData = player.getMediaMeta();
			try {
				result.setTimestamp(ISODateTimeFormat.localDateOptionalTimeParser().parseDateTime(metaData.getDate()));
			} finally {
				metaData.release();
			}
			return result;
		} catch (InterruptedException e) {
			throw new RuntimeException("Processing aborted", e);
		} finally {
			player.release();
		}
	}
}
