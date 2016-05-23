package com.sleepyzzz.photo_selectordemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;

import com.sleepyzzz.photo_selector.activity.PhotoPickerActivity;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    private static final String url = "http://10.10.77.129:8080/DetectionServer/DetectionServlet";

    @Bind(R.id.id_btn)
    Button mIdBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);


    }

    @OnClick(R.id.id_btn)
    public void onClick() {

        PhotoPickerActivity.actionStart(MainActivity.this, 9, null, url);
    }
}
