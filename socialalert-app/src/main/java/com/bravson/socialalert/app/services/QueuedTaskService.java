package com.bravson.socialalert.app.services;

import java.util.List;

import javax.validation.constraints.NotNull;

import org.springframework.validation.annotation.Validated;

import com.bravson.socialalert.app.domain.BaseTaskPayload;

@Validated
public interface QueuedTaskService {

	List<BaseTaskPayload<?>> resetStalledTasks(int pageSize);

	boolean beginTask(@NotNull BaseTaskPayload<?> taskPayload);
	
	void executeTask(@NotNull BaseTaskPayload<?> taskPayload);

	void enqueueTask(@NotNull BaseTaskPayload<?> payload);
}
