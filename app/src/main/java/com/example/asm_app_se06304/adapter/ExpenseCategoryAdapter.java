package com.example.asm_app_se06304.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.asm_app_se06304.model.Category;

import java.text.DecimalFormat;
import java.util.List;

public class ExpenseCategoryAdapter extends ArrayAdapter<Category> {

    private List<Category> categories;

    public ExpenseCategoryAdapter(Context context, List<Category> categories) {
        super(context, 0, categories);
        this.categories = categories;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
        }

        Category category = getItem(position);

        TextView tvName = convertView.findViewById(android.R.id.text1);
        TextView tvAmount = convertView.findViewById(android.R.id.text2);

        tvName.setText(category.getCategoryName());
        DecimalFormat formatter = new DecimalFormat("#,### VNĐ");
        tvAmount.setText(formatter.format(category.getTotalAmount()));

        return convertView;
    }

    public void updateCategories(List<Category> newCategories) {
        this.categories.clear();
        this.categories.addAll(newCategories);
        notifyDataSetChanged();
    }
}