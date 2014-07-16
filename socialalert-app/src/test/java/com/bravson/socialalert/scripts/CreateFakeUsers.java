package com.bravson.socialalert.scripts;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.UUID;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HeaderIterator;
import org.apache.http.ProtocolVersion;
import org.apache.http.RequestLine;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.params.HttpParams;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.web.client.RestTemplate;

import com.bravson.socialalert.app.entities.ApplicationUser;
import com.bravson.socialalert.app.entities.UserProfile;
import com.bravson.socialalert.app.services.PasswordService;
import com.bravson.socialalert.infrastructure.SimpleServiceTest;

public class CreateFakeUsers extends SimpleServiceTest {

	@Resource
	private VelocityEngine velocityEngine;
	
	@Resource
	private PasswordService passwordService; 
	
	@Test
	@Ignore
	public void createFakeUsers() throws ClientProtocolException, IOException, URISyntaxException {
		for (int i = 4; i < 20; i++) {
			ApplicationUser user = new ApplicationUser("user"+i + "@test.com", "user"+i, "123");
			user.changePassword(passwordService.encodePassword(user, user.getPassword()));
			UserProfile profile = new UserProfile(user.getNickname());
			user.activate(profile);
			executeRequest("http://gd04b:18789/socialalert-data/ApplicationUser/update?commit=true", renderUser(user));
			executeRequest("http://gd04b:18789/socialalert-data/UserProfile/update?commit=true", renderProfile(profile));
		}
	}
	
	private void executeRequest(String url, String content) throws ClientProtocolException, IOException, URISyntaxException {
		System.out.println(content);
		CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		HttpPost httpPost = new HttpPost(new URI(url));
		httpPost.setEntity(new StringEntity(content, ContentType.TEXT_XML));
		StatusLine status = httpClient.execute(httpPost).getStatusLine();
		assertEquals(200, status.getStatusCode());
		httpClient.close();
	}
	
	private String renderUser(ApplicationUser user) {
		Template template = velocityEngine.getTemplate("com/bravson/socialalert/scripts/user.vm");
		StringWriter writer = new StringWriter(2000);
		VelocityContext context = new VelocityContext();
		context.put("user", user);
		template.merge(context, writer);
		return writer.toString();
	}
	
	private String renderProfile(UserProfile profile) {
		Template template = velocityEngine.getTemplate("com/bravson/socialalert/scripts/profile.vm");
		StringWriter writer = new StringWriter(2000);
		VelocityContext context = new VelocityContext();
		context.put("profile", profile);
		template.merge(context, writer);
		return writer.toString();
	}
}

