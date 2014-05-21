package com.bravson.socialalert.app.services;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.openid4java.association.AssociationException;
import org.openid4java.consumer.ConsumerException;
import org.openid4java.consumer.ConsumerManager;
import org.openid4java.consumer.VerificationResult;
import org.openid4java.discovery.DiscoveryException;
import org.openid4java.discovery.DiscoveryInformation;
import org.openid4java.message.AuthFailure;
import org.openid4java.message.AuthImmediateFailure;
import org.openid4java.message.AuthRequest;
import org.openid4java.message.AuthSuccess;
import org.openid4java.message.MessageException;
import org.openid4java.message.ParameterList;
import org.openid4java.message.ax.AxMessage;
import org.openid4java.message.ax.FetchRequest;
import org.openid4java.message.ax.FetchResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.openid.AuthenticationCancelledException;
import org.springframework.security.openid.OpenIDAttribute;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.security.web.authentication.www.NonceExpiredException;
import org.springframework.stereotype.Service;

import com.bravson.socialalert.app.domain.ExternalProfileInfo;

@Service
public class OpenIdAuthenticationServiceImpl implements OpenIdAuthenticationService {

    private ConsumerManager consumerManager = new ConsumerManager();
    
    private List<OpenIDAttribute> attributesToFetch;
    
    @Value("${nonce.max.age}")
    private int nonceMaxAge;
    
    private static OpenIDAttribute createRequiredAttribute(String name, String type) {
    	OpenIDAttribute attribute = new OpenIDAttribute(name, type);
    	attribute.setRequired(true);
    	return attribute;
    }
    
    @PostConstruct
    protected void init() {
    	attributesToFetch = Arrays.asList(
    			createRequiredAttribute("email", "http://axschema.org/contact/email"),
    			createRequiredAttribute("nickname", "http://axschema.org/namePerson/friendly"),
    			new OpenIDAttribute("firstname", "http://openid.net/schema/namePerson/first"),
    			new OpenIDAttribute("lastname", "http://openid.net/schema/namePerson/last"),
    			new OpenIDAttribute("birthday", "http://openid.net/schema/birthDate/birthday"),
    			new OpenIDAttribute("gender", "http://openid.net/schema/gender"),
    			new OpenIDAttribute("language", "http://openid.net/schema/language/pref"),
    			new OpenIDAttribute("country", "http://openid.net/schema/contact/country/home"),
    			new OpenIDAttribute("image", "http://openid.net/schema/media/image"));
    	consumerManager.setMaxNonceAge(nonceMaxAge);
    }

    @SuppressWarnings("unchecked")
	@Override
	public URL beginOpenIdConsumption(URL identityUrl, URL returnToUrl) {
        List<DiscoveryInformation> discoveries;

        try {
            discoveries = consumerManager.discover(identityUrl.toString());
        } catch (DiscoveryException e) {
            throw new AuthenticationServiceException("Error during discovery", e);
        }

        DiscoveryInformation information = consumerManager.associate(discoveries);

        AuthRequest authReq;

        try {
            authReq = consumerManager.authenticate(information, returnToUrl.toString());

            if (!attributesToFetch.isEmpty()) {
                FetchRequest fetchRequest = FetchRequest.createFetchRequest();
                for (OpenIDAttribute attr : attributesToFetch) {
                    fetchRequest.addAttribute(attr.getName(), attr.getType(), attr.isRequired(), attr.getCount());
                }
                authReq.addExtension(fetchRequest);
            }
        } catch (MessageException e) {
            throw new AuthenticationServiceException("Error processing ConsumerManager authentication", e);
        } catch (ConsumerException e) {
            throw new AuthenticationServiceException("Error processing ConsumerManager authentication", e);
        }

        PreAuthenticatedAuthenticationToken preAuth = new PreAuthenticatedAuthenticationToken(identityUrl, returnToUrl);
        preAuth.setDetails(information);
		SecurityContextHolder.getContext().setAuthentication(preAuth);
        
        try {
			return new URL(authReq.getDestinationUrl(true));
		} catch (MalformedURLException e) {
			throw new AuthenticationServiceException("Cannot build return url", e);
		}
    }
    
