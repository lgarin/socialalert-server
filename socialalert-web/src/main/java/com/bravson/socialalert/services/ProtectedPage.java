package com.bravson.socialalert.services;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.bravson.socialalert.common.domain.UserRole;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ProtectedPage {

	UserRole[] allow() default {};
	
	UserRole[] disallow() default {};
}
