package com.bravson.socialalert.app.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.solr.core.query.SimpleUpdateField;
import org.springframework.data.solr.core.query.UpdateAction;
import org.springframework.data.solr.core.query.UpdateField;
import org.springframework.stereotype.Service;

import com.bravson.socialalert.app.entities.ApplicationUser;
import com.bravson.socialalert.app.entities.ProfileLink;
import com.bravson.socialalert.app.repositories.ProfileLinkRepository;
import com.bravson.socialalert.common.domain.PublicProfileInfo;
import com.bravson.socialalert.common.domain.QueryResult;
import com.bravson.socialalert.common.domain.UserContent;

@Service
public class ProfileLinkServiceImpl implements ProfileLinkService {

	@Resource
	private ProfileLinkRepository linkRepository;
	
	@Value("${query.max.result}")
	private int maxPageSize;
	
	private PageRequest createPageRequest(int pageNumber, int pageSize) {
		if (pageSize > maxPageSize) {
			throw new IllegalArgumentException("Page size is limited to " + maxPageSize);
		}
		// TODO add lastUpdate in sort
		PageRequest pageRequest = new PageRequest(pageNumber, pageSize, Direction.DESC, "weight");
		return pageRequest;
	}
	
	@Override
	public QueryResult<UUID> getObservedProfiles(UUID sourceProfileId, int pageNumber, int pageSize) {
		Page<ProfileLink> links = linkRepository.findBySourceProfileId(sourceProfileId, createPageRequest(pageNumber, pageSize));
		List<UUID> result = new ArrayList<>(links.getNumberOfElements());
		for (ProfileLink link : links) {
			result.add(link.getTargetProfileId());
		}
		return new QueryResult<>(result, links.getNumber(), links.getTotalPages());
	}
	
	@Override
	public QueryResult<UUID> getObserverProfiles(UUID targetProfileId, int pageNumber, int pageSize) {
		Page<ProfileLink> links = linkRepository.findByTargetProfileId(targetProfileId, createPageRequest(pageNumber, pageSize));
		List<UUID> result = new ArrayList<>(links.getNumberOfElements());
		for (ProfileLink link : links) {
			result.add(link.getSourceProfileId());
		}
		return new QueryResult<>(result, links.getNumber(), links.getTotalPages());
	}

	@Override
	public boolean isObserverOf(UUID sourceProfileId, UUID observedProfileId) {
		String linkId = ProfileLink.buildLinkId(sourceProfileId, observedProfileId);
		return linkRepository.exists(linkId);
	}

	@Override
	public boolean addObservedProfile(UUID sourceProfileId, UUID observedProfileId) {
		boolean result = isObserverOf(sourceProfileId, observedProfileId);
		if (result) {
			return false;
		}
		linkRepository.save(new ProfileLink(sourceProfileId, observedProfileId));
		return true;
	}

	@Override
	public boolean removeObservedProfile(UUID sourceProfileId, UUID observedProfileId) {
		boolean result = isObserverOf(sourceProfileId, observedProfileId);
		if (!result) {
			return false;
		}
		linkRepository.delete(ProfileLink.buildLinkId(sourceProfileId, observedProfileId));
		return true;
	}
	
	@Override
	public boolean increaseActivityWeight(UUID sourceProfileId, UUID targetProfileId, int delta) {
		String linkId = ProfileLink.buildLinkId(sourceProfileId, targetProfileId);
		if (!linkRepository.exists(linkId)) {
			return false;
		}
		UpdateField weightInc = new SimpleUpdateField("weight", delta, UpdateAction.INC);
		linkRepository.partialUpdate(linkId, Collections.singletonList(weightInc));
		return true;
	}

	@Override
	public void updateObservedStatus(UUID sourceProfileId, Collection<? extends PublicProfileInfo> profiles) {
		HashMap<String, Boolean> observedMap = new HashMap<>(profiles.size());
		for (PublicProfileInfo profile : profiles) {
			observedMap.put(ProfileLink.buildLinkId(sourceProfileId, profile.getProfileId()), Boolean.FALSE);
		}
		for (ProfileLink link : linkRepository.findAll(observedMap.keySet())) {
			observedMap.put(link.getId(), Boolean.TRUE);
		}
		for (PublicProfileInfo profile : profiles) {
			profile.setFollowed(observedMap.get(ProfileLink.buildLinkId(sourceProfileId, profile.getProfileId())));
		}
	}
}
