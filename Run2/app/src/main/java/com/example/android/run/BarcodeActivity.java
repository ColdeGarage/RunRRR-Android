package com.example.android.run;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;

import com.onbarcode.barcode.android.AndroidColor;
import com.onbarcode.barcode.android.AndroidFont;
import com.onbarcode.barcode.android.Code39;
import com.onbarcode.barcode.android.IBarcode;

public class BarcodeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(new MyView(this));
        setContentView(R.layout.activity_barcode);
        RelativeLayout r = (RelativeLayout)findViewById(R.id.relative);
        r.addView(new MyView(this));
        //toolbar.addView(new MyView(this));
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

    //=====================內存=====================
    private static int uid;
    private static String token;
    private void readPrefs(){
        SharedPreferences settings = getSharedPreferences("data",MODE_PRIVATE);
        uid = settings.getInt("uid",0);
        token = settings.getString("token","");
        System.out.println(token);
    }

    public class MyView extends View {
        public MyView(Context context) {
            super(context);
            // TODO Auto-generated constructor stub
        }

        @Override
        protected void onDraw(Canvas canvas) {
            // TODO Auto-generated method stub
            super.onDraw(canvas);

            //show the barcode
            try{
                readPrefs();
                testCODE39(canvas);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private static void testCODE39(Canvas canvas) throws Exception
    {
        Code39 barcode = new Code39();

        /*
           Code39 Valid data char set:
                0, 1, 2, 3, 4, 5, 6, 7, 8, 9 (Digits)
                A - Z (Uppercase letters)
                - (Dash), $ (Dollar), % (Percentage), (Space), . (Point), / (Slash), + (Plus)

           Code39 extension Valid data char set:
                All ASCII 128 characters
        */
        // Code39 encodes upper case chars only, for lower case chars, use Code 39 extension
        //barcode.setData("123456789012");
        barcode.setData(String.valueOf(uid));

        barcode.setExtension(false);

        barcode.setAddCheckSum(false);

        // Code 39 Wide Narrow bar Ratio
        // Valid value is from 2.0 to 3.0 inclusive.
        barcode.setN(3.0f);
        // The space between 2 characters in code 39; This a multiple of X; The default is 1.;
        // Valid value is from 1.0 (inclusive) to 5.3 (exclusive)
        barcode.setI(1.0f);
        barcode.setShowStartStopInText(true);

        // Unit of Measure, pixel, cm, or inch
        barcode.setUom(IBarcode.UOM_PIXEL);
        // barcode bar module width (X) in pixel
        barcode.setX(4f);
        // barcode bar module height (Y) in pixel
        barcode.setY(150f);

        // barcode image margins
        barcode.setLeftMargin(5f);
        barcode.setRightMargin(5f);
        barcode.setTopMargin(2f);
        barcode.setBottomMargin(3f);

        // barcode image resolution in dpi
        barcode.setResolution(72);

        // disply barcode encoding data below the barcode
        barcode.setShowText(false);
        // barcode encoding data font style
        barcode.setTextFont(new AndroidFont("Arial", Typeface.NORMAL, 24));
        // space between barcode and barcode encoding data
        barcode.setTextMargin(6);
        barcode.setTextColor(AndroidColor.black);

        // barcode bar color and background color in Android device
        barcode.setForeColor(AndroidColor.black);
        barcode.setBackColor(AndroidColor.white);

        /*
        specify your barcode drawing area
	    */
        //(left,top,right,button)
        RectF bounds = new RectF(200, 300, 100, 100);
        barcode.drawBarcode(canvas, bounds);
    }
}
