package com.bravson.socialalert.services;

import java.net.MalformedURLException;
import java.net.URL;

import javax.annotation.Resource;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.security.authentication.AuthenticationServiceException;

import com.bravson.socialalert.app.services.OAuthAuthenicationService;
import com.bravson.socialalert.infrastructure.SimpleServiceTest;

@Ignore
public class OAuthAuthenicationServiceTest extends SimpleServiceTest {

	@Resource
	private OAuthAuthenicationService service;
	

	@Test
	public void beginGoogleLogin() throws MalformedURLException {
		URL url = service.beginOAuthConsumption("google", new URL("http://opensource.brickred.com:9092/loginSuccess"));
		assertNotNull(url);
		assertTrue(url.toString().startsWith("https://www.google.com/accounts/o8/ud"));
	}
	
	@Test(expected=AuthenticationServiceException.class)
	public void completeGoogleLoginWithoutBegin() throws MalformedURLException {
		URL response = new URL("http://opensource.brickred.com:9092/loginSuccess?openid.ns=http%3A%2F%2Fspecs.openid.net%2Fauth%2F2.0&openid.mode=id_res&openid.op_endpoint=https%3A%2F%2Fwww.google.com%2Faccounts%2Fo8%2Fud&openid.response_nonce=2013-08-11T13%3A10%3A44ZGmWUbMZGFrbsaw&openid.return_to=http%3A%2F%2Fopensource.brickred.com%3A9092%2FloginSuccess&openid.assoc_handle=1.AMlYA9UKgNlONk6w4O9Rif71eZe0toORjDF_RzvImbD84QfRSDLLMGhZpprgHg&openid.signed=op_endpoint%2Cclaimed_id%2Cidentity%2Creturn_to%2Cresponse_nonce%2Cassoc_handle%2Cns.ext1%2Cns.ext2%2Cext1.mode%2Cext1.type.firstname%2Cext1.value.firstname%2Cext1.type.lastname%2Cext1.value.lastname%2Cext1.type.language%2Cext1.value.language%2Cext1.type.email%2Cext1.value.email%2Cext1.type.country%2Cext1.value.country%2Cext2.scope%2Cext2.request_token&openid.sig=HbSicGAgT0jxZFnB6GuykQPhY2Y%3D&openid.identity=https%3A%2F%2Fwww.google.com%2Faccounts%2Fo8%2Fid%3Fid%3DAItOawmAT7ZajNxelXOeBG-9x9S3nbrmZJg7QJ0&openid.claimed_id=https%3A%2F%2Fwww.google.com%2Faccounts%2Fo8%2Fid%3Fid%3DAItOawmAT7ZajNxelXOeBG-9x9S3nbrmZJg7QJ0&openid.ns.ext1=http%3A%2F%2Fopenid.net%2Fsrv%2Fax%2F1.0&openid.ext1.mode=fetch_response&openid.ext1.type.firstname=http%3A%2F%2Faxschema.org%2FnamePerson%2Ffirst&openid.ext1.value.firstname=Lucien&openid.ext1.type.lastname=http%3A%2F%2Faxschema.org%2FnamePerson%2Flast&openid.ext1.value.lastname=Garin&openid.ext1.type.language=http%3A%2F%2Faxschema.org%2Fpref%2Flanguage&openid.ext1.value.language=en&openid.ext1.type.email=http%3A%2F%2Faxschema.org%2Fcontact%2Femail&openid.ext1.value.email=lgarin%40gmx.ch&openid.ext1.type.country=http%3A%2F%2Faxschema.org%2Fcontact%2Fcountry%2Fhome&openid.ext1.value.country=CH&openid.ns.ext2=http%3A%2F%2Fspecs.openid.net%2Fextensions%2Foauth%2F1.0&openid.ext2.scope=https%3A%2F%2Fwww.google.com%2Fm8%2Ffeeds%2F&openid.ext2.request_token=4%2F5XurZgG51rQvYoco5KD2pMJvyrAL.Aqq_G3bNb3kZOl05ti8ZT3YjzwPbgAI");
		service.endOAuthConsumption(response);
	}
	
	@Test(expected=AuthenticationServiceException.class)
	public void doGoogleLoginWithOldResponse()  throws MalformedURLException {
		service.beginOAuthConsumption("google", new URL("http://opensource.brickred.com:9092/loginSuccess"));
		URL response = new URL("http://opensource.brickred.com:9092/loginSuccess?openid.ns=http%3A%2F%2Fspecs.openid.net%2Fauth%2F2.0&openid.mode=id_res&openid.op_endpoint=https%3A%2F%2Fwww.google.com%2Faccounts%2Fo8%2Fud&openid.response_nonce=2013-08-11T13%3A10%3A44ZGmWUbMZGFrbsaw&openid.return_to=http%3A%2F%2Fopensource.brickred.com%3A9092%2FloginSuccess&openid.assoc_handle=1.AMlYA9UKgNlONk6w4O9Rif71eZe0toORjDF_RzvImbD84QfRSDLLMGhZpprgHg&openid.signed=op_endpoint%2Cclaimed_id%2Cidentity%2Creturn_to%2Cresponse_nonce%2Cassoc_handle%2Cns.ext1%2Cns.ext2%2Cext1.mode%2Cext1.type.firstname%2Cext1.value.firstname%2Cext1.type.lastname%2Cext1.value.lastname%2Cext1.type.language%2Cext1.value.language%2Cext1.type.email%2Cext1.value.email%2Cext1.type.country%2Cext1.value.country%2Cext2.scope%2Cext2.request_token&openid.sig=HbSicGAgT0jxZFnB6GuykQPhY2Y%3D&openid.identity=https%3A%2F%2Fwww.google.com%2Faccounts%2Fo8%2Fid%3Fid%3DAItOawmAT7ZajNxelXOeBG-9x9S3nbrmZJg7QJ0&openid.claimed_id=https%3A%2F%2Fwww.google.com%2Faccounts%2Fo8%2Fid%3Fid%3DAItOawmAT7ZajNxelXOeBG-9x9S3nbrmZJg7QJ0&openid.ns.ext1=http%3A%2F%2Fopenid.net%2Fsrv%2Fax%2F1.0&openid.ext1.mode=fetch_response&openid.ext1.type.firstname=http%3A%2F%2Faxschema.org%2FnamePerson%2Ffirst&openid.ext1.value.firstname=Lucien&openid.ext1.type.lastname=http%3A%2F%2Faxschema.org%2FnamePerson%2Flast&openid.ext1.value.lastname=Garin&openid.ext1.type.language=http%3A%2F%2Faxschema.org%2Fpref%2Flanguage&openid.ext1.value.language=en&openid.ext1.type.email=http%3A%2F%2Faxschema.org%2Fcontact%2Femail&openid.ext1.value.email=lgarin%40gmx.ch&openid.ext1.type.country=http%3A%2F%2Faxschema.org%2Fcontact%2Fcountry%2Fhome&openid.ext1.value.country=CH&openid.ns.ext2=http%3A%2F%2Fspecs.openid.net%2Fextensions%2Foauth%2F1.0&openid.ext2.scope=https%3A%2F%2Fwww.google.com%2Fm8%2Ffeeds%2F&openid.ext2.request_token=4%2F5XurZgG51rQvYoco5KD2pMJvyrAL.Aqq_G3bNb3kZOl05ti8ZT3YjzwPbgAI");
		service.endOAuthConsumption(response);
	}
}
