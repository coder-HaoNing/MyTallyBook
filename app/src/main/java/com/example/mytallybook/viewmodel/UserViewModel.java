package com.example.mytallybook.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.mytallybook.database.UserDataAccess;
import com.example.mytallybook.model.User;
import com.example.mytallybook.repository.UserRepository;

/**
 * 用户数据ViewModel类
 */
public class UserViewModel extends AndroidViewModel {
    
    private UserRepository repository;
    
    public UserViewModel(@NonNull Application application) {
        super(application);
        repository = UserRepository.getInstance(application);
    }
    
    // 用户登录
    public void login(String username, String password, UserDataAccess.GetUserCallback callback) {
        repository.loginUser(username, password, callback);
    }
    
    // 使用用户名或手机号登录
    public void loginWithPhoneOrUsername(String usernameOrPhone, String password, UserDataAccess.GetUserCallback callback) {
        repository.loginWithPhoneOrUsername(usernameOrPhone, password, callback);
    }
    
    // 注册新用户
    public void register(User user, UserDataAccess.InsertCallback callback) {
        repository.registerUser(user, callback);
    }
    
    // 更新用户信息
    public void updateUser(User user, UserDataAccess.UpdateCallback callback) {
        repository.updateUser(user, callback);
    }
    
    // 获取当前用户
    public LiveData<User> getCurrentUser() {
        return repository.getCurrentUser();
    }
    
    // 设置当前用户
    public void setCurrentUser(User user) {
        repository.setCurrentUser(user);
    }
    
    // 通过ID获取用户
    public void getUserById(int userId, UserDataAccess.GetUserCallback callback) {
        repository.getUserById(userId, callback);
    }
}