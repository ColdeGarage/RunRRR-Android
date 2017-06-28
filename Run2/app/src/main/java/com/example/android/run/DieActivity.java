package com.example.android.run;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class DieActivity extends AppCompatActivity {
    private static String token;
    private static int uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_die);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //read uid and token
        readPrefs();

        Button bt = (Button) findViewById(R.id.button_send);
        final EditText editTextUid = (EditText) findViewById(R.id.edit_uid);
        final EditText editTextPass = (EditText) findViewById(R.id.edit_pass);

        bt.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                String hunterUid = editTextUid.getText().toString();
                String hunterPass = editTextPass.getText().toString();
                //Log.i("text",hunterUid + "  " + hunterPass);

                MyTaskPut diePut = new MyTaskPut();
                diePut.execute(getResources().getString(R.string.apiURL)+"/member/liveordie"
                        ,"uid=" + String.valueOf(uid) + "&operator_uid=" + hunterUid + "&token=" + hunterPass
                                + "&status=" + true);

                //get result from function "onPostExecute" in class "myTaskPut"
                try {
                    String readDataFromHttp = diePut.get();

                    //If die
                    if(parseJson(readDataFromHttp) == 0){
                        new AlertDialog.Builder(v.getContext())
                                .setCancelable(false)   //按到旁邊也不會消失
                                .setMessage("You are dead.")
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                }).show();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            super.onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //========================內存=========================
    private void readPrefs(){
        SharedPreferences settings = getSharedPreferences("data",MODE_PRIVATE);
        uid = settings.getInt("uid",0);
        token = settings.getString("token","");
    }

    //Parse json received from server
    int parseJson (String info){
        int brea = 0;
        try {
            JSONObject jObject = new JSONObject(info);
            brea = jObject.getInt("brea");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return brea;
    }

    //HTTPPUT
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
}


