package com.darwinreforged.server.modules.todo.modularwiki;

import java.util.ArrayList;
import java.util.List;

public class WikiStorageModel {
    List<WikiObject> entries = new ArrayList<>();

    public WikiStorageModel(List<WikiObject> entries) {
        this.entries = entries;
    }

    public WikiStorageModel() {
    }

    public List<WikiObject> getEntries() {
        return entries;
    }

    public void setEntries(List<WikiObject> entries) {
        this.entries = entries;
    }
}
