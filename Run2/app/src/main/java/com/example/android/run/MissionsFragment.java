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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Provides UI for the view with Tile.
 */
public class MissionsFragment extends Fragment
{
    //顯示文字內容
    private String text = "";

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);

        //取得MainActivity的方法，將文字放入text字串
        MainActivity mMainActivity = (MainActivity) activity;
        text = mMainActivity.getMissionsText();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        RecyclerView recyclerView = (RecyclerView) inflater.inflate(
                R.layout.recycler_view, container, false);
        MissionsFragment.ContentAdapter adapter = new MissionsFragment.ContentAdapter(recyclerView.getContext());
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        return recyclerView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView type;
        public TextView name;
        public TextView time;
        public ImageView state;
        public ViewHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.item_missions, parent, false));
            type = (ImageView) itemView.findViewById(R.id.list_type);
            name = (TextView) itemView.findViewById(R.id.list_name);
            time = (TextView) itemView.findViewById(R.id.list_time);
            state = (ImageView) itemView.findViewById(R.id.list_state);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int id = v.getId();
                    //TODO:Intent to other activity
                    Context context = v.getContext();
                    Intent intent = new Intent(context, MissionPopActivity.class);
                    intent.putExtra(MissionPopActivity.EXTRA_POSITION, getAdapterPosition());
                    context.startActivity(intent);
                }
            });
        }
    }

    /**
     * Adapter to display recycler view.
     */
    public static class ContentAdapter extends RecyclerView.Adapter<ViewHolder> {
        // Set numbers of List in RecyclerView.
        private int LENGTH;

        private final String[] mName;
        private final String[] mTime;
        private final Drawable[] mType;
        private final Drawable[] mState;

        public ContentAdapter(Context context) {
                Resources resources = context.getResources();
                LENGTH = resources.getStringArray(R.array.mission_name).length;
                mName = resources.getStringArray(R.array.mission_name);
                mTime = resources.getStringArray(R.array.mission_time);
                TypedArray a = resources.obtainTypedArray(R.array.mission_type);
                TypedArray b = resources.obtainTypedArray(R.array.mission_state);
                mType = new Drawable[a.length()];
                mState = new Drawable[b.length()];
                for (int i = 0; i < mType.length; i++) {
                    mType[i] = a.getDrawable(i);
                    mState[i] = b.getDrawable(i);
                }
                a.recycle();
                b.recycle();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()), parent);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.type.setImageDrawable(mType[position % mType.length]);
            holder.name.setText(mName[position % mName.length]);
            holder.time.setText(mTime[position % mTime.length]);
            holder.state.setImageDrawable(mState[position % mState.length]);
        }

        @Override
        public int getItemCount() {
            return LENGTH;
        }
    }
}