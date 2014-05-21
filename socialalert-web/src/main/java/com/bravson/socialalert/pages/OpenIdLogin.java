package com.bravson.socialalert.pages;

import java.io.IOException;
import java.net.URL;

import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.RequestGlobals;

import com.bravson.socialalert.common.domain.UserInfo;
import com.bravson.socialalert.common.facade.UserFacade;
import com.bravson.socialalert.services.DisplayState;
import com.googlecode.jsonrpc4j.JsonRpcClientException;

public class OpenIdLogin {

	@Inject
	private UserFacade userService;
	
	@Inject
	private RequestGlobals requestGlobals;
	
	@SessionState
	private DisplayState displayState;
	
	@SessionState(create=false)
    private UserInfo userInfo;

	Object onActivate() throws IOException {
		try {
			String requestUrl = requestGlobals.getHTTPServletRequest().getRequestURL().toString();
			String queryString = requestGlobals.getHTTPServletRequest().getQueryString();
			userInfo = userService.completeOpenIdLogin(new URL(requestUrl + "?" + queryString));
			displayState.clear();
			return UserHome.class;
		} catch (JsonRpcClientException e) {
			return Index.class;
		}
	}
}
