package com.example.mytallybook.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

/**
 * 用户实体类
 */
@Entity(tableName = "users")
public class User {
    @PrimaryKey(autoGenerate = true)
    private int id; // 添加自增主键id

    @NonNull
    private String username;
    private String password;
    private String phoneNumber; // 添加手机号字段
    private String email; // 添加email字段

    // 添加无参构造函数供Room数据库使用
    public User() {
    }

    @Ignore // 标记此构造函数被Room忽略
    public User(@NonNull String username, String password, String email) {
        this.username = username;
        this.password = password;
        this.email = email;
    }
    
    @Ignore // 标记此构造函数被Room忽略
    public User(@NonNull String username, String password, String phoneNumber, String email) {
        this.username = username;
        this.password = password;
        this.phoneNumber = phoneNumber;
        this.email = email;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @NonNull
    public String getUsername() {
        return username;
    }

    public void setUsername(@NonNull String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getPhoneNumber() {
        return phoneNumber;
    }
    
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}