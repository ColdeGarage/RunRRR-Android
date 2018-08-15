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

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import static android.content.Context.MODE_PRIVATE;
import static com.google.android.gms.internal.zzid.runOnUiThread;

/**
 * Provides UI for the view with Cards.
 */
//Tab分頁class繼承Fragment
public class BagFragment extends Fragment
{
    public static int REFRESH = 0, NOT_REFRESH = 1;

    private static int uid;
    private static String token;
    private static BagFragment instance = null;
    private ContentAdapter adapter = null;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout refreshLayout;

    private ArrayList<ArrayList<HashMap<String, String>>> packList = new ArrayList<>();
    private String[] toolIds = new String[100];
    private String[] toolPIds = new String[100];
    private String[] clueIds = new String[50];
    private String[] pName = new String[200];
    private String[] pUrl = new String[200];
    private String[] pCount = new String[200];
    private String[] pContent = new String[200];
    private ArrayList<String[]> pID = new ArrayList<>();
    int toolNum=0;

    public static BagFragment getInstance() {
        synchronized (BagFragment.class) {
            instance = new BagFragment();
        }
        return instance;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        readPrefs();
        View rootView = inflater.inflate(R.layout.recycler_view, container, false);

        recyclerView = (RecyclerView) rootView.findViewById(R.id.bag_recycler_view);

        refreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.refresh_layout);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                instance.Refresh();
                //Notify();
                refreshLayout.setRefreshing(false);
            }
        });

        try {
            adapter = new ContentAdapter(recyclerView.getContext());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        return rootView;
    }

    public void Refresh() {
        try {
            adapter = new ContentAdapter(recyclerView.getContext());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        recyclerView.setAdapter(adapter);
    }

    //====================內存====================
    private void readPrefs() {
        SharedPreferences settings = getContext().getSharedPreferences("data",MODE_PRIVATE);
        uid = settings.getInt("uid",0);
        token = settings.getString("token","");
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView tool1;
        TextView name1;
        TextView count1;
        ImageView tool2;
        TextView name2;
        TextView count2;
        ImageView tool3;
        TextView name3;
        TextView count3;

        ViewHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.fragment_bag, parent, false));
            tool1 = (ImageView) itemView.findViewById(R.id.toolImage1);
            name1 = (TextView) itemView.findViewById(R.id.toolName1);
            count1 = (TextView) itemView.findViewById(R.id.toolNumber1);
            tool2 = (ImageView) itemView.findViewById(R.id.toolImage2);
            name2 = (TextView) itemView.findViewById(R.id.toolName2);
            count2 = (TextView) itemView.findViewById(R.id.toolNumber2);
            tool3 = (ImageView) itemView.findViewById(R.id.toolImage3);
            name3 = (TextView) itemView.findViewById(R.id.toolName3);
            count3 = (TextView) itemView.findViewById(R.id.toolNumber3);
        }
    }

    //Adapter to display recycler view.
    public class ContentAdapter extends RecyclerView.Adapter<ViewHolder> {
        // Set numbers of List in RecyclerView.
        private int LENGTH;

        ContentAdapter(Context context) throws MalformedURLException {
            if(!isNetworkAvailable()) {
                Alert("Please check your internet connection, then try again.");
            }
            packList.clear();
            toolIds = new String[100];
            toolPIds = new String[100];
            clueIds = new String[50];
            pName = new String[200];
            pUrl = new String[200];
            pCount = new String[200];
            pContent = new String[200];
            pID.clear();
            toolNum=0;
            int money=0;

            Resources resources = context.getResources();
            myTaskGet httpGet= new myTaskGet(resources.getString(R.string.apiURL)+"/member/read?operator_uid="+String.valueOf(uid)+"&uid="+String.valueOf(uid)+"&token="+token);
            httpGet.execute();
            try {
                money = ParseJsonFromMemberForMoney(httpGet.get());
            } catch (Exception e) {
                e.printStackTrace();
            }

            httpGet = new myTaskGet(resources.getString(R.string.apiURL)+"/pack/read?operator_uid="+String.valueOf(uid)+"&uid="+String.valueOf(uid)+"&token="+token);
            httpGet.execute();
            //get tools[] and clues[]
            try {
                ParseJsonFromPack(httpGet.get());
            } catch (Exception e) {
                e.printStackTrace();
            }

            for(int i =0 ; toolIds[i]!=null ; i++) {
                httpGet = new myTaskGet(resources.getString(R.string.apiURL)+"/tool/read?operator_uid="+String.valueOf(uid)+"&uid="+String.valueOf(uid)+"&token="+token +"&tid="+toolIds[i]);
                httpGet.execute();
                try {
                    ParseJsonFromTools(httpGet.get(),toolPIds[i]);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            for(int i =0 ;  clueIds[i] != null; i++) {
                httpGet = new myTaskGet(resources.getString(R.string.apiURL)+"/clue/read?operator_uid="+String.valueOf(uid)+"&uid="+String.valueOf(uid)+"&token="+token +"&cid="+clueIds[i]);
                httpGet.execute();
                try {
                    ParseJsonFromClues(httpGet.get());
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
            System.out.println("packList=");
            System.out.println(packList);
            System.out.println("toolNum = " + toolNum);

            pName[0] = "金錢";
            pUrl[0] = "money.jpg";
            pCount[0] = String.valueOf(money);
            pContent[0] = "刀，Dollar";
            for(int i=1; i <=packList.size(); i++){
                pName[i] = packList.get(i-1).get(0).get("title");
                pUrl[i] = packList.get(i-1).get(0).get("url");
                pCount[i] = packList.get(i-1).get(0).get("count");
                pContent[i] = packList.get(i-1).get(0).get("content");

                if(i<=toolNum){
                    String[] pid = new String[50];
                    for(int j=0; j < packList.get(i-1).size(); j++){
                        pid[j] = packList.get(i-1).get(j).get("pid");
                    }
                    pID.add(pid);
                }
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

            if(pCount[position*3]!="" && pCount[position*3]!="0" && pCount[position*3] != null) {
                holder.count1.setText(" x" + pCount[position*3] + " ");
                holder.count1.setVisibility(View.VISIBLE);
            } else {
                holder.count1.setVisibility(View.INVISIBLE);
            }
            holder.name2.setText(pName[position*3+1]);
            if(pCount[position*3+1]!="" && pCount[position*3+1]!="0" && pCount[position*3+1] != null) {
                holder.count2.setText(" x" + pCount[position*3+1] + " ");
                holder.count2.setVisibility(View.VISIBLE);
            } else {
                holder.count2.setVisibility(View.INVISIBLE);
            }
            holder.name3.setText(pName[position*3+2]);
            if(pCount[position*3+2]!="" && pCount[position*3+2]!="0" && pCount[position*3+2] != null) {
                holder.count3.setText(" x" + pCount[position*3+2] + " ");
                holder.count3.setVisibility(View.VISIBLE);
            } else {
                holder.count3.setVisibility(View.INVISIBLE);
            }

            new Thread(new Runnable() {
                @Override
                public void run() {
                    Resources resources = getContext().getResources();

                    if( pUrl[position*3]!="") {
                        final Bitmap mBitmap =
                                getBitmapFromURL(resources.getString(R.string.apiURL)+"/download/img/" + pUrl[position * 3]);
                        if(mBitmap!=null) {
                            final Bitmap circularBitmap = ImageConverter.getRoundedCornerBitmap(mBitmap, 30);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    holder.tool1.setImageBitmap(circularBitmap);
                                }
                            });
                        }
                    }

                    if( pUrl[position*3+1]!="" && pUrl[position*3+1]!=null) {
                        final Bitmap mBitmap2 =
                                getBitmapFromURL(resources.getString(R.string.apiURL)+"/download/img/" + pUrl[position * 3 + 1]);
                        if(mBitmap2!=null) {
                            final Bitmap circularBitmap2 = ImageConverter.getRoundedCornerBitmap(mBitmap2, 30);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    holder.tool2.setImageBitmap(circularBitmap2);
                                }
                            });
                        }
                    }

                    if( pUrl[position*3+2]!="" && pUrl[position*3+2]!=null) {
                        final Bitmap mBitmap3 =
                                getBitmapFromURL(resources.getString(R.string.apiURL)+"/download/img/" + pUrl[position * 3 + 2]);
                        if(mBitmap3!=null) {
                            final Bitmap circularBitmap3 = ImageConverter.getRoundedCornerBitmap(mBitmap3, 30);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    holder.tool3.setImageBitmap(circularBitmap3);
                                }
                            });
                        }
                    }
                }
            }).start();

            holder.tool1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View vv) {
                    Context context = vv.getContext();

                    Intent intent = new Intent(context, BagPopActivity.class);

                    Bundle bundle=new Bundle();

                    bundle.putString("uid",String.valueOf(uid));
                    bundle.putString("token",token);
                    bundle.putString("NAME",pName[position*3]);
                    bundle.putString("IMAGEURL",pUrl[position*3]);
                    bundle.putString("CONTENT",pContent[position*3]);
                    bundle.putString("COUNT",pCount[position*3]);
                    if(position*3<=toolNum && position!=0){
                        bundle.putStringArray("IDs",pID.get(position*3-1));
                    }
                    intent.putExtras(bundle);
                    startActivityForResult( intent, REFRESH);
                }
            });
            if(LENGTH > position*3+1){
                holder.tool2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View vv) {
                        Context context = vv.getContext();

                        Intent intent = new Intent(context, BagPopActivity.class);

                        Bundle bundle=new Bundle();
                        bundle.putString("uid",String.valueOf(uid));
                        bundle.putString("token",token);
                        bundle.putString("NAME",pName[position*3+1]);
                        bundle.putString("IMAGEURL",pUrl[position*3+1]);
                        bundle.putString("CONTENT",pContent[position*3+1]);
                        bundle.putString("COUNT",pCount[position*3+1]);
                        if(position*3+1<=toolNum){
                            bundle.putStringArray("IDs",pID.get(position*3));
                        }
                        intent.putExtras(bundle);
                        startActivityForResult( intent, REFRESH);
                    }
                });
            }
            if(LENGTH > position*3+2){
                holder.tool3.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View vv) {
                        Context context = vv.getContext();

                        Intent intent = new Intent(context, BagPopActivity.class);

                        Bundle bundle=new Bundle();
                        bundle.putString("uid",String.valueOf(uid));
                        bundle.putString("token",token);
                        bundle.putString("NAME",pName[position*3+2]);
                        bundle.putString("IMAGEURL",pUrl[position*3+2]);
                        bundle.putString("CONTENT",pContent[position*3+2]);
                        bundle.putString("COUNT",pCount[position*3+2]);
                        if(position*3+2<=toolNum){
                            bundle.putStringArray("IDs",pID.get(position*3+1));
                        }
                        intent.putExtras(bundle);
                        startActivityForResult( intent, REFRESH);
                    }
                });
            }
        }

        @Override
        public int getItemCount() {
            LENGTH = packList.size()+1;
            if(LENGTH%3!=0) {
                return (LENGTH/3+1);
            }
            else return (LENGTH/3);
        }
    }

    // Call Back method to get the Message form other Activity
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // check if the request code is same as what is passed here it is 0
        if(resultCode==REFRESH)
        {
            System.out.println("back with code 0");
            Refresh();
        }
        else if(resultCode==NOT_REFRESH){
            System.out.println("back with code 1");
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

    void ParseJsonFromPack(String info){
        int tool_index = 0;
        int clue_index = 0;
        if (info != null) {
            try {
                JSONObject jsonObj = new JSONObject(info);
                JSONObject payload = jsonObj.getJSONObject("payload");
                // Getting JSON Array node
                JSONArray objects = payload.getJSONArray("objects");
                // looping through All Contacts
                System.out.println("pack");
                System.out.println(objects);
                for (int i = 0; i < objects.length(); i++) {
                    JSONObject c = objects.getJSONObject(i);

                    String id = c.getString("id");
                    String pid = c.getString("pid");
                    String  type = c.getString("class");
                    if(type.equals("TOOL")){
                        toolIds[tool_index] = id;
                        toolPIds[tool_index] = pid;
                        tool_index++;
                    } else{
                        clueIds[clue_index] = id;
                        clue_index++;
                    }
                }
            } catch (final JSONException e) {
                System.out.print("Json parsing error: " + e.getMessage());
            }
        } else {
            System.out.print("Couldn't get json from server.");
        }
    }

    void ParseJsonFromTools(String info, String pid) {
        System.out.println("parse from tool"+info);
        if (info != null) {
            try {
                JSONObject jsonObj = new JSONObject(info);
                JSONObject payload = jsonObj.getJSONObject("payload");
                // Getting JSON Array node
                JSONArray objects = payload.getJSONArray("objects");
                // looping through All Contacts

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
                    tool.put("pid",pid);
                    tool.put("id", tid);
                    tool.put("title", title);
                    tool.put("content", content);
                    tool.put("url", url);
                    tool.put("expire", expire);
                    tool.put("price", price);

                    // adding contact to contact list
                    if(findIndex(tool.get("title"))>=0){
                        packList.get(findIndex(tool.get("title"))).add(tool);
                        String ct = packList.get(findIndex(tool.get("title"))).get(0).get("count");
                        int aa = Integer.parseInt(ct);
                        aa++;
                        packList.get(findIndex(tool.get("title"))).get(0).put("count", String.valueOf(aa));
                    } else {
                        tool.put("count","1");
                        toolKind.add(tool);
                        packList.add(toolKind);
                    }
                    toolNum = packList.size();
                }
            } catch (final JSONException e) {
                System.out.print("Json parsing error: " + e.getMessage());
            }
        } else {
            System.out.print("Couldn't get json from server.");
        }
    }

    int ParseJsonFromMemberForMoney(String info) {
        int money=0;
        if (info != null) {
            try {
                System.out.println("member info = "+info);
                JSONObject jsonObj = new JSONObject(info);
                JSONObject payload = jsonObj.getJSONObject("payload");
                // Getting JSON Array node
                JSONArray objects = payload.getJSONArray("objects");
                // looping through All Contacts
                JSONObject c = objects.getJSONObject(0);
                money = c.getInt("money");
            } catch (final JSONException e) {
                System.out.print("Json parsing error: " + e.getMessage());
            }
        } else {
            System.out.print("Couldn't get json from server.");
        }
        return money;
    }

    int findIndex(String target) {
        for (int i=0; i<packList.size(); i++){
            if(target.equals(packList.get(i).get(0).get("title")))
                return i;
        }
        return -1;
    }

    void ParseJsonFromClues(String info) {
        if (info != null) {
            try {
                JSONObject jsonObj = new JSONObject(info);
                JSONObject payload = jsonObj.getJSONObject("payload");
                // Getting JSON Array node
                JSONArray objects = payload.getJSONArray("objects");
                // looping through All Contacts
                for (int i = 0; i < objects.length(); i++) {
                    JSONObject c = objects.getJSONObject(i);

                    String cid = c.getString("cid");
                    String content = c.getString("content");
                    // tmp hash map for single contact
                    ArrayList<HashMap<String, String>> clues = new ArrayList<>();
                    HashMap<String, String> clue = new HashMap<>();

                    // adding each child node to HashMap key => value
                    clue.put("id", cid);
                    clue.put("content", content);
                    clue.put("title", "clue");
                    clue.put("url", "clue.jpg");
                    clue.put("count","1");
                    // adding contact to contact list
                    clues.add(clue);
                    packList.add(clues);
                }
            } catch (final JSONException e) {
                System.out.print("Json parsing error: " + e.getMessage());
            }
        } else {
            System.out.print("Couldn't get json from server.");
        }
    }

    class myTaskGet extends AsyncTask<Void,Void,String> {
        URL url;

        myTaskGet(String toGet) throws MalformedURLException {
            url = new URL(toGet);
            System.out.println("url="+url);
        }

        @Override
        public String doInBackground(Void... arg0) {
            BufferedReader reader = null;
            StringBuilder stringBuilder;

            try {
                // create the HttpURLConnection
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
                return stringBuilder.toString();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
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
    private void Alert(String mes){
        new AlertDialog.Builder(getActivity())
                .setMessage(mes)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {} })
                .show();
    }
}
