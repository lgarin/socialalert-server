package com.bravson.socialalert.pages;

import org.apache.tapestry5.annotations.ActivationRequestParameter;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.annotations.SetupRender;
import org.apache.tapestry5.ioc.annotations.Inject;

import com.bravson.socialalert.common.domain.UserInfo;
import com.bravson.socialalert.common.facade.UserFacade;
import com.bravson.socialalert.services.DisplayState;
import com.googlecode.jsonrpc4j.JsonRpcClientException;

public class Activation {

	@SessionState
	private DisplayState displayState;
	
	@ActivationRequestParameter("token")
	private String token;
	
	@SessionState(create=false)
    private UserInfo userInfo;
	
	@Inject
    private UserFacade userService;
	
	@Property
	private boolean success;
	
	 @SetupRender
	 void setupRender() throws Exception {
		 if (userInfo == null) {
			displayState.setNextPage(this);
			displayState.showDialog("loginModal");
		    displayState.showTab("loginModalTab", "login");
		 } else {
			 try {
				 userService.activateUser(token);
				 success = true;
			 } catch (JsonRpcClientException e) {
				 success = false;
			 }
		 }
	 }
}
