package com.bravson.socialalert.app.services;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;

import com.bravson.socialalert.app.domain.ProfileStatisticUpdate;
import com.bravson.socialalert.app.entities.AlertComment;
import com.bravson.socialalert.app.exceptions.DataMissingException;
import com.bravson.socialalert.app.repositories.AlertCommentRepository;
import com.bravson.socialalert.common.domain.ActivityInfo;
import com.bravson.socialalert.common.domain.CommentInfo;
import com.bravson.socialalert.common.domain.QueryResult;

@Service
public class AlertCommentServiceImpl implements AlertCommentService {

	@Resource
	private AlertCommentRepository commentRepository;
	
	@Resource
	private ProfileStatisticService statisticService;
	
	@Value("${query.max.result}")
	private int maxPageSize;
	
	private PageRequest createPageRequest(int pageNumber, int pageSize) {
		if (pageSize > maxPageSize) {
			throw new IllegalArgumentException("Page size is limited to " + maxPageSize);
		}
		PageRequest pageRequest = new PageRequest(pageNumber, pageSize, Direction.DESC, "creation");
		return pageRequest;
	}
	
	@Override
	public CommentInfo addComment(URI mediaUri, UUID profileId, String comment) {
		AlertComment entity = new AlertComment(mediaUri, profileId, comment);
		statisticService.updateProfileStatistic(profileId, ProfileStatisticUpdate.INCREMENT_COMMENT_COUNT);
		return commentRepository.save(entity).toCommentInfo();
	}
	
	private static QueryResult<CommentInfo> toQueryResult(Page<AlertComment> page) {
		ArrayList<CommentInfo> pageContent = new ArrayList<>(page.getSize());
		for (AlertComment comment : page) {
			pageContent.add(comment.toCommentInfo());
		}
		return new QueryResult<>(pageContent, page.getNumber(), page.getTotalPages());
	}
	
	@Override
	public QueryResult<CommentInfo> searchCommentByMediaUri(URI mediaUri, int pageNumber, int pageSize) {
		return toQueryResult(commentRepository.findByMediaUri(mediaUri, createPageRequest(pageNumber, pageSize)));
	}
	
	private List<AlertComment> getCommentsByCommentIds(Collection<UUID> commentIds) {
		if (commentIds.isEmpty()) {
			return Collections.emptyList();
		}
		return commentRepository.findByCommentIds(commentIds, new PageRequest(0, commentIds.size()));
	}
	
	@Override
	public void populateComments(List<ActivityInfo> items) {
		HashMap<UUID, AlertComment> commentIdMap = new HashMap<>(items.size());
		for (ActivityInfo item : items) {
			if (item.getCommentId() != null) {
				commentIdMap.put(item.getCommentId(), null);
			}
		}
		List<AlertComment> comments = getCommentsByCommentIds(commentIdMap.keySet());
		for (AlertComment comment : comments) {
			commentIdMap.put(comment.getId(), comment);
		}
		for (ActivityInfo item : items) {
			AlertComment comment = commentIdMap.get(item.getCommentId());
			if (comment != null) {
				item.setMessage(comment.getComment());
			}
		}
	}
	
	@Override
	public CommentInfo getCommentInfo(UUID commentId) {
		AlertComment entity = commentRepository.findById(commentId);
		if (entity == null) {
			throw new DataMissingException("Cannot find comment "  + commentId);
		}
		return entity.toCommentInfo();
	}
}
