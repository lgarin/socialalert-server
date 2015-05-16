package com.bravson.socialalert.app.services;

import io.humble.video.Codec;
import io.humble.video.Decoder;
import io.humble.video.Demuxer;
import io.humble.video.DemuxerStream;
import io.humble.video.Encoder;
import io.humble.video.FilterGraph;
import io.humble.video.FilterPictureSink;
import io.humble.video.FilterPictureSource;
import io.humble.video.Global;
import io.humble.video.KeyValueBag;
import io.humble.video.MediaAudio;
import io.humble.video.MediaDescriptor;
import io.humble.video.MediaPacket;
import io.humble.video.MediaPicture;
import io.humble.video.Muxer;
import io.humble.video.MuxerFormat;
import io.humble.video.PixelFormat;
import io.humble.video.awt.MediaPictureConverter;
import io.humble.video.awt.MediaPictureConverterFactory;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import com.bravson.socialalert.app.domain.VideoMetadata;

@Service
public class VideoFileServiceImpl implements VideoFileService {

	private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
	private static final Pattern LOCATION_PATTERN = Pattern.compile("([+-]\\d+.\\d+)([+-]\\d+.\\d+)([+-]\\d+.\\d+)/");
	
	@Value("${video.snapshot.delay}")
	private long snapshotDelay;
	
	@Value("classpath:/resource/logo.jpg")
	private Resource watermarkFile;
	
	private BufferedImage watermarkImage;
	
	@Autowired
	private PictureFileService pictureService;
	
	@PostConstruct
	protected void init() throws IOException {
		watermarkImage = ImageIO.read(watermarkFile.getInputStream());
	}
	
	private MediaPicture buildPicture(Demuxer demuxer, DemuxerStream stream) throws InterruptedException, IOException {
		Decoder decoder = stream.getDecoder();
		decoder.open(null, null);
		MediaPicture picture = MediaPicture.make(decoder.getWidth(), decoder.getHeight(), decoder.getPixelFormat());
		MediaPacket packet = MediaPacket.make();
		while (demuxer.read(packet) >= 0) {
			if (packet.getStreamIndex() == stream.getIndex()) {
				if (decodePicture(packet, decoder, picture)) {
					return picture;
				}
			}
		}

		if (decodePicture(null, decoder, picture)) {
			return picture;
		}

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
		return findStream(demuxer, MediaDescriptor.Type.MEDIA_VIDEO);
	}
	
