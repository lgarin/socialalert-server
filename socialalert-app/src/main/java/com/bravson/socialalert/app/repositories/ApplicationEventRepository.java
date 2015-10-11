package com.bravson.socialalert.app.repositories;

import java.util.UUID;

import com.bravson.socialalert.app.entities.ApplicationEvent;
import com.bravson.socialalert.app.infrastructure.CustomBaseRepository;

public interface ApplicationEventRepository extends CustomBaseRepository<ApplicationEvent, UUID> {

}
