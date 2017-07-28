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
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Provides UI for the view with List.
 */
public class MoreFragment extends Fragment {
    //顯示文字內容
    private String text = "";
    static MoreFragment instance = null;

    public static MoreFragment getInstance() {
        synchronized (MoreFragment.class) {
                instance = new MoreFragment();
        }
        return instance;
    }

//    @Override
//    public void onAttach(Activity activity) {
//        super.onAttach(activity);
//
//        //取得MainActivity的方法，將文字放入text字串
//        TabActivity mTabActivity = (TabActivity) activity;
//        text = mTabActivity.getMissionsText();
//    }

    /*@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        //導入Tab分頁的Fragment Layout
        return inflater.inflate(R.layout.item_more, container, false);
    }*/

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_more, container, false);
        RecyclerView recyclerView = (RecyclerView) v.findViewById(R.id.more_recycler_view);
        ContentAdapter adapter = new ContentAdapter(recyclerView.getContext());
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        //取得TextView元件並帶入text字串
        //TextView mText = (TextView) getView().findViewById(R.id.list_title);
        //mText.setText("輸入序號");

    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        public TextView moreItem_name;
        public LinearLayout moreItem_background;
        public ConstraintLayout moreItem_die;
        public ConstraintLayout moreItem_about_us;
        public ConstraintLayout moreItem_barcode;
        public ConstraintLayout moreItem_sos;
        public ConstraintLayout moreItem_logout;

        public ViewHolder(final LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.item_list_more, parent, false));
            moreItem_name = (TextView) itemView.findViewById(R.id.list_title);
            moreItem_background = (LinearLayout) itemView.findViewById(R.id.more_list_background);
            moreItem_die = (ConstraintLayout) itemView.findViewById(R.id.more_die);
            moreItem_about_us = (ConstraintLayout) itemView.findViewById(R.id.more_about_us);
            moreItem_barcode = (ConstraintLayout) itemView.findViewById(R.id.more_barcode);
            moreItem_sos = (ConstraintLayout) itemView.findViewById(R.id.more_sos);
            moreItem_logout = (ConstraintLayout) itemView.findViewById(R.id.more_logout);
            /*
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int id = v.getId();
                    //TODO:Intent to other activity
                    Context context = v.getContext();
                    Intent intent = new Intent(context, AboutUsActivity.class);
                    //intent.putExtra(DetailActivity.EXTRA_POSITION, getAdapterPosition());
                    context.startActivity(intent);
                }
            });*/
        }
    }

    /**
     * Adapter to display recycler view.
     */
    public static class ContentAdapter extends RecyclerView.Adapter<ViewHolder> {
        // Set numbers of List in RecyclerView.
        private static final int LENGTH = 5;

        private final String[] mMoreList;

        //Check if the layout is folded
        public boolean isDieFolded = true;
        public boolean isAboutUSFolded = true;
        public boolean isBarcodeFolded = true;
        public boolean isSOSFolded = true;
        public boolean isLogoutFolded = true;

        public ContentAdapter(Context context) {
            Resources resources = context.getResources();
            mMoreList = resources.getStringArray(R.array.more);
        }
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()), parent);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            final String moreItem = mMoreList[position % mMoreList.length];
            GradientDrawable itemShape;
            int itemColor;

            holder.moreItem_name.setText(moreItem);
            //Set layout content
            holder.moreItem_die.setVisibility(View.GONE);
            holder.moreItem_about_us.setVisibility(View.GONE);
            holder.moreItem_barcode.setVisibility(View.GONE);
            holder.moreItem_sos.setVisibility(View.GONE);
            holder.moreItem_logout.setVisibility(View.GONE);
            //Set background color
            switch(position){
                case 0: //Die
//                    //Setting corner padding
//                    holder.more_background.setBackgroundResource(R.color.background_grey);
//                    //Setting item background color
//                    holder.more_list.setBackgroundResource(R.drawable.background_round);
//                    itemShape = (GradientDrawable)holder.more_list.getBackground().getCurrent();
//                    itemColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.die);
//                    itemShape.setColor(itemColor);
                    holder.moreItem_background.setBackgroundResource(R.color.die);
                    break;
                case 1: //About us
//                    //Setting corner padding
//                    if(isDieFolded) {
//                        holder.moreItem_background.setBackgroundResource(R.color.die);
//                    } else {
//                        holder.moreItem_background.setBackgroundResource(R.color.background_grey);
//                    }
//                    //Setting item background color
//                    holder.more_list.setBackgroundResource(R.drawable.background_round);
//                    itemShape = (GradientDrawable)holder.more_list.getBackground().getCurrent();
//                    itemColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.about_us);
//                    itemShape.setColor(itemColor);
                    holder.moreItem_background.setBackgroundResource(R.color.about_us);
                    break;
                case 2: //Barcode
//                    //Setting corner padding
//                    if(isAboutUSFolded) {
//                        holder.moreItem_background.setBackgroundResource(R.color.about_us);
//                    } else {
//                        holder.moreItem_background.setBackgroundResource(R.color.background_grey);
//                    }
//                    //Setting item background color
//                    holder.more_list.setBackgroundResource(R.drawable.background_round);
//                    itemShape = (GradientDrawable)holder.more_list.getBackground().getCurrent();
//                    itemColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.barcode);
//                    itemShape.setColor(itemColor);
                    holder.moreItem_background.setBackgroundResource(R.color.barcode);
                    break;
                case 3: //SOS
//                    //Setting corner padding
//                    if(isBarcodeFolded) {
//                        holder.moreItem_background.setBackgroundResource(R.color.barcode);
//                    } else {
//                        holder.moreItem_background.setBackgroundResource(R.color.background_grey);
//                    }
//                    //Setting item background color
//                    holder.more_list.setBackgroundResource(R.drawable.background_round);
//                    itemShape = (GradientDrawable)holder.more_list.getBackground().getCurrent();
//                    itemColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.sos);
//                    itemShape.setColor(itemColor);
                    holder.moreItem_background.setBackgroundResource(R.color.sos);
                    break;
                case 4: //Logout
//                    //Setting corner padding
//                    if(isSosFolded) {
//                        holder.moreItem_background.setBackgroundResource(R.color.sos);
//                    } else {
//                        holder.moreItem_background.setBackgroundResource(R.color.background_grey);
//                    }
//                    //Setting item background color
//                    holder.more_list.setBackgroundResource(R.drawable.background_round);
//                    itemShape = (GradientDrawable)holder.more_list.getBackground().getCurrent();
//                    itemColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.logout);
//                    itemShape.setColor(itemColor);
                    holder.moreItem_background.setBackgroundResource(R.color.logout);
                    holder.moreItem_logout.setVisibility(View.VISIBLE);
                    break;
                default:
                    break;
            }

            //Set onclickListener
            holder.moreItem_name.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    Context context = v.getContext();

                    switch(position){
                        case 0: //Die
//                            Intent intent = new Intent(context, DieActivity.class);
//                            context.startActivity(intent);
                            if(isDieFolded) {
                                isDieFolded = false;
                                holder.moreItem_die.setVisibility(View.VISIBLE);
                            } else {
                                isDieFolded = true;
                                holder.moreItem_die.setVisibility(View.GONE);
                            }
                            break;
                        case 1: //About us
//                            intent = new Intent(context, AboutUsActivity.class);
//                            context.startActivity(intent);
                            if(isAboutUSFolded) {
                                isAboutUSFolded = false;
                                holder.moreItem_about_us.setVisibility(View.VISIBLE);
                            } else {
                                isAboutUSFolded = true;
                                holder.moreItem_about_us.setVisibility(View.GONE);
                            }
                            break;
                        case 2: //Barcode
//                            intent = new Intent(context, BarcodeActivity.class);
//                            context.startActivity(intent);
                            if(isBarcodeFolded) {
                                isBarcodeFolded = false;
                                holder.moreItem_barcode.setVisibility(View.VISIBLE);
                            } else {
                                isBarcodeFolded = true;
                                holder.moreItem_barcode.setVisibility(View.GONE);
                            }
                            break;
                        case 3: //SOS
//                            intent = new Intent(context, AboutUsActivity.class);
//                            context.startActivity(intent);
                            if(isSOSFolded) {
                                isSOSFolded = false;
                                holder.moreItem_sos.setVisibility(View.VISIBLE);
                            } else {
                                isSOSFolded = true;
                                holder.moreItem_sos.setVisibility(View.GONE);
                            }
                            break;
                        case 4: //Logout
                            Intent intent = new Intent(context, LogoutActivity.class);
                            context.startActivity(intent);
//                            if(isLogoutFolded) {
//                                isLogoutFolded = false;
//                                holder.moreItem_logout.setVisibility(View.VISIBLE);
//                            } else {
//                                isLogoutFolded = true;
//                                holder.moreItem_logout.setVisibility(View.GONE);
//                            }
                            break;
                        default:
                            break;
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return LENGTH;
        }
    }


}