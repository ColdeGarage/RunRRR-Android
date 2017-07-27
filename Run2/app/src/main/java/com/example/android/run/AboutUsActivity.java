package com.example.android.run;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.TextView;

public class AboutUsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);

        TextView about = (TextView) findViewById(R.id.about_us_content);
        about.setText("IOS：\n" +
                "大逃殺就是一種生存遊戲，逃亡期間當然團隊越精簡行動越方便，所以這個APP就是讓你手持一機即可以個人為單位掌握訊息、回報任務，同時也具有記名功能，易於辨別死活、統整資料、計算分數...等，淘汰過去不先進的遊戲過程，可謂營隊遊戲之一大進化。\n" +
                "\n" +
                "Android:\n" +
                "陳玉璇：我會魯4/3輩子QQ\n" +
                "曾筱茵：全世界等著我凱瑞！\n" +
                "ㄅㄎ：一時衝動就入了這個坑，超嗨der！");

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
