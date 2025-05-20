package com.example.mytallybook.model;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.Date;

/**
 * 记账条目实体类
 */
@Entity(tableName = "expense_records")
public class ExpenseRecord {
    @PrimaryKey(autoGenerate = true)
    private long id;
    
    private double amount; // 金额
    private boolean isIncome; // 是否为收入
    private String category; // 分类
    private Date date; // 日期
    private String note; // 备注
    private int userId; // 添加用户ID，关联到User表
    
    // 默认构造函数供Room使用
    public ExpenseRecord() {
    }
    
    @Ignore // 标记此构造函数被Room忽略
    public ExpenseRecord(double amount, String category, Date date, String note, int userId) {
        this.amount = amount;
        this.category = category;
        this.date = date;
        this.note = note;
        this.userId = userId;
    }
    
    @Ignore // 标记此构造函数被Room忽略
    public ExpenseRecord(double amount, boolean isIncome, String category, Date date, String note, int userId) {
        this.amount = amount;
        this.isIncome = isIncome;
        this.category = category;
        this.date = date;
        this.note = note;
        this.userId = userId;
    }
    
    public long getId() {
        return id;
    }
    
    public void setId(long id) {
        this.id = id;
    }
    
    public double getAmount() {
        return amount;
    }
    
    public void setAmount(double amount) {
        this.amount = amount;
    }
    
    public boolean isIncome() {
        return isIncome;
    }
    
    public void setIncome(boolean income) {
        isIncome = income;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public Date getDate() {
        return date;
    }
    
    public void setDate(Date date) {
        this.date = date;
    }
    
    public String getNote() {
        return note;
    }
    
    public void setNote(String note) {
        this.note = note;
    }
    
    public int getUserId() {
        return userId;
    }
    
    public void setUserId(int userId) {
        this.userId = userId;
    }
}