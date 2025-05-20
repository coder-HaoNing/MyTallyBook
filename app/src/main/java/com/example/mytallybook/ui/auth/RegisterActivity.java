package com.example.mytallybook.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.mytallybook.R;
import com.example.mytallybook.database.UserDataAccess;
import com.example.mytallybook.model.User;
import com.example.mytallybook.viewmodel.UserViewModel;
import com.google.android.material.textfield.TextInputEditText;

public class RegisterActivity extends AppCompatActivity {
    
    private UserViewModel userViewModel;
    private TextInputEditText editTextPhone, editTextUsername, editTextPassword, editTextConfirmPassword;
    private Button buttonRegister;
    private TextView textViewLogin;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        
        // 初始化视图
        editTextPhone = findViewById(R.id.editTextPhone);
        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        buttonRegister = findViewById(R.id.buttonRegister);
        textViewLogin = findViewById(R.id.textViewLogin);
        
        // 初始化ViewModel
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        
        // 设置注册按钮点击监听
        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });
        
        // 设置登录文本点击监听
        textViewLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 返回登录页面
                finish();
            }
        });
    }
    
    private void registerUser() {
        String phoneNumber = editTextPhone.getText().toString().trim();
        String username = editTextUsername.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String confirmPassword = editTextConfirmPassword.getText().toString().trim();
        
        // 输入验证
        if (TextUtils.isEmpty(phoneNumber)) {
            editTextPhone.setError("请输入手机号");
            return;
        }
        
        if (phoneNumber.length() != 11) {
            editTextPhone.setError("请输入11位手机号码");
            return;
        }
        
        if (TextUtils.isEmpty(username)) {
            editTextUsername.setError("请输入用户名");
            return;
        }
        
        if (TextUtils.isEmpty(password)) {
            editTextPassword.setError("请输入密码");
            return;
        }
        
        if (password.length() < 6) {
            editTextPassword.setError("密码长度至少6位");
            return;
        }
        
        if (!password.equals(confirmPassword)) {
            editTextConfirmPassword.setError("两次输入的密码不一致");
            return;
        }
        
        // 显示加载进度
        buttonRegister.setEnabled(false);
        buttonRegister.setText("注册中...");
        
        // 创建用户并注册
        User newUser = new User(username, password, phoneNumber, null); // 最后一个参数是email，设置为null
        
        userViewModel.register(newUser, new UserDataAccess.InsertCallback() {
            @Override
            public void onInsertSuccess(User user) {
                runOnUiThread(() -> {
                    Toast.makeText(RegisterActivity.this, "注册成功，请登录", Toast.LENGTH_SHORT).show();
                    
                    // 重置按钮状态
                    buttonRegister.setEnabled(true);
                    buttonRegister.setText("注册");
                    
                    // 返回登录页面
                    finish();
                });
            }

            @Override
            public void onInsertFailed(String errorMessage) {
                runOnUiThread(() -> {
                    Toast.makeText(RegisterActivity.this, "注册失败: " + errorMessage, Toast.LENGTH_SHORT).show();
                    
                    if(errorMessage.contains("UNIQUE constraint failed")) {
                        editTextPhone.setError("该手机号或用户名已被注册");
                    }
                    
                    // 重置按钮状态
                    buttonRegister.setEnabled(true);
                    buttonRegister.setText("注册");
                });
            }
        });
    }
}