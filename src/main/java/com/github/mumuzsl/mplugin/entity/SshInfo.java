package com.github.mumuzsl.mplugin.entity;

/**
 * @auther zhaosenlin
 * @date 2022/9/24 20:50
 */
public class SshInfo {

    private String ip;
    private String username;
    private String password;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
