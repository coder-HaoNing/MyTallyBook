package com.example.mytallybook.ui;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.mytallybook.R;
import com.example.mytallybook.model.ExpenseRecord;
import com.example.mytallybook.viewmodel.ExpenseViewModel;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AddRecordFragment extends Fragment {

    private ExpenseViewModel viewModel;
    private TabLayout tabLayout;
    private RadioGroup radioGroupExpenseCategory;
    private RadioGroup radioGroupIncomeCategory;
    private TextInputEditText editTextAmount;
    private TextInputEditText editTextDate;
    private TextInputEditText editTextNote;
    private Button buttonSave;
    
    private boolean isIncome = false; // 默认为支出
    private Calendar selectedDate = Calendar.getInstance();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA);

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_record, container, false);
        
        // 初始化视图
        tabLayout = view.findViewById(R.id.tabLayout);
        radioGroupExpenseCategory = view.findViewById(R.id.radioGroupExpenseCategory);
        radioGroupIncomeCategory = view.findViewById(R.id.radioGroupIncomeCategory);
        editTextAmount = view.findViewById(R.id.editTextAmount);
        editTextDate = view.findViewById(R.id.editTextDate);
        editTextNote = view.findViewById(R.id.editTextNote);
        buttonSave = view.findViewById(R.id.buttonSave);
        
        // 设置日期为当前日期
        updateDateDisplay();
        
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // 初始化ViewModel
        viewModel = new ViewModelProvider(requireActivity()).get(ExpenseViewModel.class);
        
        // 设置Tab切换监听
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) { // 支出
                    isIncome = false;
                    radioGroupExpenseCategory.setVisibility(View.VISIBLE);
                    radioGroupIncomeCategory.setVisibility(View.GONE);
                } else { // 收入
                    isIncome = true;
                    radioGroupExpenseCategory.setVisibility(View.GONE);
                    radioGroupIncomeCategory.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // 不需要处理
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // 不需要处理
            }
        });
        
        // 设置日期选择器
        editTextDate.setOnClickListener(v -> showDateTimePicker());
        
        // 设置保存按钮监听
        buttonSave.setOnClickListener(v -> saveRecord());
    }
    
    // 显示日期时间选择器
    private void showDateTimePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    selectedDate.set(Calendar.YEAR, year);
                    selectedDate.set(Calendar.MONTH, month);
                    selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    
                    // 接着显示时间选择器
                    TimePickerDialog timePickerDialog = new TimePickerDialog(
                            requireContext(),
                            (view1, hourOfDay, minute) -> {
                                selectedDate.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                selectedDate.set(Calendar.MINUTE, minute);
                                updateDateDisplay();
                            },
                            selectedDate.get(Calendar.HOUR_OF_DAY),
                            selectedDate.get(Calendar.MINUTE),
                            true);
                    timePickerDialog.show();
                },
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }
    
    // 更新日期显示
    private void updateDateDisplay() {
        editTextDate.setText(dateFormat.format(selectedDate.getTime()));
    }
    
    // 保存记录
    private void saveRecord() {
        try {
            // 获取金额
            String amountStr = editTextAmount.getText().toString().trim();
            if (amountStr.isEmpty()) {
                Toast.makeText(requireContext(), "请输入金额", Toast.LENGTH_SHORT).show();
                return;
            }
            double amount = Double.parseDouble(amountStr);
            
            // 获取分类
            RadioGroup activeGroup = isIncome ? radioGroupIncomeCategory : radioGroupExpenseCategory;
            int selectedId = activeGroup.getCheckedRadioButtonId();
            if (selectedId == -1) {
                Toast.makeText(requireContext(), "请选择分类", Toast.LENGTH_SHORT).show();
                return;
            }
            RadioButton radioButton = requireView().findViewById(selectedId);
            String category = radioButton.getText().toString();
            
            // 获取备注
            String note = editTextNote.getText().toString().trim();
            
            // 创建记录并保存
            Date recordDate = selectedDate.getTime();
            ExpenseRecord record = new ExpenseRecord(amount, isIncome, category, recordDate, note);
            viewModel.insert(record);
            
            // 提示保存成功
            Toast.makeText(requireContext(), "记录已保存", Toast.LENGTH_SHORT).show();
            
            // 清空输入
            clearInputs();
            
        } catch (NumberFormatException e) {
            Toast.makeText(requireContext(), "请输入有效金额", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(requireContext(), "保存失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    // 清空输入
    private void clearInputs() {
        editTextAmount.setText("");
        editTextNote.setText("");
        // 默认选择第一个分类
        if (radioGroupExpenseCategory.getVisibility() == View.VISIBLE) {
            ((RadioButton) radioGroupExpenseCategory.getChildAt(0)).setChecked(true);
        } else {
            ((RadioButton) radioGroupIncomeCategory.getChildAt(0)).setChecked(true);
        }
    }
}