package com.sleepyzzz.photo_selector.activity;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;

import com.sleepyzzz.photo_selector.R;
import com.sleepyzzz.photo_selector.view.TopBar;

/**
 * User: datou_SleepyzzZ(SleepyzzZ19911002@126.com)
 * Date: 2016-05-09
 * Time: 14:35
 * FIXME
 */
public class TestActivity extends Activity {

    private TopBar mTopBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.toptest);
        mTopBar = (TopBar) findViewById(R.id.topBar);

        mTopBar.setOnTopBarListener(new TopBar.OnTopBarClickListener() {
            @Override
            public void leftClick() {
                Toast.makeText(getApplicationContext(),
                        "leftClick", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void rightClick() {
                Toast.makeText(getApplicationContext(),
                        "rightClick", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
