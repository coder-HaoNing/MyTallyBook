package com.example.mytallybook.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.example.mytallybook.model.ExpenseRecord;
import com.example.mytallybook.model.User;

/**
 * 应用数据库类，使用单例模式
 */
@Database(entities = {ExpenseRecord.class, User.class}, version = 2, exportSchema = false)
@TypeConverters({DateConverter.class})
public abstract class AppDatabase extends RoomDatabase {
    
    private static final String DATABASE_NAME = "tallybook_db";
    private static AppDatabase instance;
    
    public abstract ExpenseRecordDao expenseRecordDao();
    public abstract UserDao userDao();
    
    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                    context.getApplicationContext(),
                    AppDatabase.class,
                    DATABASE_NAME)
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
}