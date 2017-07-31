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

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v7.app.AlertDialog;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;


/**
 * Provides UI for the main screen.
 */
public class ViewPagerActivity extends FragmentActivity
{
    private boolean tabletSize;
    private static FragmentManager fm;
    public static NonSwipeableViewPager mViewPager;

    static int bar_height;
    static int tab_height;
    static LinearLayout tab_layout;
    static View pager_bar;
    static ImageView tab_map;
    static ImageView tab_mission;
    static ImageView tab_bag;
    static ImageView tab_more;

    static int iCurrPage = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_pager);

        fm = getSupportFragmentManager();

        // setting the tab size depending on device size
        pager_bar = (View) findViewById(R.id.pager_bar);
        tab_layout = (LinearLayout) findViewById(R.id.tab_layout);
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
        }

        tab_map = (ImageView) findViewById(R.id.tab_map);
        tab_mission = (ImageView) findViewById(R.id.tab_mission);
        tab_bag = (ImageView) findViewById(R.id.tab_bag);
        tab_more = (ImageView) findViewById(R.id.tab_more);

        tab_map.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                switchPage(0);
            }
        });
        tab_mission.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                switchPage(1);
            }
        });
        tab_bag.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                switchPage(2);
            }
        });
        tab_more.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                switchPage(3);
            }
        });

        // setting viewpagers
        mViewPager = (NonSwipeableViewPager) findViewById(R.id.viewpager);
        mViewPager.setOffscreenPageLimit(4); //before setAdapter
        mViewPager.setAdapter(new FragPagerAdapter(fm));
        mViewPager.setCurrentItem(0, false);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        switchPage(0);
    }

    public void switchPage(int index){

        iCurrPage = index;
        final LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );

        if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            Alert("Please check your GPS.");
        }

        if(!isNetworkAvailable()){
            Alert("Please check your internet connection, then try again.");
        }
        else {
            switch( index ) {
                case 0: {
                    System.out.println("gogogogMAPAPAP~");
                    MapsFragment.instance.Refresh();
                    mViewPager.setCurrentItem(0, false);
                    pager_bar.setBackgroundResource(R.color.tab_map);
                    tab_map.setImageResource(R.drawable.tab_map_focused);
                    tab_mission.setImageResource(R.drawable.tab_mission_idle);
                    tab_bag.setImageResource(R.drawable.tab_bag_idle);
                    tab_more.setImageResource(R.drawable.tab_more_idle);
                }
                break;
                case 1: {
                    System.out.println("GOGOGOGOGOGmission~~");
                    MissionsFragment.instance.Refresh();
                    mViewPager.setCurrentItem(1, false);
                    pager_bar.setBackgroundResource(R.color.tab_mission);
                    tab_map.setImageResource(R.drawable.tab_map_idle);
                    tab_mission.setImageResource(R.drawable.tab_mission_focused);
                    tab_bag.setImageResource(R.drawable.tab_bag_idle);
                    tab_more.setImageResource(R.drawable.tab_more_idle);
                }
                break;
                case 2: {
                    System.out.println("gogogogoBAG~");
                    BagFragment.instance.Refresh();
                    mViewPager.setCurrentItem(2, false);
                    pager_bar.setBackgroundResource(R.color.tab_bag);
                    tab_map.setImageResource(R.drawable.tab_map_idle);
                    tab_mission.setImageResource(R.drawable.tab_mission_idle);
                    tab_bag.setImageResource(R.drawable.tab_bag_focused);
                    tab_more.setImageResource(R.drawable.tab_more_idle);
                }
                break;
                case 3: {
                    System.out.println("gogogogMOOORE~");
                    MoreFragment.instance.Refresh();
                    mViewPager.setCurrentItem(3, false);
                    pager_bar.setBackgroundResource(R.color.tab_more);
                    tab_map.setImageResource(R.drawable.tab_map_idle);
                    tab_mission.setImageResource(R.drawable.tab_mission_idle);
                    tab_bag.setImageResource(R.drawable.tab_bag_idle);
                    tab_more.setImageResource(R.drawable.tab_more_focused);
                }
                break;
            }
        }

    }

    static class FragPagerAdapter extends FragmentPagerAdapter {

        private int NUM_ITEMS = 4;

        public FragPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        @Override
        public int getCount() {
            return NUM_ITEMS;
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
            Intent intentHome = new Intent(Intent.ACTION_MAIN);
            intentHome.addCategory(Intent.CATEGORY_HOME);
            intentHome.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intentHome);

            return true;
        }

        return super.onKeyDown(keyCode, event);
    }
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
    //show an alert dialog
    void Alert(String mes){
        new AlertDialog.Builder(ViewPagerActivity.this)
                .setMessage(mes)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).show();
    }
}