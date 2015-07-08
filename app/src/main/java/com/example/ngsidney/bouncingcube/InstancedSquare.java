package com.example.ngsidney.bouncingcube;

import android.opengl.GLES30;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Created by ngsidney on 6/19/15.
 */
public class InstancedSquare {
    final float side = 3.0f;

    public final int numInstances = 10000;

    private FloatBuffer vertexBuffer;
    private final int mProgram;

    // VBO handles
    private final int mVertexVBO;
    private final int mPosVBO;
    private final int mSizeVBO;
    private final int mColorVBO;

    // Attribute locations
    private int vertexAttr;
    private int posAttr;
//    private final int VERTEX_LOC = 0;
//    private final int POSITION_LOC = 1;

    // vertex shader for rendering vertices
    private final String vertexShaderCode =
            // This matrix member variable provides a hook to manipulate
            // the coordinates of the objects that use this vertex shader
            //"layout(location = 0) in vec4 a_vertex;   \n" +
            //"layout(location = 1) in vec4 a_position;  \n" +
                    //"layout(location = 1) in mat4 a_mvpMatrix;  \n" +
                    //"out vec4 v_color;  \n" +
                    "attribute vec4 a_vertex;" +
                    "attribute vec4 a_position;" +
                            "attribute vec4 a_size;" +
                            "attribute vec4 a_color;" +

                    "uniform mat4 a_mvpMatrix;" +

                            "varying vec4 v_color;" +
//                            "uniform vec3 u_camUp;" +
//                            "uniform vec3 u_camRight;" +
                    //"attribute vec4 vPosition;" +
                    "void main() {  \n" +
                    //"  v_color = a_color;   \n" +
                    //"  vec4 vertex_pos = a_vertex + a_position;" +
                    //        "  vec4 vertex_pos = a_vertex + vec4(1.0f, 0.0f, 0.0f, 0.0f) * a_position.x * a_size.x + vec4(0.0f, 1.0f, 0.0f, 0.0f) * a_position.y * a_size.y;" +
                    //        "  vec4 vertex_pos = vec4(a_vertex.x * a_size.x, a_vertex.y * a_size.y, a_vertex.z, a_vertex.w) + a_position;" +
                            "  v_color = a_color;" +
                            "  vec3 vertex_pos = vec3(a_vertex.x * a_size.x, a_vertex.y * a_size.y, a_vertex.z) + a_position.xyz;" +
                            "  gl_Position = a_mvpMatrix * vec4(vertex_pos, 1.0f);" + // pass only the positions
                            //                    "  gl_Position = a_mvpMatrix * vertex_pos;  \n" +
//                            "  gl_PointSize = 20.0f;" +
                    "}";

    // Use to access and set the view transformation
    private int mMVPMatrixHandle;

    // Fragment shader for rendering the face of the shape with colors and textures
    private final String fragmentShaderCode =
            "precision mediump float;" +
                    "varying vec4 v_color;   \n" +
                    //"layout(location=0) out vec4 outColor;  \n" +
                    //"uniform vec4 vColor;" +
                    "void main() {" +
                    //"  outColor = v_color;" +
                    "  gl_FragColor = v_color;" +
                    "}";

    // number of coordinates per vertex in this array
    static final int COORDS_PER_VERTEX = 3;
    static float cubeCoords[] = {   // in counterclockwise order:
            // Front face -- Square
            -1.0f, 1.0f, 1.0f,
            -1.0f, -1.0f, 1.0f,
            1.0f, 1.0f, 1.0f,
            -1.0f, -1.0f, 1.0f,
            1.0f, -1.0f, 1.0f,
            1.0f, 1.0f, 1.0f

            //0.0f, 0.0f, 1.0f // only a point
    };

    // Set color with red, green, blue and alpha (opacity) values
    float color[] = { 0.63671875f, 0.76953125f, 0.22265625f, 1.0f };

