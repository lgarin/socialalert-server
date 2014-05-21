package com.bravson.socialalert.pages;

import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.upload.services.UploadedFile;

import com.bravson.socialalert.common.domain.UserRole;
import com.bravson.socialalert.services.ProtectedPage;

@ProtectedPage(allow={UserRole.USER})
public class UploadPicture {

	@Property
    private UploadedFile file;
	
    
    Object onCancel() {
    	return UserHome.class;
    }
}
