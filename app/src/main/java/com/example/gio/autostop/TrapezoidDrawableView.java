package com.example.gio.autostop;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Transformation;


public class TrapezoidDrawableView extends View {
    Paint white, gray, black;
    int whiteInt;
    Point[] leftTop = new Point[3];
    Point[] rightTop = new Point[3];
    Point[] leftBottom = new Point[3];
    Point[] rightBottom = new Point[3];
    Path trapezoid;
    boolean oneThird = false;
    boolean twoThird = false;
    Path miniTrapezoid0, miniTrapezoid1, miniTrapezoid2;
    int width;
    double height;


    public TrapezoidDrawableView(Context context) {
        super(context);
        init();
    }

    public TrapezoidDrawableView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        miniTrapezoid0 = new Path();
        miniTrapezoid1 = new Path();
        miniTrapezoid2 = new Path();
        trapezoid = new Path();
        whiteInt = ContextCompat.getColor(getContext(), R.color.white);
        white = new Paint();
        white.setAntiAlias(true);
        white.setStyle(Paint.Style.FILL);
        white.setColor(whiteInt);


        black = new Paint();
        black.setAntiAlias(true);
        black.setStyle(Paint.Style.FILL);
        black.setColor(ContextCompat.getColor(getContext(), R.color.black));

        gray = new Paint();
        gray.setAntiAlias(true);
        gray.setColor(Color.GRAY);
        gray.setStyle(Paint.Style.FILL_AND_STROKE);
        gray.setStrokeWidth(10);
        gray.setAntiAlias(true);
        for (int i = 0; i < 3; i++) {
            leftTop[i] = new Point();
            leftBottom[i] = new Point();
            rightTop[i] = new Point();
            rightBottom[i] = new Point();
        }
        startScrolling();
    }

    public void startScrolling() {
        ScrollingAnimation animation = new ScrollingAnimation();
        animation.setDuration(1500);
        animation.setRepeatCount(Animation.INFINITE);
        startAnimation(animation);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        trapezoid.reset();
        trapezoid.moveTo(canvas.getWidth() / 3, 0);
        trapezoid.lineTo((canvas.getWidth() * 2) / 3, 0);
        trapezoid.lineTo((canvas.getWidth() * 19) / 20, canvas.getHeight());
        trapezoid.lineTo(canvas.getWidth() / 20, canvas.getHeight());
        trapezoid.lineTo(canvas.getWidth() / 3, 0);
        canvas.drawPath(trapezoid, gray);
        drawMiniTrapezoid(canvas, miniTrapezoid0, 0);
        canvas.drawPath(miniTrapezoid0, white);
        if (reachedOneThird(leftTop[0].y)) {
            oneThird = true;
            drawMiniTrapezoid(canvas, miniTrapezoid1, 1);
            canvas.drawPath(miniTrapezoid1, black);
        }
//        if (reachedTwoThird(leftTop[0].y) && reachedOneThird(leftTop[1].y)) {
//            twoThird = true;
//            drawMiniTrapezoid(canvas, miniTrapezoid2, 2);
//            canvas.drawPath(miniTrapezoid2, white);
//        }



    }

    public void drawMiniTrapezoid(Canvas canvas, Path path, int i) {
        path.reset();
        path.setFillType(Path.FillType.EVEN_ODD);
        path.moveTo(canvas.getWidth() * 40 / 81 - leftTop[i].x, leftTop[i].y);
        path.lineTo(canvas.getWidth() * 41 / 81 + rightTop[i].x, rightTop[i].y);
        path.lineTo(canvas.getWidth() * 42 / 81 + rightBottom[i].x, (canvas.getHeight() / 5) + rightBottom[i].y);
        path.lineTo(canvas.getWidth() * 39 / 81 - leftBottom[i].x, (canvas.getHeight() / 5) + leftBottom[i].y);
        path.lineTo(canvas.getWidth() * 40 / 81 - leftTop[i].x, leftTop[i].y);
    }

    public boolean reachedOneThird(double y) {
        if (y >= height / 3.0)
            return true;
        return false;
    }

    public boolean reachedTwoThird(double y) {
        if (y >= height * 3.0 / 4.0)
            return true;
        return false;
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
            displayWidthandHeight();// initshia gasatani  tu initidan shemovedi aq ar vamateb arapers
            leftTop[0].x = leftTop[0].x + 1;
            leftTop[0].y = leftTop[0].y + 10;
            rightTop[0].x = rightTop[0].x + 1;
            rightTop[0].y = rightTop[0].y + 10;
            rightBottom[0].x = rightBottom[0].x + 1;
            rightBottom[0].y = rightBottom[0].y + 10;
            leftBottom[0].x = leftBottom[0].x + 1;
            leftBottom[0].y = leftBottom[0].y + 10;
            if (oneThird) {
                leftTop[1].x = leftTop[1].x + 1;
                leftTop[1].y = leftTop[1].y + 10;
                rightTop[1].x = rightTop[1].x + 1;
                rightTop[1].y = rightTop[1].y + 10;
                rightBottom[1].x = rightBottom[1].x + 1;
                rightBottom[1].y = rightBottom[1].y + 10;
                leftBottom[1].x = leftBottom[1].x + 1;
                leftBottom[1].y = leftBottom[1].y + 10;
            }
            if (twoThird) {
                leftTop[2].x = leftTop[2].x + 1;
                leftTop[2].y = leftTop[2].y + 10;
                rightTop[2].x = rightTop[2].x + 1;
                rightTop[2].y = rightTop[2].y + 10;
                rightBottom[2].x = rightBottom[2].x + 1;
                rightBottom[2].y = rightBottom[2].y + 10;
                leftBottom[2].x = leftBottom[2].x + 1;
                leftBottom[2].y = leftBottom[2].y + 10;
            }

            if (checkingIfSurpassed(leftTop[0].y))
                makeZero(0);

            if (checkingIfSurpassed(leftTop[1].y))
                makeZero(1);
//            if (checkingIfSurpassed(leftTop[2].y))
//                makeZero(2);
            TrapezoidDrawableView.this.requestLayout();
            TrapezoidDrawableView.this.invalidate();
        }

        public boolean checkingIfSurpassed(int Ycoordinate) {

            return Ycoordinate >= (int) height;
        }

        public void displayWidthandHeight() {
            WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            width = size.x;
            height = (size.y * 1.0) / 3.0;
        }


        public void makeZero(int i) {
            leftTop[i].x = 0;
            leftTop[i].y = 0;
            rightTop[i].x = 0;
            rightTop[i].y = 0;
            rightBottom[i].x = 0;
            rightBottom[i].y = 0;
            leftBottom[i].x = 0;
            leftBottom[i].y = 0;
        }


    }
}

