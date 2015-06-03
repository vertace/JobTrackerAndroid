package com.tt.jobtracker;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;

import com.tt.adapters.FullScreenImageAdapter;
import com.tt.fragments.TaskLineItemDetailFragment;

/**
 * Created by BS-308 on 6/3/2015.
 */
public class FullScreenViewActivity extends Activity {

    private FullScreenImageAdapter adapter;
    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen_image_view);

        viewPager = (ViewPager) findViewById(R.id.pager);

        //utils = new Utils(getApplicationContext());

        Intent i = getIntent();
        int position = i.getIntExtra("position", 0);

        adapter = new FullScreenImageAdapter(FullScreenViewActivity.this, TaskLineItemDetailFragment.imageArray);

        viewPager.setAdapter(adapter);

        // displaying selected image first
        viewPager.setCurrentItem(position);
    }

}
