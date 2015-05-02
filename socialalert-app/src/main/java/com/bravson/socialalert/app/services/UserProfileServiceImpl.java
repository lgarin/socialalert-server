package com.bravson.socialalert.app.services;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bravson.socialalert.app.domain.ExternalProfileInfo;
import com.bravson.socialalert.app.entities.UserProfile;
import com.bravson.socialalert.app.exceptions.DataMissingException;
import com.bravson.socialalert.app.repositories.UserProfileRepository;
import com.bravson.socialalert.app.utilities.SolrUtils;
import com.bravson.socialalert.common.domain.ProfileInfo;
import com.bravson.socialalert.common.domain.PublicProfileInfo;
import com.bravson.socialalert.common.domain.QueryResult;

@Service
public class UserProfileServiceImpl implements UserProfileService {

	@Resource
	private UserProfileRepository profileRepository;
	
	@Resource
	private MediaStorageService storageService;
	
	@Value("${media.delete.delay}")
	private long pictureDeleteDelay;
	
	@Value("${query.max.result}")
	private int maxPageSize;
	
	@Override
	@Transactional(rollbackFor={Throwable.class})
	public UserProfile createEmptyProfile(String nickname) {
		UserProfile emptyProfile = new UserProfile(nickname);
		return profileRepository.save(emptyProfile);
	}
	
	@Override
	public UserProfile getProfileById(UUID profileId) {
		UserProfile profile = profileRepository.findById(profileId);
		if (profile == null) {
			throw new DataMissingException("Cannot find user profile "  + profileId);
		}
		return profile;
	}
	
	@Override
	public Map<UUID, UserProfile> getProfileMap(List<UUID> profileIdList) {
		if (profileIdList.size() == 0) {
			return Collections.emptyMap();
		}
		HashMap<UUID, UserProfile> result = new HashMap<>(profileIdList.size());
		for (UserProfile profile : profileRepository.findAll(profileIdList)) {
			result.put(profile.getId(), profile);
		}
		return result;
	}
	
	@Override
	@Transactional(rollbackFor={Throwable.class})
	public UserProfile completeProfile(UUID profileId, ExternalProfileInfo info) {
		UserProfile profile = lock(profileId);
		profile.complete(info);
		return profileRepository.save(profile);
	}
	
	@Override
	@Transactional(rollbackFor={Throwable.class})
	public UserProfile updateProfile(UUID profileId, ProfileInfo info) {
		UserProfile profile = lock(profileId);
		profile.update(info);
		return profileRepository.save(profile);
	}
	
	@Override
	@Transactional(rollbackFor={Throwable.class})
	public UserProfile downloadProfilePicture(UUID profileId, URL pictureUrl) throws IOException {
		UserProfile profile = lock(profileId);
		URI pictureUri = storageService.storeRemotePicture(pictureUrl);
		return updateImage(profile, pictureUri);
	}
	
	@Override
	@Transactional(rollbackFor={Throwable.class})
	public UserProfile claimProfilePicture(UUID profileId, URI pictureUri) {
		UserProfile profile = lock(profileId);
		File pictureFile = storageService.resolveMediaUri(pictureUri);
		if (pictureFile == null) {
			throw new DataMissingException("The picture " + pictureUri + " does not exists");
		}
		return updateImage(profile, pictureUri);
	}

	private UserProfile lock(UUID profileId) {
		UserProfile profile = profileRepository.lockById(profileId);
		if (profile == null) {
			throw new DataMissingException("Cannot find user profile "  + profileId);
		}
		return profile;
	}
	
	private UserProfile updateImage(UserProfile profile, URI pictureUri) {
		URI finalUri = URI.create("profiles/" + profile.getId().toString() + ".jpg");
		storageService.archiveMedia(pictureUri, finalUri);
		profile.updateImage(finalUri);
		return profileRepository.save(profile);
	}
	
	private static QueryResult<PublicProfileInfo> toQueryResult(Page<UserProfile> page) {
		ArrayList<PublicProfileInfo> pageContent = new ArrayList<>(page.getSize());
		for (UserProfile profile : page) {
			pageContent.add(profile.toPublicInfo());
		}
		return new QueryResult<>(pageContent, page.getNumber(), page.getTotalPages());
	}
	
	private PageRequest createPageRequest(int pageNumber, int pageSize, Sort sort) {
		if (pageSize > maxPageSize) {
			throw new IllegalArgumentException("Page size is limited to " + maxPageSize);
		}
		PageRequest pageRequest = new PageRequest(pageNumber, pageSize, sort);
		return pageRequest;
	}
	
	@Override
	public QueryResult<PublicProfileInfo> searchProfiles(String keyword,  int pageNumber, int pageSize) {
		keyword = SolrUtils.escapeSolrCharacters(keyword);
		return toQueryResult(profileRepository.findWithKeyword(keyword, createPageRequest(pageNumber, pageSize, null)));
	}

	@Override
	public List<String> findNicknameSuggestions(String partial) {
		return profileRepository.findSuggestion(partial);
	}
}
