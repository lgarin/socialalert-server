package com.bravson.socialalert.app.tasks;

import java.util.Collections;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.bravson.socialalert.app.domain.BaseTaskPayload;
import com.bravson.socialalert.app.services.QueuedTaskService;

public class QueuedTaskSchedulerImpl implements QueuedTaskScheduler {

	@Resource
	private TaskScheduler taskScheduler;
	
	@Resource
	private QueuedTaskService taskService;
	
	@Value("${task.page.size}")
	private int pageSize;
	
	private void scheduleTasks(final List<BaseTaskPayload<?>> taskPayloads) {
		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
			@Override
			public void afterCommit() {
				for (BaseTaskPayload<?> payload : taskPayloads) {
					taskScheduler.schedule(payload, payload.getTrigger().toDate());
				}
			}
		});
	}
	
	@Transactional(rollbackFor={Throwable.class})
	@Override
	public void scheduleTask(final BaseTaskPayload<?> payload) {
		taskService.enqueueTask(payload);
		scheduleTasks(Collections.<BaseTaskPayload<?>>singletonList(payload));
	}


	@Transactional(rollbackFor={Throwable.class})
	@Override
	@Scheduled(fixedRateString="${task.stalled.delay}")
    public void runner()  {
		List<BaseTaskPayload<?>> taskPayloads = taskService.resetStalledTasks(pageSize);
		scheduleTasks(taskPayloads);
    }

}
