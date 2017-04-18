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
import android.os.AsyncTask;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        //導入Tab分頁的Fragment Layout
        return inflater.inflate(R.layout.item_missions, container, false);
    }
    /*@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        RecyclerView recyclerView = (RecyclerView) inflater.inflate(
                R.layout.recycler_view, container, false);
        ContentAdapter adapter = new ContentAdapter(recyclerView.getContext());
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        return recyclerView;
    }*/
    ArrayList<HashMap<String,String>> missionList;

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        String readDataFromHttp = null;

        myTaskGet httpGet = new myTaskGet();
        httpGet.execute();

        //get result from function "onPostExecute" in class "myTaskGet"
        try {
            readDataFromHttp = httpGet.get();
            //Parse JSON info
            Parsejson(readDataFromHttp);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(missionList);

        //取得TextView元件並帶入text字串
        TextView mText = (TextView) getView().findViewById(R.id.list_title);
        mText.setText(text);

        //取得TextView元件並帶入text字串
        TextView mText2 = (TextView) getView().findViewById(R.id.list_time);
        mText2.setText("03:00");

        //取得ImageView元件並帶入指定圖片
        ImageView mImg = (ImageView) getActivity().findViewById(R.id.list_avatar);
        mImg.setImageResource(R.drawable.missions_main);

        //取得ImageView元件並帶入指定圖片
        ImageView mImg2 = (ImageView) getActivity().findViewById(R.id.list_check);
        mImg2.setImageResource(R.drawable.missions_check);

    }

    /*public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView avator;
        public TextView name;
        public TextView description;
        public ViewHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.item_missions, parent, false));
            avator = (ImageView) itemView.findViewById(R.id.list_avatar);
            name = (TextView) itemView.findViewById(R.id.list_title);
            description = (TextView) itemView.findViewById(R.id.list_time);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Context context = v.getContext();
                    //Intent intent = new Intent(context, DetailActivity.class);
                    //intent.putExtra(DetailActivity.EXTRA_POSITION, getAdapterPosition());
                    //context.startActivity(intent);
                }
            });
        }
    }*/

    /**
     * Adapter to display recycler view.
     */
    /*public static class ContentAdapter extends RecyclerView.Adapter<ViewHolder> {
        // Set numbers of List in RecyclerView.
        private static final int LENGTH = 18;

        private final String[] mPlaces;
        private final String[] mPlaceDesc;
        private final Drawable[] mPlaceAvators;

        public ContentAdapter(Context context) {
            Resources resources = context.getResources();
            mPlaces = resources.getStringArray(R.array.places);
            mPlaceDesc = resources.getStringArray(R.array.place_desc);
            TypedArray a = resources.obtainTypedArray(R.array.place_avator);
            mPlaceAvators = new Drawable[a.length()];
            for (int i = 0; i < mPlaceAvators.length; i++) {
                mPlaceAvators[i] = a.getDrawable(i);
            }
            a.recycle();
        }
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()), parent);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.avator.setImageDrawable(mPlaceAvators[position % mPlaceAvators.length]);
            holder.name.setText(mPlaces[position % mPlaces.length]);
            holder.description.setText(mPlaceDesc[position % mPlaceDesc.length]);
        }

        @Override
        public int getItemCount() {
            return LENGTH;
        }
    }*/

    //Parse json received from server
    void Parsejson (String info){
        missionList = new ArrayList<>();
        try {
            JSONObject jObject = new JSONObject(info);
            JSONObject payload = new JSONObject(jObject.getString("payload"));
            JSONArray objects = payload.getJSONArray("objects");
            for(int i=0;i<objects.length();i++){
                JSONObject subObject;
                subObject = objects.getJSONObject(i);
                HashMap<String,String> mission = new HashMap<>();
                mission.put("title",subObject.getString("title"));
                mission.put("time_end",subObject.getString("time_end"));
                mission.put("class",subObject.getString("class"));      //MAIN, URG, SUB
                missionList.add(mission);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}

//HTTPGet
class myTaskGet extends AsyncTask<Void,Void,String> {
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

            url = new URL("http://192.168.0.2:8081/api/v1/mission/read");
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

            String line = null;
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
