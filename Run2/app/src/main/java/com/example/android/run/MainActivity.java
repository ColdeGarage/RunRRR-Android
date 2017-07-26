package com.example.android.run;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    boolean loginState;
    String account_in, pass_in;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_layout);
        loginState = false;

        /*get Preference file
        * if user has logged in before
        * he/she will be directly leaded to Map*/
        if(readPrefs()){
            goMap();
            //Alert("Skip Login");
        }

        ImageView icon = (ImageView) findViewById(R.id.ic_login);
        TextView login = (TextView) findViewById(R.id.login);
        TextView about = (TextView) findViewById(R.id.about_us);
        final EditText acc = (EditText) findViewById(R.id.account);
        final EditText pass = (EditText) findViewById(R.id.password);

        icon.setImageResource(R.drawable.ic_login);

        //add space at the beginning
        acc.setPadding(10, 0, 0, 0);
        pass.setPadding(10, 0, 0, 0);

        login.setOnClickListener(new TextView.OnClickListener() {
            @Override
            public void onClick(View v) {
                account_in = acc.getText().toString();
                pass_in = pass.getText().toString();

                if(account_in.isEmpty()){
                    Alert("Account can't be empty.");
                }else if(pass_in.isEmpty()) {
                    Alert("Password can't be empty.");
                }else {
                    //if account&password aren't empty, check whether it's valid
                    checkAccount();
                }
            }
        });

        about.setOnClickListener(new TextView.OnClickListener() {
            @Override
            public void onClick(View v) {
            goAboutUs();
            }
        });
    }

    @Override
    public boolean moveTaskToBack(boolean nonRoot) {
        return super.moveTaskToBack(nonRoot);
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    void checkAccount(){
        String readDataFromHttp = null;

        //POST email&password to server
        MyTaskPost httpPost = new MyTaskPost();
        httpPost.execute();

        try {
            //get result from function "onPostExecute" in class "myTaskPost"
            readDataFromHttp = httpPost.get();
            //System.out.println(readDataFromHttp);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //If brea=0, login success
        if(Parsejson(readDataFromHttp)==0){
            loginState = true;
            storePrefs();
            //Alert("Success");
            goMap();
        }else{
            Alert("Login Fail");
        }
    }

    //show an alert dialog
    void Alert(String mes){
        new AlertDialog.Builder(MainActivity.this)
                .setMessage(mes)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).show();
    }
    private int uid;
    private String token;
    //Parse json received from server
    int Parsejson (String info){
        System.out.println("info is");
        System.out.println(info);
        int correct=1;
        try {
            JSONObject jObject = new JSONObject(info);
            uid = jObject.getInt("uid");
            token = jObject.getString("token");
            JSONObject payload = new JSONObject(jObject.getString("payload"));
            correct = payload.getInt("correct");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return correct;
    }

    //===================Intent==========================
    void goMap(){
        Intent intent = new Intent();
        intent.setClass(MainActivity.this,ViewPagerActivity.class);
        startActivity(intent);
        finish();
    }

    void goAboutUs(){
        Intent intent = new Intent();
        intent.setClass(MainActivity.this,AboutUsActivity.class);
        startActivity(intent);
        finish();
    }

    //===================內存=========================
    //store login state and uid
    private void storePrefs(){
        SharedPreferences settings = getSharedPreferences("data",MODE_PRIVATE);
        settings.edit().putBoolean("login",loginState)
                        .putInt("uid",uid)
                        .putString("token",token)
                        .apply();
    }
    //read login state
    private boolean readPrefs(){
        SharedPreferences settings = getSharedPreferences("data",MODE_PRIVATE);
        return settings.getBoolean("login",false);
    }

    //===================HTTP==========================
    //HTTPGet, we will not use it in this Activity
    class MyTaskGet extends AsyncTask<Void,Void,String>{
        @Override
        public void onPreExecute() {
            super.onPreExecute();
        }
        @Override
        public String doInBackground(Void...arg0) {
            URL url;
            BufferedReader reader = null;
            StringBuilder stringBuilder;

            try
            {
                // create the HttpURLConnection

                url = new URL("https://www.google.com.tw"); //Just use this to try the function is able to work or not
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

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

    //HTTPPost
    class MyTaskPost extends AsyncTask<Void,Void,String>{

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
                url = new URL(getApplicationContext().getResources().getString(R.string.apiURL) + "/member/login");
                urlConnection = (HttpURLConnection) url.openConnection();

                //連線方式
                urlConnection.setRequestMethod("POST");

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

                writer.write("email=" + account_in + "&password=" + pass_in);

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
}

