package com.bravson.socialalert.services;

import java.net.MalformedURLException;
import java.net.URL;

import javax.annotation.Resource;

import org.junit.Test;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.web.authentication.www.NonceExpiredException;

import com.bravson.socialalert.app.services.OpenIdAuthenticationService;
import com.bravson.socialalert.infrastructure.SimpleServiceTest;

public class OpenIdAuthenticationServiceTest extends SimpleServiceTest {

	@Resource
	private OpenIdAuthenticationService service;
	
	@Test
	public void beginGoogleLogin() throws MalformedURLException {
		URL url = service.beginOpenIdConsumption(new URL("https://www.google.com/accounts/o8/id"), new URL("http://localhost:9092/loginSuccess"));
		assertNotNull(url);
		assertTrue(url.toString().startsWith("https://www.google.com/accounts/o8/ud"));
	}
	
	@Test(expected=AuthenticationCredentialsNotFoundException.class)
	public void completeGoogleLoginWithoutBegin() throws MalformedURLException {
		URL response = new URL("http://localhost:9092/loginSuccess?openid.ns=http%3A%2F%2Fspecs.openid.net%2Fauth%2F2.0&openid.mode=id_res&openid.op_endpoint=https%3A%2F%2Fwww.google.com%2Faccounts%2Fo8%2Fud&openid.response_nonce=2013-08-01T11%3A17%3A39ZSbaUfxkHVql7Yg&openid.return_to=http%3A%2F%2Flocalhost%3A9092%2FloginSuccess&openid.assoc_handle=1.AMlYA9X9bKCCRNBh9L9lgZDu9uKkMAVHmHip9QvkdFmBa85D9dAMCJN1oZEnIHRvrdz6zcHKwI7o7Q&openid.signed=op_endpoint%2Cclaimed_id%2Cidentity%2Creturn_to%2Cresponse_nonce%2Cassoc_handle%2Cns.ext1%2Cext1.mode%2Cext1.type.email%2Cext1.value.email&openid.sig=EFHErSIbNpqr%2BV3gVloAYpv79QyKz%2FacKX6eC%2BWzvDU%3D&openid.identity=https%3A%2F%2Fwww.google.com%2Faccounts%2Fo8%2Fid%3Fid%3DAItOawnEhgzPSmiBaaBEQlvJrkpzqfokn2pA3E4&openid.claimed_id=https%3A%2F%2Fwww.google.com%2Faccounts%2Fo8%2Fid%3Fid%3DAItOawnEhgzPSmiBaaBEQlvJrkpzqfokn2pA3E4&openid.ns.ext1=http%3A%2F%2Fopenid.net%2Fsrv%2Fax%2F1.0&openid.ext1.mode=fetch_response&openid.ext1.type.email=http%3A%2F%2Faxschema.org%2Fcontact%2Femail&openid.ext1.value.email=lgarin%40gmx.ch");
		service.endOpenIdConsumption(response);
	}
	
	@Test(expected=NonceExpiredException.class)
	public void doGoogleLoginWithOldResponse()  throws MalformedURLException {
		service.beginOpenIdConsumption(new URL("https://www.google.com/accounts/o8/id"), new URL("http://localhost:9092/loginSuccess"));
		URL response = new URL("http://localhost:9092/loginSuccess?openid.ns=http%3A%2F%2Fspecs.openid.net%2Fauth%2F2.0&openid.mode=id_res&openid.op_endpoint=https%3A%2F%2Fwww.google.com%2Faccounts%2Fo8%2Fud&openid.response_nonce=2013-08-01T11%3A17%3A39ZSbaUfxkHVql7Yg&openid.return_to=http%3A%2F%2Flocalhost%3A9092%2FloginSuccess&openid.assoc_handle=1.AMlYA9X9bKCCRNBh9L9lgZDu9uKkMAVHmHip9QvkdFmBa85D9dAMCJN1oZEnIHRvrdz6zcHKwI7o7Q&openid.signed=op_endpoint%2Cclaimed_id%2Cidentity%2Creturn_to%2Cresponse_nonce%2Cassoc_handle%2Cns.ext1%2Cext1.mode%2Cext1.type.email%2Cext1.value.email&openid.sig=EFHErSIbNpqr%2BV3gVloAYpv79QyKz%2FacKX6eC%2BWzvDU%3D&openid.identity=https%3A%2F%2Fwww.google.com%2Faccounts%2Fo8%2Fid%3Fid%3DAItOawnEhgzPSmiBaaBEQlvJrkpzqfokn2pA3E4&openid.claimed_id=https%3A%2F%2Fwww.google.com%2Faccounts%2Fo8%2Fid%3Fid%3DAItOawnEhgzPSmiBaaBEQlvJrkpzqfokn2pA3E4&openid.ns.ext1=http%3A%2F%2Fopenid.net%2Fsrv%2Fax%2F1.0&openid.ext1.mode=fetch_response&openid.ext1.type.email=http%3A%2F%2Faxschema.org%2Fcontact%2Femail&openid.ext1.value.email=lgarin%40gmx.ch");
		service.endOpenIdConsumption(response);
	}
}
