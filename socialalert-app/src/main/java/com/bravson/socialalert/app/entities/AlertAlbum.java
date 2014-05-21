package com.bravson.socialalert.app.entities;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.apache.solr.client.solrj.beans.Field;
import org.springframework.data.annotation.Id;
import org.springframework.data.solr.core.mapping.SolrDocument;

import com.bravson.socialalert.app.infrastructure.VersionedEntity;
import com.bravson.socialalert.common.domain.AlbumInfo;

@SolrDocument(solrCoreName="AlertAlbum")
public class AlertAlbum extends VersionedEntity {

	@Id
	@Field
	private UUID albumId;
	
	@Field
    private UUID profileId;
	
	@Field
    private List<URI> mediaUri;
	
	@Field
    private String title;
	
	@Field
    private String description;
	
	public AlertAlbum(UUID profileId, String title, String description) {
		albumId = UUID.randomUUID();
		this.profileId = profileId;
		this.title = title;
		this.description = description;
	}
	
	public void update(String title, String description, List<URI> uriList) {
		this.title = title;
		this.description = description;
		mediaUri = new ArrayList<>(uriList.size());
		mediaUri.addAll(uriList);
		touch();
	}
	
	public AlbumInfo toAlbumInfo() {
		AlbumInfo info = new AlbumInfo();
		info.setAlbumId(albumId);
		info.setProfileId(profileId);
		info.setTitle(title);
		info.setDescription(description);
		info.setCreation(creation);
		info.setLastUpdate(lastUpdate);
		info.setMediaList(mediaUri == null ? Collections.<URI>emptyList() : Collections.unmodifiableList(mediaUri));
		return info;
	}

	public UUID getAlbumId() {
		return albumId;
	}

	public UUID getProfileId() {
		return profileId;
	}

	public List<URI> getMediaUri() {
		return mediaUri;
	}

	public String getTitle() {
		return title;
	}

	public String getDescription() {
		return description;
	}
}
