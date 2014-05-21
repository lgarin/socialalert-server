package com.bravson.socialalert.components;

import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.SessionState;

import com.bravson.socialalert.services.DisplayState;

/**
 * Layout component for pages of application socialalert.
 */
@Import(stylesheet={"context:layout/layout.css", "context:css/bootstrap.css", "context:css/custom.css", "context:css/bootstrap-responsive.css"},
library={"context:js/bootstrap.min.js"})
public class Layout
{
	@SessionState(create=false)
	private DisplayState displayState;
	
	private boolean displayStateExists;
	
	public String getActiveDialog() {
		return displayStateExists ? displayState.getActiveDialog() : null;
	}

	public String getActiveNavTab() {
		return displayStateExists ? displayState.getActiveNavTab() : null;
	}

	public String getActiveTab() {
		return displayStateExists ? displayState.getActiveTab() : null;
	}
}
