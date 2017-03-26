package com.example.leochen.bk0001;

import android.widget.LinearLayout;
import android.content.Context;
import android.util.AttributeSet;

/**
 * Created by LeoChen on 2017/3/4.
 */

public class ItemRow extends LinearLayout {

    public ItemRow(Context context) {
        super(context);
    }

    public ItemRow(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
/*
    public ItemRow(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
*/

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    //    super.onMeasure(widthMeasureSpec, (int) (widthMeasureSpec*0.33));
        super.onMeasure(widthMeasureSpec,widthMeasureSpec);
    }
}
