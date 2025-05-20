package com.example.mytallybook.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.example.mytallybook.database.UserDataAccess;
import com.example.mytallybook.model.User;

/**
 * 用户数据仓库类，处理用户相关的数据操作
 */
public class UserRepository {
    private final UserDataAccess userDataAccess;
    private static UserRepository instance;

    private UserRepository(Context context) {
        userDataAccess = new UserDataAccess(context);
    }

    public static synchronized UserRepository getInstance(Context context) {
        if (instance == null) {
            instance = new UserRepository(context.getApplicationContext());
        }
        return instance;
    }

    // 注册用户
    public void registerUser(User user, UserDataAccess.InsertCallback callback) {
        userDataAccess.insert(user, callback);
    }

    // 用户登录 (仅用户名)
    public void loginUser(String username, String password, UserDataAccess.GetUserCallback callback) {
        userDataAccess.getUserByCredentials(username, password, callback);
    }

    // 用户登录 (支持用户名或手机号)
    public void loginWithPhoneOrUsername(String usernameOrPhone, String password, UserDataAccess.GetUserCallback callback) {
        userDataAccess.getUserByPhoneOrUsername(usernameOrPhone, password, callback);
    }

    // 更新用户信息
    public void updateUser(User user, UserDataAccess.UpdateCallback callback) {
        userDataAccess.update(user, callback);
    }

    // 获取当前用户信息（LiveData）
    public LiveData<User> getCurrentUser() {
        return userDataAccess.getCurrentUserLiveData();
    }

    // 设置当前用户
    public void setCurrentUser(User user) {
        userDataAccess.setCurrentUser(user);
    }

    // 通过ID获取用户
    public void getUserById(int userId, UserDataAccess.GetUserCallback callback) {
        userDataAccess.getUserById(userId, callback);
    }
}
