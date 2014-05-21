package com.bravson.socialalert.pages;

import java.io.IOException;

import org.apache.tapestry5.annotations.ActivationRequestParameter;
import org.apache.tapestry5.annotations.Component;
import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.beaneditor.Validate;
import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.corelib.components.TextField;
import org.apache.tapestry5.ioc.annotations.Inject;

import com.bravson.socialalert.common.domain.ErrorCodes;
import com.bravson.socialalert.common.facade.UserFacade;
import com.googlecode.jsonrpc4j.JsonRpcClientException;

public class InitiateResetPassword {

	@ActivationRequestParameter
	@Validate("required")
	@Property
    private String email;
	
	@InjectComponent("email")
    private TextField emailField;
	
	@Component(parameters={"clientValidation=SUBMIT"})
    private Form resetForm;
	
	@Inject
    private UserFacade userService;
	
	void onValidateFromResetForm() throws IOException {
    	
    	try {
    		userService.initiatePasswordReset(email);
    	} catch (JsonRpcClientException e) {
    		if (e.getCode() == ErrorCodes.USER_NOT_FOUND) {
    			resetForm.recordError(emailField, "EMail is not registred.");
    		} else {
    			throw e;
    		}
		 }
    }
	
	Object onSuccess() {
		return Index.class;
    }

	public void activate(String email) {
		this.email = email;
	}
}
