package com.example.android.run;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

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

public class BagPopActivity extends Activity {

    private static String uid;
    private static String token;
    private String[] IDs;
    private int currentToolIndex;
    private boolean needToReload=false;
    private int liveStatus=1;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bag_pop);

        // setting the tab size depending on device size
        int height;
        boolean tabletSize = getResources().getBoolean(R.bool.isTablet);
        LinearLayout margin = (LinearLayout) findViewById(R.id.bag_pop_margin);

        if (tabletSize) {
            // convert dip to pixels
            height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 147, getResources().getDisplayMetrics());
            margin.getLayoutParams().height = height;
        } else {
            // convert dip to pixels
            height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 103, getResources().getDisplayMetrics());
            margin.getLayoutParams().height = height;
        }

//        final ImageView toolPicture = (ImageView) findViewById(R.id.popToolImage);
        TextView toolName = (TextView) findViewById(R.id.popToolName);
        TextView toolContent = (TextView) findViewById(R.id.popContent);
        TextView useButton = (TextView) findViewById(R.id.useButton);
        TextView backButton = (TextView) findViewById(R.id.popCancelButton);
        final TextView toolCount = (TextView) findViewById(R.id.popCount);

        //get data from bagFragment
        final Bundle bundleReciever =this.getIntent().getExtras();
        final String name = bundleReciever.getString("NAME");
        String content = bundleReciever.getString("CONTENT");
        String count = " 擁有:" + bundleReciever.getString("COUNT");
        IDs = bundleReciever.getStringArray("IDs");

        currentToolIndex=0;

        uid = bundleReciever.getString("uid");
        token = bundleReciever.getString("token");
        try {
            myTaskGet httpGet= new myTaskGet("http://coldegarage.tech:8081/api/v1.1/member/read?operator_uid="+String.valueOf(uid)+"&uid="+String.valueOf(uid)+"&token="+token);
            httpGet.execute();
            try {
                ParseJson(httpGet.get());
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        toolName.setText(name);
        toolContent.setText(content);
        toolCount.setText(count);

//        toolName.setTextSize(30);
//        toolName.setTextColor(Color.BLACK);
//        toolContent.setTextSize(26);
//        toolContent.setTextColor(Color.BLACK);
//        toolCount.setTextSize(16);
//        toolCount.setTextColor(Color.BLACK);
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                //TODO Auto-generated method stub
//                final Bitmap mBitmap =
//                        getBitmapFromURL("http://coldegarage.tech:8081/api/v1.1/download/img/" + imageUrl);
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        toolPicture.setImageBitmap(mBitmap);
//                    }
//                });                    //TODO Auto-generated method stub
//            }
//        }).start();
        useButton.setText("USE");
        backButton.setText("CANCEL");
        backButton.setOnClickListener(new TextView.OnClickListener(){
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        useButton.setOnClickListener(new TextView.OnClickListener(){
            @Override
            public void onClick(View view) {
                if(liveStatus==0){
                    Alert("你已經屎惹啊！不能用道具囉～");
//                    Intent intent = new Intent(BagPopActivity.this, ToolUsedActivity.class);
//                    Bundle bundle=new Bundle();
//                    bundle.putString("content","你已經屎惹啊！不能用道具囉～");
//                    intent.putExtras(bundle);
//                    startActivity(intent);
//                    Toast.makeText(BagPopActivity.this, "你已經屎惹啊！不能用道具囉～", Toast.LENGTH_SHORT).show();
                }
                else if(name.equals("金錢")){
//                    Intent intent = new Intent(BagPopActivity.this, ToolUsedActivity.class);
//                    Bundle bundle=new Bundle();
//                    bundle.putString("content","會有機會用到的，嘿嘿嘿");
//                    intent.putExtras(bundle);
//                    startActivity(intent);
                    Alert("會有機會用到的，嘿嘿嘿");
//                    new AlertDialog.Builder(BagPopActivity.this)
//                            .setCancelable(false)   //按到旁邊也不會消失
//                            .setMessage("會有機會用到的，嘿嘿嘿")
//                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialog, int which) {
//                                }
//                            }).show();
                }
                else if(IDs != null && Integer.valueOf(bundleReciever.getString("COUNT"))-currentToolIndex>0) {

                    String readDataFromHttp = null;

                    //POST email&password to server
                    MyTaskDelete httpDelete = new MyTaskDelete();
                    httpDelete.execute();

                    try {
                        //get result from function "onPostExecute" in class "myTaskPost"
                        System.out.println(httpDelete.get());
                        //System.out.println(readDataFromHttp);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    needToReload = true;
                    currentToolIndex++;
                    int currentToolNumber = Integer.valueOf(bundleReciever.getString("COUNT"))-currentToolIndex;
                    String count = " 擁有: " + currentToolNumber;
                    toolCount.setText(count);
//                    Toast.makeText(BagPopActivity.this, "你使用了一個 " + name + "!", Toast.LENGTH_SHORT).show();
//
//                    Intent intent = new Intent(BagPopActivity.this, ToolUsedActivity.class);
//                    Bundle bundle=new Bundle();
//                    bundle.putString("content","你使用了一個 " + name + "!");
//                    intent.putExtras(bundle);
//                    startActivity(intent);
                    Alert("你使用了一個 " + name + "!");
//                    new AlertDialog.Builder(BagPopActivity.this)
//                            .setCancelable(false)   //按到旁邊也不會消失
//                            .setMessage("你使用了一個 " + name + "!")
//                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialog, int which) {
//                                }
//                            }).show();
                }
                else{

                    new AlertDialog.Builder(BagPopActivity.this)
                            .setCancelable(false)   //按到旁邊也不會消失
                            .setMessage("此物品已用盡或無法使用")
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            }).show();
                }
            }
        });
    }
    public void onBackPressed() {
        Intent intent = new Intent();
        if(needToReload){
            setResult(2,intent);
        }
        else{
            setResult(3,intent);
        }
        finish();
    }
    //HTTPPost
    class MyTaskDelete extends AsyncTask<Void,Void,String>{

        @Override
        public void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... params) {
            URL url;
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            StringBuilder stringBuilder;

            try {
                url = new URL("http://coldegarage.tech:8081/api/v1.1/pack/delete");
                urlConnection = (HttpURLConnection) url.openConnection();

                //連線方式
                urlConnection.setRequestMethod("DELETE");

                //設置輸出入流串
                urlConnection.setDoInput(true);
                urlConnection.setDoOutput(true);

                //POST方法不能緩存數據,需手動設置使用緩存的值為false
                urlConnection.setUseCaches(false);

                //Send request
                DataOutputStream wr = new DataOutputStream(
                        urlConnection.getOutputStream());

                //encode data in UTF-8
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(wr, "UTF-8"));
                System.out.println("operator_uid=" + uid + "&uid=" + uid + "&token=" + token + "&pid="+IDs[currentToolIndex]);
                writer.write("operator_uid=" + uid + "&uid=" + uid + "&token=" + token + "&pid="+IDs[currentToolIndex]);

                //flush the data in buffer to server and close the writer
                writer.flush();
                writer.close();

                //read response
                reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                stringBuilder = new StringBuilder();
                String line ;

                while ((line = reader.readLine()) != null)
                {
                    stringBuilder.append(line + "\n");
                }
                return stringBuilder.toString();

            }catch(Exception e){
                e.printStackTrace();
            }
            finally {
                urlConnection.disconnect();
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
//                    System.out.println("happy~");
//                    System.out.print(stringBuilder.toString());
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

    void ParseJson(String info){
        String jsonStr = info;

        if (jsonStr != null) {
            try {
                System.out.println("linfo = "+info);

                JSONObject jsonObj = new JSONObject(jsonStr);
                JSONObject payload = jsonObj.getJSONObject("payload");
                // Getting JSON Array node
                JSONArray objects = payload.getJSONArray("objects");
                // looping through All Contacts
                JSONObject c = objects.getJSONObject(0);
                liveStatus = c.getInt("status");
                System.out.println("liveStatus= "+liveStatus);

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
    //show an alert dialog
    void Alert(String mes){
        new AlertDialog.Builder(BagPopActivity.this)
                .setMessage(mes)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).show();
    }
}