package com.example.mytallybook.database;

import androidx.room.TypeConverter;

import java.util.Date;

/**
 * 提供日期和时间戳之间的转换
 */
public class DateConverter {
    
    /**
     * 将时间戳转换为日期对象
     * @param value 时间戳（毫秒）
     * @return 日期对象
     */
    @TypeConverter
    public static Date toDate(Long value) {
        return value == null ? null : new Date(value);
    }

    /**
     * 将日期对象转换为时间戳
     * @param date 日期对象
     * @return 时间戳（毫秒）
     */
    @TypeConverter
    public static Long fromDate(Date date) {
        return date == null ? null : date.getTime();
    }
}