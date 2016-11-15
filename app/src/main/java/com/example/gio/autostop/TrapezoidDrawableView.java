package com.example.gio.autostop;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.View;


public class TrapezoidDrawableView extends View {
    Paint gray, white;
    Path trapezoid, miniTrapezoid;
    Point leftTop, rightTop, leftBottom, rightBottom;


    public TrapezoidDrawableView(Context context) {
        super(context);

    }

    public TrapezoidDrawableView(Context context, AttributeSet attrs) {
        super(context, attrs);
        gray = new Paint(Paint.ANTI_ALIAS_FLAG);
        white = new Paint(Paint.ANTI_ALIAS_FLAG);
        trapezoid = new Path();
        miniTrapezoid = new Path();
        leftTop = new Point();
        rightTop = new Point();
        leftBottom = new Point();
        rightBottom = new Point();
    }
    @Override
    protected void onDraw(final Canvas canvas) {
        super.onDraw(canvas);
        gray.setColor(Color.GRAY);
        gray.setStyle(Paint.Style.FILL_AND_STROKE);
        gray.setStrokeWidth(10);
        gray.setAntiAlias(true);
        trapezoid.setFillType(Path.FillType.EVEN_ODD);
        trapezoid.moveTo(canvas.getWidth() / 3, 0);
        trapezoid.lineTo((canvas.getWidth() * 2) / 3, 0);
        trapezoid.lineTo((canvas.getWidth() * 19) / 20, canvas.getHeight());
        trapezoid.lineTo(canvas.getWidth() / 20, canvas.getHeight());
        trapezoid.lineTo(canvas.getWidth() / 3, 0);
        canvas.drawPath(trapezoid, gray);
        white.setColor(Color.WHITE);
        white.setStyle(Paint.Style.FILL_AND_STROKE);
        white.setStrokeWidth(10);
        white.setAntiAlias(true);
        drawMiniTrapezoid(canvas);
        canvas.drawPath(miniTrapezoid, white);
        invalidate();

    }


    public void drawMiniTrapezoid(Canvas canvas){
        miniTrapezoid.setFillType(Path.FillType.EVEN_ODD);
        miniTrapezoid.moveTo(canvas.getWidth() * 40 / 81 - leftTop.x, 0 + leftTop.y);
        miniTrapezoid.lineTo(canvas.getWidth() * 41 / 81 + rightTop.x, 0 + rightTop.y);
        miniTrapezoid.lineTo(canvas.getWidth() * 42 / 81 + rightBottom.x, (canvas.getHeight() / 5) + rightBottom.y);
        miniTrapezoid.lineTo(canvas.getWidth() * 39 / 81 - leftBottom.x, (canvas.getHeight() / 5) + leftBottom.y);
        miniTrapezoid.lineTo(canvas.getWidth() * 40 / 81 - leftTop.x, 0 + leftTop.y);

        leftTop.x = leftTop.x + 1;
        leftTop.y = leftTop.y + 5;
        rightTop.x = rightTop.x + 1;
        rightTop.y = rightTop.y + 5;
        rightBottom.x = rightBottom.x + 1;
        rightBottom.y = rightBottom.y + 5;
        leftBottom.x = leftBottom.x + 1;
        leftBottom.y = leftBottom.y + 5;

    }
}
