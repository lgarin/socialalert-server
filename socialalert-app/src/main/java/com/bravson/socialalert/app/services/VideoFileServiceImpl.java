package com.bravson.socialalert.app.services;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import net.coobird.thumbnailator.Thumbnails;

import org.apache.commons.io.FilenameUtils;
import org.joda.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.bravson.socialalert.app.domain.VideoMetadata;
import com.xuggle.mediatool.IMediaReader;
import com.xuggle.mediatool.MediaListenerAdapter;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.mediatool.event.IVideoPictureEvent;
import com.xuggle.xuggler.ICodec.Type;
import com.xuggle.xuggler.IContainer;
import com.xuggle.xuggler.IError;
import com.xuggle.xuggler.IStream;
import com.xuggle.xuggler.IStreamCoder;

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
	
	@Value("${video.snapshot.delay}")
	private long snapshotDelay;

	private File createSnapshot(File sourceFile, String prefix, final int height, final int width) throws IOException {
		if (!sourceFile.canRead()) {
			throw new IOException("Cannot read file " + sourceFile);
		}
		 final File outputFile = new File(sourceFile.getParent(), prefix + FilenameUtils.getBaseName(sourceFile.getName()) + ".jpg");
         
		 IMediaReader reader = ToolFactory.makeReader(sourceFile.getPath());
		 try {
			 final AtomicBoolean pictureTaken = new AtomicBoolean();
			 final AtomicReference<IOException> exceptionReceived = new AtomicReference<>();
			 reader.open();
			 /*
			 IStream stream = findVideoStream(reader.getContainer());
			 reader.getContainer().seekKeyFrame(stream.getIndex(), snapshotDelay, snapshotDelay, snapshotDelay * 2L, 0);
			 */
		     reader.setBufferedImageTypeToGenerate(BufferedImage.TYPE_3BYTE_BGR);
		     reader.addListener(new MediaListenerAdapter() {
		    	 @Override
		    	public void onVideoPicture(IVideoPictureEvent event) {
		    		 
		    		try {
		    			Thumbnails.of(event.getImage()).size(width, height).outputFormat("jpg").toFile(outputFile);
		    			pictureTaken.set(true);
					} catch (IOException e) {
						exceptionReceived.set(e);
					}
		    	}
		     });
		     IError error = null;
		     while ((error = reader.readPacket()) == null) {
		    	if (exceptionReceived.get() != null) {
		    		throw exceptionReceived.get();
		    	}
	            if (pictureTaken.get()) {
	            	return outputFile;
	            }
	         }
		     if (error != null) {
		    	 throw new IOException(error.getDescription() + ":" + error.getErrorNumber());
		     }
		     return null;
		 } finally {
			 reader.close();
		 }
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
	public VideoMetadata parseMetadata(File sourceFile) throws IOException {
		 IMediaReader reader = ToolFactory.makeReader(sourceFile.getPath());
		 try {
			 reader.open();
			 IContainer container = reader.getContainer();
			 
			 IStream stream = findVideoStream(container);
			 if (stream == null) {
				 throw new IOException("No video stream found");
			 }
			 VideoMetadata result = new VideoMetadata();
			 result.setHeight(stream.getStreamCoder().getHeight());
			 result.setWidth(stream.getStreamCoder().getWidth());
			 result.setDuration(Duration.millis(1000L * stream.getDuration() * stream.getFrameRate().getDenominator() / stream.getFrameRate().getNumerator()));
		     return result;
		 } finally {
			 reader.close();
		 }
	}
	
	private IStream findVideoStream(IContainer container) {
		int numStreams = container.getNumStreams();
		for (int i = 0; i < numStreams; i++) {
			IStream stream = container.getStream(i);
			IStreamCoder coder = stream.getStreamCoder();
			if (coder.getCodecType() == Type.CODEC_TYPE_VIDEO && stream.getDuration() > 0L) {
				return stream;
			}
		}
		return null;
	}
}
