package com.bravson.socialalert.app.domain;

import javax.xml.bind.annotation.XmlRegistry;

@XmlRegistry
public interface ObjectFactory {

	ActivationEmailTaskPayload createActivationEmail();
	
	PasswordResetEmailTaskPayload createPasswordResetEmail();
	
	ArchiveMediaTaskPayload createArchiveMediaTask();
	
	DeleteMediaTaskPayload createDeleteMediaTask();
	
	StoreProfilePictureTaskPayload createStoreProfilePicture();
}
