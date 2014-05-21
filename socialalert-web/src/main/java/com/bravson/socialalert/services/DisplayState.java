package com.bravson.socialalert.services;

import org.apache.tapestry5.BaseOptimizedSessionPersistedObject;

public class DisplayState extends BaseOptimizedSessionPersistedObject {

	private String activeDialog;
	private String activeNavTab;
	private String activeTab;
	private Object nextPage;
	
	public void showDialog(String dialog) {
		this.activeDialog = dialog;
		markDirty();
	}
	
	public void showTab(String navTab, String tab) {
		this.activeNavTab = navTab;
		this.activeTab = tab;
		markDirty();
	}

	public String getActiveDialog() {
		return activeDialog;
	}

	public String getActiveNavTab() {
		return activeNavTab;
	}

	public String getActiveTab() {
		return activeTab;
	}
	
	public Object getNextPage() {
		return nextPage;
	}
	
	public void setNextPage(Object nextPage) {
		this.nextPage = nextPage;
		markDirty();
	}

	public void clear() {
		activeDialog = null;
		activeNavTab = null;
		activeTab = null;
		nextPage = null;
		markDirty();
	}
}
