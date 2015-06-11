package com.bravson.socialalert.common.domain;

public enum ApprovalModifier {

	LIKE {
		@Override
		public ActivityType toMediaActivtiyType() {
			return ActivityType.LIKE_MEDIA;
		}
		
		@Override
		public ActivityType toCommentActivtiyType() {
			return ActivityType.APPROVE_COMMENT;
		}
	},
	DISLIKE {
		@Override
		public ActivityType toMediaActivtiyType() {
			return ActivityType.UNLIKE_MEDIA;
		}
		
		@Override
		public ActivityType toCommentActivtiyType() {
			return ActivityType.DISAPPROVE_COMMENT;
		}
	};
	
	public static int computeLikeDelta(ApprovalModifier oldModifier, ApprovalModifier newModifier) {
		if (newModifier == LIKE && oldModifier != LIKE) {
			return 1;
		} else if (oldModifier == LIKE && newModifier != LIKE) {
			return -1;
		} else {
			return 0;
		}
	}
	
	public static int computeDislikeDelta(ApprovalModifier oldModifier, ApprovalModifier newModifier) {
		if (newModifier == DISLIKE && oldModifier != DISLIKE) {
			return 1;
		} else if (oldModifier == DISLIKE && newModifier != DISLIKE) {
			return -1;
		} else {
			return 0;
		}
	}
	
	public static int computeApprovalDelta(ApprovalModifier oldModifier, ApprovalModifier newModifier) {
		if (newModifier == LIKE && oldModifier == DISLIKE) {
			return 2;
		} else if (oldModifier == LIKE && newModifier == DISLIKE) {
			return -2;
		} else {
			return computeLikeDelta(oldModifier, newModifier);
		}
	}
	
	public abstract ActivityType toMediaActivtiyType();
	
	public abstract ActivityType toCommentActivtiyType();
}
