package com.example.android.run;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
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


public class SOSActivity extends AppCompatActivity {
    int uid;
    String token;
    String helpInfo;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sos);

        Button helpMe = (Button) findViewById(R.id.sosButton);
        TextView helpInfoText = (TextView) findViewById(R.id.sosContent);

        readPrefs();
        if(!isNetworkAvailable()){
            Alert("Please check your internet connection, then try again.");
        }
        myTaskGet httpGet= null;
        try {
            httpGet = new myTaskGet("http://coldegarage.tech:8081/api/v1.1/utility/0");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        httpGet.execute();
        try {
            System.out.println(httpGet.get());
            ParseJsonForPhoneNumber(httpGet.get());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        System.out.println("helpinfo= " + helpInfo);
        helpInfoText.setText(helpInfo);
        helpMe.setText("help me~~");

        helpMe.setOnClickListener(new TextView.OnClickListener(){
            @Override
            public void onClick(View view) {
                myTaskGet httpGet= null;
                try {
                    httpGet = new myTaskGet("http://coldegarage.tech:8081/api/v1.1/member/read?operator_uid="+String.valueOf(uid)+"&uid="+String.valueOf(uid)+"&token="+token);
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
            }
        });
    }

    //========================內存=========================
    private void readPrefs(){
        SharedPreferences settings = getSharedPreferences("data",MODE_PRIVATE);
        uid = settings.getInt("uid",0);
        token = settings.getString("token","");
    }
    void ParseJsonForPhoneNumber(String info){

        if (info != null) {
            try {
                System.out.println("info = "+info);

                JSONObject jsonObj = new JSONObject(info);
                // Getting JSON Array node
                JSONArray objects = jsonObj.getJSONArray("object");
                // looping through All Contacts
                JSONObject c = objects.getJSONObject(0);

                helpInfo = "第一小隊:" +c.getString("name") + " " + c.getString("nickname") + " " + c.getString("phone")+"\n";
                c = objects.getJSONObject(1);
                helpInfo += "第二小隊:" +c.getString("name") + " " + c.getString("nickname") + " " + c.getString("phone")+"\n";


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
    void ParseJsonForLocation(String info){

        if (info != null) {
            try {
                System.out.println("linfo = "+info);

                JSONObject jsonObj = new JSONObject(info);
                JSONObject payload = jsonObj.getJSONObject("payload");
                // Getting JSON Array node
                JSONArray objects = payload.getJSONArray("objects");
                // looping through All Contacts
                JSONObject c = objects.getJSONObject(0);

                MyTaskPut help = new MyTaskPut();
                help.execute("http://coldegarage.tech:8081/api/v1.1/member/callhelp","uid=" + String.valueOf(uid) + "&operator_uid=" + String.valueOf(uid) + "&token=" + token
                        + "&position_e=" + c.getDouble("position_e") + "&position_n=" + c.getDouble("position_n"));
                try {
                    System.out.println(help.get());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
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

    //Parameter in string array : [0] : api, [1] : parameter sended to db
    class MyTaskPut extends AsyncTask<String,Void,String> {

        @Override
        public void onPreExecute() {
            super.onPreExecute();
        }

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
                DataOutputStream wr = new DataOutputStream(
                        urlConnection.getOutputStream());

                //encode data in UTF-8
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(wr, "UTF-8"));

                writer.write(para);

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
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
    //show an alert dialog
    void Alert(String mes){
        new AlertDialog.Builder(SOSActivity.this)
                .setMessage(mes)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).show();
    }
}
