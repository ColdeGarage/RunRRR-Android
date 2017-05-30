package com.example.android.run;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class BagPopActivity extends AppCompatActivity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bag_pop);

        final ImageView toolPicture = (ImageView) findViewById(R.id.popToolImage);
        TextView toolName = (TextView) findViewById(R.id.popToolName);
        TextView toolContent = (TextView) findViewById(R.id.popContent);
        TextView toolCount = (TextView) findViewById(R.id.popCount);
        Button backButton = (Button) findViewById(R.id.popCancelButton) ;

        int width;
        int height;

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        width = dm.widthPixels;
        height = dm.heightPixels;
        getWindow().setLayout((int)(width*.8),(int)(height*.8));

        backButton.setOnClickListener(new Button.OnClickListener(){

            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        //get data from bagFragment
        Bundle bundleReciever =this.getIntent().getExtras();
        final String imageUrl = bundleReciever.getString("IMAGEURL");
        String name = bundleReciever.getString("NAME");
        String content = bundleReciever.getString("CONTENT");
        String count = "擁有:" + bundleReciever.getString("COUNT");

        new Thread(new Runnable() {
            @Override
            public void run() {
                //TODO Auto-generated method stub
                final Bitmap mBitmap =
                        getBitmapFromURL("http://coldegarage.tech:8081/api/v1.1/download/img/" + imageUrl);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        toolPicture.setImageBitmap(mBitmap);
                    }
                });                    //TODO Auto-generated method stub
            }
        }).start();
        toolName.setText(name);
        toolContent.setText(content);
        toolCount.setText(count);
    }

    public void onBackPressed() {
        Intent intent=new Intent();
        setResult(2,intent);
        finish();
    }

    private static Bitmap getBitmapFromURL(String imageUrl) {
        try {
            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            return  BitmapFactory.decodeStream(input);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}