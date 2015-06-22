package com.example.ngsidney.bouncingcube;

import android.annotation.TargetApi;
import android.opengl.GLES20;
import android.opengl.GLES30;
import android.opengl.GLES31Ext;
import android.opengl.Matrix;
import android.os.Build;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/**
 * Created by ngsidney on 6/19/15.
 */
public class InstancedCube {
    final float side = 3.0f;

    public final int numInstances = 10;

    private FloatBuffer vertexBuffer;
    private final int mProgram;

    // VBO handles
    private final int mVertexVBO;
    //private final int mPosVBO;

    // Attribute locations
    private final int POSITION_LOC = 0;
    private final int MVP_LOC = 1;

    // vertex shader for rendering vertices
    private final String vertexShaderCode =
            // This matrix member variable provides a hook to manipulate
            // the coordinates of the objects that use this vertex shader
            "layout(location = 0) in vec4 a_position;   \n" +
                    //"layout(location = 1) in vec4 a_color;  \n" +
                    //"layout(location = 1) in mat4 a_mvpMatrix;  \n" +
                    //"out vec4 v_color;  \n" +
                    "uniform mat4 a_mvpMatrix;" +
                    //"attribute vec4 vPosition;" +
                    "void main() {  \n" +
                    //"  v_color = a_color;   \n" +
                    "  gl_Position = a_mvpMatrix * a_position;  \n" +
                    "}";

    // Use to access and set the view transformation
    private int mMVPMatrixHandle;

    // Fragment shader for rendering the face of the shape with colors and textures
    private final String fragmentShaderCode =
            "precision mediump float;" +
                    //"in vec4 v_color;   \n" +
                    //"layout(location=0) out vec4 outColor;  \n" +
                    "uniform vec4 vColor;" +
                    "void main() {" +
                    //"  outColor = v_color;" +
                    "  gl_FragColor = vColor;" +
                    "}";

    // number of coordinates per vertex in this array
    static final int COORDS_PER_VERTEX = 3;
    static float cubeCoords[] = {   // in counterclockwise order:
            // Front face
            -1.0f, 1.0f, 1.0f,
            -1.0f, -1.0f, 1.0f,
            1.0f, 1.0f, 1.0f,
            -1.0f, -1.0f, 1.0f,
            1.0f, -1.0f, 1.0f,
            1.0f, 1.0f, 1.0f/*,

            // Right face
            1.0f, 1.0f, 1.0f,
            1.0f, -1.0f, 1.0f,
            1.0f, 1.0f, -1.0f,
            1.0f, -1.0f, 1.0f,
            1.0f, -1.0f, -1.0f,
            1.0f, 1.0f, -1.0f,

            // Back face
            1.0f, 1.0f, -1.0f,
            1.0f, -1.0f, -1.0f,
            -1.0f, 1.0f, -1.0f,
            1.0f, -1.0f, -1.0f,
            -1.0f, -1.0f, -1.0f,
            -1.0f, 1.0f, -1.0f,

            // Left face
            -1.0f, 1.0f, -1.0f,
            -1.0f, -1.0f, -1.0f,
            -1.0f, 1.0f, 1.0f,
            -1.0f, -1.0f, -1.0f,
            -1.0f, -1.0f, 1.0f,
            -1.0f, 1.0f, 1.0f,

            // Top face
            -1.0f, 1.0f, -1.0f,
            -1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, -1.0f,
            -1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, -1.0f,

            // Bottom face
            1.0f, -1.0f, -1.0f,
            1.0f, -1.0f, 1.0f,
            -1.0f, -1.0f, -1.0f,
            1.0f, -1.0f, 1.0f,
            -1.0f, -1.0f, 1.0f,
            -1.0f, -1.0f, -1.0f*/
    };

    // Set color with red, green, blue and alpha (opacity) values
    float color[] = { 0.63671875f, 0.76953125f, 0.22265625f, 1.0f };

    public InstancedCube() {
        // initialize vertex byte buffer for shape coordinates
        ByteBuffer bb = ByteBuffer.allocateDirect(cubeCoords.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(cubeCoords);
        vertexBuffer.position(0);

        // VBOs
        final int buffers[] = new int[1];
        GLES30.glGenBuffers(1, buffers, 0);

        // bind the positional data
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, buffers[0]);
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, /*numInstances * */18 * 4, vertexBuffer, GLES30.GL_DYNAMIC_DRAW);

//        // bind the mvp matrix
//        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, buffers[1]);
//        // *16 because 4x4 matrix? --- TODO: consider static draw
//        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, numInstances * 16, null, GLES30.GL_DYNAMIC_DRAW);

        //GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0);

        // save the handles to the VBOs
        mVertexVBO = buffers[0];
        //mPosVBO = buffers[1];


        // Run the shader code compiler for the GLSL spec
        int vertexShader = MyGLRenderer.loadShader(GLES30.GL_VERTEX_SHADER,
                vertexShaderCode);
        int fragmentShader = MyGLRenderer.loadShader(GLES30.GL_FRAGMENT_SHADER,
                fragmentShaderCode);

        // create empty OpenGL ES Program
        mProgram = GLES30.glCreateProgram();

        // add the vertex shader to program
        GLES30.glAttachShader(mProgram, vertexShader);

        // add the fragment shader to program
        GLES30.glAttachShader(mProgram, fragmentShader);

