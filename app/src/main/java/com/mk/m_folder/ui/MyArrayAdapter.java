package com.mk.m_folder.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.mk.m_folder.R;

public class MyArrayAdapter extends ArrayAdapter<String> {

    private final Context context;
    private final String[] values;

    public static int selectedItemPosition = 100;

    private static final String TAG = "MainActivity";

    public MyArrayAdapter(Context context, String[] values) {
        super(context, -1, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.list_item, parent, false);
        TextView textView = rowView.findViewById(R.id.textView);
        textView.setText(values[position]);

        // Change color if this item is selected
        if (position == selectedItemPosition) {
            rowView.setBackgroundColor(ContextCompat.getColor(context, R.color.selected_item_color));
            textView.setTextColor(ContextCompat.getColor(context, R.color.default_item_color));
        } else {
            // Set default color for unselected items
            rowView.setBackgroundColor(ContextCompat.getColor(context, R.color.default_item_color));
            textView.setTextColor(ContextCompat.getColor(context, R.color.default_item_text_color));
        }

        return rowView;
    }

}
