package com.bravson.socialalert.app.facades;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.bravson.socialalert.common.facade.ControlFacade;

@Service
@Validated
public class ControlFacadeImpl implements ControlFacade {

	private static final String ENUM_PACKAGE = "com.bravson.socialalert.common.domain";
	
	@SuppressWarnings("unchecked")
	private Class<? extends Enum<?>> getEnumClass(String enumName) {
		try {
			return (Class<? extends Enum<?>>) Class.forName(ENUM_PACKAGE + "." + enumName);
		} catch (ClassNotFoundException e) {
			return null;
		}
	}
	
	@Override
	public List<String> listValidValues(String valueSet) {
		Class<? extends Enum<?>> enumClass = getEnumClass(valueSet);
		if (enumClass == null) {
			return Collections.emptyList();
		}
		Enum<?>[] enumValues = enumClass.getEnumConstants();
		if (enumValues == null) {
			return Collections.emptyList();
		}
		ArrayList<String> result = new ArrayList<>(enumValues.length);
		for (int i = 0; i < enumValues.length; i++) {
			result.add(enumValues[i].name());
		}
		return result;
	}
}
