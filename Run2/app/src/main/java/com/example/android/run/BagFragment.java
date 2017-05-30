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
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import jp.wasabeef.blurry.Blurry;

import static android.content.Context.MODE_PRIVATE;
import static com.google.android.gms.internal.zzid.runOnUiThread;

/**
 * Provides UI for the view with Cards.
 */
//Tab分頁class繼承Fragment
public class BagFragment extends Fragment
{
    private SwipeRefreshLayout mSwipeLayout;
    private static int uid;
    private static String token;

    View forBlur;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        readPrefs();
        RecyclerView recyclerView = (RecyclerView) inflater.inflate(
                R.layout.recycler_view, container, false);
        ContentAdapter adapter = null;
        forBlur = inflater.inflate(R.layout.fragment, container, false);
        try {
            adapter = new ContentAdapter(recyclerView.getContext());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
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
    //=====================內存=====================
    private void readPrefs(){
        SharedPreferences settings = getContext().getSharedPreferences("data",MODE_PRIVATE);
        uid = settings.getInt("uid",0);
        token = settings.getString("token","");
    }
    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView tool1;
        public TextView name1;
        public TextView count1;
        public ImageView tool2;
        public TextView name2;
        public TextView count2;
        public ImageView tool3;
        public TextView name3;
        public TextView count3;
        public ViewHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.item_bag, parent, false));
            tool1 = (ImageView) itemView.findViewById(R.id.toolImage1);
            name1 = (TextView) itemView.findViewById(R.id.toolName1);
            count1 = (TextView) itemView.findViewById(R.id.toolNumber1);
            tool2 = (ImageView) itemView.findViewById(R.id.toolImage2);
            name2 = (TextView) itemView.findViewById(R.id.toolName2);
            count2 = (TextView) itemView.findViewById(R.id.toolNumber2);
            tool3 = (ImageView) itemView.findViewById(R.id.toolImage3);
            name3 = (TextView) itemView.findViewById(R.id.toolName3);
            count3 = (TextView) itemView.findViewById(R.id.toolNumber3);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int id = v.getId();
                    //TODO:Intent to other activity
                    Context context = v.getContext();

                    Intent intent = new Intent(context, BagPopActivity.class);
//                    intent.putExtra(MissionPopActivity.EXTRA_POSITION, getAdapterPosition());
                    startActivityForResult( intent, 2);
                    long startMs = System.currentTimeMillis();
                    Blurry.with(context)
                            .radius(25)
                            .sampling(2)
                            .async()
                            .animate(500)
                            .onto((ViewGroup) forBlur.findViewById(R.id.fragmentTab));
                    Log.d(getString(R.string.app_name),
                            "TIME " + String.valueOf(System.currentTimeMillis() - startMs) + "ms");
                }
            });
        }
    }
    /*
        * Adapter to display recycler view.
        */
    public class ContentAdapter extends RecyclerView.Adapter<BagFragment.ViewHolder> {
        private ArrayList<ArrayList<HashMap<String, String>>> packList = new ArrayList<>();
        private String[] toolIds = new String[30];
        private String[] clueIds = new String[30];

        private String[] pName = new String[60];
        private String[] pUrl = new String[60];
        private String[] pCount = new String[60];

        // Set numbers of List in RecyclerView.
        private int LENGTH;

        public ContentAdapter(Context context) throws MalformedURLException {

            myTaskGet httpGet = new myTaskGet("http://coldegarage.tech:8081/api/v1.1/pack/read?operator_uid="+String.valueOf(uid)+"&token="+token);

            httpGet.execute();
            //get tools[] and clues[]
            try {
                ParseJsonFromPack(httpGet.get());
            } catch (Exception e){
                e.printStackTrace();
            }
            for(int i =0 ; toolIds[i]!=null ; i++) {
                httpGet = new myTaskGet("http://coldegarage.tech:8081/api/v1.1/tool/read?operator_uid="+String.valueOf(uid)+"&token="+token +"&tid="+toolIds[i]);
                httpGet.execute();
                System.out.println("call tool, tid="+toolIds[i]);
                System.out.println("packList:"+ packList);
                try {
                    System.out.println("call parseJsonfromTools");
                    ParseJsonFromTools(httpGet.get());
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
            for(int i =0 ;  clueIds[i] != null; i++) {
                httpGet = new myTaskGet("http://coldegarage.tech:8081/api/v1.1/clue/read?operator_uid="+String.valueOf(uid)+"&token="+token +"&tid="+clueIds[i]);
                httpGet.execute();
                System.out.println("call clue, cid="+clueIds[i]);
                System.out.println("packList:"+ packList);
                try {
                    ParseJsonFromClues(httpGet.get());
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
            System.out.println("packList=");
            System.out.println(packList);

            //TODO Auto-generated method stub
            for(int i=0; i <packList.size(); i++){
                pName[i] = packList.get(i).get(0).get("title");
                System.out.println("in put name = " + pName[i]);
                pUrl[i] = packList.get(i).get(0).get("url");
                pCount[i] = packList.get(i).get(0).get("count");
            }
        }
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()), parent);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            //tool name count
            System.out.println("position=" + position);
            holder.name1.setText(pName[position*3]);
            holder.count1.setText(pCount[position*3]);
            holder.name2.setText(pName[position*3+1]);
            holder.count2.setText(pCount[position*3+1]);
            holder.name3.setText(pName[position*3+2]);
            holder.count3.setText(pCount[position*3+2]);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    //TODO Auto-generated method stub
                    final Bitmap mBitmap =
                            getBitmapFromURL("http://coldegarage.tech:8081/api/v1.1/download/img/" + pUrl[position*3]);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            holder.tool1.setImageBitmap(mBitmap);
                        }
                    });                    //TODO Auto-generated method stub
                    final Bitmap mBitmap2 =
                            getBitmapFromURL("http://coldegarage.tech:8081/api/v1.1/download/img/" + pUrl[position*3+1]);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            holder.tool2.setImageBitmap(mBitmap2);
                        }
                    });                    //TODO Auto-generated method stub
                    final Bitmap mBitmap3 =
                            getBitmapFromURL("http://coldegarage.tech:8081/api/v1.1/download/img/" + pUrl[position*3+2]);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            holder.tool3.setImageBitmap(mBitmap3);
                        }
                    });
                }
            }).start();

