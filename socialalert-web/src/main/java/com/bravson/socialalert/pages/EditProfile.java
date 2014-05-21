package com.bravson.socialalert.pages;

import java.io.IOException;
import java.net.URI;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;
import org.apache.tapestry5.annotations.ActivationRequestParameter;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SetupRender;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.upload.services.UploadedFile;

import com.bravson.socialalert.common.domain.Gender;
import com.bravson.socialalert.common.domain.ProfileInfo;
import com.bravson.socialalert.common.domain.UserRole;
import com.bravson.socialalert.common.facade.ProfileFacade;
import com.bravson.socialalert.services.ProtectedPage;
import com.googlecode.jsonrpc4j.JsonRpcClientException;

@ProtectedPage(allow={UserRole.USER})
public class EditProfile {

	@Inject
    private ProfileFacade profileService;
	
	@Property
	@Persist
	private ProfileInfo profile;

	@Property
	private Set<String> countryNames = new TreeSet<String>();
	
	@Property
	private Set<String> languageNames = new TreeSet<String>();
	
	@Inject
	private Locale userLocale;
	
	@Property
	@Inject
	@Symbol("app.thumbnail.url")
	private String thumbnailUrl;
	
	@Inject
	@Symbol("default.profile.picture")
	private String defaultProfileUri;
	
	@ActivationRequestParameter
	private String mediaUri;
	
	@Property
	private UploadedFile file;
	
	@SetupRender
	void setupRender() throws IOException {
		Locale[] availableLocales = Locale.getAvailableLocales();

        for (Locale locale : availableLocales) {
            if (StringUtils.isNotEmpty(locale.getDisplayCountry(locale))) {
                countryNames.add(locale.getDisplayCountry(userLocale));
            }
            if (StringUtils.isNotEmpty(locale.getDisplayLanguage(locale))) {
            	languageNames.add(locale.getDisplayLanguage(userLocale));
            }
        }
		
        profile = profileService.getCurrentUserProfile();
	}
	
	public Gender getMale() {
		return Gender.MALE;
	}
	
	public Gender getFemale() {
		return Gender.FEMALE;
	}
	
	void onValidateFromEditForm() throws IOException {
    	
    	try {
    		if (mediaUri != null) {
    			profileService.claimProfilePicture(URI.create(mediaUri));
    			mediaUri = null;
    		}
    		profileService.updateProfile(profile);
    	} catch (JsonRpcClientException e) {
    		throw e;
		 }
    }
	
	Object onSuccessFromEditForm() {
		return this;
    }
	
	public String getImageUri() {
		if (mediaUri != null) {
			return mediaUri;
		}
		if (profile.getImage() != null) {
			return profile.getImage().getPath();
		}
		return defaultProfileUri;
	}
	
}
