package com.example.mytallybook.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.mytallybook.model.ExpenseRecord;
import com.example.mytallybook.repository.ExpenseRepository;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 记账数据ViewModel类
 */
public class ExpenseViewModel extends AndroidViewModel {
    
    private ExpenseRepository repository;
    private LiveData<List<ExpenseRecord>> allRecords;
    private LiveData<Double> totalIncome;
    private LiveData<Double> totalExpense;
    
    // 添加月份选择相关变量
    private MutableLiveData<Date> selectedMonthDate = new MutableLiveData<>();
    private LiveData<List<ExpenseRecord>> recordsByMonth;
    private LiveData<Double> monthlyIncome;
    private LiveData<Double> monthlyExpense;
    
    public ExpenseViewModel(@NonNull Application application) {
        super(application);
        repository = new ExpenseRepository(application);
        allRecords = repository.getAllRecords();
        totalIncome = repository.getTotalIncome();
        totalExpense = repository.getTotalExpense();
        
        // 初始化为当前月份
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        selectedMonthDate.setValue(calendar.getTime());
        
        // 设置按月查询的转换
        recordsByMonth = Transformations.switchMap(selectedMonthDate, date -> {
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            Date startDate = cal.getTime();
            
            cal.add(Calendar.MONTH, 1);
            cal.add(Calendar.MILLISECOND, -1);
            Date endDate = cal.getTime();
            
            return repository.getRecordsByDateRange(startDate, endDate);
        });
        
        // 设置月度收入的转换
        monthlyIncome = Transformations.switchMap(selectedMonthDate, date -> {
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            Date startDate = cal.getTime();
            
            cal.add(Calendar.MONTH, 1);
            cal.add(Calendar.MILLISECOND, -1);
            Date endDate = cal.getTime();
            
            return repository.getTotalIncomeByDateRange(startDate, endDate);
        });
        
        // 设置月度支出的转换
        monthlyExpense = Transformations.switchMap(selectedMonthDate, date -> {
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            Date startDate = cal.getTime();
            
            cal.add(Calendar.MONTH, 1);
            cal.add(Calendar.MILLISECOND, -1);
            Date endDate = cal.getTime();
            
            return repository.getTotalExpenseByDateRange(startDate, endDate);
        });
    }
    
    // 获取所有记录
    public LiveData<List<ExpenseRecord>> getAllRecords() {
        return allRecords;
    }
    
    // 获取指定日期范围的记录
    public LiveData<List<ExpenseRecord>> getRecordsByDateRange(Date startDate, Date endDate) {
        return repository.getRecordsByDateRange(startDate, endDate);
    }
    
    // 获取特定类型的记录（收入/支出）
    public LiveData<List<ExpenseRecord>> getRecordsByType(boolean isIncome) {
        return repository.getRecordsByType(isIncome);
    }
    
    // 获取总收入
    public LiveData<Double> getTotalIncome() {
        return totalIncome;
    }
    
    // 获取总支出
    public LiveData<Double> getTotalExpense() {
        return totalExpense;
    }
    
    // 插入记录
    public void insert(ExpenseRecord record) {
        repository.insert(record);
    }
    
    // 更新记录
    public void update(ExpenseRecord record) {
        repository.update(record);
    }
    
    // 删除记录
    public void delete(ExpenseRecord record) {
        repository.delete(record);
    }
    
    // 获取特定记录
    public LiveData<ExpenseRecord> getRecordById(long id) {
        return repository.getRecordById(id);
    }
    
    // 以下是新增的按月查询和统计功能
    
    // 设置选择的月份
    public void setSelectedMonth(int year, int month) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        selectedMonthDate.setValue(calendar.getTime());
    }
    
    // 获取当前选择的月份
    public Date getSelectedMonthDate() {
        return selectedMonthDate.getValue();
    }
    
    // 选择上一个月
    public void selectPreviousMonth() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(selectedMonthDate.getValue());
        calendar.add(Calendar.MONTH, -1);
        selectedMonthDate.setValue(calendar.getTime());
    }
    
    // 选择下一个月
    public void selectNextMonth() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(selectedMonthDate.getValue());
        calendar.add(Calendar.MONTH, 1);
        selectedMonthDate.setValue(calendar.getTime());
    }
    
    // 获取当前选择月份的记录
    public LiveData<List<ExpenseRecord>> getRecordsByMonth() {
        return recordsByMonth;
    }
    
    // 获取当前选择月份的收入
    public LiveData<Double> getMonthlyIncome() {
        return monthlyIncome;
    }
    
    // 获取当前选择月份的支出
    public LiveData<Double> getMonthlyExpense() {
        return monthlyExpense;
    }
}