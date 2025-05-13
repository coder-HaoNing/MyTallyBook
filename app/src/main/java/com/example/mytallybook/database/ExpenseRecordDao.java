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
    
    @Query("SELECT * FROM expense_records WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    LiveData<List<ExpenseRecord>> getRecordsByDateRange(Date startDate, Date endDate);
    
    @Query("SELECT * FROM expense_records WHERE id = :id")
    LiveData<ExpenseRecord> getRecordById(long id);
    
    @Query("SELECT SUM(amount) FROM expense_records WHERE isIncome = 1")
    LiveData<Double> getTotalIncome();
    
    @Query("SELECT SUM(amount) FROM expense_records WHERE isIncome = 0")
    LiveData<Double> getTotalExpense();
    
    @Query("SELECT SUM(amount) FROM expense_records WHERE isIncome = 1 AND date BETWEEN :startDate AND :endDate")
    LiveData<Double> getTotalIncomeByDateRange(Date startDate, Date endDate);
    
    @Query("SELECT SUM(amount) FROM expense_records WHERE isIncome = 0 AND date BETWEEN :startDate AND :endDate")
    LiveData<Double> getTotalExpenseByDateRange(Date startDate, Date endDate);
    
    @Query("SELECT * FROM expense_records WHERE isIncome = :isIncome ORDER BY date DESC")
    LiveData<List<ExpenseRecord>> getRecordsByType(boolean isIncome);
}