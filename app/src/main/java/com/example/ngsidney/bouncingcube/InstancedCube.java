package com.example.ngsidney.bouncingcube;

import android.annotation.TargetApi;
import android.opengl.GLES20;
import android.opengl.GLES30;
import android.opengl.GLES31Ext;
import android.opengl.Matrix;
import android.os.Build;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/**
 * Created by ngsidney on 6/19/15.
 */
public class InstancedCube {
    public final int numInstances = 1000;

    private FloatBuffer vertexBuffer;
    private final int mProgram;

    // VBO handles
    private final int mVertexVBO;
    private final int mPosVBO;

    // Attribute locations
    private final int POSITION_LOC = 0;
    private final int MVP_LOC = 1;

    // vertex shader for rendering vertices
    private final String vertexShaderCode =
            // This matrix member variable provides a hook to manipulate
            // the coordinates of the objects that use this vertex shader
            "layout(location = 0) in vec4 a_position;   \n" +
                    //"layout(location = 1) in vec4 a_color;  \n" +
                    "layout(location = 1) in mat4 a_mvpMatrix;  \n" +
                    //"out vec4 v_color;  \n" +
                    //"uniform mat4 uMVPMatrix;" +
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
            -0.3f, 0.3f, 0.3f,
            -0.3f, -0.3f, 0.3f,
            0.3f, 0.3f, 0.3f,
            -0.3f, -0.3f, 0.3f,
            0.3f, -0.3f, 0.3f,
            0.3f, 0.3f, 0.3f,

            // Right face
            0.3f, 0.3f, 0.3f,
            0.3f, -0.3f, 0.3f,
            0.3f, 0.3f, -0.3f,
            0.3f, -0.3f, 0.3f,
            0.3f, -0.3f, -0.3f,
            0.3f, 0.3f, -0.3f,

            // Back face
            0.3f, 0.3f, -0.3f,
            0.3f, -0.3f, -0.3f,
            -0.3f, 0.3f, -0.3f,
            0.3f, -0.3f, -0.3f,
            -0.3f, -0.3f, -0.3f,
            -0.3f, 0.3f, -0.3f,

            // Left face
            -0.3f, 0.3f, -0.3f,
            -0.3f, -0.3f, -0.3f,
            -0.3f, 0.3f, 0.3f,
            -0.3f, -0.3f, -0.3f,
            -0.3f, -0.3f, 0.3f,
            -0.3f, 0.3f, 0.3f,

            // Top face
            -0.3f, 0.3f, -0.3f,
            -0.3f, 0.3f, 0.3f,
            0.3f, 0.3f, -0.3f,
            -0.3f, 0.3f, 0.3f,
            0.3f, 0.3f, 0.3f,
            0.3f, 0.3f, -0.3f,

            // Bottom face
            0.3f, -0.3f, -0.3f,
            0.3f, -0.3f, 0.3f,
            -0.3f, -0.3f, -0.3f,
            0.3f, -0.3f, 0.3f,
            -0.3f, -0.3f, 0.3f,
            -0.3f, -0.3f, -0.3f
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
        final int buffers[] = new int[2];
        GLES30.glGenBuffers(2, buffers, 0);

        // bind the positional data
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, buffers[0]);
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, vertexBuffer.capacity() * 4, vertexBuffer, GLES30.GL_STATIC_DRAW);

        // bind the mvp matrix
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, buffers[1]);
        // *16 because 4x4 matrix? --- TODO: consider static draw
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, numInstances * 16, null, GLES30.GL_DYNAMIC_DRAW);

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0);

        // save the handles to the VBOs
        mVertexVBO = buffers[0];
        mPosVBO = buffers[1];


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

    private final int vertexCount = cubeCoords.length / COORDS_PER_VERTEX;
    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex

    //@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void draw(float[] vpMatrix, float[] translation) {
        // Add program to OpenGL ES environment
        GLES30.glUseProgram(mProgram);

        // load vertices
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, mVertexVBO);
        GLES30.glVertexAttribPointer(POSITION_LOC, COORDS_PER_VERTEX,
                GLES30.GL_FLOAT, false,
                vertexStride, null);
        GLES30.glEnableVertexAttribArray(POSITION_LOC);

        // load the MVP matrices
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, mPosVBO);
        FloatBuffer matrixBuf = (FloatBuffer) GLES30.glMapBufferRange(GLES30.GL_ARRAY_BUFFER,
                0, 16 * numInstances, GLES30.GL_MAP_WRITE_BIT);

        for (int i = 0; i < numInstances; i++) {
            float translateM[] = new float[16];
            float mvpM[] = new float[16];
            Matrix.translateM(translateM, 0,
                    translation[i * 3],
                    translation[i * 3 + 1],
                    translation[i * 3 + 2]);
            Matrix.multiplyMM(mvpM, 0, vpMatrix, 0, translateM, 0);
            matrixBuf.put(mvpM);

            GLES30.glVertexAttribPointer(MVP_LOC + i, 4, GLES30.GL_FLOAT, false,
                    16, null);
            GLES30.glEnableVertexAttribArray(MVP_LOC + i);
            GLES30.glVertexAttribDivisor(MVP_LOC + i, 1);
        }

        GLES30.glUnmapBuffer(GLES30.GL_ARRAY_BUFFER);

        // get handle to fragment shader's vColor member
        mColorHandle = GLES30.glGetUniformLocation(mProgram, "vColor");

        // Set color for drawing the triangle
        GLES30.glUniform4fv(mColorHandle, 1, color, 0);


        GLES30.glDrawArraysInstanced(GLES30.GL_TRIANGLES, 0, 36, numInstances);
    }
}
