package com.bravson.socialalert.components;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SetupRender;
import org.apache.tapestry5.ioc.annotations.Inject;

import com.bravson.socialalert.common.domain.AlbumInfo;
import com.bravson.socialalert.common.domain.QueryResult;
import com.bravson.socialalert.common.facade.PictureFacade;

public class ListAlbums {

	@Inject
    private PictureFacade pictureService;
	
	@Parameter
	private UUID profileId;
	
	@Property
	private List<AlbumInfo> albumList;
	
	@Property
	private AlbumInfo currentAlbum;
	
	@Persist
	private int pageNumber;
	
	private int pageCount;
	
	@SetupRender
	void setupRender() throws IOException {
		QueryResult<AlbumInfo> result = pictureService.getAlbums(profileId, pageNumber, 5);
		albumList = result.getContent();
		pageCount = result.getPageCount();
		pageNumber = result.getPageNumber();
	}
	
	public boolean isNextPageDisabled() {
		return pageNumber + 1 >= pageCount;
	}
	
	public boolean isPreviousPageDisabled() {
		return pageNumber <= 0;
	}
	
	Object onNextPage() {
		pageNumber++;
		return this;
	}
	
	Object onPreviousPage() {
		pageNumber--;
		return this;
	}
	
	Object onRemove(UUID albumId) throws IOException {
		pictureService.deleteAlbum(albumId);
		return this;
	}

	Object onSave(UUID albumId) throws IOException {
		pictureService.updateAlbum(albumId, "Test 2", "An updated description", Collections.singletonList(URI.create("20130716/f317f3c7918c83ff6ec24aabb6c017fd.jpg")));
		return this;
	}
}
