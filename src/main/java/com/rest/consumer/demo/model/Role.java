package com.rest.consumer.demo.model;

import java.io.Serializable;

public class Role implements Serializable {
    private long roleId;
    private String roleDescription;

    public Role() {}

    public Role(long roleId, String roleDescription) {
        this.roleId = roleId;
        this.roleDescription = roleDescription;
    }

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public String getRoleDescription() {
        return roleDescription;
    }

    public void setRoleDescription(String roleDescription) {
        this.roleDescription = roleDescription;
    }
}
