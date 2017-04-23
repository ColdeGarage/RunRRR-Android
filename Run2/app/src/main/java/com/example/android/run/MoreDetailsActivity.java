package com.example.android.run;

import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class MoreDetailsActivity extends AppCompatActivity {

    public static final String EXTRA_POSITION = "position";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Get postion of RecyclerView
        int postion = getIntent().getIntExtra(EXTRA_POSITION, 0);

        // Inflat layout for each item selected
        super.onCreate(savedInstanceState);
        switch (postion) {
            case 0:
                setContentView(R.layout.activity_die);

                break;
            case 1:
                setContentView(R.layout.activity_about_us);
                break;
            case 2:
                setContentView(R.layout.activity_sos);
                break;
            case 3:
                setContentView(R.layout.activity_logout);
                break;
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Set Appbar title for each layout
        switch (postion) {
            case 0:
                getSupportActionBar().setTitle("Die");
                break;
            case 1:
                getSupportActionBar().setTitle("About Us");
                break;
            case 2:
                getSupportActionBar().setTitle("Sos");
                break;
            case 3:
                getSupportActionBar().setTitle("Logout");
                break;
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
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