	private DemuxerStream findStream(Demuxer demuxer, MediaDescriptor.Type type) throws IOException {
		try {
			int ns = demuxer.getNumStreams();
			for (int i = 0; i < ns; i++) {
				DemuxerStream stream = demuxer.getStream(i);
				Decoder decoder = stream.getDecoder();
				if (decoder != null && decoder.getCodecType() == type) {
					return stream;
				}
			}
			return null;
		} catch (InterruptedException e) {
			throw new IOException("Cannot find " + type + " stream in " + demuxer.getURL(), e);
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

	
	@Override
	public File watermark(File sourceFile) throws IOException {
		File outputFile = new File(sourceFile.getParentFile(), "watermark_" + sourceFile.getName());
		
		Demuxer demuxer = Demuxer.make();
		MuxerFormat format = MuxerFormat.guessFormat(null, outputFile.getName(), null);
		Muxer muxer = Muxer.make(outputFile.toString(), format, null);
		
		FilterGraph graph = FilterGraph.make();
		MediaPacket inputPacket = MediaPacket.make();
		//MediaPacket audioPacket = MediaPacket.make();
		MediaPacket videoPacket = MediaPacket.make();
		
		try {
			try {
				demuxer.open(sourceFile.toString(), null, false, true, null, null);
			} catch (InterruptedException e) {
				throw new IOException("Cannot open file " + sourceFile, e);
			}
			
			DemuxerStream videoStream = findStream(demuxer, MediaDescriptor.Type.MEDIA_VIDEO);
			Decoder videoDecoder = videoStream.getDecoder();
			videoDecoder.open(null, null);
			
			DemuxerStream audioStream = findStream(demuxer, MediaDescriptor.Type.MEDIA_AUDIO);
			Decoder audioDecoder = audioStream.getDecoder();
			audioDecoder.open(null, null);
			
			Encoder videoEncoder = Encoder.make(Codec.findEncodingCodecByName("libx264"));
			videoEncoder.setWidth(videoDecoder.getWidth());
			videoEncoder.setHeight(videoDecoder.getHeight());
			videoEncoder.setPixelFormat(videoDecoder.getPixelFormat());
			videoEncoder.setTimeBase(videoDecoder.getTimeBase());
			if (format.getFlag(MuxerFormat.Flag.GLOBAL_HEADER)) {
				videoEncoder.setFlag(Encoder.Flag.FLAG_GLOBAL_HEADER, true);
			}
			videoEncoder.setProperty("crf", 20L);
			videoEncoder.setProperty("preset", "slow");
			videoEncoder.open(null, null);
			/*
			Encoder audioEncoder = Encoder.make(Codec.findEncodingCodecByName("aac"));
			audioEncoder.setSampleRate(audioDecoder.getSampleRate());
			audioEncoder.setChannels(audioDecoder.getChannels());
			audioEncoder.setChannelLayout(audioDecoder.getChannelLayout());
			audioEncoder.setSampleFormat(audioDecoder.getSampleFormat());
			audioEncoder.open(null, null);
			*/
			muxer.addNewStream(videoEncoder);
			//muxer.addNewStream(audioEncoder);
			muxer.open(null, null);
			
			MediaPicture watermarkPicture = createWatermarkPicture();
			
			FilterPictureSource source = graph.addPictureSource("input", videoDecoder.getWidth(), videoDecoder.getHeight(), videoDecoder.getPixelFormat(), videoDecoder.getTimeBase(), null);
			FilterPictureSource watermark = graph.addPictureSource("watermark", watermarkPicture.getWidth(), watermarkPicture.getHeight(), watermarkPicture.getFormat(), videoDecoder.getTimeBase(), null);
			FilterPictureSink sink = graph.addPictureSink("output", videoDecoder.getPixelFormat());
			graph.open("[watermark] lutrgb='a=128' [over];[input][over] overlay='x=(main_w-overlay_w)/2:y=(main_h-overlay_h)/2' [output]");
			
			//MediaAudio sourceAudio = MediaAudio.make(audioDecoder.getFrameSize(), audioDecoder.getSampleRate(), audioDecoder.getChannels(), audioDecoder.getChannelLayout(), audioDecoder.getSampleFormat());
			MediaPicture targetPicture = MediaPicture.make(videoEncoder.getWidth(), videoEncoder.getHeight(), videoEncoder.getPixelFormat());
      		MediaPicture sourcePicture = MediaPicture.make(videoDecoder.getWidth(), videoDecoder.getHeight(), videoDecoder.getPixelFormat());
      		
			 while(demuxer.read(inputPacket) >= 0) {
			      if (inputPacket.isComplete()) {
			    	Decoder d = demuxer.getStream(inputPacket.getStreamIndex()).getDecoder();
			        if (d.getCodecType() == MediaDescriptor.Type.MEDIA_AUDIO) {
			        	/*
			        	if (decodeAudio(inputPacket, audioDecoder, sourceAudio)) {
			        		do {
			      				audioEncoder.encode(audioPacket, sourceAudio);
		      			        if (audioPacket.isComplete()) {
		      			          muxer.write(videoPacket, false);
		      			        }
	      			        } while (audioPacket.isComplete());
			        	}
			        	*/
			      	} else if (d.getCodecType() == MediaDescriptor.Type.MEDIA_VIDEO) {
			      		if (decodePicture(inputPacket, videoDecoder, sourcePicture)) {
			      			watermarkPicture.setTimeStamp(sourcePicture.getTimeStamp());
			      			watermark.addPicture(watermarkPicture);
			      			source.addPicture(sourcePicture);
			      			
			      			if (sink.getPicture(targetPicture) >= 0) {
			      				do {
				      				videoEncoder.encode(videoPacket, targetPicture);
			      			        if (videoPacket.isComplete()) {
			      			          muxer.write(videoPacket, false);
			      			        }
		      			        } while (videoPacket.isComplete());
			      			}
			      		}
			      	}
			        
			      }
			    }
			 muxer.close();

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

	private MediaPicture createWatermarkPicture() {
		MediaPicture watermarkPicture = MediaPicture.make(watermarkImage.getWidth(), watermarkImage.getHeight(), PixelFormat.Type.PIX_FMT_RGBA);
		MediaPictureConverter watermarkConverter = MediaPictureConverterFactory.createConverter(watermarkImage, watermarkPicture);
		watermarkConverter.toPicture(watermarkPicture, watermarkImage, 0);
		return watermarkPicture;
	}

	private boolean decodePicture(MediaPacket packet, Decoder decoder, MediaPicture picture) {
		int size = packet == null ? 0 : packet.getSize();
		int offset = 0;
		int bytesRead = 0;
		do {
			bytesRead += decoder.decodeVideo(picture, packet, offset);
			if (picture.isComplete()) {
				return true;
			}
			if (bytesRead <= 0) {
				throw new RuntimeException("Could not decode video");
			}
			offset += bytesRead;
		} while (offset < size);
		return false;
	}
	
	private boolean decodeAudio(MediaPacket packet, Decoder decoder, MediaAudio audio) {
		int size = packet == null ? 0 : packet.getSize();
		int offset = 0;
		int bytesRead = 0;
		do {
			bytesRead += decoder.decodeAudio(audio, packet, offset);
			if (audio.isComplete()) {
				return true;
			}
			if (bytesRead <= 0) {
				throw new RuntimeException("Could not decode audio");
			}
			offset += bytesRead;
		} while (offset < size);
		return false;
	}
}
