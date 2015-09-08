package com.bravson.socialalert.app.services;

import io.humble.video.Decoder;
import io.humble.video.Demuxer;
import io.humble.video.DemuxerStream;
import io.humble.video.Encoder;
import io.humble.video.FilterAudioSink;
import io.humble.video.FilterAudioSource;
import io.humble.video.FilterGraph;
import io.humble.video.MediaAudio;
import io.humble.video.MediaDescriptor;
import io.humble.video.MediaPacket;
import io.humble.video.Muxer;
import io.humble.video.MuxerFormat;

import java.io.File;
import java.io.IOException;

import javax.validation.constraints.NotNull;

import org.springframework.validation.annotation.Validated;

import com.bravson.socialalert.app.domain.VideoMetadata;

@Validated
public interface VideoFileService {

	public File createThumbnail(@NotNull File sourceFile) throws IOException;
	
	public File createPreview(@NotNull File sourceFile) throws IOException;
	
	public VideoMetadata parseMetadata(@NotNull File sourceFile) throws IOException;

	public abstract void extractAudio(File sourceFile, File outputFile) throws IOException;
}
