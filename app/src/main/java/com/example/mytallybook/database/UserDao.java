package com.example.mytallybook.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.mytallybook.model.User;

import java.util.List;

@Dao
public interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(User user);
    
    @Query("SELECT * FROM users WHERE phoneNumber = :phoneNumber LIMIT 1")
    User getUserByPhoneSync(String phoneNumber);
    
    @Query("SELECT * FROM users WHERE phoneNumber = :phoneNumber")
    LiveData<List<User>> getUserByPhone(String phoneNumber);
    
    @Query("SELECT * FROM users WHERE phoneNumber = :phoneNumber AND password = :password LIMIT 1")
    User loginSync(String phoneNumber, String password);
    
    @Query("SELECT * FROM users WHERE phoneNumber = :phoneNumber AND password = :password")
    LiveData<List<User>> login(String phoneNumber, String password);
    
    @Query("SELECT COUNT(*) FROM users WHERE phoneNumber = :phoneNumber")
    int checkPhoneExists(String phoneNumber);
}