package com.darwinreforged.server.modules.todo.dave;

import java.util.List;

public class DaveTrigger {

    private String id;
    private List<String> trigger;
    private boolean important;
    private List<Response> responses;
    private String permission = null;

    public DaveTrigger() {
    }

    public DaveTrigger(String id, List<String> trigger, boolean important, List<Response> responses, String permission) {
        this.id = id;
        this.trigger = trigger;
        this.important = important;
        this.responses = responses;
        this.permission = permission;
    }

    public String getId() {
        return id;
    }

    public List<String> getTrigger() {
        return trigger;
    }

    public boolean isImportant() {
        return important;
    }

    public List<Response> getResponses() {
        return responses;
    }

    public String getPermission() {
        return permission;
    }

    public static class Response {
        private String message;
        private String type;

        public Response() {
        }

        public Response(String message, String type) {
            this.message = message;
            this.type = type;
        }

        public String getMessage() {
            return message;
        }

        public String getType() {
            return type;
        }
    }

}
