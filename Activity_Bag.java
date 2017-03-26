package com.example.leochen.bk0001;

import android.media.Image;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.graphics.drawable.Drawable;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;



public class Activity_Bag extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bag);

        String count;

        count = "A";

        //ImageView itemImage =  ;
        TextView itemNum = (TextView) findViewById(R.id.itemNumber);

        itemNum.setText( count );


/*
        ImageView itemImage = (ImageView) findViewById( R.id.itemImage);
        String uri = "@drawable/test1";
        int imageResource = getResources().getIdentifier(uri, null, getPackageName());
        Drawable image = getResources().getDrawable(imageResource);
        itemImage.setImageDrawable(image);
        */
    }

}