        // creates OpenGL ES program executables
        GLES30.glLinkProgram(mProgram);
    }

    // each shape should have its own draw method because it would be different for each shape
    private int mPositionHandle;
    private int mColorHandle;

    private final int vertexCount = /*cubeCoords.length*/18 / COORDS_PER_VERTEX;
    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex

    //@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void draw(float[] vpMatrix, float[] translation) {
        // Add program to OpenGL ES environment
        GLES30.glUseProgram(mProgram);

//        // load vertices
//        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, mVertexVBO);
//        GLES30.glVertexAttribPointer(POSITION_LOC, COORDS_PER_VERTEX,
//                GLES30.GL_FLOAT, false,
//                vertexStride, 0);
//        GLES30.glEnableVertexAttribArray(POSITION_LOC);
//        GLES30.glVertexAttribDivisor(POSITION_LOC, 0);

        // load the MVP matrices
        /*
        FloatBuffer matrixBuf = (FloatBuffer) GLES30.glMapBufferRange(GLES30.GL_ARRAY_BUFFER,
                0, 16 * numInstances, GLES30.GL_MAP_WRITE_BIT);*/



        /*
        float masterMVP[] = new float[numInstances * 18];
        int index = 0;
        float zCoord = 0.0f;
        for (int i = 0; i < numInstances; i++) {
            float minX = -1.0f;//translation[i * 3] - side/2;
            float maxX = 1.0f;//translation[i * 3] + side/2;
            float minY = -1.0f;//translation[i * 3 + 1] - side/2;
            float maxY = 1.0f;//translation[i * 3 + 1] + side/2;

            cubeCoords = new float[]{
                    minX, minY, zCoord, // bottom left
                    maxX, maxY, zCoord,
                    minX, maxY, zCoord,
                    // triangle 2
                    minX, minY, zCoord,
                    maxX, minY, zCoord,
                    maxX, maxY, zCoord
            };


//            float translateM[] = new float[16];
//            float mvpM[] = new float[16];
//            Matrix.translateM(translateM, 0,
//                    translation[i * 3],
//                    translation[i * 3 + 1],
//                    translation[i * 3 + 2]);
//            Matrix.multiplyMM(mvpM, 0, vpMatrix, 0, translateM, 0);
//            //matrixBuf.put(mvpM);

            for (int j = 0; j < cubeCoords.length; j++) {
                masterMVP[index] = cubeCoords[j];
                index++;
            }
        }
        Log.d("blah", "master length: " + masterMVP.length);

        ByteBuffer bb = ByteBuffer.allocateDirect(masterMVP.length * 4);
        bb.order(ByteOrder.nativeOrder());
        FloatBuffer mvpBuf = bb.asFloatBuffer();
        mvpBuf.put(masterMVP);
        mvpBuf.position(0);

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, mVertexVBO);
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, numInstances * 18 * 4, null, GLES30.GL_STREAM_DRAW);
        GLES30.glBufferSubData(GLES30.GL_ARRAY_BUFFER, 0, numInstances * 18 * 4, mvpBuf);

        //GLES30.glUnmapBuffer(GLES30.GL_ARRAY_BUFFER);
        GLES30.glEnableVertexAttribArray(POSITION_LOC);
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, mVertexVBO);
        GLES30.glVertexAttribPointer(POSITION_LOC, COORDS_PER_VERTEX, GLES30.GL_FLOAT, false,
                vertexStride, 0); // TODO: should be 16, and 16 * 4?
        GLES30.glVertexAttribDivisor(POSITION_LOC, 1);
*/
        float zCoord = 1.0f;
        float minX = translation[0] - side/2;
        float maxX = translation[0] + side/2;
        float minY = translation[1] - side/2;
        float maxY = translation[1] + side/2;
        float newVertex[] = {
                minX, minY, zCoord, // bottom left
                maxX, maxY, zCoord,
                minX, maxY, zCoord,
                // triangle 2
                minX, minY, zCoord,
                maxX, minY, zCoord,
                maxX, maxY, zCoord
        };
        ByteBuffer bb = ByteBuffer.allocateDirect(newVertex.length * 4);
        bb.order(ByteOrder.nativeOrder());
        FloatBuffer fb = bb.asFloatBuffer();
        fb.put(newVertex);
        fb.position(0);

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, mVertexVBO);
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, 18 * 4, null, GLES30.GL_DYNAMIC_DRAW);
        GLES30.glBufferSubData(GLES30.GL_ARRAY_BUFFER, 0, 18 * 4, fb);

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, mVertexVBO);
        GLES30.glEnableVertexAttribArray(POSITION_LOC);
        GLES30.glVertexAttribPointer(POSITION_LOC, COORDS_PER_VERTEX, GLES30.GL_FLOAT,
                false, vertexStride, 0);



        // get handle to fragment shader's vColor member
        mColorHandle = GLES30.glGetUniformLocation(mProgram, "vColor");

        // Set color for drawing the triangle
        GLES30.glUniform4fv(mColorHandle, 1, color, 0);

        // get handle to shape's transformation matrix
        mMVPMatrixHandle = GLES30.glGetUniformLocation(mProgram, "a_mvpMatrix");

        // Pass the projection and view transformation to the shader
        GLES30.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, vpMatrix, 0);


        //GLES30.glDrawArraysInstanced(GLES30.GL_TRIANGLES, 0, 6, numInstances);
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, 6);
    }
}
