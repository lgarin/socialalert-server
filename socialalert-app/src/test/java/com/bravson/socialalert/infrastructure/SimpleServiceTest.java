package com.bravson.socialalert.infrastructure;

import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.runner.RunWith;
import org.springframework.aop.framework.Advised;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations="classpath:testContext.xml")
public abstract class SimpleServiceTest extends Assert {

	protected void authenticate(String userId, String... roles) {
		SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken(userId, null, roles));
	}
	
	private static Object unwrap(Object proxiedInstance) throws Exception {
	  if (proxiedInstance instanceof Advised) {
	    return unwrap(((Advised) proxiedInstance).getTargetSource().getTarget());
	  }

	  return proxiedInstance;
	}
	
	protected static void setBeanField(Object bean, String fieldName, Object fieldValue) throws Exception {
		ReflectionTestUtils.setField(unwrap(bean), fieldName, fieldValue);
	}
	
	protected static <T> T createMock(Object bean, String fieldName, Class<T> mockClass) throws Exception {
		T mock = EasyMock.createMock(fieldName, mockClass);
		setBeanField(bean, fieldName, mock);
		return mock;
	}
}
