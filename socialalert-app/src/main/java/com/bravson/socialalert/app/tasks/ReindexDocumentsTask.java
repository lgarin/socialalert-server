package com.bravson.socialalert.app.tasks;

import com.bravson.socialalert.app.entities.AlertMedia;
import com.bravson.socialalert.app.entities.PictureAlert;

public interface ReindexDocumentsTask {

	void reindexAll();

	public abstract void migratePictures();
}
