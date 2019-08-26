package com.android.landicorp.f8face.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.android.landicorp.f8face.R;

/**
 * Created by admin on 2019/6/27.
 */

public class RecommandView extends View{
    private Context mContext;
    private Paint mPaint = new Paint();
    public RecommandView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    public RecommandView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Paint fontPaint = new Paint();
        fontPaint.setStyle(Paint.Style.FILL);
        fontPaint.setColor(mContext.getColor(R.color.white));
        fontPaint.setTextAlign(Paint.Align.CENTER);
        fontPaint.setTextSize(17);
        fontPaint.setAntiAlias(true);

        mPaint.setAntiAlias(true);
        mPaint.setColor(mContext.getColor(R.color.f8_tuijian_2));
        Path path = new Path();
        path.lineTo(50,0);
        path.lineTo(50,40);
        path.lineTo(25,50);
        path.lineTo(0,40);
        path.lineTo(0,0);
        canvas.drawPath(path,mPaint);

        //计算baseline
        Paint.FontMetrics fontMetrics=fontPaint.getFontMetrics();
        float distance=(fontMetrics.bottom - fontMetrics.top)/2 - fontMetrics.bottom;
        float baseline=25+distance;
        canvas.drawText("推荐", 25, baseline, fontPaint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //设置宽高
        setMeasuredDimension(50,50);
    }

}
