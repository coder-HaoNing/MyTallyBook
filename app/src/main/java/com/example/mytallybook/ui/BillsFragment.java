package com.example.mytallybook.ui;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mytallybook.R;
import com.example.mytallybook.adapter.ExpenseRecordAdapter;
import com.example.mytallybook.model.ExpenseRecord;
import com.example.mytallybook.viewmodel.ExpenseViewModel;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class BillsFragment extends Fragment implements ExpenseRecordAdapter.OnDeleteClickListener {

    private ExpenseViewModel expenseViewModel;
    private RecyclerView recyclerView;
    private ExpenseRecordAdapter adapter;
    private TextView textViewIncome, textViewExpense, textViewBalance;
    private TextView textViewCurrentMonth, textViewMonthStatistic;
    private ImageButton buttonPrevMonth, buttonNextMonth;
    private DecimalFormat decimalFormat = new DecimalFormat("¥#,##0.00");
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA);
    private SimpleDateFormat monthFormat = new SimpleDateFormat("yyyy年M月", Locale.CHINA);

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bills, container, false);
        
        // 初始化视图
        textViewIncome = view.findViewById(R.id.textViewIncome);
        textViewExpense = view.findViewById(R.id.textViewExpense);
        textViewBalance = view.findViewById(R.id.textViewBalance);
        textViewCurrentMonth = view.findViewById(R.id.textViewCurrentMonth);
        textViewMonthStatistic = view.findViewById(R.id.textViewMonthStatistic);
        buttonPrevMonth = view.findViewById(R.id.buttonPrevMonth);
        buttonNextMonth = view.findViewById(R.id.buttonNextMonth);
        recyclerView = view.findViewById(R.id.recyclerViewRecords);
        
        // 设置RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ExpenseRecordAdapter();
        recyclerView.setAdapter(adapter);
        
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // 初始化ViewModel
        expenseViewModel = new ViewModelProvider(requireActivity()).get(ExpenseViewModel.class);
        
        // 设置月份选择器
        setupMonthSelector();
        
        // 观察按月筛选的数据变化
        observeMonthlyData();
        
        // 设置列表项点击事件
        adapter.setOnItemClickListener(this::showDetailDialog);
        
        // 设置删除按钮点击事件
        adapter.setOnDeleteClickListener(this);
    }
    
    // 设置月份选择器
    private void setupMonthSelector() {
        // 显示当前选择的月份
        Date selectedMonth = expenseViewModel.getSelectedMonthDate();
        if (selectedMonth != null) {
            textViewCurrentMonth.setText(monthFormat.format(selectedMonth));
            textViewMonthStatistic.setText(monthFormat.format(selectedMonth) + "统计");
        }
        
        // 设置上个月按钮点击事件
        buttonPrevMonth.setOnClickListener(v -> {
            expenseViewModel.selectPreviousMonth();
            Date newMonth = expenseViewModel.getSelectedMonthDate();
            textViewCurrentMonth.setText(monthFormat.format(newMonth));
            textViewMonthStatistic.setText(monthFormat.format(newMonth) + "统计");
        });
        
        // 设置下个月按钮点击事件
        buttonNextMonth.setOnClickListener(v -> {
            expenseViewModel.selectNextMonth();
            Date newMonth = expenseViewModel.getSelectedMonthDate();
            textViewCurrentMonth.setText(monthFormat.format(newMonth));
            textViewMonthStatistic.setText(monthFormat.format(newMonth) + "统计");
        });
    }
    
    // 观察按月筛选的数据
    private void observeMonthlyData() {
        // 观察当前月份的记录
        expenseViewModel.getRecordsByMonth().observe(getViewLifecycleOwner(), records -> {
            adapter.setExpenseRecords(records);
        });
        
        // 观察当前月份的总收入
        expenseViewModel.getMonthlyIncome().observe(getViewLifecycleOwner(), income -> {
            double incomeValue = income != null ? income : 0.0;
            textViewIncome.setText(decimalFormat.format(incomeValue));
            updateBalance();
        });
        
        // 观察当前月份的总支出
        expenseViewModel.getMonthlyExpense().observe(getViewLifecycleOwner(), expense -> {
            double expenseValue = expense != null ? expense : 0.0;
            textViewExpense.setText(decimalFormat.format(expenseValue));
            updateBalance();
        });
    }
    
    // 显示详情弹窗
    private void showDetailDialog(ExpenseRecord record) {
        if (getContext() == null) return;
        
        Dialog dialog = new Dialog(getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_expense_detail);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        
        // 获取弹窗中的视图
        TextView textViewDetailType = dialog.findViewById(R.id.textViewDetailType);
        TextView textViewDetailAmount = dialog.findViewById(R.id.textViewDetailAmount);
        TextView textViewDetailCategory = dialog.findViewById(R.id.textViewDetailCategory);
        TextView textViewDetailDate = dialog.findViewById(R.id.textViewDetailDate);
        TextView textViewDetailNote = dialog.findViewById(R.id.textViewDetailNote);
        LinearLayout layoutNote = dialog.findViewById(R.id.layoutNote);
        Button buttonClose = dialog.findViewById(R.id.buttonClose);
        Button buttonEdit = dialog.findViewById(R.id.buttonEdit);
        Button buttonDelete = dialog.findViewById(R.id.buttonDelete);
        Button buttonSave = dialog.findViewById(R.id.buttonSave);
        
        // 编辑模式下的输入控件
        LinearLayout layoutViewMode = dialog.findViewById(R.id.layoutViewMode);
        LinearLayout layoutEditMode = dialog.findViewById(R.id.layoutEditMode);
        EditText editTextAmount = dialog.findViewById(R.id.editTextAmount);
        Spinner spinnerCategory = dialog.findViewById(R.id.spinnerCategory);
        Spinner spinnerType = dialog.findViewById(R.id.spinnerType);
        EditText editTextNote = dialog.findViewById(R.id.editTextNote);
        Button buttonSelectDate = dialog.findViewById(R.id.buttonSelectDate);
        TextView textViewSelectedDate = dialog.findViewById(R.id.textViewSelectedDate);
        
        // 初始状态：查看模式
        layoutViewMode.setVisibility(View.VISIBLE);
        layoutEditMode.setVisibility(View.GONE);
        buttonSave.setVisibility(View.GONE);
        buttonEdit.setVisibility(View.VISIBLE);
        buttonDelete.setVisibility(View.VISIBLE);
        
        // 设置弹窗内容
        textViewDetailType.setText(record.isIncome() ? "收入" : "支出");
        textViewDetailCategory.setText(record.getCategory());
        textViewDetailDate.setText(dateFormat.format(record.getDate()));
        
        // 设置金额，根据收入/支出设置颜色
        String amountText = record.isIncome() ? 
                "+" + decimalFormat.format(record.getAmount()) : 
                "-" + decimalFormat.format(record.getAmount());
        textViewDetailAmount.setText(amountText);
        textViewDetailAmount.setTextColor(record.isIncome() ? 0xFF4CAF50 : 0xFFF44336);
        
        // 设置备注，如果没有备注则隐藏该部分
        String note = record.getNote();
        if (note != null && !note.trim().isEmpty()) {
            layoutNote.setVisibility(View.VISIBLE);
            textViewDetailNote.setText(note);
        } else {
            layoutNote.setVisibility(View.GONE);
        }
        
        // 保存当前日期，用于编辑模式
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(record.getDate());
        
        // 设置关闭按钮点击事件
        buttonClose.setOnClickListener(v -> dialog.dismiss());
        
        // 设置删除按钮点击事件
        buttonDelete.setOnClickListener(v -> {
            expenseViewModel.delete(record);
            Toast.makeText(getContext(), "已删除记录", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });
        
        // 设置编辑按钮点击事件
        buttonEdit.setOnClickListener(v -> {
            // 切换到编辑模式
            layoutViewMode.setVisibility(View.GONE);
            layoutEditMode.setVisibility(View.VISIBLE);
            buttonSave.setVisibility(View.VISIBLE);
            buttonEdit.setVisibility(View.GONE);
            buttonDelete.setVisibility(View.GONE);
            
            // 设置编辑框的初始值
            editTextAmount.setText(String.valueOf(record.getAmount()));
            editTextNote.setText(record.getNote());
            
            // 设置日期选择按钮的初始显示
            textViewSelectedDate.setText(dateFormat.format(record.getDate()));
            
            // 设置类型选择器（收入/支出）
            String[] types = {"支出", "收入"};
            android.widget.ArrayAdapter<String> typeAdapter = new android.widget.ArrayAdapter<>(
                    getContext(), android.R.layout.simple_spinner_item, types);
            typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerType.setAdapter(typeAdapter);
            spinnerType.setSelection(record.isIncome() ? 1 : 0);
            
            // 根据类型（收入/支出）加载对应的类别
            String[] categories = record.isIncome() ? 
                    getResources().getStringArray(R.array.income_categories) : 
                    getResources().getStringArray(R.array.expense_categories);
            
            android.widget.ArrayAdapter<String> categoryAdapter = new android.widget.ArrayAdapter<>(
                    getContext(), android.R.layout.simple_spinner_item, categories);
            categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerCategory.setAdapter(categoryAdapter);
            
            // 设置当前类别
            for (int i = 0; i < categories.length; i++) {
                if (categories[i].equals(record.getCategory())) {
                    spinnerCategory.setSelection(i);
                    break;
                }
            }
            
            // 监听类型变化，动态更新类别选项
            spinnerType.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                    boolean isIncome = position == 1;
                    String[] newCategories = isIncome ? 
                            getResources().getStringArray(R.array.income_categories) : 
                            getResources().getStringArray(R.array.expense_categories);
                    
                    android.widget.ArrayAdapter<String> newAdapter = new android.widget.ArrayAdapter<>(
                            getContext(), android.R.layout.simple_spinner_item, newCategories);
                    newAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerCategory.setAdapter(newAdapter);
                    spinnerCategory.setSelection(0);
                }
                
                @Override
                public void onNothingSelected(android.widget.AdapterView<?> parent) {}
            });
            
            // 设置日期选择按钮点击事件
            buttonSelectDate.setOnClickListener(v2 -> {
                // 显示日期选择器
                android.app.DatePickerDialog datePickerDialog = new android.app.DatePickerDialog(
                        getContext(),
                        (view, year, month, dayOfMonth) -> {
                            calendar.set(Calendar.YEAR, year);
                            calendar.set(Calendar.MONTH, month);
                            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                            
                            // 显示时间选择器
                            android.app.TimePickerDialog timePickerDialog = new android.app.TimePickerDialog(
                                    getContext(),
                                    (view2, hourOfDay, minute) -> {
                                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                        calendar.set(Calendar.MINUTE, minute);
                                        
                                        // 更新显示的日期和时间
                                        textViewSelectedDate.setText(dateFormat.format(calendar.getTime()));
                                    },
                                    calendar.get(Calendar.HOUR_OF_DAY),
                                    calendar.get(Calendar.MINUTE),
                                    true);
                            timePickerDialog.show();
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.show();
            });
        });
        
        // 设置保存按钮点击事件
        buttonSave.setOnClickListener(v -> {
            try {
                // 获取编辑后的值
                double amount = Double.parseDouble(editTextAmount.getText().toString());
                boolean isIncome = spinnerType.getSelectedItemPosition() == 1;
                String category = spinnerCategory.getSelectedItem().toString();
                String newNote = editTextNote.getText().toString();
                Date newDate = calendar.getTime();
                
                // 更新记录
                ExpenseRecord updatedRecord = new ExpenseRecord(amount, isIncome, category, newDate, newNote);
                updatedRecord.setId(record.getId()); // 保留原记录ID
                
                expenseViewModel.update(updatedRecord);
                Toast.makeText(getContext(), "已更新记录", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "请输入有效金额", Toast.LENGTH_SHORT).show();
            }
        });
        
        // 显示弹窗
        dialog.show();
    }
    
    // 计算并更新余额
    private void updateBalance() {
        try {
            String incomeStr = textViewIncome.getText().toString().replace("¥", "").replace(",", "");
            String expenseStr = textViewExpense.getText().toString().replace("¥", "").replace(",", "");
            
            double income = Double.parseDouble(incomeStr);
            double expense = Double.parseDouble(expenseStr);
            double balance = income - expense;
            
            textViewBalance.setText(decimalFormat.format(balance));
        } catch (Exception e) {
            textViewBalance.setText("¥0.00");
        }
    }
    
    // 实现OnDeleteClickListener接口
    @Override
    public void onDeleteClick(ExpenseRecord record) {
        if (record != null) {
            expenseViewModel.delete(record);
            Toast.makeText(getContext(), "已删除记录", Toast.LENGTH_SHORT).show();
        }
    }
}