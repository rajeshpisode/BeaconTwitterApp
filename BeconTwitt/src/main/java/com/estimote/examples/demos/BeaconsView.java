package com.estimote.examples.demos;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;


public class BeaconsView extends View {

    private final Drawable drawable;

    public BeaconsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        drawable = context.getResources().getDrawable(R.drawable.bg_distance);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = drawable.getIntrinsicWidth() * canvas.getHeight() / drawable.getIntrinsicHeight();
        int deltaX = (width - canvas.getWidth()) / 2;
        drawable.setBounds(-deltaX, 0, width - deltaX, canvas.getHeight());
        drawable.draw(canvas);
    }
}
