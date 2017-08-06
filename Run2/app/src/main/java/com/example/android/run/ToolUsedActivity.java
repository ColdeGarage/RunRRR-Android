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

public class ToolUsedActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tool_used);
        TextView usedContent = (TextView) findViewById(R.id.toolUsedText);
        Button backButton = (Button) findViewById(R.id.toolUsedConfirmButton) ;

        final Bundle bundleReciever =this.getIntent().getExtras();
        String content = bundleReciever.getString("content");


        usedContent.setTextColor(Color.BLACK);
        usedContent.setText("\n  "+content);
        backButton.setText("知道了");
        backButton.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

    }

    public void onBackPressed() {
        finish();
    }
}
