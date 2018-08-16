package org.nthuee.android.run;

import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class AboutUsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us_from_login);
        TextView title = (TextView) findViewById(R.id.about_us_title);
        TextView content = (TextView) findViewById(R.id.about_us_content);
        TextView cancelButton = (TextView) findViewById(R.id.btn_cancel);
        title.setText("ABOUT US");
        content.setText(R.string.aboutUs);

        cancelButton.setOnClickListener(new TextView.OnClickListener(){
            @Override
            public void onClick(View view) {
                onBackPressed();
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
