package com.example.mytallybook;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.view.MenuItem;

import com.example.mytallybook.ui.AddRecordFragment;
import com.example.mytallybook.ui.BillsFragment;
import com.example.mytallybook.ui.MineFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    private BottomNavigationView bottomNavigationView;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // 初始化底部导航
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);
        
        // 默认显示账单页面
        if (savedInstanceState == null) {
            bottomNavigationView.setSelectedItemId(R.id.navigation_bills);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment selectedFragment = null;
        
        if (item.getItemId() == R.id.navigation_bills) {
            selectedFragment = new BillsFragment();
        } else if (item.getItemId() == R.id.navigation_add) {
            selectedFragment = new AddRecordFragment();
        } else if (item.getItemId() == R.id.navigation_mine) {
            selectedFragment = new MineFragment();
        }
        
        if (selectedFragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, selectedFragment)
                    .commit();
            
            return true;
        }
        
        return false;
    }
}