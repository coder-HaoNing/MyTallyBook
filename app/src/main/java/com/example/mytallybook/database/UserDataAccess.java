package com.example.mytallybook.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.mytallybook.model.User;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class UserDataAccess {
    private static final String TAG = "UserDataAccess";
    private final DatabaseHelper dbHelper;
    private final Executor executor;
    private final MutableLiveData<User> currentUserLiveData;

    public UserDataAccess(Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
        executor = Executors.newSingleThreadExecutor();
        currentUserLiveData = new MutableLiveData<>();
    }

    // 插入新用户
    public void insert(final User user, final InsertCallback callback) {
        executor.execute(() -> {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_USER_NAME, user.getUsername());
            values.put(DatabaseHelper.COLUMN_PASSWORD, user.getPassword());
            values.put(DatabaseHelper.COLUMN_PHONE_NUMBER, user.getPhoneNumber()); // 添加手机号字段
            values.put(DatabaseHelper.COLUMN_EMAIL, user.getEmail());
            
            Log.d(TAG, "正在插入新用户: 用户名=" + user.getUsername() + ", 手机号=" + user.getPhoneNumber());
            
            long id = db.insert(DatabaseHelper.TABLE_USERS, null, values);
            if (id != -1) {
                user.setId((int) id);
                Log.d(TAG, "用户插入成功, ID=" + id);
                if (callback != null) {
                    callback.onInsertSuccess(user);
                }
            } else {
                Log.e(TAG, "用户插入失败");
                if (callback != null) {
                    callback.onInsertFailed("插入用户失败");
                }
            }
        });
    }

    // 通过用户名和密码获取用户
    public void getUserByCredentials(String username, String password, GetUserCallback callback) {
        executor.execute(() -> {
            Log.d(TAG, "尝试使用用户名登录: " + username);
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            
            String[] projection = {
                    DatabaseHelper.COLUMN_ID,
                    DatabaseHelper.COLUMN_USER_NAME,
                    DatabaseHelper.COLUMN_PASSWORD,
                    DatabaseHelper.COLUMN_EMAIL,
                    DatabaseHelper.COLUMN_PHONE_NUMBER  // 添加手机号字段到查询
            };
            
            String selection = DatabaseHelper.COLUMN_USER_NAME + " = ? AND " + 
                               DatabaseHelper.COLUMN_PASSWORD + " = ?";
            String[] selectionArgs = {username, password};
            
            Cursor cursor = db.query(
                    DatabaseHelper.TABLE_USERS,
                    projection,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    null
            );
            
            User user = null;
            if (cursor.moveToFirst()) {
                user = new User();
                user.setId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID)));
                user.setUsername(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_NAME)));
                user.setPassword(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PASSWORD)));
                user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_EMAIL)));
                
                // 如果存在手机号字段，读取它
                int phoneColumnIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_PHONE_NUMBER);
                if (phoneColumnIndex != -1) {
                    user.setPhoneNumber(cursor.getString(phoneColumnIndex));
                }
                
                currentUserLiveData.postValue(user);
                Log.d(TAG, "用户凭据验证成功: 用户名=" + user.getUsername() + ", ID=" + user.getId() + ", 手机号=" + user.getPhoneNumber());
            } else {
                Log.d(TAG, "用户凭据验证失败: 找不到用户");
            }
            cursor.close();
            
            if (callback != null) {
                if (user != null) {
                    callback.onUserFound(user);
                } else {
                    callback.onUserNotFound();
                }
            }
        });
    }
    
    // 通过手机号或用户名和密码获取用户
    public void getUserByPhoneOrUsername(String usernameOrPhone, String password, GetUserCallback callback) {
        executor.execute(() -> {
            Log.d(TAG, "尝试使用用户名或手机号登录: " + usernameOrPhone);
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            
            String[] projection = {
                    DatabaseHelper.COLUMN_ID,
                    DatabaseHelper.COLUMN_USER_NAME,
                    DatabaseHelper.COLUMN_PASSWORD,
                    DatabaseHelper.COLUMN_EMAIL,
                    DatabaseHelper.COLUMN_PHONE_NUMBER
            };
            
            // 首先检查数据库中是否存在这个手机号
            Cursor phoneCursor = db.query(
                    DatabaseHelper.TABLE_USERS,
                    new String[]{DatabaseHelper.COLUMN_PHONE_NUMBER},
                    null,
                    null,
                    null,
                    null,
                    null
            );
            
            Log.d(TAG, "检查数据库中的手机号记录:");
            while (phoneCursor.moveToNext()) {
                String phoneInDb = phoneCursor.getString(phoneCursor.getColumnIndex(DatabaseHelper.COLUMN_PHONE_NUMBER));
                Log.d(TAG, "数据库中的手机号: " + phoneInDb);
            }
            phoneCursor.close();
            
            // 构建查询条件：用户名匹配 OR 手机号匹配，AND 密码匹配
            // 使用COLLATE NOCASE使比较不区分大小写
            String selection = "(" + DatabaseHelper.COLUMN_USER_NAME + " = ? COLLATE NOCASE OR " + 
                               DatabaseHelper.COLUMN_PHONE_NUMBER + " = ? COLLATE NOCASE) AND " + 
                               DatabaseHelper.COLUMN_PASSWORD + " = ?";
            String[] selectionArgs = {usernameOrPhone, usernameOrPhone, password};
            
            // 打印SQL查询
            String queryLog = "SQL查询: SELECT * FROM " + DatabaseHelper.TABLE_USERS + 
                              " WHERE (" + DatabaseHelper.COLUMN_USER_NAME + "='" + usernameOrPhone + 
                              "' OR " + DatabaseHelper.COLUMN_PHONE_NUMBER + "='" + usernameOrPhone + 
                              "') AND " + DatabaseHelper.COLUMN_PASSWORD + "='" + password + "'";
            Log.d(TAG, queryLog);
            
            Cursor cursor = db.query(
                    DatabaseHelper.TABLE_USERS,
                    projection,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    null
            );
            
            Log.d(TAG, "查询结果: 找到 " + cursor.getCount() + " 条记录");
            
            User user = null;
            if (cursor.moveToFirst()) {
                user = new User();
                user.setId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID)));
                user.setUsername(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_NAME)));
                user.setPassword(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PASSWORD)));
                user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_EMAIL)));
                
                // 读取手机号
                int phoneColumnIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_PHONE_NUMBER);
                if (phoneColumnIndex != -1) {
                    String phoneNumber = cursor.getString(phoneColumnIndex);
                    user.setPhoneNumber(phoneNumber);
                    Log.d(TAG, "读取到的手机号: " + phoneNumber);
                } else {
                    Log.w(TAG, "手机号字段不存在");
                }
                
                currentUserLiveData.postValue(user);
                Log.d(TAG, "用户凭据验证成功: 用户名=" + user.getUsername() + ", ID=" + user.getId());
            } else {
                Log.d(TAG, "用户凭据验证失败: 找不到用户");
                
                // 尝试直接通过手机号查询，打印调试信息
                Cursor debugCursor = db.query(
                        DatabaseHelper.TABLE_USERS,
                        new String[]{DatabaseHelper.COLUMN_ID, DatabaseHelper.COLUMN_USER_NAME, DatabaseHelper.COLUMN_PHONE_NUMBER},
                        null,
                        null,
                        null,
                        null,
                        null
                );
                
                Log.d(TAG, "数据库中的所有用户:");
                while (debugCursor.moveToNext()) {
                    int id = debugCursor.getInt(debugCursor.getColumnIndex(DatabaseHelper.COLUMN_ID));
                    String username = debugCursor.getString(debugCursor.getColumnIndex(DatabaseHelper.COLUMN_USER_NAME));
                    String phone = "";
                    int phoneIndex = debugCursor.getColumnIndex(DatabaseHelper.COLUMN_PHONE_NUMBER);
                    if (phoneIndex != -1) {
                        phone = debugCursor.getString(phoneIndex);
                    }
                    Log.d(TAG, "用户ID: " + id + ", 用户名: " + username + ", 手机号: " + phone);
                }
                debugCursor.close();
            }
            cursor.close();
            
            if (callback != null) {
                if (user != null) {
                    callback.onUserFound(user);
                } else {
                    callback.onUserNotFound();
                }
            }
        });
    }

    // 通过ID获取用户
    public void getUserById(int userId, GetUserCallback callback) {
        executor.execute(() -> {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            
            String[] projection = {
                    DatabaseHelper.COLUMN_ID,
                    DatabaseHelper.COLUMN_USER_NAME,
                    DatabaseHelper.COLUMN_PASSWORD,
                    DatabaseHelper.COLUMN_EMAIL,
                    DatabaseHelper.COLUMN_PHONE_NUMBER  // 添加手机号字段到查询
            };
            
            String selection = DatabaseHelper.COLUMN_ID + " = ?";
            String[] selectionArgs = {String.valueOf(userId)};
            
            Cursor cursor = db.query(
                    DatabaseHelper.TABLE_USERS,
                    projection,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    null
            );
            
            User user = null;
            if (cursor.moveToFirst()) {
                user = new User();
                user.setId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID)));
                user.setUsername(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_NAME)));
                user.setPassword(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PASSWORD)));
                user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_EMAIL)));
                
                // 如果存在手机号字段，读取它
                int phoneColumnIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_PHONE_NUMBER);
                if (phoneColumnIndex != -1) {
                    user.setPhoneNumber(cursor.getString(phoneColumnIndex));
                }
            }
            cursor.close();
            
            if (callback != null) {
                if (user != null) {
                    callback.onUserFound(user);
                } else {
                    callback.onUserNotFound();
                }
            }
        });
    }
    
    // 更新用户信息
    public void update(User user, UpdateCallback callback) {
        executor.execute(() -> {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_USER_NAME, user.getUsername());
            values.put(DatabaseHelper.COLUMN_PASSWORD, user.getPassword());
            values.put(DatabaseHelper.COLUMN_EMAIL, user.getEmail());
            values.put(DatabaseHelper.COLUMN_PHONE_NUMBER, user.getPhoneNumber()); // 添加手机号更新
            
            String selection = DatabaseHelper.COLUMN_ID + " = ?";
            String[] selectionArgs = {String.valueOf(user.getId())};
            
            int count = db.update(
                    DatabaseHelper.TABLE_USERS,
                    values,
                    selection,
                    selectionArgs
            );
            
            if (callback != null) {
                if (count > 0) {
                    callback.onUpdateSuccess();
                    currentUserLiveData.postValue(user);
                } else {
                    callback.onUpdateFailed("更新用户信息失败");
                }
            }
        });
    }
    
    // 获取当前用户LiveData
    public LiveData<User> getCurrentUserLiveData() {
        return currentUserLiveData;
    }
    
    // 设置当前用户
    public void setCurrentUser(User user) {
        currentUserLiveData.postValue(user);
    }
    
    // 回调接口定义
    public interface GetUserCallback {
        void onUserFound(User user);
        void onUserNotFound();
    }
    
    public interface InsertCallback {
        void onInsertSuccess(User user);
        void onInsertFailed(String errorMessage);
    }
    
    public interface UpdateCallback {
        void onUpdateSuccess();
        void onUpdateFailed(String errorMessage);
    }
}
