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

import com.example.android.run.R;

public class MissionPopActivity extends AppCompatActivity {

    public static final String EXTRA_POSITION = "position";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_missions_pop);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        // Get postion of RecyclerView
        int postion = getIntent().getIntExtra(EXTRA_POSITION, 0);
        Resources resources = getResources();

        // Set title with mission name
        TypedArray MissionTypes = resources.obtainTypedArray(R.array.mission_type);
        ImageView Type = (ImageView) findViewById(R.id.list_type);
        Type.setImageDrawable(MissionTypes.getDrawable((postion % MissionTypes.length())));

        // Set title with mission name
        String[] MissionNames = resources.getStringArray(R.array.mission_name);
        TextView Name = (TextView) findViewById(R.id.list_name);
        Name.setText(MissionNames[postion % MissionNames.length]);

        // Set title with mission name
        TypedArray MissionPictures = resources.obtainTypedArray(R.array.mission_picture);
        ImageView Picture = (ImageView) findViewById(R.id.mission_picture);
        Picture.setImageDrawable(MissionPictures.getDrawable((postion % MissionPictures.length())));
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
