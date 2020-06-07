package com.darwinreforged.server.core.internal;

public enum ModuleRegistration {
    DISABLED,
    DEPRECATED_AND_FAIL,
    DEPRECATED_AND_SUCCEEDED,
    FAILED,
    SUCCEEDED;

    String ctx;

    ModuleRegistration() {
    }

    public ModuleRegistration setCtx(String ctx) {
        this.ctx = ctx;
        return this;
    }

    public String getContext() {
        return this.ctx;
    }

}
