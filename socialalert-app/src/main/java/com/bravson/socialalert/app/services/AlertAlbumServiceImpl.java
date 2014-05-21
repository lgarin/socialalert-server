package com.bravson.socialalert.app.services;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bravson.socialalert.app.entities.AlertAlbum;
import com.bravson.socialalert.app.exceptions.DataMissingException;
import com.bravson.socialalert.app.repositories.AlertAlbumRepository;
import com.bravson.socialalert.common.domain.AlbumInfo;
import com.bravson.socialalert.common.domain.QueryResult;

@Service
public class AlertAlbumServiceImpl implements AlertAlbumService {

	@Resource
	private AlertAlbumRepository albumRepository;
	
	@Value("${query.max.result}")
	private int maxPageSize;
	
	private PageRequest createPageRequest(int pageNumber, int pageSize) {
		if (pageSize > maxPageSize) {
			throw new IllegalArgumentException("Page size is limited to " + maxPageSize);
		}
		PageRequest pageRequest = new PageRequest(pageNumber, pageSize, Direction.DESC, "creation");
		return pageRequest;
	}
	
	private static QueryResult<AlbumInfo> toQueryResult(Page<AlertAlbum> page) {
		ArrayList<AlbumInfo> pageContent = new ArrayList<>(page.getSize());
		for (AlertAlbum activity : page) {
			pageContent.add(activity.toAlbumInfo());
		}
		return new QueryResult<>(pageContent, page.getNumber(), page.getTotalPages());
	}
	
	@Override
	public AlbumInfo createEmptyAlbum(UUID profileId, String title, String description) {
		AlertAlbum entity = new AlertAlbum(profileId, title, description);
		return albumRepository.save(entity).toAlbumInfo();
	}
	
	@Override
	@Transactional(rollbackFor={Throwable.class})
	public AlbumInfo updateAlbum(UUID albumId, UUID expectedProfileId, String title, String description, List<URI> mediaList) {
		AlertAlbum entity = albumRepository.lockById(albumId);
		if (entity == null) {
			throw new DataMissingException("Cannot find album "  + albumId);
		}
		if (!Objects.equals(entity.getProfileId(), expectedProfileId)) {
			throw new IllegalArgumentException("Album " + albumId + " is not owned by profile " + expectedProfileId);
		}
		entity.update(title, description, mediaList);
		return albumRepository.save(entity).toAlbumInfo();
	}
	
	@Override
	public QueryResult<AlbumInfo> getAlbums(UUID profileId, int pageNumber, int pageSize) {
		return toQueryResult(albumRepository.findByProfileId(profileId, createPageRequest(pageNumber, pageSize)));
	}
	
	@Override
	@Transactional(rollbackFor={Throwable.class})
	public void deleteAlbum(UUID albumId, UUID expectedProfileId) {
		AlertAlbum entity = albumRepository.lockById(albumId);
		if (entity == null) {
			throw new DataMissingException("Cannot find album "  + albumId);
		}
		if (!Objects.equals(entity.getProfileId(), expectedProfileId)) {
			throw new IllegalArgumentException("Album " + albumId + " is not owned by profile " + expectedProfileId);
		}
		albumRepository.delete(entity);
	}
}
