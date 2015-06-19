package com.example.ngsidney.bouncingcube;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

/**
 * The Activity for the App
 * Sets a GLSurfaceView
 *
 * NOTE: Cannot have an associated activity
 */

public class OpenGLES20Activity extends Activity {

    private GLSurfaceView glSurfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        glSurfaceView = new MyGLSurfaceView(this);
        setContentView(glSurfaceView);
    }

    protected void onResume()
    {
        super.onResume();
        glSurfaceView.onResume();
    }
    @Override
    protected void onPause()
    {
        super.onPause();
        glSurfaceView.onPause();
    }
}
