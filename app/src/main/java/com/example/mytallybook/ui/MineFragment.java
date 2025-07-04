package com.example.mytallybook.ui;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mytallybook.R;
import com.example.mytallybook.adapter.ExpenseRecordAdapter;
import com.example.mytallybook.model.ExpenseRecord;
import com.example.mytallybook.ui.auth.LoginActivity;
import com.example.mytallybook.ui.settings.SettingsActivity;
import com.example.mytallybook.util.ShareUtil;
import com.example.mytallybook.viewmodel.ExpenseViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MineFragment extends Fragment {

    private ExpenseViewModel viewModel;
    private RecyclerView recyclerView;
    private ExpenseRecordAdapter adapter;
    private TextView textViewTotalIncome, textViewTotalExpense, textViewTotalBalance;
    private FloatingActionButton fabShare;
    private TextView textViewUsername; // 添加用户名显示
    private MaterialButton buttonLogout; // 添加退出登录按钮
    private MaterialButton buttonSettings; // 添加设置按钮
    private ImageButton buttonRefreshMine; // 添加刷新按钮
    
    private List<ExpenseRecord> recordList;
    private DecimalFormat decimalFormat = new DecimalFormat("¥#,##0.00");
    private Date startDate; // 开始日期，用于获取统计数据
    private Date endDate; // 结束日期，用于获取统计数据

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mine, container, false);
        
        // 初始化视图
        textViewTotalIncome = view.findViewById(R.id.textViewTotalIncome);
        textViewTotalExpense = view.findViewById(R.id.textViewTotalExpense);
        textViewTotalBalance = view.findViewById(R.id.textViewTotalBalance);
        recyclerView = view.findViewById(R.id.recyclerViewDetailRecords);
        fabShare = view.findViewById(R.id.fabShare);
        textViewUsername = view.findViewById(R.id.textViewUsername); 
        buttonLogout = view.findViewById(R.id.buttonLogout); 
        buttonSettings = view.findViewById(R.id.buttonSettings);
        buttonRefreshMine = view.findViewById(R.id.buttonRefreshMine); // 初始化刷新按钮
        
        // 设置RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ExpenseRecordAdapter();
        recyclerView.setAdapter(adapter);
        
        // 显示当前登录用户名
        SharedPreferences prefs = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        String username = prefs.getString("username", "用户");
        textViewUsername.setText(username);
        
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // 初始化ViewModel
        viewModel = new ViewModelProvider(requireActivity()).get(ExpenseViewModel.class);
        
        // 创建日期范围，用于获取所有时间段的收支统计
        setupDateRange();
        
        // 观察数据变化
        setupObservers();
        
        // 设置点击事件监听
        adapter.setOnItemClickListener(record -> showShareDialog(record));
        
        // 设置分享按钮监听
        fabShare.setOnClickListener(v -> shareAllRecords());
        
        // 添加退出登录按钮点击监听
        buttonLogout.setOnClickListener(v -> showLogoutConfirmation());
        
        // 添加设置按钮点击监听
        buttonSettings.setOnClickListener(v -> openSettings());
        
        // 添加刷新按钮点击监听
        buttonRefreshMine.setOnClickListener(v -> refreshData());
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // 页面恢复时刷新数据
        refreshData();
    }
    
    // 设置日期范围
    private void setupDateRange() {
        Calendar calendar = Calendar.getInstance();
        endDate = calendar.getTime(); // 当前日期作为结束日期
        
        calendar.set(Calendar.YEAR, 2000);
        calendar.set(Calendar.MONTH, 0);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        startDate = calendar.getTime(); // 2000年1月1日作为开始日期
    }
    
    // 设置观察者
    private void setupObservers() {
        // 观察数据变化
        viewModel.getAllRecords().observe(getViewLifecycleOwner(), records -> {
            recordList = records;
            adapter.setExpenseRecords(records);
        });
        
        // 观察总收入，添加日期参数
        viewModel.getTotalIncome(startDate, endDate).observe(getViewLifecycleOwner(), income -> {
            double incomeValue = income != null ? income : 0.0;
            textViewTotalIncome.setText(decimalFormat.format(incomeValue));
            updateTotalBalance();
        });
        
        // 观察总支出，添加日期参数
        viewModel.getTotalExpense(startDate, endDate).observe(getViewLifecycleOwner(), expense -> {
            double expenseValue = expense != null ? expense : 0.0;
            textViewTotalExpense.setText(decimalFormat.format(expenseValue));
            updateTotalBalance();
        });
    }
    
    // 刷新数据
    private void refreshData() {
        if (getContext() == null || viewModel == null) return;
        
        // 显示加载中提示
        Toast.makeText(getContext(), "正在刷新数据...", Toast.LENGTH_SHORT).show();
        
        // 手动触发数据刷新
        viewModel.refreshData();
        
        // 刷新完成提示
        Toast.makeText(getContext(), "数据已刷新", Toast.LENGTH_SHORT).show();
    }
    
    // 打开设置页面
    private void openSettings() {
        Intent intent = new Intent(getActivity(), SettingsActivity.class);
        startActivity(intent);
    }
    
    // 显示退出登录确认对话框
    private void showLogoutConfirmation() {
        new AlertDialog.Builder(requireContext())
            .setTitle("退出登录")
            .setMessage("确定要退出当前账户吗？")
            .setPositiveButton("确定", (dialog, which) -> {
                logout();
            })
            .setNegativeButton("取消", null)
            .show();
    }
    
    // 退出登录
    private void logout() {
        // 清除用户会话数据
        SharedPreferences.Editor editor = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE).edit();
        editor.clear();
        editor.apply();
        
        // 跳转到登录页面
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
        
        Toast.makeText(requireContext(), "已退出登录", Toast.LENGTH_SHORT).show();
    }
    
    // 计算并更新总余额
    private void updateTotalBalance() {
        try {
            String incomeStr = textViewTotalIncome.getText().toString().replace("¥", "").replace(",", "");
            String expenseStr = textViewTotalExpense.getText().toString().replace("¥", "").replace(",", "");
            
            double income = Double.parseDouble(incomeStr);
            double expense = Double.parseDouble(expenseStr);
            double balance = income - expense;
            
            textViewTotalBalance.setText(decimalFormat.format(balance));
        } catch (Exception e) {
            textViewTotalBalance.setText("¥0.00");
        }
    }
    
    // 分享单条记录
    private void showShareDialog(ExpenseRecord record) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA);
        String type = record.isIncome() ? "收入" : "支出";
        String message = "【" + type + "】" + record.getCategory() + 
                "\n金额：" + decimalFormat.format(record.getAmount()) + 
                "\n时间：" + sdf.format(record.getDate()) +
                (record.getNote().isEmpty() ? "" : "\n备注：" + record.getNote());
        
        showSharePlatformDialog(message, "分享记账明细");
    }
    
    // 分享所有记录
    private void shareAllRecords() {
        if (recordList == null || recordList.isEmpty()) {
            Toast.makeText(requireContext(), "没有记录可分享", Toast.LENGTH_SHORT).show();
            return;
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("我的记账本\n\n");
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA);
        
        // 添加统计信息
        try {
            String incomeStr = textViewTotalIncome.getText().toString();
            String expenseStr = textViewTotalExpense.getText().toString();
            String balanceStr = textViewTotalBalance.getText().toString();
            
            sb.append("总收入：").append(incomeStr).append("\n");
            sb.append("总支出：").append(expenseStr).append("\n");
            sb.append("净资产：").append(balanceStr).append("\n\n");
        } catch (Exception e) {
            // 忽略错误
        }
        
        sb.append("明细记录：\n");
        for (ExpenseRecord record : recordList) {
            String type = record.isIncome() ? "收入" : "支出";
            sb.append("--------------------\n");
            sb.append("【").append(type).append("】").append(record.getCategory()).append("\n");
            sb.append("金额：").append(decimalFormat.format(record.getAmount())).append("\n");
            sb.append("时间：").append(sdf.format(record.getDate())).append("\n");
            if (!record.getNote().isEmpty()) {
                sb.append("备注：").append(record.getNote()).append("\n");
            }
        }
        
        showSharePlatformDialog(sb.toString(), "分享所有记账记录");
    }
    
    /**
     * 显示分享平台选择对话框
     * @param content 要分享的内容
     * @param title 分享标题
     */
    private void showSharePlatformDialog(String content, String title) {
        if (getContext() == null) return;
        
        Dialog dialog = new Dialog(getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_share);
        dialog.setCancelable(true);
        
        LinearLayout layoutShareWechat = dialog.findViewById(R.id.layoutShareWechat);
        LinearLayout layoutShareQQ = dialog.findViewById(R.id.layoutShareQQ);
        LinearLayout layoutShareMore = dialog.findViewById(R.id.layoutShareMore);
        
        // 设置微信分享
        layoutShareWechat.setOnClickListener(v -> {
            boolean success = ShareUtil.shareToWeChat(requireContext(), content, title);
            if (success) {
                dialog.dismiss();
            }
        });
        
        // 设置QQ分享
        layoutShareQQ.setOnClickListener(v -> {
            boolean success = ShareUtil.shareToQQ(requireContext(), content, title);
            if (success) {
                dialog.dismiss();
            }
        });
        
        // 设置更多分享选项（使用系统分享菜单）
        layoutShareMore.setOnClickListener(v -> {
            ShareUtil.shareTextBySystem(requireContext(), content, title);
            dialog.dismiss();
        });
        
        dialog.show();
    }
}