package com.bravson.socialalert.app.services;

import java.net.URI;
import java.util.UUID;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.bravson.socialalert.app.entities.AbuseReport;
import com.bravson.socialalert.app.repositories.AbuseReportRepository;
import com.bravson.socialalert.common.domain.AbuseInfo;
import com.bravson.socialalert.common.domain.AbuseReason;
import com.bravson.socialalert.common.domain.CommentInfo;
import com.bravson.socialalert.common.domain.MediaInfo;

@Service
public class AbuseReportServiceImpl implements AbuseReportService {

	@Resource
	private AbuseReportRepository abuseRepository;
	
	@Resource
	private AlertMediaService mediaService;
	
	@Resource
	private AlertCommentService commentService;
	
	@Override
	public AbuseInfo reportAbusiveComment(UUID commentId, UUID profileId, String country, AbuseReason reason) {
		CommentInfo comment = commentService.getCommentInfo(commentId);
		AbuseInfo result = abuseRepository.save(new AbuseReport(commentId, comment.getProfileId(), profileId, country, reason)).toAbuseInfo();
		result.setMessage(comment.getComment());
		return result;
	}
	
	@Override
	public AbuseInfo reportAbusiveMedia(URI mediaUri, UUID profileId, String country, AbuseReason reason) {
		MediaInfo picture = mediaService.getMediaInfo(mediaUri);
		return abuseRepository.save(new AbuseReport(mediaUri, picture.getProfileId(), profileId, country, reason)).toAbuseInfo();
	}
}
