package com.example.ngsidney.bouncingcube;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.os.SystemClock;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by ngsidney on 6/1/15.
 *
 * User defined GLRenderer
 */
public class MyGLRenderer implements GLSurfaceView.Renderer {

    private Triangle myTriangle;
    private Square mySquare;
    private Cube myCube;
    private Cube myOtherCube;
    private CubeOutline myCubeOutline;
    private Ring myRing;

    float eyeX = 0.0f;
    float eyeY = 0.0f;
    float eyeZ = 1.5f;
    float centerX = 0.0f;
    float centerY = 0.0f;
    float centerZ = -5.0f;
    float upX = 0.0f;
    float upY = 1.0f;
    float upZ = 0.0f;

    float depth = 0.0f;
    float adjustment = -0.075f;

    MyGLSurfaceView surfaceView;

    boolean view1 = false;

    public MyGLRenderer(MyGLSurfaceView surfaceView) {
        this.surfaceView = surfaceView;
    }

    /*
        Called once to set up the environment
     */
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        // Set the background frame color
        GLES20.glClearColor(0.9f, 0.9f, 0.9f, 1.0f);

        //Hide the hidden surfaces using these APIs
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glDepthFunc(GLES20.GL_LESS);
        //GLES20.glDepthMask(true);

//        // Init a triangle and a square
//        myTriangle = new Triangle();
//        mySquare = new Square(surfaceView);
//        myCube = new Cube(surfaceView);
//        myOtherCube = new Cube(surfaceView);

        //myCubeOutline = new CubeOutline();

        myRing = new Ring();
    }

    // mMVPMatrix is an abbreviation for "Model View Projection Matrix"
    private final float[] mMVPMatrix = new float[16];
    private final float[] mProjectionMatrix = new float[16];
    private final float[] mViewMatrix = new float[16];
    private final float[] mModelMatrix = new float[16];

    /*
        Called if geometry of the view changes
     */
    private float far = 10.0f;
    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        // Define the area of the drawing context to draw to
        GLES20.glViewport(0, 0, width, height);

        // Populate a projection matrix which will be used with a camera view to more closely
        // simulate how objects are seen with the eye
        float ratio = (float) width / (float) height;

        // this projection matrix is applied to object coordinates
        // in the onDrawFrame() method
        //Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -1, 1, 3, 7); // proj matrix for triangle
        Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -1.0f, 1.0f, 1.0f, far); // proj matrix for cube
        //Matrix.setIdentityM(mProjectionMatrix, 0); // projection matrix set to identity for ortho projection
    }

    private float[] mRotationMatrix = new float[16];
    private float[] mTranslationMatrix = new float[16];

    float viewAngle = 0.0f;
    float viewX = 0.0f;
    float viewZ = 0.0f;
    float viewAngleAdj = 9.0f;
    float viewXAdj = 0.1f;
    float viewZAdj = 0.25f;
    float desiredViewAngle = 90.0f;
    float desiredViewX = 1.0f;
    float desiredViewZ = 2.5f;

    /*
        Called on each redraw of the view
    */
    @Override
    public void onDrawFrame(GL10 gl) {
        float[] scratch = new float[16];

        // Redraw background color
        GLES20.glClearDepthf(1.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        // Define the camera view
        // Set the camera position (View matrix)
        //Matrix.setLookAtM(mViewMatrix, 0, 0, 0, -3, 0f, 0f, 0f, 0f, 1.0f, 0.0f); // the view matrix used for triangle

        Matrix.setLookAtM(mViewMatrix, 0, eyeX, eyeY, eyeZ, centerX, centerY, centerZ, upX, upY, upZ); // view matrix for cube

        if (view1) {
            if (viewAngle < desiredViewAngle) {
                viewAngle += viewAngleAdj;
            }
            if (viewX < desiredViewX) {
                viewX += viewXAdj;
            }
            if (viewZ < desiredViewZ) {
                viewZ += viewZAdj;
            }
        } else {
            if (viewAngle > 0) {
                viewAngle -= viewAngleAdj;
            }
            if (viewX > 0) {
                viewX -= viewXAdj;
            }
            if (viewZ > 0) {
                viewZ -= viewZAdj;
            }
        }
        Matrix.rotateM(mViewMatrix, 0, viewAngle, 0.0f, 1.0f, 0.0f);
        Matrix.translateM(mViewMatrix, 0, viewX, 0.0f, viewZ);

        //Matrix.setIdentityM(mViewMatrix, 0); // view matrix set to identity for ortho projection

        // Calculate the projection and view transformation
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);

        /** --- Triangle Specific Rotation
        // Create a rotation transformation for the triangle
        long time = SystemClock.uptimeMillis() % 4000L;
        float angle = 0.090f * ((int) time);
        Matrix.setRotateM(mRotationMatrix, 0, angle, 0, 0, -1.0f);
         */

        // Cube rotation
        long time = SystemClock.uptimeMillis() % 4000L;
        float angle = 0.090f * ((int) time);
        Matrix.setRotateM(mRotationMatrix, 0, angle, -1.0f, -1.0f, 0.0f);

        //Matrix.setIdentityM(mRotationMatrix, 0);

        // Create the translation matrix
        Matrix.setIdentityM(mTranslationMatrix, 0);
        // Translates the cube along the z-axis to go between foreground and background
        depth += adjustment;
        if (depth <= -5.0f || depth > 0.0f) {
            adjustment *= -1.0f;
        }
        Matrix.translateM(mTranslationMatrix, 0, 0.0f, 0.0f, depth);

        // Combine the rotation and the translation matrix to make the model matrix
        // NOTE: Translation MUST go first in order to have the move THEN spin
        Matrix.multiplyMM(mModelMatrix, 0, mTranslationMatrix, 0, mRotationMatrix, 0);

        // Combine the rotation matrix with the projection and camera view
        // Note that the mMVPMatrix factor *must be first* in order
        // for the matrix multiplication product to be correct.
        Matrix.multiplyMM(scratch, 0, mMVPMatrix, 0, mModelMatrix, 0);

        //myTriangle.draw(scratch);
        //mySquare.draw(scratch);

        //myCube.draw(scratch);
        //myCubeOutline.draw(scratch);

        // draw the other cube
        /*
        Matrix.setIdentityM(mTranslationMatrix, 0);
        Matrix.translateM(mTranslationMatrix, 0, 1.0f, 0.0f, -2.5f);
        Matrix.multiplyMM(mModelMatrix, 0, mTranslationMatrix, 0, mRotationMatrix, 0);
        Matrix.multiplyMM(scratch, 0, mMVPMatrix, 0, mModelMatrix, 0);
        myOtherCube.draw(scratch);
        */

        myRing.draw(mMVPMatrix);
    }

    /*
        Utility code to compile shader code (GLSL)
     */
    public static int loadShader(int type, String shaderCode){

        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }

    public void toggleView() {
        view1 = !view1;
    }

}