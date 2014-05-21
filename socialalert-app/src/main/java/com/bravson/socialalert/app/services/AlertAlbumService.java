package com.bravson.socialalert.app.services;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.validation.annotation.Validated;

import com.bravson.socialalert.common.domain.AlbumConstants;
import com.bravson.socialalert.common.domain.AlbumInfo;
import com.bravson.socialalert.common.domain.QueryResult;

@Validated
public interface AlertAlbumService {

	public QueryResult<AlbumInfo> getAlbums(@NotNull UUID profileId, @Min(0) int pageNumber, @Min(1) int pageSize);

	public AlbumInfo updateAlbum(@NotNull UUID albumId, @NotNull UUID expectedProfileId, @NotEmpty String title, String description, @Size(max=AlbumConstants.MAX_MEDIA_COUNT) List<URI> mediaList);

	public AlbumInfo createEmptyAlbum(@NotNull UUID profileId, @NotEmpty String title, String description);
	
	public void deleteAlbum(@NotNull UUID albumId, @NotNull UUID expectedProfileId);
}
