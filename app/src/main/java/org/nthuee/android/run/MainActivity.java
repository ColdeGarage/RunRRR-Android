package org.nthuee.android.run;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
    private int uid;
    private String token;
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
        if(readPrefs())
            goMap();

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

                if(account_in.isEmpty()) {
                    Alert("Email can't be empty.");
                } else if(pass_in.isEmpty()) {
                    Alert("Password can't be empty.");
                } else if (!isNetworkAvailable()) {
                    Alert("Please check your internet connection.");
                } else {
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
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    private void checkAccount() {
        String readDataFromHttp = null;

        //POST email&password to server
        MyTaskPost httpPost = new MyTaskPost();
        httpPost.execute();

        try {
            //get result from function "onPostExecute" in class "myTaskPost"
            readDataFromHttp = httpPost.get();
            System.out.println(readDataFromHttp);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //If brea=0, login success
        if(ParseJson(readDataFromHttp)==0) {
            loginState = true;
            storePrefs();
            goMap();
        } else {
            Alert("Login Fail");
        }
    }

    //show an alert dialog
    private void Alert(String mes) {
        new AlertDialog.Builder(this)
                .setMessage(mes)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {} })
                .show();
    }

    //Parse json received from server
    private int ParseJson (String info) {
        int correct=1;
        if(info != null) {
            try {
                JSONObject jObject = new JSONObject(info);
                uid = jObject.getInt("uid");
                token = jObject.getString("token");
                JSONObject payload = new JSONObject(jObject.getString("payload"));
                correct = payload.getInt("correct");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return correct;
    }

    //===================Intent==========================
    private void goMap() {
        Intent intent = new Intent();
        intent.setClass(this,ViewPagerActivity.class);
        startActivity(intent);
        finish();
    }

    private void goAboutUs() {
        Intent intent = new Intent();
        intent.setClass(this,AboutUsActivity.class);
        startActivity(intent);
    }

    //===================內存=========================
    //store login state and uid
    private void storePrefs() {
        SharedPreferences settings = getSharedPreferences("data",MODE_PRIVATE);
        settings.edit().putBoolean("login",loginState)
                .putInt("uid",uid)
                .putString("token",token)
                .apply();
    }

    //read login state
    private boolean readPrefs() {
        SharedPreferences settings = getSharedPreferences("data",MODE_PRIVATE);
        return settings.getBoolean("login",false);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    //===================HTTP==========================
    //HTTPPost
    class MyTaskPost extends AsyncTask<Void,Void,String> {
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
                DataOutputStream wr = new DataOutputStream(urlConnection.getOutputStream());

                //encode data in UTF-8
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(wr, "UTF-8"));

                String msg = "email=" + account_in + "&password=" + pass_in;
                System.out.println(msg);
                writer.write("email=" + account_in + "&password=" + pass_in);

                //flush the data in buffer to server and close the writer
                writer.flush();
                writer.close();

                //read response
                reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                stringBuilder = new StringBuilder();

                String line ;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line + "\n");
                }
                return stringBuilder.toString();
            } catch(Exception e){
                e.printStackTrace();
            } finally {
                urlConnection.disconnect();
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
}

