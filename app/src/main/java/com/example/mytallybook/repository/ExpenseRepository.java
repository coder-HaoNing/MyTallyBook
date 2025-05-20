package com.example.mytallybook.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.example.mytallybook.database.ExpenseRecordDataAccess;
import com.example.mytallybook.model.ExpenseRecord;

import java.util.Date;
import java.util.List;

/**
 * 支出记录仓库类，处理支出记录相关的数据操作
 */
public class ExpenseRepository {
    private final ExpenseRecordDataAccess expenseDataAccess;
    private static ExpenseRepository instance;

    private ExpenseRepository(Context context) {
        expenseDataAccess = new ExpenseRecordDataAccess(context);
    }

    public static synchronized ExpenseRepository getInstance(Context context) {
        if (instance == null) {
            instance = new ExpenseRepository(context.getApplicationContext());
        }
        return instance;
    }

    // 添加新的支出记录
    public void addExpense(ExpenseRecord record, ExpenseRecordDataAccess.InsertCallback callback) {
        expenseDataAccess.insert(record, callback);
    }

    // 更新支出记录
    public void updateExpense(ExpenseRecord record, ExpenseRecordDataAccess.UpdateCallback callback) {
        expenseDataAccess.update(record, callback);
    }

    // 删除支出记录
    public void deleteExpense(ExpenseRecord record, ExpenseRecordDataAccess.DeleteCallback callback) {
        expenseDataAccess.delete(record, callback);
    }

    // 获取用户的所有支出记录
    public LiveData<List<ExpenseRecord>> getAllExpensesForUser(int userId) {
        return expenseDataAccess.getAllExpensesForUser(userId);
    }
    
    // 获取所有支出记录
    public LiveData<List<ExpenseRecord>> getAllExpenses() {
        return expenseDataAccess.getAllExpenses();
    }
    
    // 通过ID获取支出记录
    public void getExpenseById(int expenseId, ExpenseRecordDataAccess.GetExpenseCallback callback) {
        expenseDataAccess.getExpenseById(expenseId, callback);
    }
    
    // 按日期范围获取记录
    public LiveData<List<ExpenseRecord>> getRecordsByDateRange(Date startDate, Date endDate) {
        return expenseDataAccess.getRecordsByDateRange(startDate, endDate);
    }
    
    // 按日期范围获取用户的记录
    public LiveData<List<ExpenseRecord>> getRecordsByDateRangeForUser(int userId, Date startDate, Date endDate) {
        return expenseDataAccess.getRecordsByDateRangeForUser(userId, startDate, endDate);
    }
    
    // 按日期范围获取总收入
    public LiveData<Double> getTotalIncomeByDateRange(Date startDate, Date endDate) {
        return expenseDataAccess.getTotalIncomeByDateRange(startDate, endDate);
    }
    
    // 按日期范围获取总支出
    public LiveData<Double> getTotalExpenseByDateRange(Date startDate, Date endDate) {
        return expenseDataAccess.getTotalExpenseByDateRange(startDate, endDate);
    }
}