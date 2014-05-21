package com.bravson.socialalert.app.services;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.geometry.Positions;

import org.apache.commons.collections.IteratorUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.bravson.socialalert.app.domain.PictureMetadata;
import com.drew.imaging.jpeg.JpegMetadataReader;
import com.drew.imaging.jpeg.JpegProcessingException;
import com.drew.lang.GeoLocation;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.exif.GpsDirectory;
import com.drew.metadata.jpeg.JpegDirectory;

@Service
public class PictureFileServiceImpl implements PictureFileService {

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
	
	@Override
	public PictureMetadata parseJpegMetadata(File sourceFile) throws JpegProcessingException, IOException {
		Metadata metadata = JpegMetadataReader.readMetadata(sourceFile);
		
		if (metadata.hasErrors()) {
			ArrayList<Iterator<String>> errorList = new ArrayList<>();
			for (Directory directory : metadata.getDirectories()) {
	           errorList.add(directory.getErrors().iterator());
	        }
			
			@SuppressWarnings("unchecked")
			Iterator<String> allErrors = IteratorUtils.chainedIterator(errorList);
			throw new JpegProcessingException(StringUtils.join(allErrors, "; "));
		}
		
		PictureMetadata result = new PictureMetadata();
		
		ExifIFD0Directory exifTags = metadata.getDirectory(ExifIFD0Directory.class);
		if (exifTags != null) {
			Date dateTime = exifTags.getDate(ExifIFD0Directory.TAG_DATETIME);
			if (dateTime != null) {
				result.setTimestamp(new DateTime(dateTime));
			}
			result.setCameraMaker(exifTags.getString(ExifIFD0Directory.TAG_MAKE));
			result.setCameraModel(exifTags.getString(ExifIFD0Directory.TAG_MODEL));
			result.setHeight(exifTags.getInteger(ExifIFD0Directory.TAG_Y_RESOLUTION));
			result.setWidth(exifTags.getInteger(ExifIFD0Directory.TAG_X_RESOLUTION));
		}
		
		ExifSubIFDDirectory exifSubTags = metadata.getDirectory(ExifSubIFDDirectory.class);
		if (exifSubTags != null) {
			Date dateTime = exifSubTags.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
			if (dateTime != null) {
				result.setTimestamp(new DateTime(dateTime));
			}
		}
		
		JpegDirectory jpegTags = metadata.getDirectory(JpegDirectory.class);
		if (jpegTags != null) {
			result.setHeight(jpegTags.getInteger(JpegDirectory.TAG_JPEG_IMAGE_HEIGHT));
			result.setWidth(jpegTags.getInteger(JpegDirectory.TAG_JPEG_IMAGE_WIDTH));
		}
		
		GpsDirectory gpsTags = metadata.getDirectory(GpsDirectory.class);
		if (gpsTags != null) {
			GeoLocation location = gpsTags.getGeoLocation();
			if (location != null) {
				result.setLatitude(location.getLatitude());
				result.setLongitude(location.getLongitude());
			}
		}
		
		return result;
	}
	
	@Override
	public File createJpegThumbnail(File sourceFile) throws IOException {
		File thumbnailFile = new File(sourceFile.getParent(), thumbnailPrefix + sourceFile.getName());
		Thumbnails.of(sourceFile).size(thumbnailWidth, thumbnailHeight).crop(Positions.CENTER).outputFormat("jpg").toFile(thumbnailFile);
		return thumbnailFile;
	}
	
	@Override
	public File createJpegPreview(File sourceFile) throws IOException {
		File thumbnailFile = new File(sourceFile.getParent(), previewPrefix + sourceFile.getName());
		Thumbnails.of(sourceFile).size(previewWidth, previewHeight).outputFormat("jpg").toFile(thumbnailFile);
		return thumbnailFile;
	}
}
