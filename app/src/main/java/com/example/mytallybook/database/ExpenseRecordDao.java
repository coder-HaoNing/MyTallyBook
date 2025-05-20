package com.example.mytallybook.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.mytallybook.model.ExpenseRecord;

import java.util.Date;
import java.util.List;

@Dao
public interface ExpenseRecordDao {
    @Insert
    void insert(ExpenseRecord record);
    
    @Update
    void update(ExpenseRecord record);
    
    @Delete
    void delete(ExpenseRecord record);
    
    @Query("SELECT * FROM expense_records ORDER BY date DESC")
    LiveData<List<ExpenseRecord>> getAllRecords();
    
    @Query("SELECT * FROM expense_records WHERE date BETWEEN :startDateMillis AND :endDateMillis ORDER BY date DESC")
    LiveData<List<ExpenseRecord>> getRecordsByDateRange(long startDateMillis, long endDateMillis);
    
    /**
     * 接收Date参数并转换为long后调用查询
     */
    default LiveData<List<ExpenseRecord>> getRecordsByDateRange(Date startDate, Date endDate) {
        return getRecordsByDateRange(startDate.getTime(), endDate.getTime());
    }
    
    @Query("SELECT * FROM expense_records WHERE id = :id")
    LiveData<ExpenseRecord> getRecordById(long id);
    
    // 修复SUM查询，使用CAST确保返回Double
    @Query("SELECT CAST(SUM(amount) AS DOUBLE) FROM expense_records WHERE isIncome = 1")
    LiveData<Double> getTotalIncome();
    
    // 修复SUM查询，使用CAST确保返回Double
    @Query("SELECT CAST(SUM(amount) AS DOUBLE) FROM expense_records WHERE isIncome = 0")
    LiveData<Double> getTotalExpense();
    
    // 修复SUM查询，使用CAST确保返回Double
    @Query("SELECT CAST(SUM(amount) AS DOUBLE) FROM expense_records WHERE isIncome = 1 AND date BETWEEN :startDateMillis AND :endDateMillis")
    LiveData<Double> getTotalIncomeByDateRange(long startDateMillis, long endDateMillis);
    
    /**
     * 接收Date参数并转换为long后调用查询
     */
    default LiveData<Double> getTotalIncomeByDateRange(Date startDate, Date endDate) {
        return getTotalIncomeByDateRange(startDate.getTime(), endDate.getTime());
    }
    
    // 修复SUM查询，使用CAST确保返回Double
    @Query("SELECT CAST(SUM(amount) AS DOUBLE) FROM expense_records WHERE isIncome = 0 AND date BETWEEN :startDateMillis AND :endDateMillis")
    LiveData<Double> getTotalExpenseByDateRange(long startDateMillis, long endDateMillis);
    
    /**
     * 接收Date参数并转换为long后调用查询
     */
    default LiveData<Double> getTotalExpenseByDateRange(Date startDate, Date endDate) {
        return getTotalExpenseByDateRange(startDate.getTime(), endDate.getTime());
    }
    
    @Query("SELECT * FROM expense_records WHERE isIncome = :isIncome ORDER BY date DESC")
    LiveData<List<ExpenseRecord>> getRecordsByType(boolean isIncome);
}