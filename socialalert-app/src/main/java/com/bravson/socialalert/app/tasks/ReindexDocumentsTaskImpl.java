package com.bravson.socialalert.app.tasks;

import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.TaskScheduler;

import com.bravson.socialalert.app.entities.AlertMedia;
import com.bravson.socialalert.app.entities.PictureAlert;
import com.bravson.socialalert.app.infrastructure.CustomBaseRepository;
import com.bravson.socialalert.app.repositories.AlertMediaRepository;
import com.bravson.socialalert.app.repositories.PictureAlertRepository;

public class ReindexDocumentsTaskImpl implements ReindexDocumentsTask, Runnable {

	private Logger logger = Logger.getLogger(getClass());
	
	@Resource
	private TaskScheduler taskScheduler;
	
	@Resource
	private ApplicationContext context;
	
	
	@PostConstruct
	protected void init() {
		taskScheduler.schedule(this, DateTime.now().plusSeconds(10).toDate());
	}
	
	@Override
	public void run() {
		reindexAll();
		migratePictures();
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public void reindexAll() {
		Map<String, CustomBaseRepository> repositoryMap = context.getBeansOfType(CustomBaseRepository.class);
		for (Map.Entry<String, CustomBaseRepository> entry : repositoryMap.entrySet()) {
			long start = System.currentTimeMillis();
			logger.info("Reindexing " + entry.getKey());
			int count = entry.getValue().reindexAll(100);
			long end = System.currentTimeMillis();
			Duration duration = new Duration(start, end);
			logger.info("Reindexed " + count + " entites in " + entry.getKey() + " in " + duration);
		}
	}
	
	@Resource
	private PictureAlertRepository pictureRepository;
	
	@Resource
	private AlertMediaRepository mediaRepository;
	
	@Override
	public void migratePictures() {
		for (PictureAlert picture : pictureRepository.findAll()) {
			if (!mediaRepository.exists(picture.getPictureUri())) {
				logger.info("Migrating " + picture.getPictureUri());
				AlertMedia media = picture.toMedia();
				mediaRepository.save(media);
			}
		}
	}
}
