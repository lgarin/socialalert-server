package com.bravson.socialalert.components;

import java.io.IOException;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.tapestry5.annotations.Component;
import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.beaneditor.Validate;
import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.corelib.components.PasswordField;
import org.apache.tapestry5.corelib.components.TextField;
import org.apache.tapestry5.ioc.annotations.Inject;

import com.bravson.socialalert.common.domain.ErrorCodes;
import com.bravson.socialalert.common.domain.UserConstants;
import com.bravson.socialalert.common.domain.UserRole;
import com.bravson.socialalert.common.facade.UserFacade;
import com.bravson.socialalert.pages.Index;
import com.bravson.socialalert.services.DisplayState;
import com.bravson.socialalert.services.ProtectedPage;
import com.googlecode.jsonrpc4j.JsonRpcClientException;

@ProtectedPage(allow={UserRole.ANONYMOUS})
public class Register {

	@SessionState
	private DisplayState displayState;
	
	@Validate("required")
	@Property
	@Persist("flash")
    private String email;
	
	@Validate("required,regexp=" + UserConstants.NICKNAME_PATTERN)
	@Property
	@Persist("flash")
    private String nickname;

	@Validate("required,minLength=" + UserConstants.MIN_PASSWORD_LENGTH)
    @Property
    private String password;
    
	@Validate("required,minLength=" + UserConstants.MIN_PASSWORD_LENGTH)
    @Property
    private String password2;
    
	@InjectComponent("nickname")
    private TextField nicknameField;
	
    @InjectComponent("email")
    private TextField emailField;
    
    @InjectComponent("password2")
    private PasswordField password2Field;

    @Component(parameters={"clientValidation=SUBMIT"})
    private Form registerForm;
    
    @Inject
    private UserFacade userService;
    
    /**
     * Do the cross-field validation
     */
    void onValidateFromRegisterForm() throws IOException {
    	if (ObjectUtils.notEqual(password, password2)) {
    		registerForm.recordError(password2Field, "Password does not match.");
    	}
    	
    	try {
    		String uniqueNickname = userService.generateUniqueNickname(nickname);
    		if (!ObjectUtils.equals(uniqueNickname, nickname)) {
    			registerForm.recordError(nicknameField, "Try with '" + uniqueNickname + "'");
    			nickname = uniqueNickname;
    		}
    	} catch (JsonRpcClientException e) {
    		if (e.getCode() == ErrorCodes.NON_UNIQUE_INPUT) {
    			registerForm.recordError(nicknameField, "Nickname is already used.");
    		} else {
    			throw e;
    		}
    	}
    	
    	if (!registerForm.isValid()) {
    		return;
    	}
    	
    	try {
    		userService.create(email, nickname, password);
    	} catch (JsonRpcClientException e) {
    		if (e.getCode() == ErrorCodes.DUPLICATE_KEY) {
    			registerForm.recordError(emailField, "EMail is already registred.");
    		} else if (e.getCode() == ErrorCodes.INVALID_INPUT) {
    			registerForm.recordError(emailField, "EMail is invalid.");
    		} else {
    			throw e;
    		}
		 }
    }

    /**
     * Validation passed, so we'll go to the "PostLogin" page
     */
    Object onSuccess()
    {
    	displayState.showDialog("loginModal");
    	displayState.showTab("loginModalTab", "login");
    	return Index.class;
    }
    
    Object onFailure() {
    	displayState.showDialog("loginModal");
    	displayState.showTab("loginModalTab", "register");
    	return Index.class;
    }
}
