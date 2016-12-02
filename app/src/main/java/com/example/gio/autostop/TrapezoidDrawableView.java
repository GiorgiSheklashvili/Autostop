package com.example.gio.autostop;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;


public class TrapezoidDrawableView extends View {
    Paint white,gray;
    int whiteInt;
    Point[] leftTop = new Point[3];
    Point[] rightTop = new Point[3];
    Point[] leftBottom = new Point[3];
    Point[] rightBottom = new Point[3];
    Path trapezoid, miniTrapezoid;
    public TrapezoidDrawableView(Context context) {
        super(context);
        init();
    }

    public TrapezoidDrawableView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        miniTrapezoid = new Path();
        trapezoid = new Path();
        whiteInt = ContextCompat.getColor(getContext(), R.color.white);
        white = new Paint();
        white.setAntiAlias(true);
        white.setStyle(Paint.Style.FILL);
        white.setColor(whiteInt);
        gray = new Paint();
        gray.setAntiAlias(true);
        gray.setColor(Color.GRAY);
        gray.setStyle(Paint.Style.FILL_AND_STROKE);
        gray.setStrokeWidth(10);
        gray.setAntiAlias(true);
        for(int i=0;i<3;i++){
            leftTop[i]=new Point();
            leftBottom[i]=new Point();
            rightTop[i]=new Point();
            rightBottom[i]=new Point();
        }
    }

    public void startScrolling() {
        ScrollingAnimation animation=new ScrollingAnimation();
        animation.setDuration(1500);
        animation.setRepeatCount(Animation.INFINITE);
        startAnimation(animation);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        trapezoid.moveTo(canvas.getWidth() / 3, 0);
        trapezoid.lineTo((canvas.getWidth() * 2) / 3, 0);
        trapezoid.lineTo((canvas.getWidth() * 19) / 20, canvas.getHeight());
        trapezoid.lineTo(canvas.getWidth() / 20, canvas.getHeight());
        trapezoid.lineTo(canvas.getWidth() / 3, 0);
        canvas.drawPath(trapezoid, gray);
        drawMiniTrapezoid(canvas);
        canvas.drawPath(miniTrapezoid, white);
        startScrolling();
    }
    public void drawMiniTrapezoid(Canvas canvas) {
        miniTrapezoid.setFillType(Path.FillType.EVEN_ODD);
        for(int i=0;i<3;i++){
        miniTrapezoid.moveTo(canvas.getWidth() * 40 / 81 - leftTop[i].x, leftTop[i].y);
        miniTrapezoid.lineTo(canvas.getWidth() * 41 / 81 + rightTop[i].x, rightTop[i].y);
        miniTrapezoid.lineTo(canvas.getWidth() * 42 / 81 + rightBottom[i].x, (canvas.getHeight() / 5) + rightBottom[i].y);
        miniTrapezoid.lineTo(canvas.getWidth() * 39 / 81 - leftBottom[i].x, (canvas.getHeight() / 5) + leftBottom[i].y);
        miniTrapezoid.lineTo(canvas.getWidth() * 40 / 81 - leftTop[i].x, leftTop[i].y);
        }
    }
    private class ScrollingAnimation extends Animation {
        public ScrollingAnimation() {
            setAnimationListener(new AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {

                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                    TrapezoidDrawableView.this.requestLayout();
                    TrapezoidDrawableView.this.invalidate();
                }
            });
        }


        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            super.applyTransformation(interpolatedTime, t);
            leftTop[0].x = leftTop[0].x + 1;
            leftTop[0].y = leftTop[0].y + 10;
            rightTop[0].x = rightTop[0].x + 1;
            rightTop[0].y = rightTop[0].y + 10;
            rightBottom[0].x = rightBottom[0].x + 1;
            rightBottom[0].y = rightBottom[0].y + 10;
            leftBottom[0].x = leftBottom[0].x + 1;
            leftBottom[0].y = leftBottom[0].y + 10;
            if (interpolatedTime > 0.3) {
                leftTop[1].x = leftTop[1].x + 1;
                leftTop[1].y = leftTop[1].y + 10;
                rightTop[1].x = rightTop[1].x + 1;
                rightTop[1].y = rightTop[1].y + 10;
                rightBottom[1].x = rightBottom[1].x + 1;
                rightBottom[1].y = rightBottom[1].y + 10;
                leftBottom[1].x = leftBottom[1].x + 1;
                leftBottom[1].y = leftBottom[1].y + 10;
            }
            if (interpolatedTime > 0.6) {
                leftTop[2].x = leftTop[2].x + 1;
                leftTop[2].y = leftTop[2].y + 10;
                rightTop[2].x = rightTop[2].x + 1;
                rightTop[2].y = rightTop[2].y + 10;
                rightBottom[2].x = rightBottom[2].x + 1;
                rightBottom[2].y = rightBottom[2].y + 10;
                leftBottom[2].x = leftBottom[2].x + 1;
                leftBottom[2].y = leftBottom[2].y + 10;
            }
            TrapezoidDrawableView.this.requestLayout();
            TrapezoidDrawableView.this.invalidate();
        }
    }
}
