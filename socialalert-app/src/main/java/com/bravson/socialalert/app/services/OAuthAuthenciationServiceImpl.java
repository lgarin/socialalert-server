package com.bravson.socialalert.app.services;

import java.net.URL;
import java.util.HashMap;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.brickred.socialauth.AuthProvider;
import org.brickred.socialauth.Profile;
import org.brickred.socialauth.SocialAuthManager;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.stereotype.Service;

import com.bravson.socialalert.app.domain.ExternalProfileInfo;

@Service
@Scope(value = "session", proxyMode = ScopedProxyMode.INTERFACES)
public class OAuthAuthenciationServiceImpl implements OAuthAuthenicationService {

	@Resource
	private SocialAuthManager authManager;

	@Override
	public URL beginOAuthConsumption(String providerId, URL successUrl) {
		try {
			return new URL(authManager.getAuthenticationUrl(providerId, successUrl.toString()));
		} catch (Exception e) {
			throw new AuthenticationServiceException("Cannot build authentication url for provider " + providerId, e);
		}
	}
	
	@Override
	public ExternalProfileInfo endOAuthConsumption(URL receivingUrl) {
		HashMap<String,String> parameters = new HashMap<>();
		for (NameValuePair pair : URLEncodedUtils.parse(receivingUrl.getQuery(), null)) {
			parameters.put(pair.getName(), pair.getValue());
		}
		try {
			AuthProvider provider = authManager.connect(parameters);
			Profile profile = provider.getUserProfile();
			provider.logout();
			ExternalProfileInfo info = new ExternalProfileInfo();
			info.setEmail(profile.getEmail());
			info.setNickname(StringUtils.defaultString(profile.getDisplayName(), profile.getFullName()));
			info.setIdentifier(profile.getValidatedId());
			info.setFirstname(profile.getFirstName());
			info.setLastname(profile.getLastName());
			info.setCountry(profile.getCountry());
			info.setLanguage(profile.getLanguage());
			info.setGender(profile.getGender());
			info.setBirthdate(profile.getDob());
			info.setImage(profile.getProfileImageURL());
			return info;
		} catch (Exception e) {
			throw new AuthenticationServiceException("Cannot connect to OAuth provider", e);
		}
	}
}
