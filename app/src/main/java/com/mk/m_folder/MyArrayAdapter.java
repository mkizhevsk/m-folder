package com.mk.m_folder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class MyArrayAdapter extends ArrayAdapter<String> {

    private final Context context;
    private final String[] values;

    public static int selectedItemPosition = 100;
    //boolean newAdapter;

    private static final String TAG = "MainActivity";

    public MyArrayAdapter(Context context, String[] values) {
        super(context, -1, values);
        this.context = context;
        this.values = values;
        //this.newAdapter = true;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //Log.d(TAG, String.valueOf("maa " + position));
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.list_item, parent, false);
        TextView textView = (TextView) rowView.findViewById(R.id.textView);
        textView.setText(values[position]);

//        if(position == selectedItemPosition) {
//            textView.setTextColor(Color.parseColor("#FFFFFF"));
//        }

        return rowView;
    }

}
