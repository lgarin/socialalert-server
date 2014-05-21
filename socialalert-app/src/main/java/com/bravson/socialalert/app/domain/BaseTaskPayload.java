package com.bravson.socialalert.app.domain;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.UUID;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import com.bravson.socialalert.app.infrastructure.BackgroundTask;
import com.bravson.socialalert.app.services.QueuedTaskService;

public abstract class BaseTaskPayload<T extends BackgroundTask<?>> implements Serializable, Runnable {
	
	private static final long serialVersionUID = 1L;
	
	private transient UUID queuedTaskId;
	private transient DateTime trigger;
	private transient QueuedTaskService taskService;
	
	public BaseTaskPayload() {
		this(DateTime.now(DateTimeZone.UTC));
	}
	
	protected BaseTaskPayload(DateTime trigger) {
		this.trigger = trigger;
	}
	
	public void init(UUID queuedTaskId, QueuedTaskService taskService) {
		this.queuedTaskId = queuedTaskId;
		this.taskService = taskService;
	}
	
	public UUID getQueuedTaskId() {
		return queuedTaskId;
	}
	
	public DateTime getTrigger() {
		return trigger;
	}

    @SuppressWarnings("unchecked")
	private Class<T> resolveTaskClassFromGernericType() {
		ParameterizedType parameterizedType = resolveTaskClassFromGernericType(getClass());
		return (Class<T>) parameterizedType.getActualTypeArguments()[0];
	}

	private ParameterizedType resolveTaskClassFromGernericType(Class<?> clazz) {
		Object genericSuperclass = clazz.getGenericSuperclass();
		if (genericSuperclass instanceof ParameterizedType) {
			ParameterizedType parameterizedType = (ParameterizedType) genericSuperclass;
			Type rawtype = parameterizedType.getRawType();
			if (BaseTaskPayload.class.equals(rawtype)) {
				return parameterizedType;
			}
		}
		return resolveTaskClassFromGernericType(clazz.getSuperclass());
	}
    
	public Class<T> getTaskClass() throws ClassNotFoundException {
    	return resolveTaskClassFromGernericType();
    }
	
	@Override
	public final void run() {
		if (taskService.beginTask(this)) {
			taskService.executeTask(this);
		}
	}
}
