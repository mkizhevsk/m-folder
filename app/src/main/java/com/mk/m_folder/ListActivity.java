package com.mk.m_folder;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;

public class ListActivity extends Activity {

    @Override
    public void onCreate (Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text);

        Intent intent = getIntent();
        String wrongSongsText = intent.getStringExtra("content");

        TextView textView = findViewById(R.id.textField);
        textView.setMovementMethod(new ScrollingMovementMethod());
        textView.setText(wrongSongsText);
    }
}
