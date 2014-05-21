package com.bravson.socialalert.infrastructure;

import com.bravson.socialalert.app.domain.BaseTaskPayload;
import com.bravson.socialalert.app.tasks.QueuedTaskScheduler;

public class DummyQueuedTaskScheduler implements QueuedTaskScheduler {

	@Override
	public void runner() {
	}
	
	@Override
	public void scheduleTask(BaseTaskPayload<?> payload) {
	}
}
