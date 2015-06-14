package com.bravson.socialalert.pages;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.apache.tapestry5.annotations.Component;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.beaneditor.Validate;
import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.ioc.annotations.Inject;

import com.bravson.socialalert.common.domain.UserInfo;
import com.bravson.socialalert.common.domain.UserRole;
import com.bravson.socialalert.common.facade.MediaFacade;
import com.bravson.socialalert.services.ProtectedPage;

@ProtectedPage(disallow={UserRole.ANONYMOUS})
public class Albums {

	@Inject
    private MediaFacade pictureService;
	
	@SessionState(create=false)
	@Property
    private UserInfo userInfo;
	
	@Component(parameters={"clientValidation=SUBMIT"})
    private Form createForm;
	
	@Property
	@Validate("required")
	private String title;
	
	@Property
	private String description;
	
	public Object onSuccessFromCreateForm() throws ClientProtocolException, IOException
    {
		pictureService.createEmptyAlbum(title, description);
		return UserHome.class;
    }
}
