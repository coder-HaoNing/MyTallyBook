package com.example.mytallybook.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.mytallybook.model.User;
import com.example.mytallybook.repository.UserRepository;

/**
 * 用户数据ViewModel类
 */
public class UserViewModel extends AndroidViewModel {
    
    private UserRepository repository;
    
    public UserViewModel(@NonNull Application application) {
        super(application);
        repository = new UserRepository(application);
    }
    
    // 通过手机号获取用户
    public LiveData<User> getUserByPhone(String phoneNumber) {
        return repository.getUserByPhone(phoneNumber);
    }
    
    // 用户登录
    public LiveData<User> login(String phoneNumber, String password) {
        return repository.login(phoneNumber, password);
    }
    
    // 注册新用户
    public void register(User user) {
        repository.register(user);
    }
    
    // 检查手机号是否已存在
    public boolean isPhoneNumberExists(String phoneNumber) {
        return repository.isPhoneNumberExists(phoneNumber);
    }
}