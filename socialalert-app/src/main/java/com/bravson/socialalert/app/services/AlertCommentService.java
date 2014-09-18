package com.bravson.socialalert.app.services;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.validation.annotation.Validated;

import com.bravson.socialalert.common.domain.ActivityInfo;
import com.bravson.socialalert.common.domain.CommentInfo;
import com.bravson.socialalert.common.domain.QueryResult;

@Validated
public interface AlertCommentService {

	public CommentInfo addComment(@NotNull URI mediaUri, @NotNull UUID profileId, @NotEmpty String comment);

	public QueryResult<CommentInfo> searchCommentByMediaUri(@NotNull URI mediaUri, @Min(0) int pageNumber, @Min(1) int pageSize);

	public void populateComments(@NotNull List<ActivityInfo> items);

	public CommentInfo getCommentInfo(@NotNull UUID commentId);
}
