package com.example.mytallybook.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.mytallybook.model.ExpenseRecord;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ExpenseRecordDataAccess {
    private static final String TAG = "ExpenseRecordDataAccess";
    
    private final DatabaseHelper dbHelper;
    private final Executor executor;
    private final MutableLiveData<List<ExpenseRecord>> allExpensesLiveData;
    private final MutableLiveData<List<ExpenseRecord>> userExpensesLiveData;
    private final MutableLiveData<List<ExpenseRecord>> dateRangeExpensesLiveData;
    private final MutableLiveData<Double> totalIncomeLiveData;
    private final MutableLiveData<Double> totalExpenseLiveData;

    public ExpenseRecordDataAccess(Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
        executor = Executors.newSingleThreadExecutor();
        allExpensesLiveData = new MutableLiveData<>(new ArrayList<>());
        userExpensesLiveData = new MutableLiveData<>(new ArrayList<>());
        dateRangeExpensesLiveData = new MutableLiveData<>(new ArrayList<>());
        totalIncomeLiveData = new MutableLiveData<>(0.0);
        totalExpenseLiveData = new MutableLiveData<>(0.0);
    }

    // 插入新支出记录
    public void insert(ExpenseRecord record, final InsertCallback callback) {
        executor.execute(() -> {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues values = createContentValues(record);
            
            long id = db.insert(DatabaseHelper.TABLE_EXPENSE_RECORDS, null, values);
            if (id != -1) {
                record.setId((int) id);
                if (callback != null) {
                    callback.onInsertSuccess(record);
                }
                // 更新LiveData
                refreshExpenses(record.getUserId());
            } else {
                if (callback != null) {
                    callback.onInsertFailed("插入支出记录失败");
                }
            }
        });
    }

    // 更新支出记录
    public void update(ExpenseRecord record, final UpdateCallback callback) {
        executor.execute(() -> {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues values = createContentValues(record);
            
            String selection = DatabaseHelper.COLUMN_ID + " = ?";
            String[] selectionArgs = {String.valueOf(record.getId())};
            
            int count = db.update(
                    DatabaseHelper.TABLE_EXPENSE_RECORDS,
                    values,
                    selection,
                    selectionArgs);
            
            if (callback != null) {
                if (count > 0) {
                    callback.onUpdateSuccess();
                    // 更新LiveData
                    refreshExpenses(record.getUserId());
                } else {
                    callback.onUpdateFailed("更新支出记录失败");
                }
            }
        });
    }

    // 删除支出记录
    public void delete(ExpenseRecord record, final DeleteCallback callback) {
        executor.execute(() -> {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            
            String selection = DatabaseHelper.COLUMN_ID + " = ?";
            String[] selectionArgs = {String.valueOf(record.getId())};
            
            int count = db.delete(DatabaseHelper.TABLE_EXPENSE_RECORDS, selection, selectionArgs);
            
            if (callback != null) {
                if (count > 0) {
                    callback.onDeleteSuccess();
                    // 更新LiveData
                    refreshExpenses(record.getUserId());
                } else {
                    callback.onDeleteFailed("删除支出记录失败");
                }
            }
        });
    }

    // 获取指定用户的所有支出记录
    public LiveData<List<ExpenseRecord>> getAllExpensesForUser(int userId) {
        refreshExpenses(userId);
        return userExpensesLiveData;
    }
    
    // 获取所有支出记录
    public LiveData<List<ExpenseRecord>> getAllExpenses() {
        refreshAllExpenses();
        return allExpensesLiveData;
    }
    
    // 按ID获取支出记录
    public void getExpenseById(int expenseId, final GetExpenseCallback callback) {
        executor.execute(() -> {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            
            String[] projection = {
                    DatabaseHelper.COLUMN_ID,
                    DatabaseHelper.COLUMN_AMOUNT,
                    DatabaseHelper.COLUMN_CATEGORY,
                    DatabaseHelper.COLUMN_DATE,
                    DatabaseHelper.COLUMN_NOTE,
                    DatabaseHelper.COLUMN_USER_ID
            };
            
            String selection = DatabaseHelper.COLUMN_ID + " = ?";
            String[] selectionArgs = {String.valueOf(expenseId)};
            
            Cursor cursor = db.query(
                    DatabaseHelper.TABLE_EXPENSE_RECORDS,
                    projection,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    null
            );
            
            ExpenseRecord record = null;
            if (cursor.moveToFirst()) {
                record = cursorToExpenseRecord(cursor);
            }
            cursor.close();
            
            final ExpenseRecord finalRecord = record;
            if (callback != null) {
                if (finalRecord != null) {
                    callback.onExpenseFound(finalRecord);
                } else {
                    callback.onExpenseNotFound();
                }
            }
        });
    }

    // 刷新指定用户的支出记录列表
    private void refreshExpenses(int userId) {
        executor.execute(() -> {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            List<ExpenseRecord> expenses = new ArrayList<>();
            
            String[] projection = {
                    DatabaseHelper.COLUMN_ID,
                    DatabaseHelper.COLUMN_AMOUNT,
                    DatabaseHelper.COLUMN_CATEGORY,
                    DatabaseHelper.COLUMN_DATE,
                    DatabaseHelper.COLUMN_NOTE,
                    DatabaseHelper.COLUMN_USER_ID
            };
            
            String selection = DatabaseHelper.COLUMN_USER_ID + " = ?";
            String[] selectionArgs = {String.valueOf(userId)};
            String sortOrder = DatabaseHelper.COLUMN_DATE + " DESC";
            
            Cursor cursor = db.query(
                    DatabaseHelper.TABLE_EXPENSE_RECORDS,
                    projection,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    sortOrder
            );
            
            while (cursor.moveToNext()) {
                ExpenseRecord expense = cursorToExpenseRecord(cursor);
                expenses.add(expense);
            }
            cursor.close();
            
            userExpensesLiveData.postValue(expenses);
        });
    }
    
    // 刷新所有支出记录列表
    private void refreshAllExpenses() {
        executor.execute(() -> {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            List<ExpenseRecord> expenses = new ArrayList<>();
            
            String[] projection = {
                    DatabaseHelper.COLUMN_ID,
                    DatabaseHelper.COLUMN_AMOUNT,
                    DatabaseHelper.COLUMN_CATEGORY,
                    DatabaseHelper.COLUMN_DATE,
                    DatabaseHelper.COLUMN_NOTE,
                    DatabaseHelper.COLUMN_USER_ID
            };
            
            String sortOrder = DatabaseHelper.COLUMN_DATE + " DESC";
            
            Cursor cursor = db.query(
                    DatabaseHelper.TABLE_EXPENSE_RECORDS,
                    projection,
                    null,
                    null,
                    null,
                    null,
                    sortOrder
            );
            
            while (cursor.moveToNext()) {
                ExpenseRecord expense = cursorToExpenseRecord(cursor);
                expenses.add(expense);
            }
            cursor.close();
            
            allExpensesLiveData.postValue(expenses);
        });
    }

    // 按日期范围获取记录
    public LiveData<List<ExpenseRecord>> getRecordsByDateRange(Date startDate, Date endDate) {
        executor.execute(() -> {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            List<ExpenseRecord> expenses = new ArrayList<>();
            
            String[] projection = {
                    DatabaseHelper.COLUMN_ID,
                    DatabaseHelper.COLUMN_AMOUNT,
                    DatabaseHelper.COLUMN_CATEGORY,
                    DatabaseHelper.COLUMN_DATE,
                    DatabaseHelper.COLUMN_NOTE,
                    DatabaseHelper.COLUMN_USER_ID
            };
            
            // 将日期转换为时间戳
            long startTime = startDate.getTime();
            long endTime = endDate.getTime();
            
            String selection = DatabaseHelper.COLUMN_DATE + " >= ? AND " + 
                              DatabaseHelper.COLUMN_DATE + " <= ?";
            String[] selectionArgs = {String.valueOf(startTime), String.valueOf(endTime)};
            String sortOrder = DatabaseHelper.COLUMN_DATE + " DESC";
            
            Cursor cursor = db.query(
                    DatabaseHelper.TABLE_EXPENSE_RECORDS,
                    projection,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    sortOrder
            );
            
            while (cursor.moveToNext()) {
                ExpenseRecord expense = cursorToExpenseRecord(cursor);
                expenses.add(expense);
            }
            cursor.close();
            
            dateRangeExpensesLiveData.postValue(expenses);
        });
        
        return dateRangeExpensesLiveData;
    }
    
    // 按日期范围获取用户的记录
    public LiveData<List<ExpenseRecord>> getRecordsByDateRangeForUser(int userId, Date startDate, Date endDate) {
        executor.execute(() -> {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            List<ExpenseRecord> expenses = new ArrayList<>();
            
            String[] projection = {
                    DatabaseHelper.COLUMN_ID,
                    DatabaseHelper.COLUMN_AMOUNT,
                    DatabaseHelper.COLUMN_CATEGORY,
                    DatabaseHelper.COLUMN_DATE,
                    DatabaseHelper.COLUMN_NOTE,
                    DatabaseHelper.COLUMN_USER_ID
            };
            
            // 将日期转换为时间戳
            long startTime = startDate.getTime();
            long endTime = endDate.getTime();
            
            String selection = DatabaseHelper.COLUMN_USER_ID + " = ? AND " +
                              DatabaseHelper.COLUMN_DATE + " >= ? AND " + 
                              DatabaseHelper.COLUMN_DATE + " <= ?";
            String[] selectionArgs = {
                String.valueOf(userId),
                String.valueOf(startTime),
                String.valueOf(endTime)
            };
            String sortOrder = DatabaseHelper.COLUMN_DATE + " DESC";
            
            Cursor cursor = db.query(
                    DatabaseHelper.TABLE_EXPENSE_RECORDS,
                    projection,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    sortOrder
            );
            
            while (cursor.moveToNext()) {
                ExpenseRecord expense = cursorToExpenseRecord(cursor);
                expenses.add(expense);
            }
            cursor.close();
            
            // 在这里使用userExpensesLiveData，因为查询结果是用户的特定记录
            userExpensesLiveData.postValue(expenses);
        });
        
        return userExpensesLiveData;
    }
    
    // 按日期范围获取总收入
    public LiveData<Double> getTotalIncomeByDateRange(Date startDate, Date endDate) {
        executor.execute(() -> {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            double totalIncome = 0.0;
            
            // 将日期转换为时间戳
            long startTime = startDate.getTime();
            long endTime = endDate.getTime();
            
            // 假设收入是金额大于0的记录
            String selection = DatabaseHelper.COLUMN_AMOUNT + " > 0 AND " +
                              DatabaseHelper.COLUMN_DATE + " >= ? AND " + 
                              DatabaseHelper.COLUMN_DATE + " <= ?";
            String[] selectionArgs = {String.valueOf(startTime), String.valueOf(endTime)};
            
            Cursor cursor = db.query(
                    DatabaseHelper.TABLE_EXPENSE_RECORDS,
                    new String[]{"SUM(" + DatabaseHelper.COLUMN_AMOUNT + ")"},
                    selection,
                    selectionArgs,
                    null,
                    null,
                    null
            );
            
            if (cursor.moveToFirst()) {
                totalIncome = cursor.getDouble(0);
            }
            cursor.close();
            
            totalIncomeLiveData.postValue(totalIncome);
        });
        
        return totalIncomeLiveData;
    }
    
    // 按日期范围获取总支出
    public LiveData<Double> getTotalExpenseByDateRange(Date startDate, Date endDate) {
        executor.execute(() -> {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            double totalExpense = 0.0;
            
            // 将日期转换为时间戳
            long startTime = startDate.getTime();
            long endTime = endDate.getTime();
            
            // 假设支出是金额小于0的记录，我们存储绝对值
            String selection = DatabaseHelper.COLUMN_AMOUNT + " < 0 AND " +
                              DatabaseHelper.COLUMN_DATE + " >= ? AND " + 
                              DatabaseHelper.COLUMN_DATE + " <= ?";
            String[] selectionArgs = {String.valueOf(startTime), String.valueOf(endTime)};
            
            Cursor cursor = db.query(
                    DatabaseHelper.TABLE_EXPENSE_RECORDS,
                    new String[]{"SUM(ABS(" + DatabaseHelper.COLUMN_AMOUNT + "))"},
                    selection,
                    selectionArgs,
                    null,
                    null,
                    null
            );
            
            if (cursor.moveToFirst()) {
                totalExpense = cursor.getDouble(0);
            }
            cursor.close();
            
            totalExpenseLiveData.postValue(totalExpense);
        });
        
        return totalExpenseLiveData;
    }

    // 从Cursor创建ExpenseRecord
    private ExpenseRecord cursorToExpenseRecord(Cursor cursor) {
        ExpenseRecord record = new ExpenseRecord();
        record.setId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID)));
        record.setAmount(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_AMOUNT)));
        record.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CATEGORY)));
        long dateMillis = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DATE));
        record.setDate(new Date(dateMillis));
        record.setNote(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NOTE)));
        record.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_ID)));
        return record;
    }
    
    // 创建ContentValues
    private ContentValues createContentValues(ExpenseRecord record) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_AMOUNT, record.getAmount());
        values.put(DatabaseHelper.COLUMN_CATEGORY, record.getCategory());
        values.put(DatabaseHelper.COLUMN_DATE, record.getDate().getTime());
        values.put(DatabaseHelper.COLUMN_NOTE, record.getNote());
        values.put(DatabaseHelper.COLUMN_USER_ID, record.getUserId());
        return values;
    }
    
    // 回调接口定义
    public interface InsertCallback {
        void onInsertSuccess(ExpenseRecord record);
        void onInsertFailed(String errorMessage);
    }
    
    public interface UpdateCallback {
        void onUpdateSuccess();
        void onUpdateFailed(String errorMessage);
    }
    
    public interface DeleteCallback {
        void onDeleteSuccess();
        void onDeleteFailed(String errorMessage);
    }
    
    public interface GetExpenseCallback {
        void onExpenseFound(ExpenseRecord record);
        void onExpenseNotFound();
    }
}
