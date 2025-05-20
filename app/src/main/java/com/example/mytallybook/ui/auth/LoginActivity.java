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
import com.example.mytallybook.database.UserDataAccess;
import com.example.mytallybook.model.User;
import com.example.mytallybook.viewmodel.UserViewModel;
import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {
    
    private UserViewModel userViewModel;
    private TextInputEditText editTextUsernameOrPhone, editTextPassword;
    private Button buttonLogin;
    private TextView textViewRegister;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        
        // 初始化视图
        editTextUsernameOrPhone = findViewById(R.id.editTextPhone);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        textViewRegister = findViewById(R.id.textViewRegister);
        
        // 初始化ViewModel
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        
        // 检查用户是否已登录
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        if (prefs.contains("username")) {
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
        String usernameOrPhone = editTextUsernameOrPhone.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        
        // 输入验证
        if (TextUtils.isEmpty(usernameOrPhone)) {
            editTextUsernameOrPhone.setError("请输入用户名或手机号");
            return;
        }
        
        if (TextUtils.isEmpty(password)) {
            editTextPassword.setError("请输入密码");
            return;
        }
        
        // 显示加载进度
        buttonLogin.setEnabled(false);
        buttonLogin.setText("登录中...");
        
        // 尝试登录，允许使用用户名或手机号
        userViewModel.loginWithPhoneOrUsername(usernameOrPhone, password, new UserDataAccess.GetUserCallback() {
            @Override
            public void onUserFound(User user) {
                runOnUiThread(() -> {
                    // 登录成功，保存用户信息
                    saveUserSession(user);
                    
                    // 重置按钮状态
                    buttonLogin.setEnabled(true);
                    buttonLogin.setText("登录");
                    
                    // 跳转到主页面
                    navigateToMainActivity();
                });
            }

            @Override
            public void onUserNotFound() {
                runOnUiThread(() -> {
                    // 登录失败
                    Toast.makeText(LoginActivity.this, "用户名/手机号或密码不正确", Toast.LENGTH_SHORT).show();
                    
                    // 重置按钮状态
                    buttonLogin.setEnabled(true);
                    buttonLogin.setText("登录");
                });
            }
        });
    }
    
    private void saveUserSession(User user) {
        SharedPreferences.Editor editor = getSharedPreferences("user_prefs", MODE_PRIVATE).edit();
        editor.putString("username", user.getUsername());
        editor.putInt("user_id", user.getId());
        editor.putString("phone_number", user.getPhoneNumber()); // 保存手机号
        editor.apply();
        
        // 设置当前用户
        userViewModel.setCurrentUser(user);
    }
    
    private void navigateToMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}