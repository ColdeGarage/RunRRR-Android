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
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.AnimationDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;

import static android.content.Context.MODE_PRIVATE;

/**
 * Provides UI for the view with Tile.
 */
public class MissionsFragment extends Fragment
{

    public static final int MY_MISSION_REFRESH = 0;
    static MissionsFragment instance = null;

    private View rootView;
    private RecyclerView recyclerView;
    private ContentAdapter adapter;

    private static int uid;
    private static String token;
    private static String mid;

    public static MissionsFragment getInstance() {
//        if( instance == null ) {
//            synchronized (MissionsFragment.class) {
//                if (instance == null) {
//                    instance = new MissionsFragment();
//                }
//            }
//        }
        synchronized (MissionsFragment.class) {
            instance = new MissionsFragment();
        }
        return instance;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //RecyclerView recyclerView = (RecyclerView) inflater.inflate(
        //        R.layout.recycler_view, container, false);
        rootView = inflater.inflate(R.layout.fragment_missions, container, false);

        //read uid and token
        readPrefs();

        /*
            * Sets up a SwipeRefreshLayout.OnRefreshListener that is invoked when the user
            * performs a swipe-to-refresh gesture.
        */

        recyclerView = (RecyclerView) rootView.findViewById(R.id.mission_recycler_view);

        adapter = new ContentAdapter(recyclerView.getContext());
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
    }

    public void Refresh(){
//        // Create new fragment and transaction
//        Fragment newFragment = new MissionsFragment();
//        FragmentTransaction transaction = getFragmentManager().beginTransaction();
//
//        // Replace whatever is in the fragment_container view with this fragment,
//        // and add the transaction to the back stack
//        transaction.replace(R.id.swiperefresh, newFragment)
//                .addToBackStack(null)
//                .commit();
        adapter = new ContentAdapter(recyclerView.getContext());
        recyclerView.setAdapter(adapter);
    }

    // Call Back method  to get the Message form other Activity
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        Refresh();

    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public LinearLayout list;
        public TextView type;
        public TextView name;
        public TextView time;
        public ImageView state;

