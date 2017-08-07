package com.example.android.run;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.ColorRes;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class CheckPickFromGalleryActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_pick_from_gallery);
        TextView usedContent = (TextView) findViewById(R.id.toolUsedText);
        TextView confirmButton = (TextView) findViewById(R.id.photoPickedConfirmButton);
        TextView repickButton = (TextView) findViewById(R.id.photoRepickButton);


        usedContent.setTextColor(Color.BLACK);
        usedContent.setTextSize(20);
        usedContent.setText("\n   Are you sure?");

        repickButton.setText("BACK");
        repickButton.setTextColor(Color.BLACK);
        repickButton.setTextSize(22);
        repickButton.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                setResult(2,intent);
                onBackPressed();
            }
        });

        confirmButton.setText("YES");
        confirmButton.setTextColor(Color.RED);
        confirmButton.setTextSize(22);
        confirmButton.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                setResult(3,intent);
                onBackPressed();
            }
        });
    }

    public void onBackPressed() {
        finish();
    }
}
