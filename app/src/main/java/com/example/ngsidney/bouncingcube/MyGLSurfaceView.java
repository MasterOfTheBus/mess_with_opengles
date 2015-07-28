package com.example.ngsidney.bouncingcube;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.RectF;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

/**
 * Created by ngsidney on 6/1/15.
 *
 * The user defined GLSurfaceView
 */
public class MyGLSurfaceView extends GLSurfaceView {

    private final MyGLRenderer mRenderer;
    private ScaleGestureDetector scaleGestureDetector;
    private GestureDetector gestureDetector;
    private static String TAG = MyGLSurfaceView.class.getSimpleName();


    public MyGLSurfaceView(Context context) {
        super(context);

        // Create an OpenGL ES 2.0 context
//        setEGLContextClientVersion(2);
//        setEGLConfigChooser( 8, 8, 8, 8, 16, 0 );

        setEGLContextClientVersion(3);
        setEGLConfigChooser( 8, 8, 8, 8, 16, 0 );


        mRenderer = new MyGLRenderer(this);

        // Set the Renderer for drawing on the GLSurfaceView
        setRenderer(mRenderer);


        scaleGestureDetector = new ScaleGestureDetector(context, new ScaleListener());
        gestureDetector = new GestureDetector(context, new GestureListener());


    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
//        if (e.getAction() == MotionEvent.ACTION_DOWN) {
//            mRenderer.toggleView();
//        }
        scaleGestureDetector.onTouchEvent(e);
        gestureDetector.onTouchEvent(e);




        return true;
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            mRenderer.scaleFactor *= Math.max(0.1f, Math.min(detector.getScaleFactor(), 5.0f));

            return true;
        }
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            float viewportOffsetX = distanceX * (mRenderer.back / mRenderer.WORKING_SCROLL) * 2
                    * mRenderer.ratio / mRenderer.scaleFactor / (float) Math.sqrt(mRenderer.viewPortWidth);
            float viewportOffsetY = -distanceY * (mRenderer.back / mRenderer.WORKING_SCROLL) * 2
                    / mRenderer.scaleFactor / (float) Math.sqrt(mRenderer.viewportHeight);

            mRenderer.eyeX += viewportOffsetX;
            mRenderer.eyeY += viewportOffsetY;
            mRenderer.centerX += viewportOffsetX;
            mRenderer.centerY += viewportOffsetY;

            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            Log.d(TAG, "onDoubleTapEvent");
//            mRenderer.progressToNextView();
            mRenderer.prevRenderMode = mRenderer.render3d;
            mRenderer.render3d = !mRenderer.render3d;
            return true;
        }



    }



}
