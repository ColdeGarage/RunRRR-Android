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
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Provides UI for the view with Tile.
 */
public class MissionsFragment extends Fragment
{

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
        ArrayList<HashMap<String,String>> missionList;

        // Set numbers of List in RecyclerView.
        private int LENGTH;

        // Get information from values/arrays.xml
        private String[] mName = new String[20];
        private String[] mTime = new String[20];
        private String[] mType = new String[20];
        //private String[] mState;

        public ContentAdapter(Context context) {
            Resources resources = context.getResources();

            String readDataFromHttp;

            MyTaskGet httpGet = new MyTaskGet();
            httpGet.execute();

            //get result from function "onPostExecute" in class "myTaskGet"
            try {
                readDataFromHttp = httpGet.get();
                //Parse JSON info
                Parsejson(readDataFromHttp);
            } catch (Exception e) {
                e.printStackTrace();
            }
            //System.out.println(missionList);

            // Set missions data
            for(int i=0;i<LENGTH;i++){
                mName[i] = missionList.get(i).get("title");
                mTime[i] = missionList.get(i).get("time_end");
                mType[i] = missionList.get(i).get("class");
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()), parent);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            // Use holder to get data from arrays in ContentAdapter
            holder.name.setText(mName[position % mName.length]);
            holder.time.setText(mTime[position % mTime.length]);
            String type = mType[position % mType.length];
            if(type.equals("MAIN")){
                holder.type.setImageResource(R.drawable.missions_main_2);
            }else if(type.equals("SUB")){
                holder.type.setImageResource(R.drawable.missions_sub);
            }else{
                holder.type.setImageResource(R.drawable.missions_urg);
            }
            //holder.state.setImageDrawable(mState[position % mState.length]);
        }

        @Override
        public int getItemCount() {
            return LENGTH;
        }


        //Parse json received from server
        void Parsejson (String info){
            missionList = new ArrayList<>();
            try {
                JSONObject jObject = new JSONObject(info);
                JSONObject payload = new JSONObject(jObject.getString("payload"));
                JSONArray objects = payload.getJSONArray("objects");
                //Get mission number
                LENGTH = objects.length();
                for(int i=0;i<LENGTH;i++){
                    JSONObject subObject;
                    subObject = objects.getJSONObject(i);
                    HashMap<String,String> mission = new HashMap<>();
                    mission.put("mid",subObject.getString("mid"));
                    mission.put("title",subObject.getString("title"));
                    mission.put("time_end",subObject.getString("time_end").substring(11,16));
                    mission.put("class",subObject.getString("class"));      //MAIN, URG, SUB
                    missionList.add(mission);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    //HTTPGet
    static class MyTaskGet extends AsyncTask<Void,Void,String> {
        URL url = null;
        HttpURLConnection connection = null;

        @Override
        public void onPreExecute() {
            super.onPreExecute();
        }
        @Override
        public String doInBackground(Void...arg0) {
            BufferedReader reader = null;
            StringBuilder stringBuilder;

            try
            {
                // create the HttpURLConnection

                url = new URL("http://coldegarage.tech:8081/api/v1/mission/read?operator_uid=288");
                connection = (HttpURLConnection) url.openConnection();

                // 使用甚麼方法做連線
                connection.setRequestMethod("GET");

                // 是否添加參數(ex : json...等)
                //connection.setDoOutput(true);

                // 設定TimeOut時間
                connection.setReadTimeout(15*1000);
                connection.connect();

                // 伺服器回來的參數
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                stringBuilder = new StringBuilder();

                String line;
                while ((line = reader.readLine()) != null)
                {
                    stringBuilder.append(line + "\n");
                }
                return stringBuilder.toString();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            finally
            {
                connection.disconnect();
                // close the reader; this can throw an exception too, so
                // wrap it in another try/catch block.
                if (reader != null)
                {
                    try
                    {
                        reader.close();
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }
            return null;
        }
        @Override
        public void onPostExecute(String result) {
            super.onPostExecute(result);
        }

    }

}