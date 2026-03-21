package com.taskmanagement.dto.websocket;

public final class EventType {
    
    public static final String TASK_CREATED = "TASK_CREATED";
    public static final String TASK_UPDATED = "TASK_UPDATED";
    public static final String TASK_DELETED = "TASK_DELETED";
    public static final String TASK_STATUS_CHANGED = "TASK_STATUS_CHANGED";
    public static final String TASK_ASSIGNED = "TASK_ASSIGNED";
    public static final String TASK_POSITION_CHANGED = "TASK_POSITION_CHANGED";

    public static final String PROJECT_UPDATED = "PROJECT_UPDATED";
    public static final String PROJECT_DELETED = "PROJECT_DELETED";
    public static final String MEMBER_ADDED = "MEMBER_ADDED";
    public static final String MEMBER_REMOVED = "MEMBER_REMOVED";
    public static final String MEMBER_ROLE_CHANGED = "MEMBER_ROLE_CHANGED";

    public static final String COMMENT_ADDED = "COMMENT_ADDED";
    public static final String COMMENT_DELETED = "COMMENT_DELETED";

    public static final String USER_JOINED = "USER_JOINED";
    public static final String USER_LEFT = "USER_LEFT";
    public static final String PRESENCE_UPDATE = "PRESENCE_UPDATE";

    private EventType() {
    }
}
