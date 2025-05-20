package com.example.mytallybook.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "DatabaseHelper";
    private static final String DATABASE_NAME = "tallybook_db";
    private static final int DATABASE_VERSION = 4; // 增加版本号，确保数据库更新

    // 支出记录表
    public static final String TABLE_EXPENSE_RECORDS = "expense_records";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_AMOUNT = "amount";
    public static final String COLUMN_IS_INCOME = "isIncome"; // 新增：是否为收入
    public static final String COLUMN_CATEGORY = "category";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_NOTE = "note";
    public static final String COLUMN_USER_ID = "user_id";

    // 用户表
    public static final String TABLE_USERS = "users";
    public static final String COLUMN_USER_NAME = "username";
    public static final String COLUMN_PASSWORD = "password";
    public static final String COLUMN_PHONE_NUMBER = "phoneNumber"; // 手机号
    public static final String COLUMN_EMAIL = "email";

    // 支出记录表创建语句
    private static final String CREATE_TABLE_EXPENSE_RECORDS =
            "CREATE TABLE " + TABLE_EXPENSE_RECORDS + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_AMOUNT + " REAL NOT NULL, " +
                    COLUMN_IS_INCOME + " INTEGER NOT NULL DEFAULT 0, " + // 新增：是否为收入，0为支出，1为收入
                    COLUMN_CATEGORY + " TEXT NOT NULL, " +
                    COLUMN_DATE + " INTEGER NOT NULL, " +
                    COLUMN_NOTE + " TEXT, " +
                    COLUMN_USER_ID + " INTEGER NOT NULL, " +
                    "FOREIGN KEY (" + COLUMN_USER_ID + ") REFERENCES " +
                    TABLE_USERS + "(" + COLUMN_ID + "))";

    // 用户表创建语句
    private static final String CREATE_TABLE_USERS =
            "CREATE TABLE " + TABLE_USERS + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_USER_NAME + " TEXT NOT NULL UNIQUE, " +
                    COLUMN_PASSWORD + " TEXT NOT NULL, " +
                    COLUMN_PHONE_NUMBER + " TEXT UNIQUE, " + // 手机号，添加UNIQUE约束
                    COLUMN_EMAIL + " TEXT)";

    private static DatabaseHelper instance;

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "创建数据库表...");
        db.execSQL(CREATE_TABLE_USERS);
        db.execSQL(CREATE_TABLE_EXPENSE_RECORDS);
        Log.d(TAG, "数据库表创建成功");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "升级数据库: 从版本 " + oldVersion + " 到版本 " + newVersion);
        
        // 处理各种版本升级情况
        if (oldVersion < 3) {
            // 从版本2及以下升级到版本3
            try {
                // 为支出表添加收入标记字段
                if (!columnExists(db, TABLE_EXPENSE_RECORDS, COLUMN_IS_INCOME)) {
                    Log.d(TAG, "添加字段 " + COLUMN_IS_INCOME + " 到表 " + TABLE_EXPENSE_RECORDS);
                    db.execSQL("ALTER TABLE " + TABLE_EXPENSE_RECORDS + " ADD COLUMN " + 
                               COLUMN_IS_INCOME + " INTEGER NOT NULL DEFAULT 0");
                }
                
                // 为用户表添加手机号字段（无唯一约束）
                if (!columnExists(db, TABLE_USERS, COLUMN_PHONE_NUMBER)) {
                    Log.d(TAG, "添加字段 " + COLUMN_PHONE_NUMBER + " 到表 " + TABLE_USERS);
                    db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + 
                               COLUMN_PHONE_NUMBER + " TEXT");
                }
            } catch (Exception e) {
                Log.e(TAG, "升级到版本3过程中出错: " + e.getMessage());
                // 如果升级失败，则采取强制重建策略
                recreateTables(db);
            }
        }
        
        if (oldVersion < 4) {
            // 从版本3升级到版本4，确保手机号字段具有唯一约束
            try {
                Log.d(TAG, "升级到版本4: 确保手机号唯一性");
                
                // 临时表名
                String tempTableName = TABLE_USERS + "_temp";
                
                // 创建临时表（包含所有字段和约束条件）
                db.execSQL("CREATE TABLE " + tempTableName + " (" +
                            COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                            COLUMN_USER_NAME + " TEXT NOT NULL UNIQUE, " +
                            COLUMN_PASSWORD + " TEXT NOT NULL, " +
                            COLUMN_PHONE_NUMBER + " TEXT UNIQUE, " + // 带有UNIQUE约束
                            COLUMN_EMAIL + " TEXT)");
                
                // 复制数据（确保不复制NULL手机号，以避免唯一性冲突）
                db.execSQL("INSERT OR IGNORE INTO " + tempTableName + 
                           " SELECT " + COLUMN_ID + ", " + 
                           COLUMN_USER_NAME + ", " + 
                           COLUMN_PASSWORD + ", " + 
                           "CASE WHEN " + COLUMN_PHONE_NUMBER + " = '' THEN NULL ELSE " + COLUMN_PHONE_NUMBER + " END, " +
                           COLUMN_EMAIL + 
                           " FROM " + TABLE_USERS);
                           
                // 删除旧表
                db.execSQL("DROP TABLE " + TABLE_USERS);
                
                // 重命名新表
                db.execSQL("ALTER TABLE " + tempTableName + " RENAME TO " + TABLE_USERS);
                
                Log.d(TAG, "成功更新用户表结构，添加了手机号唯一约束");
                
            } catch (Exception e) {
                Log.e(TAG, "升级到版本4过程中出错: " + e.getMessage());
                // 如果升级失败，则采取强制重建策略
                recreateTables(db);
            }
        }
    }
    
    // 检查表中是否存在特定列
    private boolean columnExists(SQLiteDatabase db, String tableName, String columnName) {
        boolean result = false;
        try {
            db.query(tableName, new String[]{columnName}, null, null, null, null, null, "1");
            result = true;
        } catch (Exception e) {
            // 列不存在
            Log.d(TAG, "列 " + columnName + " 在表 " + tableName + " 中不存在");
        }
        return result;
    }
    
    // 重建所有表
    private void recreateTables(SQLiteDatabase db) {
        Log.w(TAG, "重建所有数据表");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EXPENSE_RECORDS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }
    
    /**
     * 清空数据库中的所有数据
     * 这个方法会删除所有表中的记录，但保留表结构
     */
    public void clearAllData() {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            db.beginTransaction();
            
            // 删除支出记录表中的所有数据
            db.execSQL("DELETE FROM " + TABLE_EXPENSE_RECORDS);
            
            // 删除用户表中的所有数据
            db.execSQL("DELETE FROM " + TABLE_USERS);
            
            // 重置主键自增ID
            db.execSQL("DELETE FROM sqlite_sequence WHERE name='" + TABLE_EXPENSE_RECORDS + "'");
            db.execSQL("DELETE FROM sqlite_sequence WHERE name='" + TABLE_USERS + "'");
            
            db.setTransactionSuccessful();
            Log.i(TAG, "已成功清空数据库中的所有数据");
        } catch (Exception e) {
            Log.e(TAG, "清空数据库失败: " + e.getMessage());
        } finally {
            db.endTransaction();
        }
    }
    
    /**
     * 创建一个默认管理员用户
     * 当数据库初始化或清空后，可以调用此方法添加一个默认用户
     */
    public void createDefaultUser() {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            db.beginTransaction();
            
            // 插入默认用户
            db.execSQL("INSERT INTO " + TABLE_USERS + " (" + 
                    COLUMN_USER_NAME + ", " + 
                    COLUMN_PASSWORD + ", " + 
                    COLUMN_PHONE_NUMBER + ", " + 
                    COLUMN_EMAIL + 
                    ") VALUES ('admin', 'admin123', '13800138000', 'admin@example.com')");
            
            db.setTransactionSuccessful();
            Log.i(TAG, "已创建默认用户");
        } catch (Exception e) {
            Log.e(TAG, "创建默认用户失败: " + e.getMessage());
        } finally {
            db.endTransaction();
        }
    }
}