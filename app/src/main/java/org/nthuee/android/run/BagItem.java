package org.nthuee.android.run;

import android.widget.LinearLayout;
import android.content.Context;
import android.util.AttributeSet;

/**
 * Created by LeoChen on 2017/3/4.
 */

public class BagItem extends LinearLayout {

    public BagItem(Context context) {
        super(context);
    }

    public BagItem(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //    super.onMeasure(widthMeasureSpec, (int) (widthMeasureSpec*0.33));
        super.onMeasure(widthMeasureSpec,widthMeasureSpec);
    }
}
