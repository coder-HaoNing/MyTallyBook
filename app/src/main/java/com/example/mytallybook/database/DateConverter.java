package com.example.mytallybook.database;

import androidx.room.TypeConverter;

import java.util.Date;

/**
 * 日期转换器，用于Room数据库存储Date类型
 */
public class DateConverter {
    @TypeConverter
    public static Date fromTimestamp(Long value) {
        return value == null ? null : new Date(value);
    }

    @TypeConverter
    public static Long dateToTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }
}