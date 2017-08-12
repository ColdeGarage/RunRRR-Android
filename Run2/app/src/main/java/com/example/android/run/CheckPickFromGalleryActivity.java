package com.example.android.run;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class CheckPickFromGalleryActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_pick_from_gallery);
        TextView confirmButton = (TextView) findViewById(R.id.photoPickedConfirmButton);
        TextView repickButton = (TextView) findViewById(R.id.photoRepickButton);

        repickButton.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                setResult(2,intent);
                onBackPressed();
            }
        });

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
