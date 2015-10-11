package com.bravson.socialalert.app.services;

import java.util.UUID;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.validation.annotation.Validated;

import com.bravson.socialalert.app.entities.ApplicationEvent;

@Validated
public interface ApplicationEventService {

	ApplicationEvent createEvent(@NotNull UUID profileId, @NotEmpty String action, String parameter);
}
