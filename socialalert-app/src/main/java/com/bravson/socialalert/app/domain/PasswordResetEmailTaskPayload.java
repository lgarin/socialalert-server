package com.bravson.socialalert.app.domain;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.bravson.socialalert.app.services.PasswordResetEmailTask;

@XmlRootElement(name="passwordResetEmail")
public class PasswordResetEmailTaskPayload extends BaseTaskPayload<PasswordResetEmailTask> {

	private static final long serialVersionUID = 1L;
	
	@XmlElement
	public String email;
	
	public PasswordResetEmailTaskPayload() {
	}
	
	public PasswordResetEmailTaskPayload(String email) {
		this.email = email;
	}
	
	@Override
	public String toString() {
		return new ToStringBuilder(this).append("email", email).build();
	}
}
