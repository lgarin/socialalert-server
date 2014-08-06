package com.bravson.socialalert.app.services;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;

@Service
@Scope(value = "session", proxyMode = ScopedProxyMode.INTERFACES)
public class UserSessionServiceImpl implements UserSessionService, Externalizable {

	private Set<URI> viewedUriSet = new HashSet<>();
	private Set<URI> repostedUriSet = new HashSet<>();
	private Set<UUID> repostedCommentSet = new HashSet<>();
	
	@Override
	public boolean addViewedUri(URI uri) {
		return viewedUriSet.add(uri);
	}
	
	@Override
	public boolean addRepostedUri(URI uri) {
		return repostedUriSet.add(uri);
	}
	
	@Override
	public boolean addRepostedComment(UUID commentId) {
		return repostedCommentSet.add(commentId);
	}
	
	@Override
	public void clearAll() {
		viewedUriSet.clear();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		viewedUriSet = (Set<URI>) in.readObject();
	}
	
	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(viewedUriSet);
	}
}
