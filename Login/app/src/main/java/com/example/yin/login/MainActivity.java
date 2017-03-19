package com.example.yin.login;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    Map<String,String> Account = new HashMap<String,String>();
    boolean loginState;

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
                String account_in = acc.getText().toString();
                String pass_in = pass.getText().toString();
                if(account_in.isEmpty()){
                    Alert("Account can't be empty.");

                }else if(pass_in.isEmpty()) {
                    Alert("Password can't be empty.");

                }else {
                    checkAccount(account_in,pass_in);
                }
            }
        });
    }

    void checkAccount(String account, String password){
        if(Account.containsKey(account)){
            if(Account.get(account).equals(password)){
                loginState = true;
                Alert("Success");
                String correct = httpGet();
                System.out.println(correct);

                //goMap();

            }else{
                Alert("Wrong password!!!");
            }
        }else{
            Alert("Account doesn't exist.");
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

    void goMap(){
        Intent intent = new Intent();
        intent.setClass(MainActivity.this,MapsActivity.class);
        startActivity(intent);
        finish();
    }

    //發送HttpURLConnection_POST(網址,資料內容,編碼方式)
    public String sendHttpURLConnectionPOST() {
        URL url;
        HttpURLConnection urlConnection = null;
        try {
            url = new URL("");
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

            wr.writeBytes("");
            //InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            //readStream(in);
        }catch(Exception e){
            e.printStackTrace();
            System.out.printf(e.toString());
        }
        /*finally {
            urlConnection.disconnect();
        }*/

        return "";
    }

    public String httpGet(  ) {

        URL url = null;
        BufferedReader reader = null;
        StringBuilder stringBuilder;

        try
        {
            // create the HttpURLConnection
            /*FileReader fr = new FileReader("D:\\login.json");
            BufferedReader br=new BufferedReader(fr);
            String line;
            while ((line=br.readLine()) != null) {
                return line;
            }*/

            //url = new URL("file:///D:/login.json");
            url = new URL("http://www.android.com/");
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


}
