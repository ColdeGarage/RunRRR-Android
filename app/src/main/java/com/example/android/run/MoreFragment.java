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
import android.graphics.Canvas;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.onbarcode.barcode.android.AndroidColor;
import com.onbarcode.barcode.android.AndroidFont;
import com.onbarcode.barcode.android.Code39;
import com.onbarcode.barcode.android.IBarcode;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutionException;

import static android.content.Context.MODE_PRIVATE;

/**
 * Provides UI for the view with List.
 */
public class MoreFragment extends Fragment {
    static MoreFragment instance = null;
    int uid;
    String token;
    String helpInfo = "";

    private RecyclerView recyclerView;
    private OnExpandAdapter adapter;

    public static MoreFragment getInstance() {
        synchronized (MoreFragment.class) {
            instance = new MoreFragment();
        }
        return instance;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_more, container, false);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.more_recycler_view);
        adapter = new OnExpandAdapter(recyclerView.getContext());
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        readPrefs();

        return rootView;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView moreItem_name;
        public LinearLayout moreItem_list;
        public LinearLayout moreItem_die;
        public LinearLayout moreItem_about_us;
        public LinearLayout moreItem_barcode;
        public LinearLayout moreItem_sos;
        public LinearLayout moreItem_logout;
        public TextView helpMe;
        public TextView helpInfoText;
        public TextView bt_enter;
        public final EditText editTextUid;
        public final EditText editTextPass;
        public TextView btYes;

        public RelativeLayout r ;

        private ViewHolder(final LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.item_list_more, parent, false));
            moreItem_name = (TextView) itemView.findViewById(R.id.list_title);
            moreItem_list = (LinearLayout) itemView.findViewById(R.id.more_list);
            moreItem_die = (LinearLayout) itemView.findViewById(R.id.more_die);
            moreItem_about_us = (LinearLayout) itemView.findViewById(R.id.more_about_us);
            moreItem_barcode = (LinearLayout) itemView.findViewById(R.id.more_barcode);
            moreItem_sos = (LinearLayout) itemView.findViewById(R.id.more_sos);
            moreItem_logout = (LinearLayout) itemView.findViewById(R.id.more_logout);

            bt_enter = (TextView) moreItem_die.findViewById(R.id.button_enter);
            editTextUid = (EditText) moreItem_die.findViewById(R.id.edit_uid);
            editTextPass = (EditText) moreItem_die.findViewById(R.id.edit_pass);

            helpMe = (TextView) moreItem_sos.findViewById(R.id.sosButton);
            helpInfoText = (TextView) moreItem_sos.findViewById(R.id.sosContent);

            r = (RelativeLayout) moreItem_barcode.findViewById(R.id.relative);

            btYes = (TextView) moreItem_logout.findViewById(R.id.logout_yes);
        }
    }

    /**
     * Adapter to display recycler view.
     */
    public class OnExpandAdapter extends RecyclerView.Adapter<ViewHolder> {
        // Set numbers of List in RecyclerView.
        private static final int LENGTH = 5;

        private final String[] mMoreList;

        //Record item position
        private int currentItem = -1;

        private OnExpandAdapter(Context context) {
            Resources resources = context.getResources();
            mMoreList = resources.getStringArray(R.array.more);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()), parent);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            final String moreItem = mMoreList[position % mMoreList.length];
            System.out.println("more on bind bind bind");
            holder.moreItem_name.setText(moreItem);

            //Set tag for recording item position
            holder.moreItem_list.setTag(position);

            //Set layout content
            holder.moreItem_die.setVisibility(View.GONE);
            holder.moreItem_about_us.setVisibility(View.GONE);
            holder.moreItem_barcode.setVisibility(View.GONE);
            holder.moreItem_sos.setVisibility(View.GONE);
            holder.moreItem_logout.setVisibility(View.GONE);

            switch(position) {
                case 0: //Die
                    holder.bt_enter.setOnClickListener(new TextView.OnClickListener(){
                        @Override
                        public void onClick(View v) {
                            String hunterUid = holder.editTextUid.getText().toString();
                            String hunterPass = holder.editTextPass.getText().toString();
                            if(!isNetworkAvailable()) {
                                Alert("Please check your internet connection, then try again.");
                            } else if (hunterUid.equals("")) {
                                Alert("Please enter a HUNTER UID.");
                            } else if (hunterPass.equals("")) {
                                Alert("Please enter a HUNTER PASSWORD.");
                            } else {
                                MyTaskPut diePut = new MyTaskPut();
                                diePut.execute(getResources().getString(R.string.apiURL) + "/member/liveordie", "uid=" + String.valueOf(uid) + "&operator_uid=" + hunterUid + "&token=" + hunterPass
                                                + "&status=" + true);

                                //get result from function "onPostExecute" in class "myTaskPut"
                                try {
                                    String readDataFromHttp = diePut.get();

                                    //If die
                                    if (parseJson(readDataFromHttp) == 0) {
                                        new AlertDialog.Builder(v.getContext())
                                                .setCancelable(false)   //按到旁邊也不會消失
                                                .setMessage("You are dead.")
                                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {} })
                                                .show();
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });
                    holder.moreItem_list.setBackgroundResource(R.color.die);

                    //Set visibility of layout content with currentItem
                    if (currentItem == position) {
                        holder.moreItem_die.setVisibility(View.VISIBLE);
                    } else {
                        holder.moreItem_die.setVisibility(View.GONE);
                        holder.moreItem_about_us.setVisibility(View.GONE);
                        holder.moreItem_barcode.setVisibility(View.GONE);
                        holder.moreItem_sos.setVisibility(View.GONE);
                        holder.moreItem_logout.setVisibility(View.GONE);
                    }
                    break;
                case 1: //About us

                    holder.moreItem_list.setBackgroundResource(R.color.about_us);

                    //Set visibility of layout content with currentItem
                    if (currentItem == position) {
                        holder.moreItem_about_us.setVisibility(View.VISIBLE);
                    } else {
                        holder.moreItem_die.setVisibility(View.GONE);
                        holder.moreItem_about_us.setVisibility(View.GONE);
                        holder.moreItem_barcode.setVisibility(View.GONE);
                        holder.moreItem_sos.setVisibility(View.GONE);
                        holder.moreItem_logout.setVisibility(View.GONE);
                    }
                    break;
                case 2: //Barcode

                    MyView barcode = new MyView(getContext());
                    holder.r.addView(barcode);

                    holder.moreItem_list.setBackgroundResource(R.color.barcode);

                    //Set visibility of layout content with currentItem
                    if (currentItem == position) {
                        holder.moreItem_barcode.setVisibility(View.VISIBLE);
                    } else {
                        holder.moreItem_die.setVisibility(View.GONE);
                        holder.moreItem_about_us.setVisibility(View.GONE);
                        holder.moreItem_barcode.setVisibility(View.GONE);
                        holder.moreItem_sos.setVisibility(View.GONE);
                        holder.moreItem_logout.setVisibility(View.GONE);
                    }
                    break;
                case 3: //SOS

                    myTaskGet httpGet = null;
                    try {
                        httpGet = new myTaskGet(getResources().getString(R.string.apiURL) + "/utility/0");
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }
                    httpGet.execute();
                    try {
                        ParseJsonForPhoneNumber(httpGet.get());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }

                    holder.helpInfoText.setText(helpInfo);

                    holder.helpMe.setOnClickListener(new TextView.OnClickListener(){
                        @Override
                        public void onClick(View view) {
                            myTaskGet httpGet= null;
                            try {
                                httpGet = new myTaskGet(getResources().getString(R.string.apiURL) + "/member/read?operator_uid="+String.valueOf(uid)+"&uid="+String.valueOf(uid)+"&token="+token);
                            } catch (MalformedURLException e) {
                                e.printStackTrace();
                            }
                            httpGet.execute();

                            try {
                                ParseJsonForLocation(httpGet.get());
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            } catch (ExecutionException e) {
                                e.printStackTrace();
                            }
                            Alert("原地待命");
                        }
                    });
                    holder.moreItem_list.setBackgroundResource(R.color.sos);

                    //Set visibility of layout content with currentItem
                    if (currentItem == position) {
                        holder.moreItem_sos.setVisibility(View.VISIBLE);
                    } else {
                        holder.moreItem_die.setVisibility(View.GONE);
                        holder.moreItem_about_us.setVisibility(View.GONE);
                        holder.moreItem_barcode.setVisibility(View.GONE);
                        holder.moreItem_sos.setVisibility(View.GONE);
                        holder.moreItem_logout.setVisibility(View.GONE);
                    }
                    break;
                case 4: //Logout

                    holder.btYes.setOnClickListener(new TextView.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //change login state
                            String KEY = "login";
                            if (!isNetworkAvailable()) {
                                Alert("Please check your internet connection, then try again.");
                            } else {
                                SharedPreferences settings = getContext().getSharedPreferences("data",MODE_PRIVATE);
                                settings.edit().putBoolean(KEY,false)
                                        .apply();

                                //intent to login
                                Context context = v.getContext();
                                MapsFragment.instance.updateHandler.removeCallbacks(MapsFragment.instance.updateRunnable);
                                Intent intent = new Intent(context, MainActivity.class);
                                context.startActivity(intent);
                                getActivity().finish();
                            }
                        }
                    });
                    holder.moreItem_list.setBackgroundResource(R.color.logout);

                    //Set visibility of layout content with currentItem
                    if (currentItem == position) {
                        holder.moreItem_logout.setVisibility(View.VISIBLE);
                    } else {
                        holder.moreItem_die.setVisibility(View.GONE);
                        holder.moreItem_about_us.setVisibility(View.GONE);
                        holder.moreItem_barcode.setVisibility(View.GONE);
                        holder.moreItem_sos.setVisibility(View.GONE);
                        holder.moreItem_logout.setVisibility(View.GONE);
                    }
                    break;
                default:
                    break;
            }

            //Set onclickListener
            holder.moreItem_list.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    Context context = v.getContext();
                    int tag = (Integer) v.getTag();

                    if (tag == currentItem) { //Click again
                        currentItem = -1; //initial the currentItem
                    } else {
                        currentItem = tag;
                    }

                    notifyDataSetChanged();

                }
            });
        }
        @Override
        public int getItemCount() {
            return LENGTH;
        }
    }

    //========================內存=========================
    private void readPrefs(){
        SharedPreferences settings = getContext().getSharedPreferences("data",MODE_PRIVATE);
        uid = settings.getInt("uid",0);
        token = settings.getString("token","");
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

    class MyTaskPut extends AsyncTask<String,Void,String> {
        @Override
        protected String doInBackground(String... arg0) {
            URL url;
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            StringBuilder stringBuilder;
            String urlStr = arg0[0];
            String para = arg0[1];

            try {
                url = new URL(urlStr);
                urlConnection = (HttpURLConnection) url.openConnection();

                //連線方式
                urlConnection.setRequestMethod("PUT");

                //設置輸出入流串
                urlConnection.setDoInput(true);
                urlConnection.setDoOutput(true);

                //POST方法不能緩存數據,需手動設置使用緩存的值為false
                urlConnection.setUseCaches(false);

                //Send request
                DataOutputStream wr = new DataOutputStream(urlConnection.getOutputStream());

                //encode data in UTF-8
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(wr, "UTF-8"));

                writer.write(para);

                //flush the data in buffer to server and close the writer
                writer.flush();
                writer.close();

                //read response
                reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                stringBuilder = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line + "\n");
                }
                return stringBuilder.toString();
            } catch(Exception e) {
                e.printStackTrace();
            } finally {
                urlConnection.disconnect();
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
    }

    void ParseJsonForPhoneNumber(String info) {
        if (info != null) {
            try {
                System.out.println("info = "+info);

                JSONObject jsonObj = new JSONObject(info);
                // Getting JSON Array node
                JSONArray objects = jsonObj.getJSONArray("object");
                // looping through All Contacts
                helpInfo="";
                for(int i=0; i < objects.length(); i++) {
                    JSONObject c = objects.getJSONObject(i);
                    System.out.println("c=");
                    System.out.println(c.getString("team") + "小:" + c.getString("name") + " " + c.getString("nickname") + " " + c.getString("phone") + "\n");
                    helpInfo += (c.getString("team") + "小:" + c.getString("name") + " " + c.getString("nickname") + " " + c.getString("phone") + "\n");
                }

            } catch (final JSONException e) {
                System.out.print("Json parsing error: " + e.getMessage());
            }
        } else {
            System.out.print("Couldn't get json from server.");
        }
    }

    void ParseJsonForLocation(String info) {
        if (info != null) {
            try {
                System.out.println("info = "+info);

                JSONObject jsonObj = new JSONObject(info);
                JSONObject payload = jsonObj.getJSONObject("payload");
                // Getting JSON Array node
                JSONArray objects = payload.getJSONArray("objects");
                // looping through All Contacts
                JSONObject c = objects.getJSONObject(0);

                MyTaskPut help = new MyTaskPut();
                help.execute(getResources().getString(R.string.apiURL)+"/member/callhelp", "uid=" + String.valueOf(uid) + "&operator_uid=" + String.valueOf(uid) + "&token=" + token
                        + "&position_e=" + c.getDouble("position_e") + "&position_n=" + c.getDouble("position_n"));

            } catch (final JSONException e) {
                System.out.print("Json parsing error: " + e.getMessage());
            }
        } else {
            System.out.print("Couldn't get json from server.");
        }
    }
    int parseJson (String info) {
        int brea = -1;
        if(info!=null) {
            try {
                JSONObject jObject = new JSONObject(info);
                brea = jObject.getInt("brea");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return brea;
    }

    public class MyView extends View {
        public MyView(Context context) {
            super(context);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            //show the barcode
            try{
                readPrefs();
                testCODE39(canvas);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private void testCODE39(Canvas canvas) throws Exception
    {
        Code39 barcode = new Code39();

        /*
           Code39 Valid data char set:
                0, 1, 2, 3, 4, 5, 6, 7, 8, 9 (Digits)
                A - Z (Uppercase letters)
                - (Dash), $ (Dollar), % (Percentage), (Space), . (Point), / (Slash), + (Plus)

           Code39 extension Valid data char set:
                All ASCII 128 characters
        */
        // Code39 encodes upper case chars only, for lower case chars, use Code 39 extension
        //barcode.setData("123456789012");
        barcode.setData(String.valueOf(uid));

        barcode.setExtension(false);

        barcode.setAddCheckSum(false);

        // Code 39 Wide Narrow bar Ratio
        // Valid value is from 2.0 to 3.0 inclusive.
        barcode.setN(3.0f);
        // The space between 2 characters in code 39; This a multiple of X; The default is 1.;
        // Valid value is from 1.0 (inclusive) to 5.3 (exclusive)
        barcode.setI(1.0f);
        barcode.setShowStartStopInText(true);

        // Unit of Measure, pixel, cm, or inch
        barcode.setUom(IBarcode.UOM_PIXEL);
        // barcode bar module width (X) in pixel
        barcode.setX(5f);
        // barcode bar module height (Y) in pixel
        barcode.setY(180f);

        // barcode image margins
        barcode.setLeftMargin(1f);
        barcode.setRightMargin(1f);
        barcode.setTopMargin(1f);
        barcode.setBottomMargin(1f);

        // barcode image resolution in dpi
        barcode.setResolution(36);

        // disply barcode encoding data below the barcode
        barcode.setShowText(false);
        // barcode encoding data font style
        barcode.setTextFont(new AndroidFont("Arial", Typeface.NORMAL, 24));
        // space between barcode and barcode encoding data
        barcode.setTextMargin(4);
        barcode.setTextColor(AndroidColor.black);

        // barcode bar color and background color in Android device
        barcode.setForeColor(AndroidColor.black);
        barcode.setBackColor(AndroidColor.white);

        /*
        specify your barcode drawing area
	    */
        //(left,top,right,button)
        RectF bounds = new RectF(150, 50, 0, 0);
        barcode.drawBarcode(canvas, bounds);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    //show an alert dialog
    void Alert(String mes) {
        new AlertDialog.Builder(getActivity())
                .setMessage(mes)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {} })
                .show();
    }
}