package com.achenging.sbprogressview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.achenging.view.SBProgressView;

public class MainActivity extends AppCompatActivity {

    SBProgressView mSBProgressView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSBProgressView = findViewById(R.id.sbView);
    }

    public void startAnimation(View view) {
        mSBProgressView.startAnimation();
    }

    public void stopAnimation(View view) {
        mSBProgressView.stopAnimation();
    }


    public void pauseAnimation(View view) {
        mSBProgressView.pauseAnimation();
    }

    public void resumeAnimation(View view) {
        mSBProgressView.resumeAnimation();
    }
}