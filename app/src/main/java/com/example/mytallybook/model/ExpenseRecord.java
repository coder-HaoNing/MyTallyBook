package com.example.mytallybook.model;

import androidx.room.Entity;
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
    
    public ExpenseRecord(double amount, boolean isIncome, String category, Date date, String note) {
        this.amount = amount;
        this.isIncome = isIncome;
        this.category = category;
        this.date = date;
        this.note = note;
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
}