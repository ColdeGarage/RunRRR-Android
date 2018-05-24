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

import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerTitleStrip;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;


/**
 * Provides UI for the main screen.
 */
public class ViewPagerActivity extends AppCompatActivity{
    //private boolean tabletSize;
    private FragmentManager fm;
    private ViewPager viewPager;
    private PagerTitleStrip pagerTitleStrip;

    //static int bar_height;
    //static int tab_height;
    private TabLayout tabLayout;
    /*private View pager_bar;
    private ImageView tab_map;
    private ImageView tab_mission;
    private ImageView tab_bag;
    private ImageView tab_more;*/
    private TextView title;
    //static int iCurrPage = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_pager);

        fm = getSupportFragmentManager();
        //tab
        /*tab_map = (ImageView) findViewById(R.id.tab_map);
        tab_mission = (ImageView) findViewById(R.id.tab_mission);
        tab_bag = (ImageView) findViewById(R.id.tab_bag);
        tab_more = (ImageView) findViewById(R.id.tab_more);
        pager_bar = findViewById(R.id.pager_bar);*/
        //viewPager
        title = (TextView) findViewById(R.id.title);
        title.setText("地圖");
        viewPager = (ViewPager) findViewById(R.id.viewPager);
        tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        //tabLayout.setupWithViewPager(viewPager);
        tabLayout.setSmoothScrollingEnabled(true);

        FragmentPagerAdapter adapter = new FragPagerAdapter(fm);
        //
        LayoutInflater inflater = this.getLayoutInflater();

        TabLayout.Tab tab = tabLayout.newTab();
        View view = inflater.inflate(R.layout.tab, null);
        tab.setCustomView(view);
        ImageView imageView = (ImageView) view.findViewById(R.id.imageView);
        imageView.setImageResource(R.drawable.map_icon);
        tabLayout.addTab(tab);

        tab = tabLayout.newTab();
        view = inflater.inflate(R.layout.tab, null);
        tab.setCustomView(view);
        imageView = (ImageView) view.findViewById(R.id.imageView);
        imageView.setImageResource(R.drawable.mission_icon);
        tabLayout.addTab(tab);

        tab = tabLayout.newTab();
        view = inflater.inflate(R.layout.tab, null);
        tab.setCustomView(view);
        imageView = (ImageView) view.findViewById(R.id.imageView);
        imageView.setImageResource(R.drawable.bag_icon);
        tabLayout.addTab(tab);

        tab = tabLayout.newTab();
        view = inflater.inflate(R.layout.tab, null);
        tab.setCustomView(view);
        imageView = (ImageView) view.findViewById(R.id.imageView);
        imageView.setImageResource(R.drawable.more_icon);
        tabLayout.addTab(tab);

        //

        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(viewPager));
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(3);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                tabLayout.getTabAt(position).select();
                switch (position) {
                    case 0:
                        MapsFragment.instance.Refresh();
                        title.setText("地圖");
                        break;
                    case 1:
                        MissionsFragment.instance.Refresh();
                        title.setText("任務");
                        break;
                    case 2:
                        BagFragment.instance.Refresh();
                        title.setText("背包");
                        break;
                    case 3:
                        MoreFragment.instance.Refresh();
                        title.setText("其他");
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }

        // setting the tab size depending on device size

        /*tab_layout = (LinearLayout) findViewById(R.id.tab_layout);
        tabletSize = getResources().getBoolean(R.bool.isTablet);

        if (tabletSize) {
            // convert dip to pixels
            bar_height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, getResources().getDisplayMetrics());
            tab_height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 135, getResources().getDisplayMetrics());
            pager_bar.getLayoutParams().height = bar_height;
            tab_layout.getLayoutParams().height = tab_height;
        } else {
            // convert dip to pixels
            bar_height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics());
            tab_height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 95, getResources().getDisplayMetrics());
            pager_bar.getLayoutParams().height = bar_height;
            tab_layout.getLayoutParams().height = tab_height;
        }*/


     class FragPagerAdapter extends FragmentPagerAdapter {

         public FragPagerAdapter(FragmentManager fragmentManager) {
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
    /*private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }*/
    //show an alert dialog
    /*void Alert(String mes){
        new AlertDialog.Builder(ViewPagerActivity.this)
                .setMessage(mes)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).show();
    }*/
}