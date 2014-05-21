package com.bravson.socialalert.services;

import java.io.IOException;

import org.apache.tapestry5.runtime.Component;
import org.apache.tapestry5.services.ApplicationStateManager;
import org.apache.tapestry5.services.ComponentClassResolver;
import org.apache.tapestry5.services.ComponentSource;
import org.apache.tapestry5.services.Dispatcher;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.Response;

import com.bravson.socialalert.common.domain.UserInfo;
import com.bravson.socialalert.common.domain.UserRole;

public class AccessController implements Dispatcher {
	private ApplicationStateManager asm;
	private ComponentClassResolver resolver;
	private ComponentSource componentSource;

	/**
	 * Receive all the services needed as constructor arguments. When we bind
	 * this service, T5 IoC will provide all the services !
	 */
	public AccessController(ApplicationStateManager asm, ComponentClassResolver resolver,
			ComponentSource componentSource) {
		this.asm = asm;
		this.resolver = resolver;
		this.componentSource = componentSource;
	}

	public boolean dispatch(Request request, Response response) throws IOException {
		/*
		 * We need to get the Tapestry page requested by the user. So we parse
		 * the path extracted from the request
		 */
		String path = request.getPath();
		if (path.equals(""))
			return false;

		int nextslashx = path.length();
		String pageName;

		while (true) {
			pageName = path.substring(1, nextslashx);
			if (!pageName.endsWith("/") && resolver.isPageName(pageName))
				break;
			nextslashx = path.lastIndexOf('/', nextslashx - 1);
			if (nextslashx <= 1)
				return false;
		}
		return checkAccess(pageName, request, response);
	}

	/**
	 * Check the rights of the user for the page requested
	 */
	public boolean checkAccess(String pageName, Request request, Response response) throws IOException {

		Component page = componentSource.getPage(pageName);
		if (!canAccess(page)) {
			DisplayState displayState = asm.get(DisplayState.class);
			displayState.showDialog("loginModal");
	    	displayState.showTab("loginModalTab", "login");
	    	displayState.setNextPage(page);
	    	response.sendRedirect(request.getContextPath());
			return true;
		}

		return false;
	}

	private boolean canAccess(Component page) {

		
		ProtectedPage protection = page.getClass().getAnnotation(ProtectedPage.class);

		if (protection != null) {
			return UserRole.checkPermission(asm.getIfExists(UserInfo.class), protection.allow(), protection.disallow());
		}
		return true;
	}
}
