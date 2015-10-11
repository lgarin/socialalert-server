package com.bravson.socialalert.app.services;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.mail.MessagingException;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.validator.internal.constraintvalidators.hv.EmailValidator;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import com.bravson.socialalert.app.exceptions.SystemExeption;

public class EmailServiceImpl implements EmailService {

	private final String[] SERVER_ATTRIBUTES = {"SRV"};
	
	private DirContext dnsContext;
	
	private EmailValidator validator = new EmailValidator();
	
	@Resource
	private JavaMailSender mailSender;
	
	private Logger logger = Logger.getLogger(getClass());
	
	@PostConstruct
	protected void init() throws NamingException {
		dnsContext = new InitialDirContext();
	}
	
	@Override
	public boolean isValidEmailAddress(String emailAddress) {
		if (!validator.isValid(emailAddress, null)) {
			return false;
		}
		
		String domain = StringUtils.substringAfterLast(emailAddress, "@");
		if (StringUtils.isEmpty(domain)) {
			return false;
		}
		try {
			Attributes attrs = dnsContext.getAttributes("dns:/" + domain, SERVER_ATTRIBUTES);
			return attrs != null;
		} catch (NamingException e) {
			return false;
		}
	}
	
	@Override
	public void sendEmail(String senderAddress, String receiverAddress, String subject, String htmlContent) {
		MimeMessageHelper message =  new MimeMessageHelper(mailSender.createMimeMessage());
		try {
			message.setFrom(senderAddress);
			message.setSubject(subject);
			message.setTo(receiverAddress);
			message.setText(htmlContent, true);
		} catch (MessagingException e) {
			throw new SystemExeption("Cannot prepare email", e);
		}
		mailSender.send(message.getMimeMessage());
		logger.info("Sent email to " + receiverAddress);
	}
}
