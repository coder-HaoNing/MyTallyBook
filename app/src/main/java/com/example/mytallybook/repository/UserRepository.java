package com.example.mytallybook.repository;

import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import com.example.mytallybook.database.AppDatabase;
import com.example.mytallybook.database.UserDao;
import com.example.mytallybook.model.User;

import java.util.concurrent.ExecutionException;

/**
 * 用户数据仓库类
 */
public class UserRepository {
    private UserDao userDao;
    
    public UserRepository(Application application) {
        AppDatabase database = AppDatabase.getInstance(application);
        userDao = database.userDao();
    }
    
    // 通过手机号获取用户
    public LiveData<User> getUserByPhone(String phoneNumber) {
        return userDao.getUserByPhone(phoneNumber);
    }
    
    // 用户登录
    public LiveData<User> login(String phoneNumber, String password) {
        return userDao.login(phoneNumber, password);
    }
    
    // 注册新用户
    public void register(User user) {
        new InsertUserAsyncTask(userDao).execute(user);
    }
    
    // 检查手机号是否已存在
    public boolean isPhoneNumberExists(String phoneNumber) {
        try {
            return new CheckPhoneExistsAsyncTask(userDao).execute(phoneNumber).get() > 0;
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // 异步插入用户任务
    private static class InsertUserAsyncTask extends AsyncTask<User, Void, Void> {
        private UserDao userDao;
        
        InsertUserAsyncTask(UserDao userDao) {
            this.userDao = userDao;
        }
        
        @Override
        protected Void doInBackground(User... users) {
            userDao.insert(users[0]);
            return null;
        }
    }
    
    // 异步检查手机号是否存在任务
    private static class CheckPhoneExistsAsyncTask extends AsyncTask<String, Void, Integer> {
        private UserDao userDao;
        
        CheckPhoneExistsAsyncTask(UserDao userDao) {
            this.userDao = userDao;
        }
        
        @Override
        protected Integer doInBackground(String... phoneNumbers) {
            return userDao.checkPhoneExists(phoneNumbers[0]);
        }
    }
}
