package com.bravson.socialalert.common.facade;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.UUID;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;
import org.joda.time.DateTime;

import com.bravson.socialalert.common.domain.AbuseInfo;
import com.bravson.socialalert.common.domain.AbuseReason;
import com.bravson.socialalert.common.domain.ActivityCount;
import com.bravson.socialalert.common.domain.ActivityInfo;
import com.bravson.socialalert.common.domain.ProfileInfo;
import com.bravson.socialalert.common.domain.ProfileStatisticInfo;
import com.bravson.socialalert.common.domain.PublicProfileInfo;
import com.bravson.socialalert.common.domain.QueryResult;
import com.googlecode.jsonrpc4j.JsonRpcParam;
import com.googlecode.jsonrpc4j.JsonRpcService;

@JsonRpcService(value="profileFacade", useNamedParams=true)
public interface ProfileFacade {

	ProfileInfo getCurrentUserProfile() throws IOException;
	
	ProfileInfo updateProfile(@JsonRpcParam("info") @NotNull @Valid ProfileInfo info) throws IOException;
	
	ProfileInfo claimProfilePicture(@JsonRpcParam("pictureUri") @NotNull URI pictureUri) throws IOException;

	QueryResult<ActivityInfo> getProfileActivity(@JsonRpcParam("profileId") @NotNull UUID profileId, @JsonRpcParam("pageNumber") @Min(0) int pageNumber, @JsonRpcParam("pageSize") @Min(1) int pageSize) throws IOException;
	
	QueryResult<ActivityInfo> getNetworkedProfileActivity(@JsonRpcParam("profileId") @NotNull UUID profileId, @JsonRpcParam("pageNumber") @Min(0) int pageNumber, @JsonRpcParam("pageSize") @Min(1) int pageSize) throws IOException;
	
	ProfileStatisticInfo getUserProfile(@JsonRpcParam("profileId") @NotNull UUID profileId) throws IOException;
	
	QueryResult<PublicProfileInfo> getFollowedProfiles(@JsonRpcParam("pageNumber") @Min(0) int pageNumber, @JsonRpcParam("pageSize") @Min(1) int pageSize) throws IOException;
	
	QueryResult<PublicProfileInfo> getFollowerProfiles(@JsonRpcParam("pageNumber") @Min(0) int pageNumber, @JsonRpcParam("pageSize") @Min(1) int pageSize) throws IOException;
	
	boolean isFollowing(@JsonRpcParam("profileId") @NotNull UUID profileId) throws IOException;
	
	boolean follow(@JsonRpcParam("profileId") @NotNull UUID profileId) throws IOException;
	
	boolean unfollow(@JsonRpcParam("profileId") @NotNull UUID profileId) throws IOException;
	
	QueryResult<PublicProfileInfo> searchProfiles(@JsonRpcParam("keyword") @NotEmpty String keyword, @JsonRpcParam("pageNumber") @Min(0) int pageNumber, @JsonRpcParam("pageSize") @Min(1) int pageSize) throws IOException;
	
	List<String> findNicknameSuggestions(@JsonRpcParam("partial") @NotEmpty String partial) throws IOException;
	
	QueryResult<ProfileStatisticInfo> getTopCreators(@JsonRpcParam("pageNumber") @Min(0) int pageNumber, @JsonRpcParam("pageSize") @Min(1) int pageSize) throws IOException;

	List<ActivityCount> getRecentActivityStatistic(@JsonRpcParam("lastCheck") @NotNull DateTime lastCheck) throws IOException;
	
	AbuseInfo reportAbusiveComment(@JsonRpcParam("commentId") @NotNull UUID commentId, @JsonRpcParam("country") String country, @JsonRpcParam("reason") @NotNull AbuseReason reason) throws IOException;
	
	AbuseInfo reportAbusiveMedia(@JsonRpcParam("mediaId") @NotNull URI mediaId, @JsonRpcParam("country") String country, @JsonRpcParam("reason") @NotNull AbuseReason reason) throws IOException;
}