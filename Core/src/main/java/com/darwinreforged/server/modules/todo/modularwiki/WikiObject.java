package com.darwinreforged.server.modules.todo.modularwiki;

public class WikiObject {

    private String id;
    private String name;
    private String permission;
    private String[] description;
    private Boolean hidden;

    public WikiObject() {
    }

    public WikiObject(String id, String name, String permission, String[] description, Boolean hidden) {
        this.id = id;
        this.name = name;
        this.permission = permission;
        this.description = description;
        this.hidden = hidden;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPermission() {
        return permission;
    }

    public String[] getDescription() {
        return description;
    }

    public Boolean isHidden() {
        return hidden != null ? hidden : Boolean.FALSE;
    }
}
