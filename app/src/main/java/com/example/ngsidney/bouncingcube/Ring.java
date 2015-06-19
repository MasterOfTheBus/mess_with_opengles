package com.example.ngsidney.bouncingcube;

import android.opengl.GLES20;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Created by ngsidney on 6/12/15.
 */
public class Ring {
    private FloatBuffer vertexBuffer;
    private final int mProgram;

    /*
        Shaders contain OpenGL Shading Language (GLSL) that must be precompiled
     */
    // vertex shader for rendering vertices
    private final String vertexShaderCode =
            // This matrix member variable provides a hook to manipulate
            // the coordinates of the objects that use this vertex shader
            "uniform mat4 uMVPMatrix;" +
                    "attribute vec4 vPosition;" +
                    "void main() {" +
                    "  gl_Position = uMVPMatrix * vPosition;" +
                    "}";

    // Use to access and set the view transformation
    private int mMVPMatrixHandle;

    // Fragment shader for rendering the face of the shape with colors and textures
    private final String fragmentShaderCode =
            "precision mediump float;" +
                    "uniform vec4 vColor;" +
                    "void main() {" +
                    "  gl_FragColor = vColor;" +
                    "}";

    // number of coordinates per vertex in this array
    static final int COORDS_PER_VERTEX = 3;
    private float outerRadius = 0.5f;
    private float innerRadius = 0.35f;
    //private int numVertices = 30;
    private int outerVertices = 30;
    private int innerVertices = 30;
    float ringCoords[] = new float[(innerVertices+outerVertices)*3];

    // Set color with red, green, blue and alpha (opacity) values
    float color[] = { 1.0f, 0.6f, 0.0f, 1.0f };

    private static String TAG = Ring.class.getSimpleName();

    public Ring() {
        int index = 0;
//        ringCoords[index] = 0.0f;
//        ringCoords[index++] = 0.0f;
//        ringCoords[index++] = 0.0f;

        for (int i = 0; i < outerVertices; i++) {
            float radian = 2 * (float) Math.PI * (i / (float) (outerVertices-1));

            float cos = (float) Math.cos(radian);
            float sin = (float) Math.sin(radian);

            // outer ring
            ringCoords[index] = outerRadius * cos; // the x coord
            index++;

            ringCoords[index] = outerRadius * sin; // the y coord
            index++;

            ringCoords[index] = 0.0f; // the z coord
            index++;

            // inner ring
            ringCoords[index] = innerRadius * cos; // the x coord
            index++;

            ringCoords[index] = innerRadius * sin; // the y coord
            index++;

            ringCoords[index] = 0.0f; // the z coord
            index++;
        }

        // initialize vertex byte buffer for shape coordinates
        ByteBuffer bb = ByteBuffer.allocateDirect(
                // (number of coordinate values * 4 bytes per float)
                ringCoords.length * 4);
        // use the device hardware's native byte order
        bb.order(ByteOrder.nativeOrder());

        // create a floating point buffer from the ByteBuffer
        vertexBuffer = bb.asFloatBuffer();
        // add the coordinates to the FloatBuffer
        vertexBuffer.put(ringCoords);
        // set the buffer to read the first coordinate
        vertexBuffer.position(0);

        // Run the shader code compiler for the GLSL spec
        int vertexShader = MyGLRenderer.loadShader(GLES20.GL_VERTEX_SHADER,
                vertexShaderCode);
        int fragmentShader = MyGLRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER,
                fragmentShaderCode);

        // create empty OpenGL ES Program
        mProgram = GLES20.glCreateProgram();

        // add the vertex shader to program
        GLES20.glAttachShader(mProgram, vertexShader);

        // add the fragment shader to program
        GLES20.glAttachShader(mProgram, fragmentShader);

        // creates OpenGL ES program executables
        GLES20.glLinkProgram(mProgram);
    }

    // each shape should have its own draw method because it would be different for each shape
    private int mPositionHandle;
    private int mColorHandle;

    private final int vertexCount = ringCoords.length / COORDS_PER_VERTEX;
    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex

    public void draw(float[] mvpMatrix) {
        // Add program to OpenGL ES environment
        GLES20.glUseProgram(mProgram);

        // get handle to vertex shader's vPosition member
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");

        // Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        // Prepare the triangle coordinate data
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                vertexStride, vertexBuffer);

        // get handle to fragment shader's vColor member
        mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");

        // Set color for drawing the triangle
        GLES20.glUniform4fv(mColorHandle, 1, color, 0);

        /*
            Apply the projection and view transformations
         */
        // get handle to shape's transformation matrix
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");

        // Pass the projection and view transformation to the shader
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);

        // Draw the triangle
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, outerVertices + innerVertices);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
    }
}
