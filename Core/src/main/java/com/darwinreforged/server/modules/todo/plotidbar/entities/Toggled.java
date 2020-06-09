package com.darwinreforged.server.modules.todo.plotidbar.entities;


import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Toggled {

    private List<UUID> membersToggledBarOff = new ArrayList<>();

    private List<UUID> membersToggledMemberBarOff = new ArrayList<>();



    public void setMembersToggledMemberBarOff(List<UUID> membersToggledMemberBarOff) {
        this.membersToggledMemberBarOff = membersToggledMemberBarOff;
    }

    public void setMembersToggledBarOff(List<UUID> membersToggledBarOff) {
        this.membersToggledBarOff = membersToggledBarOff;
    }

    public List<UUID> getMembersToggledBarOff() {
        return membersToggledBarOff;
    }

    public List<UUID> getMembersToggledMemberBarOff() {
        return membersToggledMemberBarOff;
    }

}
