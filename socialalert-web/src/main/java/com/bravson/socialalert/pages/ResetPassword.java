package com.bravson.socialalert.pages;

import java.io.IOException;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.tapestry5.annotations.ActivationRequestParameter;
import org.apache.tapestry5.annotations.Component;
import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.annotations.SetupRender;
import org.apache.tapestry5.beaneditor.Validate;
import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.corelib.components.PasswordField;
import org.apache.tapestry5.corelib.components.TextField;
import org.apache.tapestry5.ioc.annotations.Inject;

import com.bravson.socialalert.common.domain.ErrorCodes;
import com.bravson.socialalert.common.domain.UserConstants;
import com.bravson.socialalert.common.facade.UserFacade;
import com.bravson.socialalert.services.DisplayState;
import com.googlecode.jsonrpc4j.JsonRpcClientException;

public class ResetPassword {

	@SessionState
	private DisplayState displayState;
	
	@ActivationRequestParameter("token")
	private String token;

	@Validate("required")
	@Property
    private String email;

	@Validate("required,minLength=" + UserConstants.MIN_PASSWORD_LENGTH)
    @Property
    private String password;
    
	@Validate("required,minLength=" + UserConstants.MIN_PASSWORD_LENGTH)
    @Property
    private String password2;
    
    @InjectComponent("email")
    private TextField emailField;
    
    @InjectComponent("password2")
    private PasswordField password2Field;
	
	@Inject
    private UserFacade userService;
	
	@InjectPage
	private InitiateResetPassword startPage;
	
	@Property
	private boolean success;
	
	@Component(parameters={"clientValidation=SUBMIT"})
    private Form resetForm;

	@SetupRender
	void setupRender() {
		displayState.clear();
	}
	
	void onValidateFromResetForm() throws IOException {
    	if (ObjectUtils.notEqual(password, password2)) {
    		resetForm.recordError(password2Field, "Password does not match.");
    	}
    	try {
    		userService.resetPassword(email, token, password);
    		success = true;
    	} catch (JsonRpcClientException e) {
    		success = false;
    		if (e.getCode() == ErrorCodes.USER_NOT_FOUND) {
    			resetForm.recordError(emailField, "EMail is not registred.");
    		} else {
    			throw e;
    		}
		 }
    }
	
	Object onSuccess() {
		if (success) {
			displayState.showDialog("loginModal");
	    	displayState.showTab("loginModalTab", "login");
	    	return Index.class;
		} else {
			startPage.activate(email);
			return startPage;
		}
    }
}
