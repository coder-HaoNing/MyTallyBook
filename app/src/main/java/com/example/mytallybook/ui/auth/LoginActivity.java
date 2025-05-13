package com.example.mytallybook.ui.auth;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.mytallybook.MainActivity;
import com.example.mytallybook.R;
import com.example.mytallybook.viewmodel.UserViewModel;
import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {
    
    private UserViewModel userViewModel;
    private TextInputEditText editTextPhone, editTextPassword;
    private Button buttonLogin;
    private TextView textViewRegister;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        
        // 初始化视图
        editTextPhone = findViewById(R.id.editTextPhone);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        textViewRegister = findViewById(R.id.textViewRegister);
        
        // 初始化ViewModel
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        
        // 检查用户是否已登录
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        if (prefs.contains("phone_number")) {
            // 自动跳转到主页面
            navigateToMainActivity();
        }
        
        // 设置登录按钮点击监听
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });
        
        // 设置注册文本点击监听
        textViewRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 导航到注册页面
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }
    
    private void loginUser() {
        String phoneNumber = editTextPhone.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        
        // 输入验证
        if (TextUtils.isEmpty(phoneNumber)) {
            editTextPhone.setError("请输入手机号");
            return;
        }
        
        if (phoneNumber.length() != 11) {
            editTextPhone.setError("请输入11位手机号码");
            return;
        }
        
        if (TextUtils.isEmpty(password)) {
            editTextPassword.setError("请输入密码");
            return;
        }
        
        // 尝试登录
        userViewModel.login(phoneNumber, password).observe(this, user -> {
            if (user != null) {
                // 登录成功，保存用户信息
                saveUserSession(phoneNumber, user.getUsername());
                // 跳转到主页面
                navigateToMainActivity();
            } else {
                // 登录失败
                Toast.makeText(LoginActivity.this, "手机号或密码不正确", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void saveUserSession(String phoneNumber, String username) {
        SharedPreferences.Editor editor = getSharedPreferences("user_prefs", MODE_PRIVATE).edit();
        editor.putString("phone_number", phoneNumber);
        editor.putString("username", username);
        editor.apply();
    }
    
    private void navigateToMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}