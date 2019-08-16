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

package org.nthuee.android.run;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;

import static android.content.Context.MODE_PRIVATE;

/**
 * Provides UI for the view with Tile.
 */
public class MissionsFragment extends Fragment {
    private static MissionsFragment instance = null;
    public static final int MY_MISSION_REFRESH = 0;

    private RecyclerView recyclerView;
    private SwipeRefreshLayout refreshLayout;
    private ContentAdapter adapter;

    private static int uid;
    private static String token;

    public static MissionsFragment getInstance() {
        synchronized (MissionsFragment.class) {
            instance = new MissionsFragment();
        }
        return instance;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_missions, container, false);

        //read uid and token
        readPrefs();

        recyclerView = (RecyclerView) rootView.findViewById(R.id.mission_recycler_view);

        refreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.refresh_layout);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                instance.Refresh();
                //Notify();
                refreshLayout.setRefreshing(false);
            }
        });

        adapter = new ContentAdapter(recyclerView.getContext());
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        return rootView;
    }

    public void Refresh() {
        adapter = new ContentAdapter(recyclerView.getContext());
        recyclerView.setAdapter(adapter);
    }

    // Call Back method to get the Message from other Activity
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Refresh();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public LinearLayout list;
        public TextView type;
        public TextView name;
        public TextView time;
        public ImageView state;

        private ViewHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.item_list_missions, parent, false));

            list = itemView.findViewById(R.id.list_mission);
            type = itemView.findViewById(R.id.list_type);
            name = itemView.findViewById(R.id.list_name);
            time = itemView.findViewById(R.id.list_time);
            state = itemView.findViewById(R.id.list_state);
        }
    }

    //====================內存====================
    private void readPrefs() {
        SharedPreferences settings = getContext().getSharedPreferences("data",MODE_PRIVATE);
        uid = settings.getInt("uid",0);
        token = settings.getString("token","");
    }

    //====================建立RecyclerView====================
    public class ContentAdapter extends RecyclerView.Adapter<ViewHolder> {
        private ArrayList<HashMap<String,String>> missionList;
        private ArrayList<HashMap<String,String>> reportList;
        private ArrayList<HashMap<String,String>> solvingMissionList;
        private ArrayList<HashMap<String,String>> unsolvedMissionList;
        private ArrayList<HashMap<String,String>> completedMissionList;

        // Set numbers of List in RecyclerView.
        private int LENGTH;
        private int serverTimeHour;
        private int serverTimeMin;

        private String[] mMid = new String[20];
        private String[] mName = new String[20];
        private String[] mTime = new String[20];
        private String[] mType = new String[20];
        private String[] mState = new String[20];
        private String[] mContent = new String[20];
        private String[] mPrize = new String[20];
        private String[] mScore = new String[20];
        private String[] mUrl = new String[20];

        private ContentAdapter(Context context) {
            Resources resources = context.getResources();
            String readDataFromHttp;

            if(!isNetworkAvailable()) {
                Alert("Please check your internet connection, then try again.");
            }

            //get mission list from server
            MyTaskGet httpGetMission = new MyTaskGet();
            httpGetMission.execute(resources.getString(R.string.apiURL)+"/mission/read?operator_uid="+uid+"&token="+token);

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
                System.out.println(readDataFromHttp);
                parseJson(readDataFromHttp,"report");
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println(reportList);
            System.out.println("serverTime = ");
            System.out.println(serverTimeHour+":"+serverTimeMin);

            missionState();
            missionSort();

            //TODO:
            //compare the new mission list with the old one
            //newMissionNotify();

            // Set missions data to string array
            for(int i=0;i<solvingMissionList.size();i++) {


                mName[i] = solvingMissionList.get(i).get("title");
                mTime[i] = solvingMissionList.get(i).get("time_end");
                mType[i] = solvingMissionList.get(i).get("class");
                mState[i] = solvingMissionList.get(i).get("status");
                mContent[i] = solvingMissionList.get(i).get("content");
                mPrize[i] = solvingMissionList.get(i).get("prize");
                mScore[i] = solvingMissionList.get(i).get("score");
                mUrl[i] = solvingMissionList.get(i).get("url");
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
            holder.time.setTextColor(Color.BLACK);

            String type = mType[position % mType.length];
            String state = mState[position % mState.length];

            //missions type : MAIN,SUB,URG, set different icon
            switch (type) {
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

                    Context context = v.getContext();
                    Intent intent = new Intent(context, MissionPopActivity.class);

                    //New Bundle object fot passing data
                    Bundle bundle = new Bundle();
                    bundle.putString("mid", mMid[position % mMid.length]);
                    bundle.putString("title", mName[position % mName.length]);
                    bundle.putString("time_end", mTime[position % mTime.length]);
                    bundle.putString("class", mType[position % mType.length]);
                    bundle.putString("state", mState[position % mState.length]);
                    bundle.putString("content", mContent[position % mContent.length]);
                    bundle.putString("prize", mPrize[position % mPrize.length]);
                    bundle.putString("score", mScore[position % mScore.length]);
                    bundle.putString("url", mUrl[position % mUrl.length]);
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

        //====================取得任務頁面顯示的內容====================
        //Parse json received from server
        private void parseJson(String info, String missionOrReport) {
            System.out.println("info = "+info);
            if(missionOrReport.equals("mission")){
                missionList = new ArrayList<>();
                try {
                    JSONObject jsonObj = new JSONObject(info);
                    JSONObject payload = jsonObj.getJSONObject("payload");
                    JSONArray objects = payload.getJSONArray("objects");

                    //Get mission number
                    LENGTH = objects.length();
                    for(int i=0;i<LENGTH;i++) {
                        JSONObject subObject;
                        subObject = objects.getJSONObject(i);
                        HashMap<String,String> mission = new HashMap<>();

                        //put mid into hashmap
                        mission.put("mid", subObject.getString("mid"));

                        //put title into hashmap
                        mission.put("title", subObject.getString("title"));

                        //put content into hashmap
                        mission.put("content", subObject.getString("content"));

                        //put prize into hashmap
                        mission.put("prize", subObject.getString("prize"));

                        //put score into hashmap
                        mission.put("score", subObject.getString("score"));

                        //put url into hashmap
                        mission.put("url", subObject.getString("url"));

                        //parse time, take hour&min only
                        //and put time_end into hashmap
                        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                        try {
                            Date date = dateFormat.parse(subObject.getString("time_end"));
                            mission.put("time_end",new SimpleDateFormat("HH:mm").format(date));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        //put class into hashmap
                        if(subObject.getString("class").equals("URG")) {
                            mission.put("class","0");
                        } else if(subObject.getString("class").equals("MAIN")) {
                            mission.put("class","1");
                        } else if(subObject.getString("class").equals("SUB")) {
                            mission.put("class","2");
                        }
                        missionList.add(mission);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else if(missionOrReport.equals("report")) {
                reportList = new ArrayList<>();
                System.out.println("report = ");
                System.out.println(info);
                try {
                    JSONObject jObject = new JSONObject(info);

                    //parse and get server time
                    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                    String serverTime=" : ";
                    try {
                        System.out.println(jObject.getString("server_time"));
                        Date date = dateFormat.parse(jObject.getString("server_time"));
                        System.out.println("date= " + date);
                        serverTime = new SimpleDateFormat("HH:mm").format(date);

                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    String[] part = serverTime.split(":");
                    serverTimeHour = Integer.valueOf(part[0]);
                    serverTimeMin = Integer.valueOf(part[1]);

                    JSONObject payload = new JSONObject(jObject.getString("payload"));
                    JSONArray objects = payload.getJSONArray("objects");
                    for(int i=0;i<objects.length();i++) {
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
        void missionState() {
            for(int i=0;i<reportList.size();i++) {
                for(int j=0;j<missionList.size();j++) {
                    //find mid in each report
                    //and add status to the corresponding mission in missionlist
                    if(reportList.get(i).get("mid").equals(missionList.get(j).get("mid"))) {
                        String status = reportList.get(i).get("status");  //0:being judged 1:success 2:fail
                        missionList.get(j).put("status",status);
                        break;
                    }
                }
            }

            //add ("status",-1) to the missions that doesn't appear in reportlist
            for(int i=0;i<missionList.size();i++) {
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
            for(int i=0;i<missionList.size();i++) {
                String missionTime = missionList.get(i).get("time_end");
                String[] part = missionTime.split(":");
                int hour = Integer.valueOf(part[0]);
                int min = Integer.valueOf(part[1]);
                if(hour<serverTimeHour) {
                    if(!missionList.get(i).get("status").equals("1")) {
                        missionList.get(i).put("expire","true");
                    }
                } else if(hour==serverTimeHour) {
                    if(min<serverTimeMin) {
                        if(!missionList.get(i).get("status").equals("1")) {
                            missionList.get(i).put("expire","true");
                        }
                    }
                }
            }

            //classify the rest missions into three lists by status
            for(int i=0;i<missionList.size();i++) {
                if(!missionList.get(i).containsKey("expire")) {
                    HashMap mission = missionList.get(i);
                    if(mission.get("status").equals("1")) {
                        completedMissionList.add(mission);
                    } else if(mission.get("status").equals("-1")) {
                        unsolvedMissionList.add(mission);
                    } else {
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

    //====================HTTP====================
    public static class MyTaskGet extends AsyncTask<String,Void,String> {
        URL url = null;
        HttpURLConnection connection = null;

        @Override
        public String doInBackground(String...arg0) {
            BufferedReader reader = null;
            StringBuilder stringBuilder;
            String urlStr = arg0[0];

            try {
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
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line + "\n");
                }
                return stringBuilder.toString();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                connection.disconnect();
                // close the reader; this can throw an exception too, so wrap it in another try/catch block.
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            return null;
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    //show an alert dialog
    private void Alert(String mes) {
        new AlertDialog.Builder(getActivity())
                .setMessage(mes)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {} })
                .show();
    }

    private void Notify() {
        int notificationID = 1;
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(getActivity())
                        .setSmallIcon(R.drawable.ic_login)
                        .setContentTitle("New Mission")
                        .setContentText("Gogogo");
        // Creates an explicit intent for an Activity in your app
        Intent intent = new Intent(getActivity(), ViewPagerActivity.class);
        intent.putExtra("notificationID", notificationID);

        PendingIntent resultPendingIntent = PendingIntent.getActivity(getActivity(), 0, intent, 0);
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(notificationID, mBuilder.build());
    }
}