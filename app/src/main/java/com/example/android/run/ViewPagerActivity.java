/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.run;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Provides UI for the main screen.
 */
public class ViewPagerActivity extends AppCompatActivity {
    private FragmentManager fm;
    private ViewPager viewPager;
    private TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_pager);

        fm = getSupportFragmentManager();

        viewPager = (ViewPager) findViewById(R.id.viewPager);
        tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        //tabLayout.setupWithViewPager(viewPager);
        tabLayout.setSmoothScrollingEnabled(true);
        tabLayout.setSelectedTabIndicatorColor(0);

        FragmentStatePagerAdapter adapter = new FragPagerStateAdapter(fm);

        LayoutInflater inflater = this.getLayoutInflater();

        TabLayout.Tab tab = tabLayout.newTab();
        View view = inflater.inflate(R.layout.tab, null);
        tab.setCustomView(view);
        ImageView imageView = (ImageView) view.findViewById(R.id.imageView);
        TextView textView = (TextView) view.findViewById(R.id.textView);
        textView.setText("地圖");
        imageView.setImageResource(R.drawable.map_icon);
        tabLayout.addTab(tab);

        tab = tabLayout.newTab();
        view = inflater.inflate(R.layout.tab, null);
        tab.setCustomView(view);
        imageView = (ImageView) view.findViewById(R.id.imageView);
        textView = (TextView) view.findViewById(R.id.textView);
        textView.setText("任務");
        imageView.setImageResource(R.drawable.mission_icon);
        tabLayout.addTab(tab);

        tab = tabLayout.newTab();
        view = inflater.inflate(R.layout.tab, null);
        tab.setCustomView(view);
        imageView = (ImageView) view.findViewById(R.id.imageView);
        textView = (TextView) view.findViewById(R.id.textView);
        textView.setText("背包");
        imageView.setImageResource(R.drawable.bag_icon);
        tabLayout.addTab(tab);

        tab = tabLayout.newTab();
        view = inflater.inflate(R.layout.tab, null);
        tab.setCustomView(view);
        imageView = (ImageView) view.findViewById(R.id.imageView);
        textView = (TextView) view.findViewById(R.id.textView);
        textView.setText("其他");
        imageView.setImageResource(R.drawable.more_icon);
        tabLayout.addTab(tab);

        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(viewPager));
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(3);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

    }

     class FragPagerStateAdapter extends FragmentStatePagerAdapter {

         public FragPagerStateAdapter(FragmentManager fragmentManager) {
             super(fragmentManager);
         }

         @Override
         public int getCount() {
             return 4;
         }

         @Override
         public Fragment getItem(int position) {
             switch (position) {
                case 0:
                    return MapsFragment.getInstance();
                case 1:
                    return MissionsFragment.getInstance();
                case 2:
                    return BagFragment.getInstance();
                case 3:
                    return MoreFragment.getInstance();
                default:
                    return null;
            }
        }
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK)
        {
            // Show home screen when pressing “back” button,
            //  so that this app won’t be closed accidentally
            new AlertDialog.Builder(ViewPagerActivity.this)
                    .setMessage("確定要離開RunRRR嗎?")
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intentHome = new Intent(Intent.ACTION_MAIN);
                            intentHome.addCategory(Intent.CATEGORY_HOME);
                            intentHome.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intentHome);
                        }
                    }).show();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }
}