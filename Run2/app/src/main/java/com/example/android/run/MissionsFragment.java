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
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
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
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;

/**
 * Provides UI for the view with Tile.
 */
public class MissionsFragment extends Fragment
{
    private SwipeRefreshLayout mSwipeLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //RecyclerView recyclerView = (RecyclerView) inflater.inflate(
        //        R.layout.recycler_view, container, false);

        View v = inflater.inflate(R.layout.recycler_view, container, false);

        /*
            * Sets up a SwipeRefreshLayout.OnRefreshListener that is invoked when the user
            * performs a swipe-to-refresh gesture.
        */
        mSwipeLayout = (SwipeRefreshLayout) v.findViewById(R.id.swiperefresh);
        mSwipeLayout.setColorSchemeColors(Color.RED);
        mSwipeLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        mSwipeLayout.setRefreshing(true);

                        // Create new fragment and transaction
                        Fragment newFragment = new MissionsFragment();
                        // consider using Java coding conventions (upper first char class names!!!)
                        FragmentTransaction transaction = getFragmentManager().beginTransaction();

                        // Replace whatever is in the fragment_container view with this fragment,
                        // and add the transaction to the back stack
                        transaction.replace(R.id.swiperefresh, newFragment)
                                    .addToBackStack(null)
                                    .commit();

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mSwipeLayout.setRefreshing(false);
                            }
                        }, 3000);
                    }
                }
        );

        RecyclerView recyclerView = (RecyclerView) v.findViewById(R.id.my_recycler_view);
        if(recyclerView.getParent()!=null)
            ((ViewGroup)recyclerView.getParent()).removeView(recyclerView);
        MissionsFragment.ContentAdapter adapter = new MissionsFragment.ContentAdapter(v.getContext());
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        ((ViewGroup)v).addView(recyclerView);

        return v;
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
                    //TODO:Intent to other activity
                    Context context = v.getContext();
                    Intent intent = new Intent(context, MissionPopActivity.class);
                    intent.putExtra(MissionPopActivity.EXTRA_POSITION, getAdapterPosition());
                    context.startActivity(intent);
                }
            });
        }
    }

    public void refresh(){

    }


    /**
     * Adapter to display recycler view.
     */
    public static class ContentAdapter extends RecyclerView.Adapter<ViewHolder> {
        ArrayList<HashMap<String,String>> missionList;
        ArrayList<HashMap<String,String>> reportList;
        ArrayList<HashMap<String,String>> solvingMissionList;
        ArrayList<HashMap<String,String>> unsolvedMissionList;
        ArrayList<HashMap<String,String>> completeMissionList;
        // Set numbers of List in RecyclerView.
        private int LENGTH;
        private int serverTimeHour;
        private int serverTimeMin;

        private String[] mName = new String[20];
        private String[] mTime = new String[20];
        private String[] mType = new String[20];
        private String[] mState = new String[20];

        public ContentAdapter(Context context) {
            Resources resources = context.getResources();

            String readDataFromHttp;

            //get mission list from server
            MyTaskGet httpGetMission = new MyTaskGet();
            httpGetMission.execute(resources.getString(R.string.apiURL)+"/mission/read?operator_uid=288");

            //get result from function "onPostExecute" in class "myTaskGet"
            try {
                readDataFromHttp = httpGetMission.get();
                //Parse JSON info
                Parsejson(readDataFromHttp,"mission");
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println(missionList);

            //get report(misson state)
            MyTaskGet httpGetReport = new MyTaskGet();
            httpGetReport.execute(resources.getString(R.string.apiURL)+"/report/read?operator_uid=288&uid=288");
            try {
                readDataFromHttp = httpGetReport.get();
                Parsejson(readDataFromHttp,"report");
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println(reportList);
            System.out.println(serverTimeHour+":"+serverTimeMin);

            missionState();
            missionSort();

            // Set missions data to string array
            for(int i=0;i<solvingMissionList.size();i++){
                mName[i] = solvingMissionList.get(i).get("title");
                mTime[i] = solvingMissionList.get(i).get("time_end");
                mType[i] = solvingMissionList.get(i).get("class");
                mState[i] = solvingMissionList.get(i).get("status");
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
            String state = mState[position % mState.length];

            //missions type : MAIN,SUB,URG, set different icon
            if(type.equals("1")){
                holder.type.setImageResource(R.drawable.missions_main_2);
            }else if(type.equals("2")){
                holder.type.setImageResource(R.drawable.missions_sub);
            }else{
                holder.type.setImageResource(R.drawable.missions_urg);
            }
            //state type : -1:unsolved 0:being judged 1:success 2:fail
            if(state.equals("0")){
                holder.state.setImageResource(R.drawable.waiting);
            }else if(state.equals("1")){
                holder.state.setImageResource(R.drawable.state_passed);
            }else if(state.equals("2")){
                holder.state.setImageResource(R.drawable.state_failed);
            }else{
                //holder.state.setImageResource(R.drawable.state_failed);
            }
        }

        @Override
        public int getItemCount() {
            return solvingMissionList.size();
        }

        //Parse json received from server
        void Parsejson (String info, String missionOrReport){
            if(missionOrReport.equals("mission")){
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

                        //parse time, need hour&min only
                        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.s'Z'");
                        Date date ;
                        Calendar cal = Calendar.getInstance();
                        try {
                            date = dateFormat.parse(subObject.getString("time_end"));
                            cal.setTime(date);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        mission.put("time_end",String.valueOf(cal.get(Calendar.HOUR_OF_DAY))+":"+String.valueOf(cal.get(Calendar.MINUTE)));
                        if(subObject.getString("class").equals("URG")){
                            mission.put("class","0");
                        }else if(subObject.getString("class").equals("MAIN")){
                            mission.put("class","1");
                        }else if(subObject.getString("class").equals("SUB")){
                            mission.put("class","2");
                        }
                        missionList.add(mission);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }else{
                reportList = new ArrayList<>();
                try {
                    JSONObject jObject = new JSONObject(info);
                    //get server time
                    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.s'Z'");
                    Date date ;
                    Calendar cal = Calendar.getInstance();
                    try {
                        date = dateFormat.parse(jObject.getString("server_time"));
                        cal.setTime(date);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    //serverTimeHour = cal.get(Calendar.HOUR_OF_DAY);
                    //serverTimeMin = cal.get(Calendar.MINUTE);
                    serverTimeHour = 0;
                    serverTimeMin = 0;

                    JSONObject payload = new JSONObject(jObject.getString("payload"));
                    JSONArray objects = payload.getJSONArray("objects");
                    for(int i=0;i<objects.length();i++){
                        JSONObject subObject;
                        subObject = objects.getJSONObject(i);
                        HashMap<String,String> report = new HashMap<>();
                        report.put("mid",subObject.getString("mid"));
                        report.put("status",subObject.getString("status"));
                        reportList.add(report);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }


        //Add mission state
        void missionState(){
            for(int i=0;i<reportList.size();i++){
                for(int j=0;j<missionList.size();j++){
                    if(reportList.get(i).get("mid").equals(missionList.get(j).get("mid"))){
                        String status = reportList.get(i).get("status");   //0:being judged 1:success 2:fail
                        missionList.get(j).put("status",status);
                        break;
                    }
                }
            }
            for(int i=0;i<missionList.size();i++){
                if(!missionList.get(i).containsKey("status")) {
                    missionList.get(i).put("status","-1");
                }
            }
        }

        //sort mission order
        void missionSort(){
            unsolvedMissionList = new ArrayList<>();
            solvingMissionList = new ArrayList<>();
            completeMissionList = new ArrayList<>();

            //filter out expired mission
            for(int i=0;i<missionList.size();i++){
                String missionTime = missionList.get(i).get("time_end");
                String[] part = missionTime.split(":");
                int hour = Integer.valueOf(part[0]);
                int min = Integer.valueOf(part[1]);
                if(hour<serverTimeHour){
                    if(!missionList.get(i).get("status").equals("1")){
                        missionList.get(i).put("expire","true");
                    }
                }else if(hour==serverTimeHour){
                    if(min<serverTimeMin){
                        if(!missionList.get(i).get("status").equals("1")){
                            missionList.get(i).put("expire","true");
                        }
                    }
                }
            }

            for(int i=0;i<missionList.size();i++){
                if(!missionList.get(i).containsKey("expire")){
                    HashMap mission = missionList.get(i);
                    if(mission.get("status").equals("1")){
                        completeMissionList.add(mission);
                    }else if(mission.get("status").equals("-1")){
                        unsolvedMissionList.add(mission);
                    }else{
                        solvingMissionList.add(mission);
                    }
                }
            }

            Collections.sort(completeMissionList,new Comparator<HashMap<String,String>>() {
                public int compare(HashMap<String, String> mapping1, HashMap<String, String> mapping2) {
                    return mapping1.get("class").compareTo(mapping2.get("class"));
                }
            });

            Collections.sort(unsolvedMissionList,new Comparator<HashMap<String,String>>() {
                public int compare(HashMap<String, String> mapping1, HashMap<String, String> mapping2) {
                    return mapping1.get("class").compareTo(mapping2.get("class"));
                }
            });

            Collections.sort(solvingMissionList,new Comparator<HashMap<String,String>>() {
                public int compare(HashMap<String, String> mapping1, HashMap<String, String> mapping2) {
                    return mapping1.get("status").compareTo(mapping2.get("status"));
                }
            });
            Collections.reverse(solvingMissionList);

            solvingMissionList.addAll(unsolvedMissionList);
            solvingMissionList.addAll(completeMissionList);
            System.out.print(solvingMissionList);
        }
    }

    //HTTPGet
    static class MyTaskGet extends AsyncTask<String,Void,String> {
        URL url = null;
        HttpURLConnection connection = null;

        @Override
        public void onPreExecute() {
            super.onPreExecute();
        }
        @Override
        public String doInBackground(String...arg0) {
            BufferedReader reader = null;
            StringBuilder stringBuilder;
            String urlStr = arg0[0];

            try
            {
                // create the HttpURLConnection
                url = new URL(urlStr);
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