//            //missions type : MAIN,SUB,URG, set different icon
//            if(type.equals("MAIN")){
//                holder.type.setImageResource(R.drawable.missions_main_2);
//            }else if(type.equals("SUB")){
//                holder.type.setImageResource(R.drawable.missions_sub);
//            }else{
//                holder.type.setImageResource(R.drawable.missions_urg);
//            }
//            //holder.state.setImageDrawable(mState[position % mState.length]);
        }

        @Override
        public int getItemCount() {
            LENGTH = packList.size();
            if(LENGTH%3!=0) {
                return (LENGTH/3+1);
            }
            else return (LENGTH/3);
        }

        void ParseJsonFromPack(String info){
            String jsonStr = info;
            int tool_index = 0;
            int clue_index = 0;
            if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);
                    JSONObject payload = jsonObj.getJSONObject("payload");
                    // Getting JSON Array node
                    JSONArray objects = payload.getJSONArray("objects");
                    // looping through All Contacts
                    System.out.println("pack");
                    System.out.println(objects);
                    for (int i = 0; i < objects.length(); i++) {
                        JSONObject c = objects.getJSONObject(i);

                        String id = c.getString("id");
                        String  type = c.getString("class");
                        // tmp hash map for single contact
                        System.out.println("type=" + type);
                        if(type.equals("TOOL")){
                            toolIds[tool_index] = id;
                            tool_index++;
                            System.out.println("tool index++, index=" + tool_index);
                        }
                        else{
                            clueIds[clue_index] = id;
                            clue_index++;
                            System.out.println("clue index++, index=" + clue_index);
                        }
                    }
                    System.out.println("toolIds=");
                    System.out.println(toolIds);
                    System.out.println("clueIds=");
                    System.out.println(clueIds);
                } catch (final JSONException e) {
                    System.out.print("Json parsing error: " + e.getMessage());
                }
            } else {
                System.out.print("Couldn't get json from server.");
            }
            try {
                JSONObject jObject = new JSONObject(info);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        void ParseJsonFromTools(String info){
            String jsonStr = info;
            System.out.println("parse from tool"+jsonStr);
            if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);
                    JSONObject payload = jsonObj.getJSONObject("payload");
                    // Getting JSON Array node
                    JSONArray objects = payload.getJSONArray("objects");
                    // looping through All Contacts
                    System.out.println("tool");
                    System.out.println(objects);
                    for (int i = 0; i < objects.length(); i++) {
                        JSONObject c = objects.getJSONObject(i);

                        String tid = c.getString("tid");
                        String title = c.getString("title");
                        String content = c.getString("content");
                        String url = c.getString("url");
                        String expire = c.getString("expire");
                        String price = c.getString("price");

                        // tmp hash map for single contact
                        ArrayList<HashMap<String, String>> toolKind = new ArrayList<>();
                        HashMap<String, String> tool = new HashMap<>();

                        // adding each child node to HashMap key => value
                        tool.put("id", tid);
                        tool.put("title", title);
                        tool.put("content", content);
                        tool.put("url", url);
                        tool.put("expire", expire);
                        tool.put("price", price);

                        // adding contact to contact list
                        System.out.println("in parse tool");
                        System.out.println(tool);
                        System.out.println(findIndex(tool.get("title")));

                        if(findIndex(tool.get("title"))>=0){
                            packList.get(findIndex(tool.get("title"))).add(tool);
                            String ct = packList.get(findIndex(tool.get("title"))).get(0).get("count");
                            int aa = Integer.parseInt(ct);
                            aa++;
                            packList.get(findIndex(tool.get("title"))).get(0).put("count", String.valueOf(aa));
                        }
                        else {
                            tool.put("count","1");
                            toolKind.add(tool);
                            packList.add(toolKind);
                        }
                    }
                } catch (final JSONException e) {
                    System.out.print("Json parsing error: " + e.getMessage());
                }
            } else {
                System.out.print("Couldn't get json from server.");
            }
            try {
                JSONObject jObject = new JSONObject(info);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        int findIndex(String target){
            int i;
            System.out.println("finding index");
            System.out.println("target = " + target);

            for (i=0; i<packList.size(); i++){
                System.out.println("checking at title = " + packList.get(i).get(0).get("title"));
                if(target.equals(packList.get(i).get(0).get("title"))){
                    return i;
                }
            }
            return -1;
        }
        void ParseJsonFromClues(String info){
            String jsonStr = info;
            if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);
                    JSONObject payload = jsonObj.getJSONObject("payload");
                    // Getting JSON Array node
                    JSONArray objects = payload.getJSONArray("objects");
                    System.out.println("clue");
                    System.out.println(objects);
                    // looping through All Contacts
                    for (int i = 0; i < objects.length(); i++) {
                        JSONObject c = objects.getJSONObject(i);

                        String cid = c.getString("cid");
                        String content = c.getString("content");
                        // tmp hash map for single contact
                        ArrayList<HashMap<String, String>> clueS = new ArrayList<>();
                        HashMap<String, String> clue = new HashMap<>();

                        // adding each child node to HashMap key => value
                        clue.put("id", cid);
                        clue.put("content", content);
                        clue.put("title", "clue");
                        clue.put("url", "");
                        clue.put("count","1");
                        // adding contact to contact list
                        System.out.println(clue);
                        clueS.add(clue);
                        packList.add(clueS);
                    }
                } catch (final JSONException e) {
                    System.out.print("Json parsing error: " + e.getMessage());
                }
            } else {
                System.out.print("Couldn't get json from server.");
            }
            try {
                JSONObject jObject = new JSONObject(info);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        class myTaskGet extends AsyncTask<Void,Void,String> {
            myTaskGet(String toGet) throws MalformedURLException {
                url = new URL(toGet);
                System.out.println("url="+url);
            }
            @Override
            public void onPreExecute() {
                super.onPreExecute();
            }

            URL url;

            @Override
            public String doInBackground(Void... arg0) {
                BufferedReader reader = null;
                StringBuilder stringBuilder;

                try {
                    // create the HttpURLConnection
                    //http://192.168.0.2:8081/api/v1/tool/read
                    //url = new URL("http://192.168.0.2:8081/api/v1.1/pack/read?operator_uid=1"/*&tid="+tid*/); //Just use to try this function is able to work or not
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                    // 使用甚麼方法做連線
                    connection.setRequestMethod("GET");

                    // 是否添加參數(ex : json...等)
                    //connection.setDoOutput(true);

                    // 設定TimeOut時間
                    connection.setReadTimeout(15 * 1000);
                    connection.connect();

                    // 伺服器回來的參數
                    reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    stringBuilder = new StringBuilder();

                    String line;
                    while ((line = reader.readLine()) != null) {
                        stringBuilder.append(line + "\n");
                    }
                    System.out.println("happy~");
                    System.out.print(stringBuilder.toString());
                    return stringBuilder.toString();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    // close the reader; this can throw an exception too, so
                    // wrap it in another try/catch block.
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
            @Override
            public void onPostExecute(String result) {
                super.onPostExecute(result);
            }
        }
    }
    private static Bitmap getBitmapFromURL(String imageUrl) {
        try {
            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap bitmap = BitmapFactory.decodeStream(input);
            return bitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
