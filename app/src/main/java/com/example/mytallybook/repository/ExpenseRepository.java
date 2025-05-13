package com.example.mytallybook.repository;

import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import com.example.mytallybook.database.AppDatabase;
import com.example.mytallybook.database.ExpenseRecordDao;
import com.example.mytallybook.model.ExpenseRecord;

import java.util.Date;
import java.util.List;

/**
 * 记账数据仓库类
 */
public class ExpenseRepository {
    private ExpenseRecordDao expenseRecordDao;
    private LiveData<List<ExpenseRecord>> allRecords;
    private LiveData<Double> totalIncome;
    private LiveData<Double> totalExpense;
    
    public ExpenseRepository(Application application) {
        AppDatabase database = AppDatabase.getInstance(application);
        expenseRecordDao = database.expenseRecordDao();
        allRecords = expenseRecordDao.getAllRecords();
        totalIncome = expenseRecordDao.getTotalIncome();
        totalExpense = expenseRecordDao.getTotalExpense();
    }
    
    // 获取所有记录
    public LiveData<List<ExpenseRecord>> getAllRecords() {
        return allRecords;
    }
    
    // 获取指定日期范围的记录
    public LiveData<List<ExpenseRecord>> getRecordsByDateRange(Date startDate, Date endDate) {
        return expenseRecordDao.getRecordsByDateRange(startDate, endDate);
    }
    
    // 获取特定类型的记录（收入/支出）
    public LiveData<List<ExpenseRecord>> getRecordsByType(boolean isIncome) {
        return expenseRecordDao.getRecordsByType(isIncome);
    }
    
    // 获取总收入
    public LiveData<Double> getTotalIncome() {
        return totalIncome;
    }
    
    // 获取总支出
    public LiveData<Double> getTotalExpense() {
        return totalExpense;
    }
    
    // 获取特定日期范围内的总收入
    public LiveData<Double> getTotalIncomeByDateRange(Date startDate, Date endDate) {
        return expenseRecordDao.getTotalIncomeByDateRange(startDate, endDate);
    }
    
    // 获取特定日期范围内的总支出
    public LiveData<Double> getTotalExpenseByDateRange(Date startDate, Date endDate) {
        return expenseRecordDao.getTotalExpenseByDateRange(startDate, endDate);
    }
    
    // 获取特定记录
    public LiveData<ExpenseRecord> getRecordById(long id) {
        return expenseRecordDao.getRecordById(id);
    }
    
    // 插入记录
    public void insert(ExpenseRecord record) {
        new InsertAsyncTask(expenseRecordDao).execute(record);
    }
    
    // 更新记录
    public void update(ExpenseRecord record) {
        new UpdateAsyncTask(expenseRecordDao).execute(record);
    }
    
    // 删除记录
    public void delete(ExpenseRecord record) {
        new DeleteAsyncTask(expenseRecordDao).execute(record);
    }
    
    // 异步插入任务
    private static class InsertAsyncTask extends AsyncTask<ExpenseRecord, Void, Void> {
        private ExpenseRecordDao asyncTaskDao;
        
        InsertAsyncTask(ExpenseRecordDao dao) {
            asyncTaskDao = dao;
        }
        
        @Override
        protected Void doInBackground(ExpenseRecord... expenseRecords) {
            asyncTaskDao.insert(expenseRecords[0]);
            return null;
        }
    }
    
    // 异步更新任务
    private static class UpdateAsyncTask extends AsyncTask<ExpenseRecord, Void, Void> {
        private ExpenseRecordDao asyncTaskDao;
        
        UpdateAsyncTask(ExpenseRecordDao dao) {
            asyncTaskDao = dao;
        }
        
        @Override
        protected Void doInBackground(ExpenseRecord... expenseRecords) {
            asyncTaskDao.update(expenseRecords[0]);
            return null;
        }
    }
    
    // 异步删除任务
    private static class DeleteAsyncTask extends AsyncTask<ExpenseRecord, Void, Void> {
        private ExpenseRecordDao asyncTaskDao;
        
        DeleteAsyncTask(ExpenseRecordDao dao) {
            asyncTaskDao = dao;
        }
        
        @Override
        protected Void doInBackground(ExpenseRecord... expenseRecords) {
            asyncTaskDao.delete(expenseRecords[0]);
            return null;
        }
    }
}