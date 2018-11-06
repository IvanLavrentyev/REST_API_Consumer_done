package com.rest.consumer.demo.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.autoconfigure.domain.EntityScan;

import javax.annotation.Generated;
import javax.persistence.Entity;
import java.io.Serializable;
import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)

@Setter
@Getter
@ToString
public class User implements Serializable {

    private long id;
    private String name;
    private String login;
    private String password;
    private Set<Role> roles;
    private String mockRole;

    public User() {}

    public User(String name, String login, String password, Set<Role> roles) {
        this(name,login,password);
        this.roles = roles;
    }

    public User(String name, String login, String password) {
        this.name = name;
        this.login = login;
        this.password = password;

    }

    public void setId(long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    public String getMockRole() {
        return mockRole;
    }

    public void setMockRole(String mockRole) {
        this.mockRole = mockRole;
    }
}
