
package com.mycompany.apac2;

import java.io.Serializable;

public class UserSession implements Serializable {
    private String username;
    private boolean online;

    public UserSession(String username, boolean online) {
        this.username = username;
        this.online = online;
    }

    public String getUsername() {
        return username;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    @Override
    public String toString() {
        return "UserSession{" +
                "username='" + username + '\'' +
                ", online=" + online +
                '}';
    }
}

