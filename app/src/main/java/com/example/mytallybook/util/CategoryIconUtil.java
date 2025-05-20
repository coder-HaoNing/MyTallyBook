package com.example.mytallybook.util;

import androidx.annotation.DrawableRes;

import com.example.mytallybook.R;

/**
 * 类别图标工具类，根据收支类别返回对应的图标资源ID
 */
public class CategoryIconUtil {

    /**
     * 根据类别名称获取对应的图标资源ID
     * @param category 类别名称
     * @param isIncome 是否为收入类别
     * @return 对应的图标资源ID
     */
    @DrawableRes
    public static int getCategoryIcon(String category, boolean isIncome) {
        if (isIncome) {
            // 收入类别图标
            switch (category) {
                case "工资":
                    return R.drawable.ic_income_salary;
                case "奖金":
                    return R.drawable.ic_income_bonus;
                case "投资":
                    return R.drawable.ic_income_investment;
                case "其他收入":
                default:
                    return R.drawable.ic_income_other;
            }
        } else {
            // 支出类别图标
            switch (category) {
                case "餐饮":
                    return R.drawable.ic_expense_food;
                case "交通":
                    return R.drawable.ic_expense_transport;
                case "购物":
                    return R.drawable.ic_expense_shopping;
                case "娱乐":
                    return R.drawable.ic_expense_entertainment;
                case "医疗":
                    return R.drawable.ic_expense_medical;
                case "教育":
                    return R.drawable.ic_expense_education;
                case "住房":
                    return R.drawable.ic_expense_housing;
                case "其他支出":
                default:
                    return R.drawable.ic_expense_other;
            }
        }
    }

    /**
     * 获取类别的默认颜色
     * @param isIncome 是否为收入
     * @return 颜色值
     */
    public static int getCategoryColor(boolean isIncome) {
        return isIncome ? 0xFF4CAF50 : 0xFFF44336; // 绿色(收入) : 红色(支出)
    }
}