        public ViewHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.item_list_missions, parent, false));

            list = (LinearLayout) itemView.findViewById(R.id.list_mission);
            type = (TextView) itemView.findViewById(R.id.list_type);
            name = (TextView) itemView.findViewById(R.id.list_name);
            time = (TextView) itemView.findViewById(R.id.list_time);
            state = (ImageView) itemView.findViewById(R.id.list_state);

        }
    }

    //=====================內存=====================
    private void readPrefs(){
        SharedPreferences settings = getContext().getSharedPreferences("data",MODE_PRIVATE);
        uid = settings.getInt("uid",0);
        token = settings.getString("token","");
    }

    //======================建立RecyclerView===========================
    public class ContentAdapter extends RecyclerView.Adapter<ViewHolder> {
        ArrayList<HashMap<String,String>> missionList;
        ArrayList<HashMap<String,String>> reportList;
        ArrayList<HashMap<String,String>> solvingMissionList;
        ArrayList<HashMap<String,String>> unsolvedMissionList;
        ArrayList<HashMap<String,String>> completedMissionList;
        // Set numbers of List in RecyclerView.
        private int LENGTH;
        private int serverTimeHour;
        private int serverTimeMin;

        private String [] mMid = new String[20];
        private String[] mName = new String[20];
        private String[] mTime = new String[20];
        private String[] mType = new String[20];
        private String[] mState = new String[20];
        private String[] mContent = new String[20];
        private String[] mUrl = new String[20];
        private String[] mPrize = new String[20];
        private String[] mScore = new String[20];

        public ContentAdapter(Context context) {
            Resources resources = context.getResources();

            String readDataFromHttp;
            if(!isNetworkAvailable()){
                Alert("Please check your internet connection, then try again.");
            }
            //get mission list from server
            MyTaskGet httpGetMission = new MyTaskGet();
            httpGetMission.execute(resources.getString(R.string.apiURL)+"/mission/read?operator_uid="+String.valueOf(uid)+"&token="+token);

            //get result from function "onPostExecute" in class "myTaskGet"
            try {
                readDataFromHttp = httpGetMission.get();
                //Parse JSON info
                parseJson(readDataFromHttp,"mission");
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println(missionList);

            //get report(to get the mission state)
            MyTaskGet httpGetReport = new MyTaskGet();
            httpGetReport.execute(resources.getString(R.string.apiURL)+"/report/read?operator_uid="+String.valueOf(uid)+"&token="+token+"&uid="+String.valueOf(uid));
            try {
                readDataFromHttp = httpGetReport.get();
                parseJson(readDataFromHttp,"report");
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println(reportList);
            System.out.println("server time");
            System.out.println(serverTimeHour+":"+serverTimeMin);

            missionState();
            missionSort();

            //compare the new mission list with the old one
            //newMissionNotify();

            // Set missions data to string array
            for(int i=0;i<solvingMissionList.size();i++){
                mMid[i] = solvingMissionList.get(i).get("mid");
                mName[i] = solvingMissionList.get(i).get("title");
                mTime[i] = solvingMissionList.get(i).get("time_end");
                mType[i] = solvingMissionList.get(i).get("class");
                mState[i] = solvingMissionList.get(i).get("status");
                mContent[i] = solvingMissionList.get(i).get("content");
                mUrl[i] = solvingMissionList.get(i).get("url");
                mPrize[i] = solvingMissionList.get(i).get("prize");
                mScore[i] = solvingMissionList.get(i).get("score");
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()), parent);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, final int position) {
            // Use holder to get data from arrays in ContentAdapter
            holder.name.setText(mName[position % mName.length]);
            holder.time.setText(mTime[position % mTime.length]);

            String content = mContent[position % mContent.length];
            String type = mType[position % mType.length];
            String state = mState[position % mState.length];

            //missions type : MAIN,SUB,URG, set different icon
            switch (type){
                case "0":
                    holder.type.setText("限");
                    holder.type.setTextColor(ContextCompat.getColor(getContext(), R.color.limit));
                    holder.list.setBackgroundResource(R.color.limit);
                    break;
                case "1":
                    holder.type.setText("主");
                    holder.type.setTextColor(ContextCompat.getColor(getContext(), R.color.main));
                    holder.list.setBackgroundResource(R.color.main);
                    break;
                case "2":
                    holder.type.setText("支");
                    holder.type.setTextColor(ContextCompat.getColor(getContext(), R.color.sub));
                    holder.list.setBackgroundResource(R.color.sub);
                    break;
                default:
                    break;
            }

            //state type : -1:unsolved 0:being judged 1:success 2:fail
            switch(state){
                case "-1":
                    break;
                case "0":
//                    holder.state.setImageResource(R.drawable.state_waiting);
                    holder.state.setBackgroundResource(R.drawable.anim_gif_waiting);
                    Object ob_waiting = holder.state.getBackground();
                    AnimationDrawable anim_waiting = (AnimationDrawable) ob_waiting;
                    anim_waiting.start();
                    break;
                case "1":
                    holder.state.setImageResource(R.drawable.state_passed);
                    break;
                case "2":
                    holder.state.setImageResource(R.drawable.state_failed);
                    break;
                default:
                    break;
            }

            //holder ItemView
            holder.name.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //TODO:Intent to other activity
                    Context context = v.getContext();
                    Intent intent = new Intent(context, MissionPopActivity.class);

                    //New Bundle object fot passing data
                    Bundle bundle = new Bundle();
                    bundle.putString("mid", mMid[position % mMid.length]);
                    bundle.putString("name", mName[position % mName.length]);
                    bundle.putString("time", mTime[position % mTime.length]);
                    bundle.putString("type", mType[position % mType.length]);
                    bundle.putString("state", mState[position % mState.length]);
                    bundle.putString("content", mContent[position % mContent.length]);
                    bundle.putString("url", mUrl[position % mUrl.length]);
                    bundle.putString("prize", mPrize[position % mPrize.length]);
                    bundle.putString("score", mScore[position % mScore.length]);
                    bundle.putString("uid",String.valueOf(uid));
                    bundle.putString("token",token);

                    intent.putExtras(bundle);
                    startActivityForResult(intent, MY_MISSION_REFRESH);
                }
            });
        }

        @Override
        public int getItemCount() {
            return solvingMissionList.size();
        }

        //====================取得任務頁面顯示的內容===========================
        //Parse json received from server
        void parseJson (String info, String missionOrReport){
            System.out.println(info);
            if(missionOrReport.equals("mission")){
                missionList = new ArrayList<>();
                try {
                    JSONObject payload = new JSONObject(new JSONObject(info).getString("payload"));
                    JSONArray objects = payload.getJSONArray("objects");
                    //Get mission number
                    LENGTH = objects.length();
                    for(int i=0;i<LENGTH;i++){
                        JSONObject subObject;
                        subObject = objects.getJSONObject(i);
                        HashMap<String,String> mission = new HashMap<>();
                        //put mid into hashmap
                        mission.put("mid",subObject.getString("mid"));
                        //put title into hashmap
                        mission.put("title",subObject.getString("title"));

                        //put content into hashmap
                        mission.put("content",subObject.getString("content"));

                        //put url into hashmap
                        mission.put("url",subObject.getString("url"));

                        //put prize into hashmap
                        mission.put("prize",subObject.getString("prize"));

                        //put score into hashmap
                        mission.put("score",subObject.getString("score"));

                        //parse time, take hour&min only
                        //and put time_end into hashmap
                        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.s'Z'");
                        Calendar cal = Calendar.getInstance();
                        try {
                            Date date = dateFormat.parse(subObject.getString("time_end"));
                            cal.setTime(date);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        mission.put("time_end",String.valueOf(cal.get(Calendar.HOUR_OF_DAY))+":"+String.valueOf(cal.get(Calendar.MINUTE)));

                        //put class into hashmap
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
            }else if(missionOrReport.equals("report")){
                reportList = new ArrayList<>();
                try {
                    JSONObject jObject = new JSONObject(info);

                    //parse and get server time
                    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.s'Z'");
                    Calendar cal = Calendar.getInstance();
                    try {
                        Date date = dateFormat.parse(jObject.getString("server_time"));
                        cal.setTime(date);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    serverTimeHour = cal.get(Calendar.HOUR_OF_DAY);
                    serverTimeMin = cal.get(Calendar.MINUTE);
//                    serverTimeHour = 0;
//                    serverTimeMin = 0;

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
                    //find mid in each report
                    //and add status to the corresponding mission in missionlist
                    if(reportList.get(i).get("mid").equals(missionList.get(j).get("mid"))){
                        String status = reportList.get(i).get("status");   //0:being judged 1:success 2:fail
                        missionList.get(j).put("status",status);
                        break;
                    }
                }
            }
            //add ("status",-1) to the missions that doesn't appear in reportlist
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
            completedMissionList = new ArrayList<>();

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

            //classify the rest missions into three lists by status
            for(int i=0;i<missionList.size();i++){
                if(!missionList.get(i).containsKey("expire")){
                    HashMap mission = missionList.get(i);
                    if(mission.get("status").equals("1")){
                        completedMissionList.add(mission);
                    }else if(mission.get("status").equals("-1")){
                        unsolvedMissionList.add(mission);
                    }else{
                        solvingMissionList.add(mission);
                    }
                }
            }

            //sorted by class
            Collections.sort(completedMissionList,new Comparator<HashMap<String,String>>() {
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

            //converge the three lists into one
            solvingMissionList.addAll(unsolvedMissionList);
            solvingMissionList.addAll(completedMissionList);
            System.out.print(solvingMissionList);
        }
    }


    //===================HTTP==========================
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
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
    //show an alert dialog
    void Alert(String mes){
        new AlertDialog.Builder(getActivity())
                .setMessage(mes)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).show();
    }
}