package com.bravson.socialalert.app.infrastructure;

import com.bravson.socialalert.app.domain.BaseTaskPayload;

public interface BackgroundTask<T extends BaseTaskPayload<?>>  {

	public void execute(T payload);
}
