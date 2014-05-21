package com.bravson.socialalert.app.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.solr.repository.Query;

import com.bravson.socialalert.app.entities.QueuedTask;
import com.bravson.socialalert.app.infrastructure.CustomBaseRepository;

public interface QueuedTaskRepository extends CustomBaseRepository<QueuedTask, UUID> {

	@Query(value="*:*", filters={"started: [* TO NOW-?0MILLIS]","trigger: [* TO NOW]"})
	public List<QueuedTask> listStalledTasks(long delay, Pageable page);
}
