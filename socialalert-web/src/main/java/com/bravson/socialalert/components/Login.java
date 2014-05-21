package com.bravson.socialalert.components;

import java.io.IOException;
import java.net.URL;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.tapestry5.Link;
import org.apache.tapestry5.annotations.Component;
import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.beaneditor.Validate;
import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.corelib.components.PasswordField;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.services.PageRenderLinkSource;

import com.bravson.socialalert.common.domain.ErrorCodes;
import com.bravson.socialalert.common.domain.UserConstants;
import com.bravson.socialalert.common.domain.UserInfo;
import com.bravson.socialalert.common.domain.UserRole;
import com.bravson.socialalert.common.facade.UserFacade;
import com.bravson.socialalert.pages.OAuthLogin;
import com.bravson.socialalert.pages.OpenIdLogin;
import com.bravson.socialalert.pages.Index;
import com.bravson.socialalert.pages.InitiateResetPassword;
import com.bravson.socialalert.pages.UserHome;
import com.bravson.socialalert.services.DisplayState;
import com.bravson.socialalert.services.ProtectedPage;
import com.googlecode.jsonrpc4j.JsonRpcClientException;

@ProtectedPage(allow={UserRole.ANONYMOUS})
public class Login {
	
	@SessionState
	private DisplayState displayState;
	
	@Validate("required")
    @Property
    private String username;

	@Validate("required,minLength=" + UserConstants.MIN_PASSWORD_LENGTH)
    @Property
    private String password;

    @InjectComponent("password")
    private PasswordField passwordField;

    @Component(parameters={"clientValidation=SUBMIT"})
    private Form loginForm;
    
    @Inject
    private UserFacade userService;
    
    @SessionState(create=false)
    private UserInfo userInfo;
    
    @InjectPage
    private InitiateResetPassword resetPage;
    
    @Inject
    @Symbol("google.login.url")
    private URL googleLoginUrl;
    
    @Inject
    private PageRenderLinkSource pageRenderLinkSource; 

    /**
     * Skip login if already logged in
     */
//    Object onActivate() {
//    	if (userInfo != null) {
//    		return onSuccess();
//    	}
//    	return null;
//    }
    
    /**
     * Do the cross-field validation
     */
    void onValidateFromLoginForm() throws IOException {
    	try {
    		userInfo = userService.login(username, password);
    	} catch (JsonRpcClientException e) {
    		if (e.getCode() == ErrorCodes.BAD_CREDENTIALS) {
    			loginForm.recordError(passwordField, "Invalid user name or password.");
    		} else if (e.getCode() == ErrorCodes.LOCKED_ACCOUNT) {
    			loginForm.recordError("Account is locked.");
    		} else if (e.getCode() == ErrorCodes.CREDENTIAL_EXPIRED) {
    			// TODO show change password
    		} else {
    			throw e;
    		}
		 }
    }
    
    Object onRecovery() {
    	displayState.clear();
    	resetPage.activate(username);
    	return resetPage;
    }
    
    Object onGoogleLogin() throws IOException {
    	Link redirectLink = pageRenderLinkSource.createPageRenderLink(OpenIdLogin.class);
    	return userService.beginOpenIdLogin(googleLoginUrl, new URL(redirectLink.toAbsoluteURI()));
    }
    
    Object onHotmailLogin() throws IOException {
    	Link redirectLink = pageRenderLinkSource.createPageRenderLink(OAuthLogin.class);
    	return userService.beginOAuthLogin("hotmail", new URL(redirectLink.toAbsoluteURI()));
    }
    
    Object onFacebookLogin() throws IOException {
    	Link redirectLink = pageRenderLinkSource.createPageRenderLink(OAuthLogin.class);
    	return userService.beginOAuthLogin("facebook", new URL(redirectLink.toAbsoluteURI()));
    }
    
    Object onTwitterLogin() throws IOException {
    	Link redirectLink = pageRenderLinkSource.createPageRenderLink(OAuthLogin.class);
    	return userService.beginOAuthLogin("twitter", new URL(redirectLink.toAbsoluteURI()));
    }

    /**
     * Validation passed, so we'll go to the "PostLogin" page
     */
    Object onSuccess() {
    	Object nextPage = ObjectUtils.defaultIfNull(displayState.getNextPage(), UserHome.class);
    	displayState.clear();
        return nextPage;
    }
    
    Object onFailure() {
    	displayState.showDialog("loginModal");
    	displayState.showTab("loginModalTab", "login");
    	return Index.class;
    }
}
