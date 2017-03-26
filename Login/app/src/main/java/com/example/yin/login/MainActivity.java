package com.example.yin.login;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    Map<String,String> Account = new HashMap<String,String>();
    boolean loginState;
    String account_in, pass_in;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        loginState = false;


        //exampleAccount
        Account.put("hihi","1234");
        Account.put("byebye","8888");

        Button bt = (Button) findViewById(R.id.button);
        final EditText acc = (EditText) findViewById(R.id.editText2);
        final EditText pass = (EditText) findViewById(R.id.editText3);

        bt.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                account_in = acc.getText().toString();
                pass_in = pass.getText().toString();
                if(account_in.isEmpty()){
                    Alert("Account can't be empty.");

                }else if(pass_in.isEmpty()) {
                    Alert("Password can't be empty.");

                }else {
                    checkAccount();
                }
            }
        });
    }

    void checkAccount(){
        loginState = true;
        String readDataFromHttp = null;
        myTaskPost httpPost = new myTaskPost();
        httpPost.execute();
        try {
            readDataFromHttp = httpPost.get();
            System.out.println(readDataFromHttp);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if(Parsejson(readDataFromHttp)==0){
            Alert("Success");
            //goMap();
        }else{
            Alert("Fail");
        }
    }

    void Alert(String mes){
        new AlertDialog.Builder(MainActivity.this)
                .setMessage(mes)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).show();
    }

    int Parsejson (String info){
        int correct=0;
        try {
            JSONObject jObject = new JSONObject(info);
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
        intent.setClass(MainActivity.this,MapsActivity.class);
        startActivity(intent);
        finish();
    }


    //===================HTTP==========================
    //HTTPGet, we will not use it in this Activity
    class myTaskGet extends AsyncTask<Void,Void,String>{
        @Override
        public void onPreExecute() {
            super.onPreExecute();
        }
        @Override
        public String doInBackground(Void...arg0) {
            URL url = null;
            BufferedReader reader = null;
            StringBuilder stringBuilder;

            try
            {
                // create the HttpURLConnection

                //url = new URL("file:///D:/login.json");
                url = new URL("https://www.google.com.tw");
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
            //readDataFromHttp = result;
        }

    }

    //HTTPPost
    class myTaskPost extends AsyncTask<Void,Void,String>{

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
                url = new URL("http://192.168.0.2:8081/api/v1/member/login");
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
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(wr, "UTF-8"));

                writer.write("email=" + account_in + "&password=" + pass_in);
                writer.flush();

                reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                stringBuilder = new StringBuilder();
                String line = null;

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
        }

            return "";
        }

        @Override
        public void onPostExecute(String result) {
            super.onPostExecute(result);
        }

    }
}