    // TODO check is something equivalent is already available
    private static <T> T as(Object object, Class<T> type) {
    	  return type.isInstance(object) ? type.cast(object) : null;
    }

    @Override
	public ExternalProfileInfo endOpenIdConsumption(URL receivingUrl) {
        
    	PreAuthenticatedAuthenticationToken preAuth = as(SecurityContextHolder.getContext().getAuthentication(), PreAuthenticatedAuthenticationToken.class);
    	if (preAuth == null) {
    		throw new AuthenticationCredentialsNotFoundException("PreAuthenticatedAuthenticationToken is not available. Possible causes are lost session or replay attack");
    	}
    	SecurityContextHolder.clearContext();
    	
        ParameterList openidResp;
		try {
			openidResp = ParameterList.createFromQueryString(StringUtils.defaultString(receivingUrl.getQuery()));
		} catch (MessageException e) {
			throw new IllegalArgumentException("Cannot parse query parameters", e);
		}

        // retrieve the previously stored discovery information
        DiscoveryInformation discovered = as(preAuth.getDetails(), DiscoveryInformation.class);
        if (discovered == null) {
            throw new AuthenticationCredentialsNotFoundException("DiscoveryInformation is not available. Possible causes are lost session or replay attack");
        }

        // verify the response
        VerificationResult verification;
        try {
            verification = consumerManager.verify(receivingUrl.toString(), openidResp, discovered);
        } catch (MessageException e) {
            throw new AuthenticationServiceException("Error verifying openid response", e);
        } catch (DiscoveryException e) {
            throw new AuthenticationServiceException("Error verifying openid response", e);
        } catch (AssociationException e) {
            throw new AuthenticationServiceException("Error verifying openid response", e);
        }

        if (verification.getAuthResponse() instanceof AuthImmediateFailure) {
        	throw new AuthenticationServiceException(verification.getStatusMsg());
        } else if (verification.getAuthResponse() instanceof AuthFailure) {
        	throw new AuthenticationCancelledException(verification.getStatusMsg());
        } else if (verification.getAuthResponse() instanceof AuthSuccess) {
        	if ("Nonce verification failed.".equals(verification.getStatusMsg())) {
        		throw new NonceExpiredException(verification.getStatusMsg());
        	} else if (verification.getVerifiedId() == null) {
        		throw new BadCredentialsException(verification.getStatusMsg());
        	}
        	return readOpenIdInformation((AuthSuccess) verification.getAuthResponse());
        } else {
        	throw new AuthenticationServiceException("Unexpected response " + verification.getAuthResponse());
        }
    }

    private ExternalProfileInfo readOpenIdInformation(AuthSuccess authSuccess) {

        if (!authSuccess.hasExtension(AxMessage.OPENID_NS_AX)) {
        	throw new AuthenticationServiceException("No AX attributes returned");
        }

        ExternalProfileInfo result = new ExternalProfileInfo();
        try {
        	result.setIdentifier(authSuccess.getIdentity());
        	FetchResponse fetchResp = as(authSuccess.getExtension(AxMessage.OPENID_NS_AX), FetchResponse.class);
            if (fetchResp != null) {
                result.setEmail(fetchResp.getAttributeValue("email"));
                result.setNickname(fetchResp.getAttributeValue("nickname"));
                result.setLastname(fetchResp.getAttributeValue("lastname"));
                result.setFirstname(fetchResp.getAttributeValue("firstname"));
                result.setLanguage(fetchResp.getAttributeValue("language"));
                result.setCountry(fetchResp.getAttributeValue("country"));
                result.setBirthdate(fetchResp.getAttributeValue("birthay"));
                result.setGender(fetchResp.getAttributeValue("gender"));
                result.setImage(fetchResp.getAttributeValue("image"));
            }
        } catch (MessageException | DiscoveryException e) {
            throw new AuthenticationServiceException("Attribute retrieval failed", e);
        }
        
        return result;
    }

}
