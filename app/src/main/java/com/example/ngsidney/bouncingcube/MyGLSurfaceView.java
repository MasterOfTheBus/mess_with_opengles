package com.example.ngsidney.bouncingcube;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

/**
 * Created by ngsidney on 6/1/15.
 *
 * The user defined GLSurfaceView
 */
public class MyGLSurfaceView extends GLSurfaceView{

    private final MyGLRenderer mRenderer;
    private ScaleGestureDetector scaleGestureDetector;

    public MyGLSurfaceView(Context context) {
        super(context);

        /*
        // Create an OpenGL ES 2.0 context
        setEGLContextClientVersion(2);
        setEGLConfigChooser( 8, 8, 8, 8, 16, 0 );
        */

        setEGLContextClientVersion(3);
        //setEGLConfigChooser( 8, 8, 8, 8, 16, 0 );


        mRenderer = new MyGLRenderer(this);

        // Set the Renderer for drawing on the GLSurfaceView
        setRenderer(mRenderer);

        scaleGestureDetector = new ScaleGestureDetector(context, new ScaleListener());

    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
//        if (e.getAction() == MotionEvent.ACTION_DOWN) {
//            mRenderer.toggleView();
//        }
        scaleGestureDetector.onTouchEvent(e);
        return true;
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            mRenderer.scaleFactor *= Math.max(0.1f, Math.min(detector.getScaleFactor(), 5.0f));

            return true;
        }
    }

}
