package com.example.mytallybook.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mytallybook.R;
import com.example.mytallybook.model.ExpenseRecord;
import com.example.mytallybook.util.CategoryIconUtil;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ExpenseRecordAdapter extends RecyclerView.Adapter<ExpenseRecordAdapter.ExpenseViewHolder> {
    
    private List<ExpenseRecord> expenseRecords = new ArrayList<>();
    private OnItemClickListener listener;
    private OnDeleteClickListener deleteListener;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA);
    private DecimalFormat decimalFormat = new DecimalFormat("¥#,##0.00");
    
    @NonNull
    @Override
    public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_expense_record, parent, false);
        return new ExpenseViewHolder(itemView);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position) {
        ExpenseRecord currentRecord = expenseRecords.get(position);
        
        // 显示日期和类别
        holder.textViewDate.setText(dateFormat.format(currentRecord.getDate()));
        holder.textViewCategory.setText(currentRecord.getCategory());
        holder.textViewCategory.setVisibility(View.VISIBLE);
        
        // 如果有备注则显示，否则隐藏
        String note = currentRecord.getNote();
        if (note != null && !note.trim().isEmpty()) {
            holder.textViewNote.setText(note);
            holder.textViewNote.setVisibility(View.VISIBLE);
        } else {
            holder.textViewNote.setVisibility(View.GONE);
        }
        
        // 设置金额文字颜色
        String amountText;
        if (currentRecord.isIncome()) {
            amountText = "+" + decimalFormat.format(currentRecord.getAmount());
            holder.textViewAmount.setTextColor(CategoryIconUtil.getCategoryColor(true)); // 绿色
        } else {
            amountText = "-" + decimalFormat.format(currentRecord.getAmount());
            holder.textViewAmount.setTextColor(CategoryIconUtil.getCategoryColor(false)); // 红色
        }
        holder.textViewAmount.setText(amountText);
        
        // 设置类别图标
        int iconRes = CategoryIconUtil.getCategoryIcon(currentRecord.getCategory(), currentRecord.isIncome());
        holder.imageViewCategoryIcon.setImageResource(iconRes);
        
        // 设置删除按钮点击事件
        holder.buttonDelete.setOnClickListener(v -> {
            if (deleteListener != null && position != RecyclerView.NO_POSITION) {
                deleteListener.onDeleteClick(expenseRecords.get(position));
            }
        });
    }
    
    @Override
    public int getItemCount() {
        return expenseRecords.size();
    }
    
    public void setExpenseRecords(List<ExpenseRecord> expenseRecords) {
        this.expenseRecords = expenseRecords;
        notifyDataSetChanged();
    }
    
    public ExpenseRecord getExpenseRecordAt(int position) {
        return expenseRecords.get(position);
    }
    
    class ExpenseViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewCategory;
        private TextView textViewDate;
        private TextView textViewNote;
        private TextView textViewAmount;
        private ImageButton buttonDelete;
        private ImageView imageViewCategoryIcon;
        
        public ExpenseViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewCategory = itemView.findViewById(R.id.textViewCategory);
            textViewDate = itemView.findViewById(R.id.textViewDate);
            textViewNote = itemView.findViewById(R.id.textViewNote);
            textViewAmount = itemView.findViewById(R.id.textViewAmount);
            buttonDelete = itemView.findViewById(R.id.buttonDelete);
            imageViewCategoryIcon = itemView.findViewById(R.id.imageViewCategoryIcon);
            
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(expenseRecords.get(position));
                }
            });
        }
    }
    
    public interface OnItemClickListener {
        void onItemClick(ExpenseRecord record);
    }
    
    public interface OnDeleteClickListener {
        void onDeleteClick(ExpenseRecord record);
    }
    
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
    
    public void setOnDeleteClickListener(OnDeleteClickListener listener) {
        this.deleteListener = listener;
    }
}
