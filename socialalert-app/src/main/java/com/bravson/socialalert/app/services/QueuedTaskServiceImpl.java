package com.bravson.socialalert.app.services;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Resource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.PageRequest;
import org.springframework.oxm.Marshaller;
import org.springframework.oxm.Unmarshaller;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bravson.socialalert.app.domain.BaseTaskPayload;
import com.bravson.socialalert.app.entities.QueuedTask;
import com.bravson.socialalert.app.exceptions.SystemExeption;
import com.bravson.socialalert.app.infrastructure.BackgroundTask;
import com.bravson.socialalert.app.repositories.QueuedTaskRepository;

@Service
public class QueuedTaskServiceImpl implements QueuedTaskService {
	
	private static final int PAYLOAD_BUFFER_SIZE = 1024;

	@Resource
	private QueuedTaskRepository taskRepository;
	
	@Resource
	ApplicationContext context;
	
	@Resource
	private Marshaller marshaller;
	
	@Resource
	private Unmarshaller unmarshaller;
	
	@Value("${task.stalled.delay}")
	private long stalledDelay;
	
	@Transactional(rollbackFor={Throwable.class})
	public List<BaseTaskPayload<?>> resetStalledTasks(int pageSize) {
		List<QueuedTask> tasks = taskRepository.listStalledTasks(stalledDelay, new PageRequest(0, pageSize));
		if (tasks.isEmpty()) {
			return Collections.emptyList();
		}
		
		tasks = taskRepository.lockAll(tasks);
		final List<BaseTaskPayload<?>> payloads = new ArrayList<>(tasks.size());
		for (QueuedTask task : tasks) {
			payloads.add(rebuildPayload(task));
		}
		taskRepository.save(tasks);
		return payloads;
	}
	
	private BaseTaskPayload<?> rebuildPayload(QueuedTask queuedTask) {
		String xmlPayload = queuedTask.resubmit();
		BaseTaskPayload<?> payload;
		try {
			payload = (BaseTaskPayload<?>) unmarshaller.unmarshal(new StreamSource(new StringReader(xmlPayload)));
		} catch (IOException e) {
			throw new SystemExeption("Cannot unmarshal payload " + xmlPayload, e);
		}
		payload.init(queuedTask.getId(), context.getBean(QueuedTaskService.class));
		return payload;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Transactional(rollbackFor={Throwable.class})
	@Override
	public void executeTask(BaseTaskPayload<?> taskPayload) {
		QueuedTask queuedTask = taskRepository.lockById(taskPayload.getQueuedTaskId());
		if (queuedTask == null || !queuedTask.isStarted()) {
			throw new IllegalStateException("The task " + taskPayload.getQueuedTaskId() + " has not been started");
		}
		BackgroundTask task;
		try {
			task = context.getBean(taskPayload.getTaskClass());
		} catch (ClassNotFoundException e) {
			throw new SystemExeption("Cannot find service for " + taskPayload.getClass(), e);
		}
		task.execute(taskPayload);
		taskRepository.delete(taskPayload.getQueuedTaskId());
	}
	
	@Transactional(rollbackFor={Throwable.class})
	@Override
	public boolean beginTask(BaseTaskPayload<?> taskPayload) {
		QueuedTask queuedTask = taskRepository.lockById(taskPayload.getQueuedTaskId());
		if (queuedTask == null || !queuedTask.startProcessing()) {
			return false;
		}
		taskRepository.save(queuedTask);
		return true;
	}

	@Transactional(rollbackFor={Throwable.class})
	@Override
	public void enqueueTask(final BaseTaskPayload<?> payload) {
		StringWriter writer = new StringWriter(PAYLOAD_BUFFER_SIZE);
		try {
			marshaller.marshal(payload, new StreamResult(writer));
		} catch (IOException e) {
			throw new SystemExeption("Cannot unmarshal " + payload.getClass(), e);
		}
		QueuedTask task = new QueuedTask(payload.getTrigger(), writer.toString());
		payload.init(task.getId(), context.getBean(QueuedTaskService.class));
		taskRepository.save(task);
	}
}
