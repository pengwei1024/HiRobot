package com.apkfuns.hi.robot;

import android.content.Intent;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.click_func).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.click_func:
                Intent killIntent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                startActivity(killIntent);
                break;
            default:
                break;
        }
    }
}
