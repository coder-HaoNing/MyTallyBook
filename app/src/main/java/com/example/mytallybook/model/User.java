package com.example.mytallybook.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * 用户实体类
 */
@Entity(tableName = "users")
public class User {
    @PrimaryKey
    @NonNull
    private String phoneNumber; // 使用手机号作为主键
    
    private String username;
    private String password;
    
    public User(@NonNull String phoneNumber, String username, String password) {
        this.phoneNumber = phoneNumber;
        this.username = username;
        this.password = password;
    }
    
    @NonNull
    public String getPhoneNumber() {
        return phoneNumber;
    }
    
    public void setPhoneNumber(@NonNull String phoneNumber) {
        this.phoneNumber = phoneNumber;
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