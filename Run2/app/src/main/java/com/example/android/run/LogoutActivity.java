package com.example.android.run;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

public class LogoutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logout);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Logout");

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Button Function
        Button btYes = (Button) findViewById(R.id.logout_yes);
        Button btNo = (Button) findViewById(R.id.logout_no);

        //button to logout
        btYes.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                //change login state
                String KEY = "login";
                SharedPreferences settings = getSharedPreferences("data",MODE_PRIVATE);
                settings.edit().putBoolean(KEY,false)
                        .apply();

                //intent to login
                Context context = v.getContext();
                Intent intent = new Intent(context, MainActivity.class);
                context.startActivity(intent);
                finish();
            }
        });

        //button to cancel
        btNo.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
            finish();
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

}
