package com.bravson.socialalert.app.services;

import java.io.StringWriter;
import java.util.Locale;

import javax.annotation.Resource;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import com.bravson.socialalert.app.domain.PasswordResetEmailTaskPayload;
import com.bravson.socialalert.app.entities.ApplicationUser;
import com.bravson.socialalert.app.infrastructure.BackgroundTask;

@Service
public class PasswordResetEmailTask implements BackgroundTask<PasswordResetEmailTaskPayload> {

	@Resource
	private EmailService emailService;
	
	@Resource
	private ApplicationUserService userService;
	
	@Value("${password.reset.link}")
	private String resetLink;
	
	@Value("${password.reset.sender}")
	private String resetSender;
	
	@Resource
	private MessageSource messageSource;
	
	@Resource
	private VelocityEngine velocityEngine;
	
	@Override
	public void execute(PasswordResetEmailTaskPayload payload) {
		String token = userService.generatePasswordResetToken(payload.email);
		if (token == null) {
			return;
		}
		ApplicationUser user = userService.getUserByEmail(payload.email);
		String subject = messageSource.getMessage("email.password.title", null, Locale.ENGLISH);
		String content = renderTemplate(user, token);
		emailService.sendEmail(resetSender, payload.email, subject, content);
	}
	
	private String renderTemplate(ApplicationUser user, String token) {
		Template template = velocityEngine.getTemplate("template/passwordEmail.vm");
		StringWriter writer = new StringWriter(2000);
		VelocityContext context = new VelocityContext();
		context.put("link", resetLink + "?token=" + token);
		context.put("user", user);
		template.merge(context, writer);
		return writer.toString();
	}

}
