package com.bravson.socialalert.app.services;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.validation.annotation.Validated;

import com.bravson.socialalert.app.domain.ExternalProfileInfo;
import com.bravson.socialalert.app.entities.UserProfile;
import com.bravson.socialalert.common.domain.ProfileInfo;
import com.bravson.socialalert.common.domain.PublicProfileInfo;
import com.bravson.socialalert.common.domain.QueryResult;

@Validated
public interface UserProfileService {

	UserProfile createEmptyProfile(@NotEmpty String nickname);
	
	UserProfile getProfileById(@NotNull UUID profileId);
	
	Map<UUID, UserProfile> getProfileMap(@NotNull List<UUID> profileIdList);
	
	UserProfile completeProfile(@NotNull UUID profileId, @NotNull ExternalProfileInfo info);
	
	UserProfile updateProfile(@NotNull UUID profileId, @NotNull ProfileInfo info);

	UserProfile downloadProfilePicture(@NotNull UUID profileId, @NotNull URL pictureUrl) throws IOException;

	UserProfile claimProfilePicture(@NotNull UUID profileId, @NotNull URI pictureUri);
	
	QueryResult<PublicProfileInfo> searchProfiles(@NotEmpty String keyword,  @Min(0) int pageNumber, @Min(1) int pageSize);
	
	List<String> findNicknameSuggestions(@NotEmpty String partial);
}
