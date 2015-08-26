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

//        setContentView(R.layout.activity_main);
//
//        glSurfaceView = (MyGLSurfaceView) findViewById(R.id.glView);

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
