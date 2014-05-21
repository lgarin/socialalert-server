package com.bravson.socialalert.services;

import java.io.IOException;

import org.apache.log4j.MDC;
import org.apache.tapestry5.services.ApplicationStateManager;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.RequestFilter;
import org.apache.tapestry5.services.RequestHandler;
import org.apache.tapestry5.services.Response;

import com.bravson.socialalert.common.domain.UserInfo;

public class RequestLoggingFilter implements RequestFilter {

	private ApplicationStateManager asm;
	
	public RequestLoggingFilter(ApplicationStateManager asm) {
		this.asm = asm;
	}

	public boolean service(Request request, Response response, RequestHandler handler) throws IOException {
		UserInfo userInfo = asm.getIfExists(UserInfo.class);
		if (userInfo != null) {
			MDC.put("user", userInfo.getNickname());
		} else {
			MDC.put("user", request.getRemoteHost());
		}
		try {
			return handler.service(request, response);
		} finally {
			MDC.remove("user");
		}
	}

}
