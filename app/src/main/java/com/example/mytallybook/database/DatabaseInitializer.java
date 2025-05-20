package com.example.mytallybook.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

/**
 * 数据库初始化工具类
 * 用于清空数据库和创建默认用户
 */
public class DatabaseInitializer {
    private static final String TAG = "DatabaseInitializer";

    /**
     * 重置数据库，清空所有数据并创建默认用户
     * @param context 应用上下文
     * @param listener 初始化完成的回调
     */
    public static void resetDatabase(Context context, DatabaseInitListener listener) {
        new ResetDatabaseTask(context, listener).execute();
    }

    /**
     * 仅清空记账数据，保留用户账户
     * @param context 应用上下文
     * @param listener 初始化完成的回调
     */
    public static void clearExpenseRecordsOnly(Context context, DatabaseInitListener listener) {
        new ClearExpenseRecordsTask(context, listener).execute();
    }

    /**
     * 初始化完成回调接口
     */
    public interface DatabaseInitListener {
        void onInitCompleted(boolean success);
    }

    /**
     * 重置整个数据库的异步任务
     */
    private static class ResetDatabaseTask extends AsyncTask<Void, Void, Boolean> {
        private Context context;
        private DatabaseInitListener listener;

        public ResetDatabaseTask(Context context, DatabaseInitListener listener) {
            this.context = context;
            this.listener = listener;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                DatabaseHelper dbHelper = DatabaseHelper.getInstance(context);
                // 清空数据库中所有表的数据
                dbHelper.clearAllData();
                // 创建默认用户
                dbHelper.createDefaultUser();
                return true;
            } catch (Exception e) {
                Log.e(TAG, "重置数据库失败: " + e.getMessage());
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                Toast.makeText(context, "数据库已重置，默认管理员账户已创建", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "数据库重置失败", Toast.LENGTH_SHORT).show();
            }

            if (listener != null) {
                listener.onInitCompleted(success);
            }
        }
    }

    /**
     * 仅清空记账记录的异步任务
     */
    private static class ClearExpenseRecordsTask extends AsyncTask<Void, Void, Boolean> {
        private Context context;
        private DatabaseInitListener listener;

        public ClearExpenseRecordsTask(Context context, DatabaseInitListener listener) {
            this.context = context;
            this.listener = listener;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                DatabaseHelper dbHelper = DatabaseHelper.getInstance(context);
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                db.beginTransaction();
                try {
                    // 仅删除支出记录表中的数据
                    db.execSQL("DELETE FROM " + DatabaseHelper.TABLE_EXPENSE_RECORDS);
                    // 重置自增ID
                    db.execSQL("DELETE FROM sqlite_sequence WHERE name='" + DatabaseHelper.TABLE_EXPENSE_RECORDS + "'");
                    db.setTransactionSuccessful();
                    return true;
                } finally {
                    db.endTransaction();
                }
            } catch (Exception e) {
                Log.e(TAG, "清空记账数据失败: " + e.getMessage());
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                Toast.makeText(context, "所有记账记录已清空", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "清空记账记录失败", Toast.LENGTH_SHORT).show();
            }

            if (listener != null) {
                listener.onInitCompleted(success);
            }
        }
    }
}