    public InstancedSquare() {
        // initialize vertex byte buffer for shape coordinates
        ByteBuffer bb = ByteBuffer.allocateDirect(cubeCoords.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(cubeCoords);
        vertexBuffer.position(0);

        // VBOs
        final int buffers[] = new int[4];
        GLES30.glGenBuffers(4, buffers, 0);

        // bind the vertex data - will never change, do a static draw
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, buffers[0]);
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, cubeCoords.length * 4,
                vertexBuffer, GLES30.GL_STATIC_DRAW);
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0);

        // bind the positional data - the center of the square
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, buffers[1]);
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, numInstances * COORDS_PER_VERTEX * 4, null, GLES30.GL_DYNAMIC_DRAW);
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0);

        // bind the size data
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, buffers[2]);
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, numInstances * 2 * 4, null, GLES30.GL_DYNAMIC_DRAW);
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0);

        // bind the color data
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, buffers[3]);
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, numInstances * 4 * 4, null, GLES30.GL_DYNAMIC_DRAW);
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0);

        // save the handles to the VBOs
        mVertexVBO = buffers[0];
        mPosVBO = buffers[1];
        mSizeVBO = buffers[2];
        mColorVBO = buffers[3];

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
    public void draw(float[] vpMatrix, float[] positions, float[] sizes, float[] colors) {
        // Add program to OpenGL ES environment
        GLES30.glUseProgram(mProgram);

        // get the handles to the attribute
        vertexAttr = GLES30.glGetAttribLocation(mProgram, "a_vertex");
        posAttr = GLES30.glGetAttribLocation(mProgram, "a_position");
        int sizeAttr = GLES30.glGetAttribLocation(mProgram, "a_size");
        int coloAttr = GLES30.glGetAttribLocation(mProgram, "a_color");

        // load vertices
        GLES30.glEnableVertexAttribArray(vertexAttr);
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, mVertexVBO);
        GLES30.glVertexAttribPointer(vertexAttr, COORDS_PER_VERTEX,
                GLES30.GL_FLOAT, false,
                vertexStride, 0);
        GLES30.glVertexAttribDivisor(vertexAttr, 0);
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0);


        ByteBuffer bb = ByteBuffer.allocateDirect(positions.length * 4);
        bb.order(ByteOrder.nativeOrder());
        FloatBuffer fb = bb.asFloatBuffer();
        fb.put(positions);
        fb.position(0);

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, mPosVBO);
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, positions.length * 4, null, GLES30.GL_DYNAMIC_DRAW);
        GLES30.glBufferSubData(GLES30.GL_ARRAY_BUFFER, 0, positions.length * 4, fb);

        GLES30.glEnableVertexAttribArray(posAttr);
        GLES30.glVertexAttribPointer(posAttr, COORDS_PER_VERTEX, GLES30.GL_FLOAT,
                false, vertexStride, 0);
        GLES30.glVertexAttribDivisor(posAttr, 1);
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0);


        ByteBuffer bb2 = ByteBuffer.allocateDirect(sizes.length * 4);
        bb2.order(ByteOrder.nativeOrder());
        FloatBuffer sb = bb2.asFloatBuffer();
        sb.put(sizes);
        sb.position(0);

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, mSizeVBO);
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, sizes.length * 4, null, GLES30.GL_DYNAMIC_DRAW);
        GLES30.glBufferSubData(GLES30.GL_ARRAY_BUFFER, 0, sizes.length * 4, sb);

        GLES30.glEnableVertexAttribArray(sizeAttr);
        GLES30.glVertexAttribPointer(sizeAttr, 2, GLES30.GL_FLOAT,
                false, 2 * 4, 0);
        GLES30.glVertexAttribDivisor(sizeAttr, 1);
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0);


        ByteBuffer bb3 = ByteBuffer.allocateDirect(colors.length * 4);
        bb3.order(ByteOrder.nativeOrder());
        FloatBuffer cb = bb3.asFloatBuffer();
        cb.put(colors);
        cb.position(0);

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, mColorVBO);
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, colors.length * 4, null, GLES30.GL_DYNAMIC_DRAW);
        GLES30.glBufferSubData(GLES30.GL_ARRAY_BUFFER, 0, colors.length * 4, cb);

        GLES30.glEnableVertexAttribArray(coloAttr);
        GLES30.glVertexAttribPointer(coloAttr, 4, GLES30.GL_FLOAT,
                false, 4 * 4, 0);
        GLES30.glVertexAttribDivisor(sizeAttr, 1);
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0);


//        // get handle to fragment shader's vColor member
//        mColorHandle = GLES30.glGetUniformLocation(mProgram, "vColor");
//
//        // Set color for drawing the triangle
//        GLES30.glUniform4fv(mColorHandle, 1, color, 0);

        // get handle to shape's transformation matrix
        mMVPMatrixHandle = GLES30.glGetUniformLocation(mProgram, "a_mvpMatrix");

        // Pass the projection and view transformation to the shader
        GLES30.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, vpMatrix, 0);


        GLES30.glDrawArraysInstanced(GLES30.GL_TRIANGLES, 0, 6, numInstances);
        //GLES30.glDrawArraysInstanced(GLES30.GL_POINTS, 0, 1, numInstances);
        //GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, 6);

        GLES30.glDisableVertexAttribArray(mPosVBO);
        GLES30.glDisableVertexAttribArray(mVertexVBO);
    }
}
