package com.bravson.socialalert.app.tasks;

import javax.validation.constraints.NotNull;

import org.springframework.validation.annotation.Validated;

import com.bravson.socialalert.app.domain.BaseTaskPayload;

@Validated
public interface QueuedTaskScheduler {

	void scheduleTask(@NotNull BaseTaskPayload<?> payload);

	void runner();
}
