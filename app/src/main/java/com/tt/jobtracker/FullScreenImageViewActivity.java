package com.tt.jobtracker;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;

import com.example.sstracker.R;
import com.tt.adapters.FullScreenImageAdapter;
import com.tt.helpers.Utility;

import java.util.ArrayList;


public class FullScreenImageViewActivity extends Activity {
    ViewPager viewPager;
    Utility utils;
    FullScreenImageAdapter adapter;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen_image_view);

        viewPager = (ViewPager) findViewById(R.id.pager);


        Intent i = getIntent();
        int position = i.getIntExtra("Position", 0);
        ArrayList<String> imageList = i.getStringArrayListExtra("ImageList");
        utils = new Utility(getApplicationContext());
        adapter = new FullScreenImageAdapter(FullScreenImageViewActivity.this, imageList);

        viewPager.setAdapter(adapter);

        // displaying selected image first
        viewPager.setCurrentItem(position);
    }
}
