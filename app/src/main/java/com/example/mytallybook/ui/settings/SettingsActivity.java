package com.example.mytallybook.ui.settings;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.example.mytallybook.R;
import com.example.mytallybook.database.DatabaseInitializer;
import com.example.mytallybook.ui.auth.LoginActivity;

/**
 * 设置界面，包含数据库管理和应用信息
 */
public class SettingsActivity extends AppCompatActivity {

    private CardView cardClearRecords, cardResetDatabase;
    private TextView textViewVersion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // 设置工具栏
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("设置");
        }

        // 初始化视图
        cardClearRecords = findViewById(R.id.cardClearRecords);
        cardResetDatabase = findViewById(R.id.cardResetDatabase);
        textViewVersion = findViewById(R.id.textViewVersion);

        // 设置版本信息
        setVersionInfo();

        // 设置点击事件
        setupClickListeners();
    }

    /**
     * 设置版本信息
     */
    private void setVersionInfo() {
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String versionName = packageInfo.versionName;
            int versionCode = packageInfo.versionCode;
            textViewVersion.setText("v" + versionName + " (" + versionCode + ")");
        } catch (PackageManager.NameNotFoundException e) {
            textViewVersion.setText("未知版本");
        }
    }

    /**
     * 设置点击事件监听
     */
    private void setupClickListeners() {
        // 清空记账数据
        cardClearRecords.setOnClickListener(v -> showClearRecordsConfirmDialog());

        // 重置数据库
        cardResetDatabase.setOnClickListener(v -> showResetDatabaseConfirmDialog());
    }

    /**
     * 显示清空记账数据确认对话框
     */
    private void showClearRecordsConfirmDialog() {
        new AlertDialog.Builder(this)
                .setTitle("清空记账数据")
                .setMessage("确定要删除所有收支记录吗？此操作不可恢复！")
                .setPositiveButton("确定", (dialog, which) -> clearExpenseRecords())
                .setNegativeButton("取消", null)
                .show();
    }

    /**
     * 显示重置数据库确认对话框
     */
    private void showResetDatabaseConfirmDialog() {
        new AlertDialog.Builder(this)
                .setTitle("重置数据库")
                .setMessage("确定要清空所有数据并创建默认管理员账户吗？此操作将删除所有用户和记录，不可恢复！")
                .setPositiveButton("确定", (dialog, which) -> resetDatabase())
                .setNegativeButton("取消", null)
                .show();
    }

    /**
     * 清空记账数据
     */
    private void clearExpenseRecords() {
        DatabaseInitializer.clearExpenseRecordsOnly(this, success -> {
            if (success) {
                // 清空成功后，刷新当前界面
                recreate();
            }
        });
    }

    /**
     * 重置数据库
     */
    private void resetDatabase() {
        DatabaseInitializer.resetDatabase(this, success -> {
            if (success) {
                // 重置成功后，跳转到登录界面
                Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}